/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.media.entity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.support.MCRSecureTokenV2;

import de.urmel_dl.dbt.media.MediaService;
import de.urmel_dl.dbt.utils.MimeType;

/**
 * The Class Sources.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "sources")
public class Sources {

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    private static final String MEDIA_TYPE_MPEG_DASH = "application/dash+xml";

    private static final String MEDIA_TYPE_HLS = "application/x-mpegURL";

    private static final String MEDIA_TYPE_RTMP = "rtmp/mp4";

    private static final Map<String, String> MEDIA_TYPE_SUFFIXES;

    private static String wowzaBaseURL = CONFIG.getString("MCR.Media.Wowza.BaseURL");

    private static String wowzaRTMPBaseURL = CONFIG.getString("MCR.Media.Wowza.RTMPBaseURL");

    private static String wowzaSMILContentPathPrefix = CONFIG.getString("MCR.Media.Wowza.SMILContentPathPrefix");

    private static String wowzaContentPathPrefix = CONFIG.getString("MCR.Media.Wowza.ContentPathPrefix");

    private static String wowzaSharedSecred = CONFIG.getString("MCR.Media.Wowza.SharedSecred");

    private static String wowzaHashParameter = CONFIG.getString("MCR.Media.Wowza.HashParameter", "wowzatokenhash");

    private String id;

    private List<Source> sources;

    static {
        MEDIA_TYPE_SUFFIXES = new HashMap<>();
        MEDIA_TYPE_SUFFIXES.put(MEDIA_TYPE_MPEG_DASH, "/manifest.mpd");
        MEDIA_TYPE_SUFFIXES.put(MEDIA_TYPE_HLS, "/playlist.m3u8");
        MEDIA_TYPE_SUFFIXES.put(MEDIA_TYPE_RTMP, "");
    }

    /**
     * Builds the media sources.
     *
     * @param id the id
     * @param files the files
     * @return the sources
     */
    public static Sources build(String id, List<Path> files) {
        Sources sources = new Sources();

        sources.id = id;
        sources.sources = Optional.ofNullable(files)
            .map(
                fs -> fs.stream().map(s -> Source.build(s, MediaService.hasSMILFile(id))).flatMap(ss -> ss.stream())
                    .collect(Collectors.toList()))
            .orElse(null);

        return sources;
    }

    /**
     * Builds the media sources with WOWZA-Token.
     *
     * @param id the id
     * @param files the files
     * @param ipAddress the ip address
     * @param queryParameters the query parameters
     * @return the sources
     */
    public static Sources build(String id, List<Path> files, String ipAddress, String... queryParameters) {
        Sources sources = new Sources();

        sources.id = id;
        sources.sources = Optional.ofNullable(files)
            .map(
                fs -> fs.stream().map(s -> Source.build(s, MediaService.hasSMILFile(id), ipAddress, queryParameters))
                    .flatMap(ss -> ss.stream())
                    .collect(Collectors.toList()))
            .orElse(null);

        return sources;
    }

    private static String toURL(String mediaType, Path file, String contentPath) {
        StringBuffer sb = new StringBuffer();

        try {
            sb.append(MEDIA_TYPE_RTMP.equals(mediaType) ? wowzaRTMPBaseURL : wowzaBaseURL);
            sb.append(new URI(null, null,
                contentPath + file.getParent().getFileName().toString() + "/" + file.getFileName().toString(), null)
                    .getRawPath());
            sb.append(MEDIA_TYPE_SUFFIXES.get(mediaType));

            return new URI(sb.toString()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toURL(String mediaType, Path file, String contentPath, String ipAddress,
        String... queryParameters) {
        MCRSecureTokenV2 token = new MCRSecureTokenV2(
            contentPath + file.getParent().getFileName().toString() + "/" + file.getFileName().toString(),
            ipAddress, wowzaSharedSecred, queryParameters);

        try {
            return token.toURI(MEDIA_TYPE_RTMP.equals(mediaType) ? wowzaRTMPBaseURL : wowzaBaseURL,
                MEDIA_TYPE_SUFFIXES.get(mediaType), wowzaHashParameter).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Sources() {
    }

    /**
     * Instantiates a new sources object.
     *
     * @param id the id
     * @param sources the sources
     */
    public Sources(String id, List<Source> sources) {
        this();
        this.id = id;
        this.sources = sources;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @XmlAttribute
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the sources.
     *
     * @return the sources
     */
    @XmlElement(name = "source")
    public List<Source> getSources() {
        return sources;
    }

    /**
     * Sets the sources.
     *
     * @param sources the sources to set
     */
    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    /**
     * The Class Source.
     *
     * @author Ren\u00E9 Adler (eagle)
     */
    @XmlRootElement(name = "source")
    public static class Source {

        private String type;

        private String src;

        /**
         * Builds the media source.
         *
         * @param file the file
         * @param hasSMIL the has SMIL
         * @return the list
         */
        public static List<Source> build(Path file, boolean hasSMIL) {
            if (file.getFileName().toString().endsWith(".smil")) {
                return MEDIA_TYPE_SUFFIXES.keySet().stream()
                    .map(mt -> new Source(mt, toURL(mt, file, wowzaSMILContentPathPrefix)))
                    .collect(Collectors.toList());
            } else {
                try {
                    return !hasSMIL ? MEDIA_TYPE_SUFFIXES.keySet().stream()
                        .map(mt -> new Source(mt, toURL(mt, file, wowzaContentPathPrefix)))
                        .collect(Collectors.toList()) : Arrays
                            .asList(
                                new Source(Optional.ofNullable(MimeType.detect(file)).orElse("video/mp4"),
                                    file.getFileName().toString()));
                } catch (IOException e) {
                    return Collections.emptyList();
                }
            }
        }

        /**
         * Builds the media source with WOZA-Token.
         *
         * @param file the file
         * @param hasSMIL the has SMIL
         * @param ipAddress the ip address
         * @param queryParameters the query parameters
         * @return the list
         */
        public static List<Source> build(Path file, boolean hasSMIL, String ipAddress, String... queryParameters) {
            if (file.getFileName().toString().endsWith(".smil")) {
                return MEDIA_TYPE_SUFFIXES.keySet().stream()
                    .map(mt -> new Source(mt,
                        toURL(mt, file, wowzaSMILContentPathPrefix, ipAddress, queryParameters)))
                    .collect(Collectors.toList());
            } else {
                try {
                    return !hasSMIL ? MEDIA_TYPE_SUFFIXES.keySet().stream()
                        .map(mt -> new Source(mt, toURL(mt, file, wowzaContentPathPrefix,
                            ipAddress, queryParameters)))
                        .collect(Collectors.toList()) : Arrays
                            .asList(
                                new Source(Optional.ofNullable(MimeType.detect(file)).orElse("video/mp4"),
                                    file.getFileName().toString()));
                } catch (IOException e) {
                    return Collections.emptyList();
                }
            }
        }

        private Source() {
        }

        /**
         * Instantiates a new source.
         *
         * @param type the type
         * @param src the src
         */
        public Source(String type, String src) {
            this();
            this.type = type;
            this.src = src;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        @XmlAttribute
        public String getType() {
            return type;
        }

        /**
         * Sets the type.
         *
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets the src.
         *
         * @return the src
         */
        @XmlAttribute
        public String getSrc() {
            return src;
        }

        /**
         * Sets the src.
         *
         * @param src the src to set
         */
        public void setSrc(String src) {
            this.src = src;
        }

    }
}
