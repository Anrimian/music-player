package com.github.anrimian.musicplayer.data.storage.providers

import com.github.anrimian.musicplayer.domain.models.exceptions.InvalidVolumeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FileVolumeTest {

    @Test
    fun `primary storage path with trailing slash resolves to primary volume`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/emulated/0/Music/"))
        assertEquals("storage/emulated/0", volume.storageKey)
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `primary storage path without trailing slash resolves to primary volume`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/emulated/0/Music"))
        assertEquals("storage/emulated/0", volume.storageKey)
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `primary storage root resolves to itself`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/emulated/0"))
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `leading slash is tolerated on primary path`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("/storage/emulated/0/Music"))
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `non-zero user id is preserved (work profile, Huawei dual app)`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("/storage/emulated/10/Music"))
        assertEquals("storage/emulated/10", volume.storageKey)
        assertEquals("storage/emulated/10", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `removable storage path resolves to UUID-rooted volume`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/8AB5-181D/Music/"))
        assertEquals("storage/8AB5-181D", volume.storageKey)
        assertEquals("storage/8AB5-181D", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `removable storage path without trailing slash resolves to UUID-rooted volume`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/8AB5-181D/Music"))
        assertEquals("storage/8AB5-181D", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `removable storage root resolves to itself`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("storage/8AB5-181D"))
        assertEquals("storage/8AB5-181D", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `leading slash is tolerated on removable path`() {
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull("/storage/8AB5-181D/Music"))
        assertEquals("storage/8AB5-181D", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `non-storage path returns null`() {
        assertNull(FileVolume.fromCanonicalPathOrNull("/data/user/0/com.github.anrimian.musicplayer"))
    }

    @Test
    fun `empty string returns null`() {
        assertNull(FileVolume.fromCanonicalPathOrNull(""))
    }

    @Test
    fun `only the storage prefix returns null`() {
        assertNull(FileVolume.fromCanonicalPathOrNull("storage/"))
        assertNull(FileVolume.fromCanonicalPathOrNull("/storage"))
    }

    @Test
    fun `emulated without a user id returns null`() {
        assertNull(FileVolume.fromCanonicalPathOrNull("/storage/emulated"))
        assertNull(FileVolume.fromCanonicalPathOrNull("storage/emulated/"))
    }

    @Test
    fun `double slash after storage returns null`() {
        // First segment ends up empty — not a real volume id, reject.
        assertNull(FileVolume.fromCanonicalPathOrNull("storage//Music"))
    }

    @Test
    fun `throwing variant returns the same volume on a valid path`() {
        val volume = FileVolume.fromCanonicalPath("storage/8AB5-181D/Music")
        assertEquals("storage/8AB5-181D", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `throwing variant raises InvalidVolumeException on a malformed path`() {
        assertThrows(InvalidVolumeException::class.java) {
            FileVolume.fromCanonicalPath("/data/user/0/foo")
        }
    }

    @Test
    fun `canonicalize rewrites mnt media rw removable path to storage form`() {
        assertEquals(
            "storage/4A21-0000/Music/song.mp3",
            FileVolume.canonicalize("/mnt/media_rw/4A21-0000/Music/song.mp3"),
        )
    }

    @Test
    fun `canonicalize rewrites mnt media rw path without leading slash`() {
        assertEquals(
            "storage/4A21-0000/Audio Files/Telegram",
            FileVolume.canonicalize("mnt/media_rw/4A21-0000/Audio Files/Telegram"),
        )
    }

    @Test
    fun `canonicalize is idempotent on storage emulated path`() {
        val path = "storage/emulated/0/Music/track.mp3"
        assertEquals(path, FileVolume.canonicalize(path))
        assertEquals(path, FileVolume.canonicalize("/$path"))
    }

    @Test
    fun `canonicalize is idempotent on removable storage path`() {
        val path = "storage/8AB5-181D/Music/track.mp3"
        assertEquals(path, FileVolume.canonicalize(path))
        assertEquals(path, FileVolume.canonicalize("/$path"))
    }

    @Test
    fun `canonicalize trims leading slash on non-storage paths`() {
        assertEquals(
            "data/user/0/foo",
            FileVolume.canonicalize("/data/user/0/foo"),
        )
    }

    @Test
    fun `canonicalize returns empty string for empty input`() {
        assertEquals("", FileVolume.canonicalize(""))
    }

    // --- mnt/expand (adoptable storage) ---

    @Test
    fun `canonicalize rewrites mnt expand path to storage form`() {
        assertEquals(
            "storage/4A21-0000/Music/song.mp3",
            FileVolume.canonicalize("/mnt/expand/4A21-0000/Music/song.mp3"),
        )
    }

    @Test
    fun `canonicalize rewrites mnt expand path without leading slash`() {
        assertEquals(
            "storage/4A21-0000/Music",
            FileVolume.canonicalize("mnt/expand/4A21-0000/Music"),
        )
    }

    // --- mnt/runtime (FUSE overlay) ---

    @Test
    fun `canonicalize rewrites mnt runtime default emulated path`() {
        assertEquals(
            "storage/emulated/0/Music/song.mp3",
            FileVolume.canonicalize("/mnt/runtime/default/emulated/0/Music/song.mp3"),
        )
    }

    @Test
    fun `canonicalize rewrites mnt runtime write emulated path`() {
        assertEquals(
            "storage/emulated/0/Music",
            FileVolume.canonicalize("mnt/runtime/write/emulated/0/Music"),
        )
    }

    @Test
    fun `canonicalize rewrites mnt runtime read removable path`() {
        assertEquals(
            "storage/8AB5-181D/Music",
            FileVolume.canonicalize("/mnt/runtime/read/8AB5-181D/Music"),
        )
    }

    @Test
    fun `canonicalize leaves malformed mnt runtime path alone`() {
        // No volume segment after the mode — let fromCanonicalPath reject it.
        assertEquals("mnt/runtime/default", FileVolume.canonicalize("/mnt/runtime/default"))
    }

    // --- data/media (rooted ROM emulated storage) ---

    @Test
    fun `canonicalize rewrites data media path to storage emulated form`() {
        assertEquals(
            "storage/emulated/0/Music/song.mp3",
            FileVolume.canonicalize("/data/media/0/Music/song.mp3"),
        )
    }

    @Test
    fun `canonicalize rewrites data media path without leading slash`() {
        assertEquals(
            "storage/emulated/10/Music",
            FileVolume.canonicalize("data/media/10/Music"),
        )
    }

    // --- end-to-end through fromCanonicalPath ---

    @Test
    fun `mnt expand path resolves to removable volume through fromCanonicalPath`() {
        val canonical = FileVolume.canonicalize("/mnt/expand/4A21-0000/Music/song.mp3")
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull(canonical))
        assertEquals("storage/4A21-0000", volume.path)
        assertFalse(volume.isPrimary)
    }

    @Test
    fun `mnt runtime emulated path resolves to primary volume through fromCanonicalPath`() {
        val canonical = FileVolume.canonicalize("/mnt/runtime/default/emulated/0/Music")
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull(canonical))
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }

    @Test
    fun `data media path resolves to primary volume through fromCanonicalPath`() {
        val canonical = FileVolume.canonicalize("/data/media/0/Music/song.mp3")
        val volume = requireNotNull(FileVolume.fromCanonicalPathOrNull(canonical))
        assertEquals("storage/emulated/0", volume.path)
        assertTrue(volume.isPrimary)
    }
}
