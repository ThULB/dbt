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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class SubtitleConverter {

    private static final String WEBVTT_HEADER = "WEBVTT";

    /** Only a cue timing line contains this, so only there a comma is a decimal separator. */
    private static final String CUE_TIMING_ARROW = "-->";

    private static final Charset FALLBACK_CHARSET = Charset.forName("windows-1252");

    private SubtitleConverter() {
    }

    public static void toWebVTT(Path source, Path target) throws IOException {
        Files.writeString(target, toWebVTT(readText(source)), StandardCharsets.UTF_8);
    }

    public static String toWebVTT(String subtitle) {
        String vtt = subtitle.lines()
                .map(line -> line.contains(CUE_TIMING_ARROW) ? line.replace(',', '.') : line)
                .collect(Collectors.joining("\n", "", "\n"));

        return vtt.startsWith(WEBVTT_HEADER) ? vtt : WEBVTT_HEADER + "\n\n" + vtt;
    }

    /**
     * Reads given file as text. Subtitles are often encoded in a legacy charset, so UTF-8 is only
     * used if the file can be decoded without errors.
     */
    private static String readText(Path source) throws IOException {
        byte[] bytes = Files.readAllBytes(source);
        int offset = hasUTF8Bom(bytes) ? 3 : 0;

        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, offset, bytes.length - offset, FALLBACK_CHARSET);
        }
    }

    private static boolean hasUTF8Bom(byte[] bytes) {
        return bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF;
    }

}