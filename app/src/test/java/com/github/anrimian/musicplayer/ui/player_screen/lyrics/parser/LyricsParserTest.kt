package com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser

import com.github.anrimian.musicplayer.ui.player_screen.lyrics.parser.LyricsParser.findTimePart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LyricsParserTest {

    private val rawLyrics = """
        [00:00.00]by RentAnAdviser.com
        [00:36.60]Manches sollte, manches nicht
        [00:40.60]Wir sehen, doch sind wir blind
        [00:44.50]Wir werfen Schatten ohne Licht
        [00:52.40]Nach uns wird es vorher geben
        [00:56.40]Aus der Jugend wird schon Not
        [01:00.10]Wir sterben weiter, bis wir leben
        [01:03.90]Sterben lebend in den Tod
        [01:07.80]Dem Ende treiben wir entgegen
        [01:11.80]Keine Rast, nur vorwärts streben
        [01:15.70]Am Ufer winkt Unendlichkeit
        [01:18.70]Gefangen so im Fluss dеr Zeit
        [01:26.60]Bitte bleib stеh'n, bleib steh'n
        [01:31.30]Zeit
        [01:33.80]Das soll immer so weitergeh'n
        [01:56.70]Warmer Körper ist bald kalt
        [02:00.80]Zukunft kann man nicht beschwör'n
        [02:04.60]Duldet keinen Aufenthalt
        [02:08.20]Erschaffen und sogleich zerstör'n
        [02:12.30]Ich liege hier in deinen Armen
        [02:16.40]Ach, könnt es doch für immer sein!
        [02:19.80]Doch die Zeit kennt kein Erbarmen
        [02:24.20]Schon ist der Moment vorbei
        [02:39.60]Zeit
        [02:42.00]Bitte bleib steh'n, bleib steh'n
        [02:47.60]Zeit
        [02:50.00]Das soll immer so weitergeh'n
        [02:55.30]Zeit
        [02:57.40]Es ist so schön, so schön
        [03:03.00]Ein jeder kennt
        [03:06.30]Den perfekten Moment
        [03:34.30]Zeit
        [03:37.10]Bitte bleib steh'n, bleib steh'n
        [03:49.60]Wenn unsre Zeit gekommen ist, dann ist es Zeit zu geh'n
        [03:57.80]Aufhör'n, wenn's am schönsten ist, die Uhren bleiben steh'n
        [04:05.50]So perfekt ist der Moment, doch weiter läuft die Zeit
        [04:13.20]Augenb****, verweile doch, ich bin noch nicht bereit
        [04:21.10]Zeit
        [04:24.10]Bitte bleib steh'n, bleib steh'n
        [04:29.10]Zeit
        [04:31.40]Das soll immer so weitergeh'n
        [04:36.70]Zeit
        [04:39.10]Es ist so schön, so schön
        [04:45.00]Ein jeder kennt
        [04:48.10]Den perfekten Moment
        [04:55.20]by RentAnAdviser.com
    """

    private var rawLyricsAdv1 = """
        [00:00.00]By RentAnAdviser.com
        [00:00.00]
        [01:00.00]So close no matter how far
        [01:04.50]
        [01:05.10]Couldn't be much more from the heart
        [01:09.80]
        [01:10.40]Forever trusting who we are
        [01:14.80]
        [01:15.30]And nothing else matters
        [01:21.10]
        [01:22.90]Never opened myself this way
        [01:27.50]
        [01:27.90]Life is ours, we live it our way
        [01:32.20]
        [01:32.50]All these words I don't just say
        [01:37.40]
        [01:38.10]And nothing else matters
        [01:42.80]
        [01:45.00]Trust I seek and I find in you
        [01:50.40]
        [01:50.70]Every day for us something new
        [01:55.40]
        [01:55.80]Open mind for a different view
        [02:00.30]
        [02:00.80]And nothing else matters
        [02:05.20]
        [02:08.30]Never cared for what they do
        [02:12.90]
        [02:13.50]Never cared for what they know
        [02:18.30]But I know
        [02:22.00]
        [02:24.70]So close no matter how far
        [02:29.20]
        [02:29.60]Couldn't be much more from the heart
        [02:35.20]Forever trusting who we are
        [02:40.10]And nothing else matters
        [02:44.40]
        [02:47.50]Never cared for what they do
        [02:52.00]
        [02:52.60]Never cared for what they know
        [02:57.40]But I know
        [03:01.20]
        [03:44.10]I never opened myself this way
        [03:48.90]
        [03:49.30]Life is ours, we live it our way
        [03:53.80]
        [03:54.00]All these words I don't just say
        [03:58.70]
        [03:59.40]And nothing else matters
        [04:04.70]
        [04:06.50]Trust I seek and I find in you
        [04:11.10]
        [04:12.10]Every day for us something new
        [04:16.50]
        [04:17.10]Open mind for a different view
        [04:21.30]
        [04:22.10]And nothing else matters
        [04:27.60]
        [04:29.70]Never cared for what they say
        [04:33.90]
        [04:34.70]Never cared for games they play
        [04:38.90]
        [04:39.80]Never cared for what they do
        [04:44.10]
        [04:44.90]Never cared for what they know
        [04:49.70]And I know
        [04:53.30]
        [05:23.80]So close no matter how far
        [05:28.70]
        [05:28.90]Couldn't be much more from the heart
        [05:33.50]
        [05:34.30]Forever trusting who we are
        [05:38.40]
        [05:39.10]No nothing else matters
        [05:43.80]
        [05:44.80]By RentAnAdviser.com
        [05:53.80]
    """

    private var rawLyricsAdv2 = """
        [ti:Monday]
[ar:Imagine Dragons]
[al:Mercury - Act 1]
[by:]

[00:00.49]<00:00.49>Monday - <00:00.67>Imagine <00:00.85>Dragons<00:01.07>
[00:01.29]<00:01.29>Lyrics <00:01.49>by：<00:01.67>Dan <00:01.86>Reynolds/<00:02.04>Wayne <00:02.23>Sermon/<00:02.42>Ben <00:02.63>McKee/<00:02.83>Daniel <00:03.01>Platzman/<00:03.20>Andrew <00:03.41>Tolman<00:03.69>
[00:03.91]<00:03.91>Composed <00:04.10>by：<00:04.30>Dan <00:04.50>Reynolds/<00:04.70>Wayne <00:04.89>Sermon/<00:05.11>Ben <00:05.32>McKee/<00:05.52>Daniel <00:05.71>Platzman/<00:05.92>Andrew <00:06.09>Tolman<00:06.24>
[00:13.24]<00:13.24>When <00:13.44>you're <00:13.60>down <00:13.96>on <00:14.76>your <00:14.95>luck<00:15.24>
[00:15.89]<00:15.89>I <00:16.12>take <00:16.39>them <00:16.59>hands <00:16.89>and <00:17.21>I <00:17.34>turn <00:17.54>it <00:17.68>up<00:18.03>
[00:18.57]<00:18.57>When <00:18.74>you're <00:18.95>face <00:19.38>to <00:20.13>the <00:20.32>floor<00:20.78>
[00:21.25]<00:21.25>I <00:21.44>turn <00:21.73>the <00:21.92>dial <00:22.27>turn <00:22.47>it <00:22.60>up <00:22.93>more<00:24.97>
[00:25.27]<00:25.27>I'm <00:25.60>here <00:26.02>for <00:26.30>you <00:27.21>will <00:27.98>ya <00:28.26>be <00:28.74>there <00:28.98>for <00:29.24>me <00:29.55>too<00:30.56>
[00:31.56]<00:31.56>Ooh<00:33.11>
[00:34.89]<00:34.89>I <00:34.92>believe<00:35.27>
[00:35.27]<00:35.27>I <00:35.56>believe<00:35.75>
[00:35.92]<00:35.92>In <00:36.08>the <00:36.28>cause<00:36.60>
[00:36.60]<00:36.60>In <00:36.75>the <00:36.96>cause<00:37.12>
[00:37.32]<00:37.32>I'm <00:37.48>pound-<00:37.77>for-<00:37.93>pound <00:38.24>baby <00:38.56>turn <00:38.73>it <00:38.94>on<00:39.45>
[00:40.14]<00:40.14>A <00:40.32>million <00:40.71>calls <00:40.90>will <00:41.04>never <00:41.29>do <00:41.55>I'll <00:41.69>never <00:41.94>get <00:42.17>enough <00:42.39>of <00:42.60>you<00:43.01>
[00:43.45]<00:43.45>I'll <00:43.63>never <00:44.10>get <00:44.46>enough <00:45.08>of <00:45.60>you<00:46.25>
[00:48.73]<00:48.73>You <00:48.97>are <00:49.21>my <00:49.59>Monday <00:50.25>you're <00:50.57>the <00:50.92>best <00:51.54>day <00:52.26>of <00:52.96>the <00:53.32>week<00:53.81>
[00:54.03]<00:54.03>So <00:54.31>underrated <00:55.58>and <00:55.93>a <00:56.25>brand <00:56.96>new <00:57.58>start<00:58.94>
[00:59.28]<00:59.28>Don't <00:59.56>care <00:59.91>what <01:00.24>all <01:00.56>the <01:00.91>kids <01:01.41>say<01:01.95>
[01:02.14]<01:02.14>You've <01:02.35>got <01:02.56>the <01:02.87>key <01:03.23>to <01:03.53>my <01:03.99>heart <01:04.64>ooh<01:08.12>
[01:09.17]<01:09.17>When <01:09.36>you <01:09.56>call<01:09.76>
[01:09.93]<01:09.93>When <01:10.08>you <01:10.24>call<01:10.45>
[01:10.63]<01:10.63>On <01:10.78>the <01:10.96>phone<01:11.27>
[01:11.27]<01:11.27>On <01:11.44>the <01:11.59>phone<01:11.82>
[01:12.00]<01:12.00>I <01:12.16>never <01:12.50>let <01:12.81>you <01:12.95>hear <01:13.09>the <01:13.27>dial <01:13.58>tone<01:13.91>
[01:14.25]<01:14.25>Beep<01:14.58>
[01:14.77]<01:14.77>I <01:14.91>believe<01:15.25>
[01:15.25]<01:15.25>I <01:15.52>believe<01:15.74>
[01:15.90]<01:15.90>In <01:16.08>your <01:16.27>touch<01:16.58>
[01:16.58]<01:16.58>In <01:16.75>your <01:16.94>touch<01:17.14>
[01:17.32]<01:17.32>I <01:17.50>know <01:17.71>I <01:17.88>can <01:18.15>be <01:18.42>a <01:18.58>little <01:18.88>much<01:20.94>
[01:21.25]<01:21.25>I'm <01:21.60>there <01:21.93>for <01:22.19>you <01:23.17>will <01:23.89>ya <01:24.02>be <01:24.52>there <01:24.88>for <01:25.26>me <01:25.62>too<01:26.75>
[01:27.71]<01:27.71>Ooh<01:28.82>
[01:31.38]<01:31.38>You <01:31.60>are <01:31.89>my <01:32.25>Monday<01:32.50>
[01:32.50]<01:32.50>You <01:32.66>are <01:32.82>my <01:33.02>Monday<01:33.23>
[01:33.23]<01:33.23>You're <01:33.39>the <01:33.64>best <01:34.28>day <01:34.90>of <01:35.28>the <01:35.43>week<01:35.59>
[01:35.59]<01:35.59>Best <01:35.76>day <01:35.94>of <01:36.12>the <01:36.28>week<01:36.49>
[01:36.68]<01:36.68>So <01:36.92>underrated<01:38.06>
[01:38.25]<01:38.25>So <01:38.47>underrated<01:38.62>
[01:38.62]<01:38.62>And <01:38.79>a <01:39.00>brand <01:39.59>new <01:40.22>start<01:40.71>
[01:40.87]<01:40.87>Brand <01:41.04>new <01:41.29>start<01:41.67>
[01:41.99]<01:41.99>Don't <01:42.25>care <01:42.55>what <01:42.90>all <01:43.21>the <01:43.58>kids <01:43.99>say<01:44.17>
[01:44.17]<01:44.17>Never <01:44.32>care<01:44.55>
[01:44.75]<01:44.75>You've <01:44.93>got <01:45.19>the <01:45.54>key <01:45.90>to <01:46.25>my <01:46.66>heart <01:47.32>ooh<01:50.92>
[02:13.71]<02:13.71>You <02:13.94>could <02:14.09>be <02:14.22>the <02:14.37>one <02:14.56>that <02:14.73>I've <02:14.89>been <02:15.05>waiting <02:15.27>all <02:15.46>my <02:15.62>life <02:15.88>for<02:16.09>
[02:16.09]<02:16.09>You <02:16.38>could <02:16.56>be <02:16.73>the <02:16.94>key <02:17.11>to <02:17.28>lead <02:17.45>me <02:17.63>up <02:17.81>into <02:17.97>the <02:18.26>highest <02:18.81>floor<02:19.08>
[02:19.27]<02:19.27>Give <02:19.45>me <02:19.65>loving <02:19.91>keep <02:20.19>me <02:20.33>going <02:20.60>'til <02:20.82>the <02:20.99>midnight <02:21.58>hour<02:21.89>
[02:22.08]<02:22.08>Bring <02:22.24>me <02:22.30>up <02:22.52>lift <02:22.73>me <02:22.94>up <02:23.23>to <02:23.47>your <02:23.66>rainbow <02:24.23>tower<02:27.99>
[02:28.86]<02:28.86>Your <02:29.03>rainbow <02:29.63>tower<02:33.47>
[02:33.98]<02:33.98>My <02:34.28>Monday <02:34.89>doo-<02:35.13>oo-<02:35.50>doo<02:35.90>
[02:36.78]<02:36.78>Doo-<02:36.97>oo-<02:37.22>doo <02:37.63>doo-<02:37.83>oo-<02:38.11>doo<02:38.45>
[02:38.61]<02:38.61>Monday <02:38.86>my <02:39.13>my <02:39.49>Monday<02:40.24>
[02:44.68]<02:44.68>My <02:44.92>Monday<02:45.57>
[02:50.14]<02:50.14>My <02:50.31>Monday <02:50.86>doo-<02:51.23>oo-<02:51.47>doo<02:51.77>
[02:52.76]<02:52.76>Doo-<02:52.94>oo-<02:53.17>doo <02:53.57>doo-<02:53.99>oo-<02:54.16>doo<02:54.73>

    """

    @Test
    fun `test findTimePart in gaps`() {
        val line = LyricsLine(
            listOf(
                LyricsWord(text = "Word 1 ", timeStart = 1000, duration = 1000), // 1000-2000
                LyricsWord(text = "Word 2 ", timeStart = 3000, duration = 1000)  // 3000-4000
            ),
            1000,
            5000
        )
        val lyrics = listOf(line)

        // Before first word (gap): 500
        val partBefore = lyrics.findTimePart(position = 500L, duration = 10000L)
        assertEquals(0, partBefore?.wordIndex) // Should return next word: index 0
        assertEquals(false, partBefore?.isFocused)

        // In first word: 1500
        val partIn1 = lyrics.findTimePart(position = 1500L, duration = 10000L)
        assertEquals(0, partIn1?.wordIndex)
        assertEquals(true, partIn1?.isFocused)

        // Gap between words: 2500
        val partGap = lyrics.findTimePart(position = 2500L, duration = 10000L)
        assertEquals(1, partGap?.wordIndex) // Should return next word: index 1
        assertEquals(false, partGap?.isFocused)

        // In second word: 3500
        val partIn2 = lyrics.findTimePart(position = 3500L, duration = 10000L)
        assertEquals(1, partIn2?.wordIndex)
        assertEquals(true, partIn2?.isFocused)
        
        // After last word in line: 4500
        val partAfter = lyrics.findTimePart(position = 4500L, duration = 10000L)
        assertEquals(null, partAfter) // Reached end of song, highlight nothing
    }

    @Test
    fun `test findTimePart in gaps with adv2 lyrics`() {
        val lyrics = LyricsParser.parseLyrics(rawLyricsAdv2)
        
        // Line [00:25.27]<00:25.27>I'm ... too<00:30.56>
        // Next Line [00:31.56]<00:31.56>Ooh<00:33.11>
        
        // Index of "Ooh" line should be 13 (0-3 metadata, 4 blank, 5-12 other lines)
        val oohIndex = 13
        assertEquals("Ooh", lyrics.lines[oohIndex].words[0].text)
        
        // 1. Between lines: 31.000.
        val partBetweenLines = lyrics.lines.findTimePart(31000L, 60000L)
        
        assertEquals(oohIndex, partBetweenLines?.lineIndex)
        assertEquals(0, partBetweenLines?.wordIndex)
        assertEquals(false, partBetweenLines?.isFocused)

        // 2. Before first word of line: 25.100.
        val partBeforeLine6 = lyrics.lines.findTimePart(21000L, 60000L)
        assertEquals(11, partBeforeLine6?.lineIndex)
        assertEquals(0, partBeforeLine6?.wordIndex)
        assertEquals(false, partBeforeLine6?.isFocused)
        
        // 3. After last line: 2:55.000
        val partAfterLastLine = lyrics.lines.findTimePart(175000L, 180000L)
        assertEquals(null, partAfterLastLine)
    }

    @Test
    fun `test lyrics parser`() {
        val lyrics = LyricsParser.parseLyrics(rawLyrics)
        println("lines: ${lyrics.lines}")
    }

    @Test
    fun `test adv lyrics parser`() {
        val lyrics = LyricsParser.parseLyrics(rawLyricsAdv2)
        
        val line = lyrics.lines.first { it.timeStart != -1L && it.text.contains("Monday") }
        assertEquals(3, line.words.size)
        assertEquals("Monday - ", line.words[0].text)
        assertEquals(490L, line.words[0].timeStart)
        assertEquals(180L, line.words[0].duration)
    }

    //contains empty parts
    @Test
    fun `test adv lyrics parser 1, check range search no results`() {
        val lyrics = LyricsParser.parseLyrics(rawLyricsAdv1)

        val pos = (1 * 60 * 1000L) + (5 * 1000)//1:05
        val part = lyrics.lines.findTimePart(pos, 6 * 60 * 1000)
        // At 1:05.00, it falls on empty Line 3 [01:04.50]. It is now skipped and anticipates next line.
        assertEquals(4, part?.lineIndex)
        assertEquals(0, part?.wordIndex)
        assertEquals(false, part?.isFocused)
    }

    @Test
    fun `test regular lines gap anticipation`() {
        val text = """
            [00:10.00]Line 1
            [00:15.00]
            [00:20.00]Line 2
        """.trimIndent()
        val lyrics = LyricsParser.parseLyrics(text)

        // At 16s we are in a blank gap and should anticipate "Line 2" (which is at index 2).
        val part = lyrics.lines.findTimePart(position = 16000L, duration = 60000L)

        assertEquals(2, part?.lineIndex)
        assertEquals(0, part?.wordIndex)
        assertEquals(false, part?.isFocused)
    }

    @Test
    fun `check lines count`() {
        val text = """
            [01:50.70]Es ist so schön, so schön
            [01:55.80]Ein jeder kennt
            [02:00.80]Den perfekten Moment
        """
        val lyrics = LyricsParser.parseLyrics(text)
        assertEquals(3, lyrics.lines.size)
        assertEquals(60000 + 50000 + 700, lyrics.lines[0].timeStart)
        assertEquals(60000 + 55000 + 800, lyrics.lines[1].timeStart)
        assertEquals(2*60000 + 800, lyrics.lines[2].timeStart)
    }

    @Test
    fun `test duration is less than time in time part`() {
        val rawLyrics = """
            [01:00.00]Line 1
            [04:00.00]Line 2
        """.trimIndent()

        val lyrics = LyricsParser.parseLyrics(rawLyrics)
        val duration = 3 * 60 * 1000L
        val part1 = lyrics.lines.findTimePart(170_000L, duration)

        assertEquals(0, part1?.lineIndex)
        assertEquals(true, part1?.isFocused)


        val part2 = lyrics.lines.findTimePart(250_000L, duration)

        assertEquals(null, part2)
    }

    @Test
    fun `test non-time tags as text`() {
        val text = """
            [ti:Song Title]
            [00:10.00]First line [Chorus]
            [00:20.00]Second line
        """.trimIndent()
        val lyrics = LyricsParser.parseLyrics(text)

        val lyricsLine = lyrics.lines.first { it.timeStart == 10000L }
        assertEquals(true, lyricsLine.text.contains("First line [Chorus]"))
    }

    @Test
    fun `test empty string input`() {
        val lyrics = LyricsParser.parseLyrics("")
        assertEquals(0, lyrics.lines.size)
    }

    @Test
    fun `test plain text without any tags`() {
        val text = "Just some plain text without any LRC tags."
        val lyrics = LyricsParser.parseLyrics(text)
        assertEquals(1, lyrics.lines.size)
        assertEquals(-1L, lyrics.lines[0].timeStart)
    }

    @Test
    fun `test malformed brackets - unclosed`() {
        val text = "[00:10.00]Valid line\n[00:20.00Unclosed bracket"
        val lyrics = LyricsParser.parseLyrics(text)
        
        // Should have two lines: one fallback, one valid
        assertEquals(2, lyrics.lines.size)
        assertEquals(10000L, lyrics.lines[0].timeStart)
        assertEquals(-1L, lyrics.lines[1].timeStart)
        // If unclosed bracket, it is usually appended to the previous text or ignored
        // Let's verify it doesn't crash
    }

    @Test
    fun `test tags containing non-numeric values`() {
        val text = "[00:xx.00]Invalid time tag"
        val lyrics = LyricsParser.parseLyrics(text)
        assertEquals(1, lyrics.lines.size)
        assertEquals(-1L, lyrics.lines[0].timeStart)
    }

    @Test
    fun `test tags with negative numbers`() {
        val text = "[-01:00.00]Negative time"
        val lyrics = LyricsParser.parseLyrics(text)
        assertEquals(1, lyrics.lines.size)
        assertEquals(-1L, lyrics.lines[0].timeStart)
    }

    @Test
    fun `test tags with missing vital numbers`() {
        // According to current parser: [:00.00] -> minutes is 0, num is 0
        val text = "[:00.00]Missing minute"
        val lyrics = LyricsParser.parseLyrics(text)
        assertEquals(1, lyrics.lines.size)
        assertEquals(0L, lyrics.lines[0].timeStart)
    }

    @Test
    fun `test parts count validation with nested tags`() {
        val text = "[00:10.00]Line 1 <00:11.00>Word 2 <00:12.00>Word 3"
        val lyrics = LyricsParser.parseLyrics(text)
        
        assertEquals(1, lyrics.lines.size)
        val line = lyrics.lines[0]
        assertEquals(3, line.words.size)
        assertEquals("Line 1 ", line.words[0].text)
        assertEquals("Word 2 ", line.words[1].text)
        assertEquals("Word 3", line.words[2].text)
    }

    @Test
    fun `test findTimePart with negative position`() {
        val text = "[00:10.00]Line 1"
        val lyrics = LyricsParser.parseLyrics(text)
        
        val part = lyrics.lines.findTimePart(-5000L, 60000L)
        // Binary search for -5000 in [10000] will return -1
        // (searchResult < 0) -> lineIndex = -1. lineIndex < 0 -> returns FocusLyricsPart(0, 0, false)
        assertEquals(0, part?.lineIndex)
        assertEquals(0, part?.wordIndex)
        assertEquals(false, part?.isFocused)
    }

    @Test
    fun `test findTimePart exactly on bounds`() {
        val text = "[00:10.00]Word 1 <00:11.00>Word 2\n[00:15.00]Next Line"
        val lyrics = LyricsParser.parseLyrics(text)
        
        // Exactly on timeStart of line 1
        val partStart = lyrics.lines.findTimePart(10000L, 60000L)
        assertEquals(0, partStart?.lineIndex)
        assertEquals(0, partStart?.wordIndex)
        assertEquals(true, partStart?.isFocused)

        // Exactly on timeStart of word 2
        val partWord2 = lyrics.lines.findTimePart(11000L, 60000L)
        assertEquals(0, partWord2?.lineIndex)
        assertEquals(1, partWord2?.wordIndex)
        assertEquals(true, partWord2?.isFocused)

        // Exactly on end of line (start of next line)
        val partNextLine = lyrics.lines.findTimePart(15000L, 60000L)
        assertEquals(1, partNextLine?.lineIndex)
        assertEquals(0, partNextLine?.wordIndex)
        assertEquals(true, partNextLine?.isFocused)
    }

    @Test
    fun `test findTimePart with large duration`() {
        val text = "[00:10.00]Line 1"
        val lyrics = LyricsParser.parseLyrics(text)
        
        // duration acts as a limit wrapper in findTimePart: if (position >= duration) return null
        val part = lyrics.lines.findTimePart(10000L, 5000L)
        assertEquals(null, part)
    }

    @Test
    fun `test findTimePart with unknown line duration`() {
        val text = "[00:10.00]Last Line"
        val lyrics = LyricsParser.parseLyrics(text)
        
        // Last line has -1L duration in resultLines (line 137 in parser)
        // lineDuration = if (line.duration == -1L) (duration - line.timeStart).coerceAtLeast(0L) else line.duration
        // position < (line.timeStart + lineDuration) -> position < (line.timeStart + duration - line.timeStart) -> position < duration
        
        val part = lyrics.lines.findTimePart(20000L, 30000L)
        assertEquals(0, part?.lineIndex)
        assertEquals(true, part?.isFocused)

        val partAfter = lyrics.lines.findTimePart(35000L, 30000L)
        assertEquals(null, partAfter)
    }

    @Test
    fun `test completely unsynced lyrics`() {
        val text = "Pure text line 1\nPure text line 2"
        val lyricsText = LyricsParser.parseLyrics(text)
        val lines = lyricsText.lines

        assertEquals(2, lines.size)

        assertEquals(-1L, lines[0].timeStart)
        assertEquals(-1L, lines[0].duration)
        assertEquals(1, lines[0].words.size)
        assertEquals("Pure text line 1", lines[0].words[0].text.trim())
        assertEquals(-1L, lines[0].words[0].timeStart)
        assertEquals(-1L, lines[0].words[0].duration)

        assertEquals(-1L, lines[1].timeStart)
        assertEquals("Pure text line 2", lines[1].words[0].text.trim())
    }


    @Test
    fun `test mixed lyrics`() {
        val text = "Intro text \n[00:10.00] First synced line\nOutro text"
        val lyricsText = LyricsParser.parseLyrics(text)
        val lines = lyricsText.lines

        assertEquals(3, lines.size)

        // Exact original relative line order
        assertEquals(-1L, lines[0].timeStart)
        assertEquals("Intro text", lines[0].words[0].text.trim())

        assertEquals(10000L, lines[1].timeStart)
        assertEquals("First synced line", lines[1].words[0].text.trim())

        assertEquals(-1L, lines[2].timeStart)
        assertEquals("Outro text", lines[2].words[0].text.trim())
    }

    @Test
    fun `test multiple tags per line`() {
        val text = "[00:10.00][01:20.00] Chorus"
        val lyricsText = LyricsParser.parseLyrics(text)
        val lines = lyricsText.lines

        assertEquals(2, lines.size)

        assertEquals(10000L, lines[0].timeStart)
        assertEquals("Chorus", lines[0].words[0].text.trim())

        assertEquals(80000L, lines[1].timeStart)
        assertEquals("Chorus", lines[1].words[0].text.trim())

        assertEquals(lines[0].text, lines[1].text)
    }

    @Test
    fun `test malformed and metadata tags ignore logic`() {
        val text = "[ar:Artist Name]\n[ti:Track Title]\n[invalid] This is just text\n[00:15.00] Valid second line"
        val lyricsText = LyricsParser.parseLyrics(text)

        val syncedLine = lyricsText.lines.find { it.timeStart == 15000L }
        assertEquals(true, syncedLine != null)
        assertEquals("Valid second line", syncedLine!!.words[0].text.trim())
    }

    @Test
    fun `test inline timestamps on single line produces single line not duplicate`() {
        val text = "[01:00.00]So close no matter how far[01:04.50]"
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(1, lyrics.lines.size)
        val line = lyrics.lines[0]
        assertEquals("So close no matter how far", line.text)
        assertEquals(60000L, line.timeStart)
        assertEquals(4500L, line.duration)
    }

    @Test
    fun `test inline timestamps combined with normal lines`() {
        val text = """
            [00:00.00]By RentAnAdviser.com
            [01:00.00]So close no matter how far[01:04.50]
            [01:05.10]Couldn't be much more from the heart
        """.trimIndent()
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(3, lyrics.lines.size)
        val middleLine = lyrics.lines[1]
        assertEquals(60000L, middleLine.timeStart)
        assertEquals(4500L, middleLine.duration)
        assertEquals("So close no matter how far", middleLine.text)
    }

    @Test
    fun `test findTimePart does not return line with duration minus one for inline timestamp line`() {
        val text = "[01:00.00]So close no matter how far[01:04.50]"
        val lyrics = LyricsParser.parseLyrics(text)

        val part = lyrics.lines.findTimePart(position = 62000L, duration = 300000L)

        val line = lyrics.lines[part!!.lineIndex]
        assertEquals(true, part.isFocused)
        assertEquals(0, part.lineIndex)
        // Assert the line's duration != -1L
        assertEquals(4500L, line.duration)
    }

    @Test
    fun `test mid-line timestamp splits into separate lines with own text`() {
        val text = "[00:00.66]My lover's got humour[00:03.51]She's the giggle at a funeral"
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(2, lyrics.lines.size)

        val line0 = lyrics.lines[0]
        assertEquals(660L, line0.timeStart)
        assertEquals("My lover's got humour", line0.text)
        assertEquals(2850L, line0.duration) // 3510 - 660

        val line1 = lyrics.lines[1]
        assertEquals(3510L, line1.timeStart)
        assertEquals("She's the giggle at a funeral", line1.text)
    }

    @Test
    fun `test mid-line timestamp with three segments`() {
        val text = "[00:00.00]First[00:05.00]Second[00:10.00]Third"
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(3, lyrics.lines.size)

        assertEquals(0L, lyrics.lines[0].timeStart)
        assertEquals("First", lyrics.lines[0].text)
        assertEquals(5000L, lyrics.lines[0].duration)

        assertEquals(5000L, lyrics.lines[1].timeStart)
        assertEquals("Second", lyrics.lines[1].text)
        assertEquals(5000L, lyrics.lines[1].duration)

        assertEquals(10000L, lyrics.lines[2].timeStart)
        assertEquals("Third", lyrics.lines[2].text)
    }

    @Test
    fun `test trailing end marker still works after mid-line fix`() {
        val text = "[01:00.00]Some text[01:04.50]"
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(1, lyrics.lines.size)
        val line = lyrics.lines[0]
        assertEquals(60000L, line.timeStart)
        assertEquals(4500L, line.duration)
        assertEquals("Some text", line.text)
    }

    @Test
    fun `test mid-line split within full lyrics context`() {
        val text = """
            [00:00.00]Intro
            [00:00.66]My lover's got humour[00:03.51]She's the giggle at a funeral
            [00:10.00]Next verse
        """.trimIndent()
        val lyrics = LyricsParser.parseLyrics(text)

        assertEquals(4, lyrics.lines.size)

        // Assert no two consecutive lines have the same text
        for (i in 0 until lyrics.lines.size - 1) {
            val currentText = lyrics.lines[i].text
            val nextText = lyrics.lines[i+1].text
            assert(currentText != nextText) { "Line $i and ${i+1} have same text: $currentText" }
        }
    }

    private val LyricsLine.text
        get() = words.joinToString(separator = "") { word -> word.text }

    private fun LyricsParser.parseLyrics(text: String) = parseLyrics(text, {})

}