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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mycore.common.MCRTestConfiguration;
import org.mycore.common.MCRTestProperty;
import org.mycore.test.MyCoReTest;

import de.urmel_dl.dbt.media.entity.Sources;

@MyCoReTest
@MCRTestConfiguration(properties = {
    @MCRTestProperty(key = "MCR.Media.Wowza.BaseURL", string = "http://localhost/wowza"),
    @MCRTestProperty(key = "MCR.Media.Wowza.RTMPBaseURL", string = "rtmp://localhost/wowza"),
    @MCRTestProperty(key = "MCR.Media.Wowza.SMILContentPathPrefix", string = "vod/_definst_/smil:"),
    @MCRTestProperty(key = "MCR.Media.Wowza.ContentPathPrefix", string = "vod/_definst_/mp4:"),
    @MCRTestProperty(key = "MCR.Media.Wowza.SharedSecred", string = "test")
})
public class TestMediaService {

    private static final String DERIVATE_ID = "dbt_derivate_00000001";

    private static final String IMPORTED_NAME = "imported.de.vtt";

    private static final String WEBVTT = "WEBVTT\n\n"
        + "00:00:01.000 --> 00:00:04.500 position:10%,line-left\n"
        + "Schön, dass Sie da sind.\n";

    private static final String GENERATED_VTT = "WEBVTT\n\n00:00:01.000 --> 00:00:04.500\nautomatisch erzeugt\n";

    @TempDir
    Path derivate;

    @AfterEach
    public void cleanUp() throws IOException {
        // removes only what this test created and evicts the file caches, so nothing leaks into
        // the next test
        MediaService.deleteMediaFiles(internalMediaId("myVideo.mp4"));
    }

    @Test
    public void testIsSubtitleSupported() {
        assertTrue(MediaService.isSubtitleSupported(derivate.resolve("myVideo.vtt")));
        assertTrue(MediaService.isSubtitleSupported(derivate.resolve("myVideo.VTT")));

        assertFalse(MediaService.isSubtitleSupported(derivate.resolve("myVideo.srt")));
        assertFalse(MediaService.isSubtitleSupported(derivate.resolve("myVideo.mp4")));
        assertFalse(MediaService.isSubtitleSupported(derivate.resolve("myVideo.txt")));
    }

    @Test
    public void testFindSubtitleFileOfMediaFile() throws IOException {
        Path mediaFile = write("myVideo.mp4", "not a real video");
        Path subtitleFile = write("myVideo.vtt", WEBVTT);

        assertEquals(Optional.of(subtitleFile), MediaService.findSubtitleFile(mediaFile));
    }

    @Test
    public void testFindMediaFileOfSubtitleFile() throws IOException {
        Path mediaFile = write("myVideo.mp4", "not a real video");
        Path subtitleFile = write("myVideo.vtt", WEBVTT);

        assertEquals(Optional.of(mediaFile), MediaService.findMediaFile(subtitleFile));
    }

    @Test
    public void testFindIgnoresDifferentName() throws IOException {
        Path mediaFile = write("myVideo.mp4", "not a real video");
        Path subtitleFile = write("untertitel.vtt", WEBVTT);

        assertTrue(MediaService.findSubtitleFile(mediaFile).isEmpty(), "different name must not match");
        assertTrue(MediaService.findMediaFile(subtitleFile).isEmpty(), "different name must not match");
    }

    @Test
    public void testFindIgnoresCase() throws IOException {
        Path mediaFile = write("MyVideo.MP4", "not a real video");
        Path subtitleFile = write("myvideo.VTT", WEBVTT);

        assertEquals(Optional.of(subtitleFile), MediaService.findSubtitleFile(mediaFile));
        assertEquals(Optional.of(mediaFile), MediaService.findMediaFile(subtitleFile));
    }

    @Test
    public void testImportedSubtitleIsCopied() throws IOException {
        Path subtitleFile = write("myVideo.vtt", WEBVTT);

        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), subtitleFile, "de");

        Path imported = storeOf("myVideo.mp4").resolve(IMPORTED_NAME);
        assertTrue(Files.exists(imported), "imported subtitle should exist");
        assertEquals(-1L, Files.mismatch(subtitleFile, imported), "imported subtitle should be unchanged");
    }

    @Test
    public void testImportedSubtitleReplacesGeneratedOne() throws IOException {
        writeGenerated("myVideo.mp4");
        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", WEBVTT), "de");

        assertEquals(List.of(IMPORTED_NAME), subtitleSourcesOf("myVideo.mp4"),
            "only the imported subtitle should be offered");
    }

    @Test
    public void testSubtitleWithoutCuesIsNotImported() throws IOException {
        writeGenerated("myVideo.mp4");

        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", ""), "de");

        assertFalse(Files.exists(storeOf("myVideo.mp4").resolve(IMPORTED_NAME)),
            "an empty subtitle must not be imported");
        assertEquals(List.of("myVideo_de.vtt"), subtitleSourcesOf("myVideo.mp4"),
            "the generated subtitle must be kept");
    }

    @Test
    public void testGeneratedSubtitleIsUsedWithoutImport() throws IOException {
        writeGenerated("myVideo.mp4");

        assertEquals(List.of("myVideo_de.vtt"), subtitleSourcesOf("myVideo.mp4"),
            "without an import nothing should change");
    }

    @Test
    public void testDeleteImportedSubtitleRestoresGeneratedOne() throws IOException {
        writeGenerated("myVideo.mp4");
        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", WEBVTT), "de");

        MediaService.deleteImportedSubtitleFile(internalMediaId("myVideo.mp4"));

        assertFalse(Files.exists(storeOf("myVideo.mp4").resolve(IMPORTED_NAME)));
        assertEquals(List.of("myVideo_de.vtt"), subtitleSourcesOf("myVideo.mp4"),
            "the generated subtitle should be offered again");
    }

    @Test
    public void testImportedSubtitleIsNamedByLanguage() throws IOException {
        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", WEBVTT), "en");
        assertEquals(List.of("imported.en.vtt"), subtitleSourcesOf("myVideo.mp4"));

        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", WEBVTT), "es");
        assertEquals(List.of("imported.es.vtt"), subtitleSourcesOf("myVideo.mp4"),
            "a changed language must replace the old subtitle, not add a second one");
    }

    @Test
    public void testImportedSubtitleNameOfLanguage() {
        assertEquals("imported.en.vtt", MediaService.importedSubtitleFileName("en"));
        assertEquals("imported.en.vtt", MediaService.importedSubtitleFileName(" EN-us "),
            "only the primary language is used");

        assertEquals("imported.vtt", MediaService.importedSubtitleFileName(null), "no language, no label");
        assertEquals("imported.vtt", MediaService.importedSubtitleFileName(""), "no language, no label");
        assertEquals("imported.vtt", MediaService.importedSubtitleFileName("../en"),
            "an invalid language must not end up in the file name");
    }

    @Test
    public void testImportedSubtitleWithoutLanguageReplacesGeneratedOne() throws IOException {
        writeGenerated("myVideo.mp4");
        MediaService.importSubtitleFile(internalMediaId("myVideo.mp4"), write("myVideo.vtt", WEBVTT), null);

        assertEquals(List.of("imported.vtt"), subtitleSourcesOf("myVideo.mp4"));

        MediaService.deleteImportedSubtitleFile(internalMediaId("myVideo.mp4"));
        assertEquals(List.of("myVideo_de.vtt"), subtitleSourcesOf("myVideo.mp4"),
            "an unlabelled import must be deleted too");
    }

    private static String internalMediaId(String fileName) {
        return MediaService.buildInternalId(DERIVATE_ID + "_" + fileName);
    }

    private static Path storeOf(String mediaFileName) {
        return MediaService.SUBT_STORAGE_PATH.resolve(internalMediaId(mediaFileName));
    }

    private static List<String> subtitleSourcesOf(String mediaFileName) {
        Sources sources = MediaService.buildSubtitleSources(internalMediaId(mediaFileName));
        return sources.getSources().stream().map(Sources.Source::getSrc).toList();
    }

    private Path write(String fileName, String content) throws IOException {
        Path file = derivate.resolve(fileName);
        Files.writeString(file, content, StandardCharsets.UTF_8);

        return file;
    }

    /**
     * Writes a subtitle into the store, like the converter would do with its result package.
     */
    private static void writeGenerated(String mediaFileName) throws IOException {
        Path store = storeOf(mediaFileName);
        Files.createDirectories(store);
        Files.writeString(store.resolve("myVideo_de.vtt"), GENERATED_VTT, StandardCharsets.UTF_8);
    }

}
