package com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser

object LyricsParser {

    private val LINE_SPLIT_REGEX = Regex("\\r?\\n")
    private val OFFSET_REGEX = Regex("^\\[offset:\\s*([+-]?\\d+)\\s*\\]$", RegexOption.IGNORE_CASE)
    private val ROLE_PREFIX_REGEX = Regex("^([A-Za-z0-9]+):")

    fun parseLyrics(text: String, onParserError: (Throwable) -> Unit): LyricsText {
        if (text.isEmpty()) {
            return LyricsText(emptyList(), false)
        }

        val rawLines = text.trim().split(LINE_SPLIT_REGEX)
        try {
            return processLines(rawLines)
        } catch (e: Exception) {
            onParserError(e)
            val fallbackLines = rawLines.map { rawLine ->
                LyricsLine(
                    words = listOf(LyricsWord(text = rawLine, timeStart = -1L, duration = -1L)),
                    timeStart = -1L,
                    duration = -1L
                )
            }
            return LyricsText(fallbackLines, false)
        }
    }

    private fun processLines(rawLines: List<String>): LyricsText {
        val globalOffset = parseGlobalOffset(rawLines)
        val resultLines = ArrayList<LyricsLine>()
        val rolesMap = mutableMapOf<String, Int>()

        for (rawLine in rawLines) {
            if (OFFSET_REGEX.matches(rawLine.trim())) {
                continue
            }

            val roleIndex = parseRoleIndex(rawLine, rolesMap)
            val lineTimestamps = ArrayList<Long>()
            val lineTextBuilder = StringBuilder()
            var hasNonWhitespace = false
            var hasLineBeenFlushed = false

            var i = 0
            while (i < rawLine.length) {
                if (rawLine[i] == '[') {
                    val time = parseTimeTag(rawLine, i + 1, ']')
                    if (time != -1L) {
                        val adjustedTime = (time - globalOffset).coerceAtLeast(0L)
                        if (hasNonWhitespace && lineTimestamps.isNotEmpty()) {
                            createLyricsLines(
                                lineTimestamps,
                                lineTextBuilder.toString(),
                                adjustedTime,
                                globalOffset,
                                roleIndex,
                                resultLines
                            )
                            lineTimestamps.clear()
                            lineTextBuilder.setLength(0)
                            hasNonWhitespace = false
                            hasLineBeenFlushed = true
                        }
                        lineTimestamps.add(adjustedTime)
                        i = rawLine.indexOf(']', i) + 1
                        continue
                    }
                }
                val char = rawLine[i]
                lineTextBuilder.append(char)
                if (!char.isWhitespace()) {
                    hasNonWhitespace = true
                }
                i++
            }

            if (lineTimestamps.isNotEmpty()) {
                val text = lineTextBuilder.toString()
                if (text.isNotBlank() || !hasLineBeenFlushed) {
                    createLyricsLines(lineTimestamps, text, -1L, globalOffset, roleIndex, resultLines)
                } else {
                    val lastTimestamp = lineTimestamps.removeAt(lineTimestamps.size - 1)
                    if (lineTimestamps.isNotEmpty()) {
                        createLyricsLines(lineTimestamps, "", lastTimestamp, globalOffset, roleIndex, resultLines)
                    }
                }
            } else if (!hasLineBeenFlushed) {
                createLyricsLines(listOf(-1L), lineTextBuilder.toString(), -1L, globalOffset, roleIndex, resultLines)
            }
        }

        return finalizeLines(resultLines)
    }

    private fun parseGlobalOffset(rawLines: List<String>): Long {
        for (rawLine in rawLines) {
            val match = OFFSET_REGEX.find(rawLine.trim()) ?: continue
            return match.groupValues[1].toLongOrNull() ?: 0L
        }
        return 0L
    }

    private fun parseRoleIndex(rawLine: String, rolesMap: MutableMap<String, Int>): Int {
        val textBuilder = StringBuilder()
        var i = 0
        while (i < rawLine.length) {
            if (rawLine[i] == '[' && parseTimeTag(rawLine, i + 1, ']') != -1L) {
                i = rawLine.indexOf(']', i) + 1
                continue
            }
            textBuilder.append(rawLine[i])
            i++
        }
        val roleMatch = ROLE_PREFIX_REGEX.find(textBuilder.toString().trimStart()) ?: return 0
        val roleName = roleMatch.groupValues[1].uppercase()
        return rolesMap.getOrPut(roleName) { rolesMap.size + 1 }
    }

    private fun finalizeLines(resultLines: List<LyricsLine>): LyricsText {
        var isHighlightAvailable = false
        var lastSyncedTime = -1L
        val linesWithSortKeys = resultLines.mapIndexed { index, line ->
            if (line.timeStart != -1L) {
                lastSyncedTime = line.timeStart
                isHighlightAvailable = true
            }
            Triple(line, lastSyncedTime, index)
        }
        val sortedLines = linesWithSortKeys.sortedWith(
            compareBy<Triple<LyricsLine, Long, Int>> { triple -> triple.second }
                .thenBy { triple -> triple.third }
        ).map { triple -> triple.first }

        var nextSyncedTime = -1L
        val processedLines = ArrayList<LyricsLine>(sortedLines.size)
        for (i in sortedLines.indices.reversed()) {
            val line = sortedLines[i]
            val currentSyncedTime = line.timeStart

            val lineDurationFromNext = if (nextSyncedTime != -1L && currentSyncedTime != -1L) {
                (nextSyncedTime - currentSyncedTime).coerceAtLeast(0L)
            } else {
                -1L
            }
            val lineDuration = if (line.duration != -1L) line.duration else lineDurationFromNext

            var charOffset = 0
            val updatedWords = line.words.mapIndexed { wordIdx, word ->
                val nextWordTime = if (wordIdx + 1 < line.words.size) {
                    line.words[wordIdx + 1].timeStart
                } else if (lineDuration != -1L && currentSyncedTime != -1L) {
                    currentSyncedTime + lineDuration
                } else {
                    -1L
                }

                val wordDuration = if (nextWordTime != -1L && word.timeStart != -1L) {
                    (nextWordTime - word.timeStart).coerceAtLeast(0L)
                } else {
                    -1L
                }

                val charStart = charOffset
                val charEnd = charOffset + word.text.length
                val trimmedEnd = charOffset + word.text.trimEnd().length
                charOffset = charEnd

                word.copy(
                    duration = wordDuration,
                    charStartIndex = charStart,
                    charEndIndex = charEnd,
                    trimmedCharEndIndex = trimmedEnd
                )
            }

            if (currentSyncedTime != -1L) {
                nextSyncedTime = currentSyncedTime
            }

            processedLines.add(line.copy(
                words = updatedWords,
                duration = lineDuration,
            ))
        }

        processedLines.reverse()

        return LyricsText(processedLines, isHighlightAvailable)
    }

    private fun createLyricsLines(
        lineTimestamps: List<Long>,
        remainingText: String,
        trailingLineEndTime: Long,
        globalOffset: Long,
        roleIndex: Int,
        resultLines: MutableList<LyricsLine>
    ) {
        val wordsPositions = mutableListOf<Pair<String, Long?>>()
        var lastWordTimestamp: Long? = null
        var trailingTagTime = -1L
        val currentWordText = StringBuilder()

        var j = 0
        while (j < remainingText.length) {
            if (remainingText[j] == '<') {
                val time = parseTimeTag(remainingText, j + 1, '>')
                if (time != -1L) {
                    val textPart = currentWordText.toString()
                    if (textPart.isNotEmpty()) {
                        wordsPositions.add(textPart to lastWordTimestamp)
                    }
                    currentWordText.setLength(0)
                    val adjustedWordTime = (time - globalOffset).coerceAtLeast(0L)
                    lastWordTimestamp = adjustedWordTime
                    trailingTagTime = adjustedWordTime
                    j = remainingText.indexOf('>', j) + 1
                    continue
                }
            }
            val char = remainingText[j]
            currentWordText.append(char)
            if (!char.isWhitespace()) {
                trailingTagTime = -1L
            }
            j++
        }
        val lastTextPart = currentWordText.toString()
        if (lastTextPart.isNotBlank()) {
            wordsPositions.add(lastTextPart to lastWordTimestamp)
            trailingTagTime = -1L
        }

        val lastIndex = lineTimestamps.size - 1
        for ((index, lineStart) in lineTimestamps.withIndex()) {
            val finalWords = if (wordsPositions.isEmpty()) {
                if (remainingText.isBlank()) {
                    emptyList()
                } else {
                    listOf(LyricsWord(remainingText, lineStart, -1L))
                }
            } else {
                wordsPositions.map { (wordText, wordTime) ->
                    LyricsWord(wordText, wordTime ?: lineStart, -1L)
                }
            }
            val lineDuration = when {
                index == lastIndex && trailingLineEndTime != -1L -> {
                    if (lineStart != -1L) (trailingLineEndTime - lineStart).coerceAtLeast(0L) else -1L
                }
                trailingTagTime != -1L -> {
                    if (lineStart != -1L) (trailingTagTime - lineStart).coerceAtLeast(0L) else -1L
                }
                else -> -1L
            }
            resultLines.add(LyricsLine(
                words = finalWords,
                timeStart = lineStart,
                duration = lineDuration,
                roleIndex = roleIndex
            ))
        }
    }

    private fun parseTimeTag(text: String, startIndex: Int, endChar: Char): Long {
        var minutes = -1
        var seconds = -1
        var millis = -1
        var currentNumber = 0
        var processedNumbersCount = 0
        var separatorSpotted = false

        var i = startIndex
        while (i < text.length) {
            val char = text[i]
            when {
                char.isDigit() -> {
                    currentNumber = currentNumber * 10 + (char - '0')
                    processedNumbersCount++
                }
                char == ':' || char == '.' -> {
                    separatorSpotted = true
                    when {
                        minutes == -1 -> minutes = currentNumber
                        seconds == -1 -> seconds = currentNumber.coerceAtMost(59)
                        else -> millis = currentNumber
                    }
                    currentNumber = 0
                    processedNumbersCount = 0
                }
                char == endChar -> {
                    if (!separatorSpotted) {
                        return -1L
                    }
                    if (seconds == -1) {
                        seconds = currentNumber.coerceAtMost(59)
                    } else {
                        millis = currentNumber
                        if (processedNumbersCount == 1) {
                            millis *= 100
                        }
                        if (processedNumbersCount == 2) {
                            millis *= 10
                        }
                    }
                    if (minutes != -1 && seconds != -1) {
                        return minutes * 60 * 1000L + seconds * 1000L + millis.coerceAtLeast(0)
                    }
                    return -1L
                }
                else -> return -1L
            }
            i++
        }
        return -1L
    }

    fun List<LyricsLine>.findTimePart(position: Long, duration: Long): FocusLyricsPart? {
        if (isEmpty() || position >= duration) {
            return null
        }

        val searchResult = binarySearch { line -> line.timeStart.compareTo(position) }
        val lineIndex = if (searchResult >= 0) searchResult else -(searchResult + 1) - 1

        if (lineIndex < 0) {
            return FocusLyricsPart(0, 0, false)
        }

        val line = this[lineIndex]
        val lineDuration = if (line.duration == -1L) {
            if (line.timeStart == -1L) -1L else (duration - line.timeStart).coerceAtLeast(0L)
        } else {
            line.duration
        }

        val isInGap = position >= line.timeStart + lineDuration
        val isEmptyLine = line.words.all { word -> word.text.isBlank() }
        if (isInGap || isEmptyLine) {
            return findNextNonEmptyLine(lineIndex + 1)
        }

        val wordSearchResult = line.words.binarySearch { word -> word.timeStart.compareTo(position) }
        val wordIndex: Int
        val isFocused: Boolean

        if (wordSearchResult >= 0) {
            // Exact match on a word start time
            wordIndex = wordSearchResult
            isFocused = true
        } else {
            val wordInsertionIndex = -(wordSearchResult + 1)
            if (wordInsertionIndex == 0) {
                // Position is before the first word — anticipate word 0
                wordIndex = 0
                isFocused = false
            } else if (wordInsertionIndex < line.words.size) {
                val prevWord = line.words[wordInsertionIndex - 1]
                val inPrevWord = prevWord.duration == -1L || position < prevWord.timeStart + prevWord.duration
                if (inPrevWord) {
                    // Still inside the previous word's duration
                    wordIndex = wordInsertionIndex - 1
                    isFocused = true
                } else {
                    // Gap between words — anticipate the next word
                    wordIndex = wordInsertionIndex
                    isFocused = false
                }
            } else {
                // Position is after the last word's timeStart — check if we're still within it
                val lastWord = line.words.last()
                val isInLastWord = lastWord.duration == -1L || position < lastWord.timeStart + lastWord.duration
                if (!isInLastWord && lineIndex == size - 1) {
                    return null
                }

                wordIndex = line.words.size - 1
                isFocused = isInLastWord
            }
        }

        return FocusLyricsPart(lineIndex, wordIndex, isFocused)
    }

    private fun List<LyricsLine>.findNextNonEmptyLine(fromIndex: Int): FocusLyricsPart? {
        for (i in fromIndex until size) {
            if (this[i].words.any { word -> word.text.isNotBlank() }) {
                return FocusLyricsPart(i, 0, false)
            }
        }
        return null
    }

}
