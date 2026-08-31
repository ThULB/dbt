/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestSubtitleConverter {
    private static final String SUBRIP = "1\r\n"
            + "00:00:01,000 --> 00:00:04,500\r\n"
            + "Schön, dass Sie da sind.\r\n"
            + "\r\n"
            + "2\r\n"
            + "00:00:04,500 --> 00:00:08,120\r\n"
            + "Heute begrüßen wir Frau Professorin.\r\n";

    private static final String WEBVTT = "WEBVTT\n"
            + "\n"
            + "1\n"
            + "00:00:01.000 --> 00:00:04.500\n"
            + "Schön, dass Sie da sind.\n"
            + "\n"
            + "2\n"
            + "00:00:04.500 --> 00:00:08.120\n"
            + "Heute begrüßen wir Frau Professorin.\n";

    @TempDir
    Path tempDir;

    @Test
    public void testSubRipIsConverted() {
        assertEquals(WEBVTT, SubtitleConverter.toWebVTT(SUBRIP));
    }

    @Test
    public void testCommaInCueTextIsKept() {
        String vtt = SubtitleConverter.toWebVTT("1\n00:00:01,000 --> 00:00:04,500\nJa, gerne, sehr gerne.\n");

        assertTrue(vtt.contains("00:00:01.000 --> 00:00:04.500"), "cue timing should use a dot");
        assertTrue(vtt.contains("Ja, gerne, sehr gerne."), "commas in cue text should be kept");
    }

    @Test
    public void testSingleDigitHourIsConverted() {
        String vtt = SubtitleConverter.toWebVTT("1\n0:00:01,000 --> 0:00:04,500\nEinstellige Stunde.\n");

        assertTrue(vtt.contains("0:00:01.000 --> 0:00:04.500"), "non standard timings should be converted too");
    }

    @Test
    public void testWebVTTGetsNoSecondHeader() {
        assertEquals(WEBVTT, SubtitleConverter.toWebVTT(WEBVTT));
    }

    @Test
    public void testLegacyCharsetIsDetected() throws IOException {
        Path source = tempDir.resolve("video.srt");
        Files.write(source, ("1\r\n00:00:01,000 --> 00:00:04,500\r\nSchön – wirklich schön.\r\n")
                .getBytes(Charset.forName("windows-1252")));

        Path target = tempDir.resolve("imported.de.vtt");
        SubtitleConverter.toWebVTT(source, target);

        assertEquals("WEBVTT\n\n1\n00:00:01.000 --> 00:00:04.500\nSchön – wirklich schön.\n",
                Files.readString(target, StandardCharsets.UTF_8));
    }

    @Test
    public void testUTF8BomIsRemoved() throws IOException {
        Path source = tempDir.resolve("video.vtt");
        try (OutputStream out = Files.newOutputStream(source)) {
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write(WEBVTT.getBytes(StandardCharsets.UTF_8));
        }

        Path target = tempDir.resolve("imported.de.vtt");
        SubtitleConverter.toWebVTT(source, target);

        assertEquals(WEBVTT, Files.readString(target, StandardCharsets.UTF_8));
    }
}
