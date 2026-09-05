#!/bin/bash
#
# De-obfuscates an R8 stacktrace using a mapping from the mapping archive repository.
#
# Mappings are produced by the `archive*Mapping` Gradle tasks and stored outside this
# repository (see `mappingArchiveDir` in gradle.properties).
#
# Usage:
#   scripts/retrace.sh --list
#   scripts/retrace.sh --app sync --code 224 [--variant nowearModernRelease] [--stacktrace crash.txt]
#   scripts/retrace.sh --mapping path/to/mapping.map.gz [--stacktrace crash.txt]
#
# The stacktrace is read from stdin when --stacktrace is omitted.
# MAPPING_ARCHIVE_DIR overrides the archive location, R8_JAR overrides the retrace binary.
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ARCHIVE_REL="${MAPPING_ARCHIVE_DIR:-$(sed -n 's/^mappingArchiveDir=//p' "$REPO_ROOT/gradle.properties" | tail -n 1)}"
ARCHIVE_REL="${ARCHIVE_REL:-../smp-mappings}"
ARCHIVE_DIR="$(cd "$REPO_ROOT" && cd "$ARCHIVE_REL" 2>/dev/null && pwd || true)"

APP=""
CODE=""
VARIANT=""
MAPPING=""
STACKTRACE=""
LIST_ONLY=false

die() {
    echo "error: $*" >&2
    exit 1
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --list) LIST_ONLY=true; shift ;;
        --app) APP="$2"; shift 2 ;;
        --code) CODE="$2"; shift 2 ;;
        --variant) VARIANT="$2"; shift 2 ;;
        --mapping) MAPPING="$2"; shift 2 ;;
        --stacktrace) STACKTRACE="$2"; shift 2 ;;
        -h|--help) sed -n '2,15p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) die "unknown argument: $1" ;;
    esac
done

list_mappings() {
    [[ -d "$ARCHIVE_DIR" ]] || die "mapping archive not found at $ARCHIVE_REL"
    find "$ARCHIVE_DIR" -name '*.map.gz' | sed "s|^$ARCHIVE_DIR/||" | sort
}

if [[ "$LIST_ONLY" == true ]]; then
    list_mappings
    exit 0
fi

# --- Resolve the mapping file ---------------------------------------------------------

if [[ -z "$MAPPING" ]]; then
    [[ -n "$APP" && -n "$CODE" ]] || die "specify --mapping, or both --app and --code (see --list)"
    [[ -d "$ARCHIVE_DIR" ]] || die "mapping archive not found at $ARCHIVE_REL"

    matches=()
    while IFS= read -r line; do
        matches+=("$line")
    done < <(find "$ARCHIVE_DIR/$APP" -name "*-b$CODE-*$VARIANT*.map.gz" 2>/dev/null | sort)

    case ${#matches[@]} in
        0)
            echo "No mapping for app '$APP' version code $CODE${VARIANT:+ variant '$VARIANT'}. Available:" >&2
            list_mappings >&2
            exit 1
            ;;
        1) MAPPING="${matches[0]}" ;;
        *)
            echo "Several mappings match, narrow it down with --variant:" >&2
            printf '  %s\n' "${matches[@]}" | sed "s|^  $ARCHIVE_DIR/|  |" >&2
            exit 1
            ;;
    esac
fi

[[ -f "$MAPPING" ]] || die "mapping file not found: $MAPPING"

# --- Locate retrace -------------------------------------------------------------------

SDK_RETRACE="${ANDROID_HOME:-$HOME/Library/Android/sdk}/cmdline-tools/latest/bin/retrace"

find_r8_jar() {
    [[ -n "${R8_JAR:-}" ]] && { echo "$R8_JAR"; return; }
    local candidate
    for candidate in \
        /Applications/Android\ Studio*.app/Contents/plugins/android/lib/r8.jar \
        "$HOME"/Applications/Android\ Studio*.app/Contents/plugins/android/lib/r8.jar \
        "$HOME"/Library/Application\ Support/JetBrains/Toolbox/apps/*/Contents/plugins/android/lib/r8.jar
    do
        [[ -f "$candidate" ]] && { echo "$candidate"; return; }
    done
    : # nothing found; the caller reports it
}

find_java() {
    [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]] && { echo "$JAVA_HOME/bin/java"; return; }
    local candidate
    for candidate in \
        /Applications/Android\ Studio*.app/Contents/jbr/Contents/Home/bin/java \
        "$HOME"/Applications/Android\ Studio*.app/Contents/jbr/Contents/Home/bin/java
    do
        [[ -x "$candidate" ]] && { echo "$candidate"; return; }
    done
    command -v java || true
}

# --- Run ------------------------------------------------------------------------------

PLAIN_MAPPING="$MAPPING"
if [[ "$MAPPING" == *.gz ]]; then
    PLAIN_MAPPING="$(mktemp -t retrace-mapping)"
    trap 'rm -f "$PLAIN_MAPPING"' EXIT
    gunzip -c "$MAPPING" > "$PLAIN_MAPPING"
fi

METADATA="${MAPPING%.map.gz}.json"
if [[ -f "$METADATA" ]]; then
    echo "# mapping: $MAPPING" >&2
    sed 's/^/# /' "$METADATA" >&2
fi

RETRACE_ARGS=("$PLAIN_MAPPING")
if [[ -n "$STACKTRACE" ]]; then
    RETRACE_ARGS+=("$STACKTRACE")
fi

if [[ -x "$SDK_RETRACE" ]]; then
    "$SDK_RETRACE" "${RETRACE_ARGS[@]}"
else
    R8_JAR_PATH="$(find_r8_jar)"
    [[ -n "$R8_JAR_PATH" ]] || die "retrace not found. Install 'cmdline-tools;latest' via sdkmanager, or set R8_JAR."
    JAVA_BIN="$(find_java)"
    [[ -n "$JAVA_BIN" ]] || die "no java runtime found. Set JAVA_HOME."
    "$JAVA_BIN" -cp "$R8_JAR_PATH" com.android.tools.r8.retrace.Retrace "${RETRACE_ARGS[@]}"
fi
