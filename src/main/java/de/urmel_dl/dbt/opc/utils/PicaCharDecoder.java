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
package de.urmel_dl.dbt.opc.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

/**
 * The Class PicaCharDecoder.
 *
 * @author René Adler (eagle)
 */
public class PicaCharDecoder {
    private static final int ISO_8859_1 = 0x1000;

    private static final int UTF_8 = 0x1001;

    @SuppressWarnings("serial")
    private static final Map<String, Map<Integer, String>> PICA_CHARS = new HashMap<>() {
        {
            put("&auml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D1");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0091");
                }
            });
            put("&euml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E8e");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A8e");
                }
            });
            put("&iuml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E8i");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A8i");
                }
            });
            put("&ouml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D2");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0092");
                }
            });
            put("&uuml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D3");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0093");
                }
            });
            put("&aacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2a");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2a");
                }
            });
            put("&eacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2e");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2e");
                }
            });
            put("&iacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2i");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2i");
                }
            });
            put("&oacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2o");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2o");
                }
            });
            put("&uacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2u");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2u");
                }
            });
            put("&yacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2y");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2y");
                }
            });
            put("&agrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1a");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1a");
                }
            });
            put("&egrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1e");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1e");
                }
            });
            put("&igrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1i");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1i");
                }
            });
            put("&ograve;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1o");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1o");
                }
            });
            put("&ugrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1u");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1u");
                }
            });
            put("&acirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3a");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3a");
                }
            });
            put("&ecirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3e");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3e");
                }
            });
            put("&icirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3i");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3i");
                }
            });
            put("&ocirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3o");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3o");
                }
            });
            put("&ucirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3u");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3u");
                }
            });
            put("&Auml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C1");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0081");
                }
            });
            put("&Euml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E8E");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A8E");
                }
            });
            put("&Iuml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E8I");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A8I");
                }
            });
            put("&Ouml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C2");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0082");
                }
            });
            put("&Uuml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C3");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0083");
                }
            });
            put("&Aacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2A");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2A");
                }
            });
            put("&Eacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2E");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2E");
                }
            });
            put("&Iacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2I");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2I");
                }
            });
            put("&Oacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2O");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2O");
                }
            });
            put("&Uacute;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E2U");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A2U");
                }
            });
            put("&Agrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1A");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1A");
                }
            });
            put("&Egrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1E");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1E");
                }
            });
            put("&Igrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1I");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1I");
                }
            });
            put("&Ograve;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1O");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1O");
                }
            });
            put("&Ugrave;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E1U");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A1U");
                }
            });
            put("&Acirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3A");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3A");
                }
            });
            put("&Ecirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3E");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3E");
                }
            });
            put("&Icirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3I");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3I");
                }
            });
            put("&Ocirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3O");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3O");
                }
            });
            put("&Ucirc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3U");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3U");
                }
            });
            put("&onder;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F6");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B6");
                }
            });
            put("&cedil;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F0");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B0");
                }
            });
            put("&uml;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E8");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A8");
                }
            });
            put("&tilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "~");
                    put(PicaCharDecoder.UTF_8, "~");
                }
            });
            put("&circ;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E3");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A3");
                }
            });
            put("&oonder;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F6o");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B6o");
                }
            });
            put("&aonder;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F6a");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B6a");
                }
            });
            put("&ntilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4n");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4n");
                }
            });
            put("&Ntilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4N");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4N");
                }
            });
            put("&otilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4o");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4o");
                }
            });
            put("&Otilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4O");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4O");
                }
            });
            put("&atilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4a");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4a");
                }
            });
            put("&Atilde;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E4A");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A4A");
                }
            });
            put("&ccedil;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F0c");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B0c");
                }
            });
            put("&Ccedil;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F0C");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B0C");
                }
            });
            put("&comre;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00FE");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00BE");
                }
            });
            put("&htili;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00FB");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00BB");
                }
            });
            put("&htire;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00FA");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00BA");
                }
            });
            put("&upadh;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F9");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B9");
                }
            });
            put("&onced;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F8");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B8");
                }
            });
            put("&hbore;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F7");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B7");
                }
            });
            put("&dboac;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F5");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B5");
                }
            });
            put("&cirke;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F4");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B4");
                }
            });
            put("&2ptac;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F3");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B3");
                }
            });
            put("&ptacc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F2");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B2");
                }
            });
            put("&hlink;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00F1");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00B1");
                }
            });
            put("&candr;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EF");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AF");
                }
            });
            put("&dblac;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EE");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AE");
                }
            });
            put("&comac;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00ED");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AD");
                }
            });
            put("&ligre;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EC");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AC");
                }
            });
            put("&ligli;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EB");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AB");
                }
            });

            // Czech Chars
            put("&#x10D;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9c");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9c");
                }
            });
            put("&#x10C;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9C");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9C");
                }
            });
            put("&#x11B;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9e");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9e");
                }
            });
            put("&#x11A;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9E");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9E");
                }
            });
            put("&#x159;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9r");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9r");
                }
            });
            put("&#x158;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9R");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9R");
                }
            });
            put("&scaron;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9s");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9s");
                }
            });
            put("&Scaron;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9S");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9S");
                }
            });
            put("&#x17E;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9z");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9z");
                }
            });
            put("&#x17D;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9Z");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9Z");
                }
            });
            put("&#x17C;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E7z");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A7z");
                }
            });
            put("&#x17B;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E7Z");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A7Z");
                }
            });

            //hacek
            put("&#x2c7;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E9");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A9");
                }
            });

            put("&#x16e;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EAU");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AAU");
                }
            });
            put("&#x16f;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EAu");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AAu");
                }
            });
            put("&ring;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00EA");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00AA");
                }
            });

            put("&ptbov;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E7");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A7");
                }
            });
            put("&bovko;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E6");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A6");
                }
            });
            put("&bovla;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E5");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A5");
                }
            });
            put("&rijze;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00E0");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u00A0");
                }
            });
            put("&pi;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00DB");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u009B");
                }
            });
            put("&gamma;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00DA");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u009A");
                }
            });
            put("&beta;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D8");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0098");
                }
            });
            put("&iexcl;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D7");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0097");
                }
            });
            put("&iquest;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D6");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0096");
                }
            });
            put("&omge;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D5");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0095");
                }
            });
            put("&omgc;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D4");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0094");
                }
            });
            put("&ijlig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D0");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0090");
                }
            });
            put("&ge;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CF");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008F");
                }
            });
            put("&iff;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CE");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008E");
                }
            });
            put("&worte;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CD");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008D");
                }
            });
            put("&sect;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CC");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008C");
                }
            });
            put("&times;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CB");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008B");
                }
            });
            put("&int;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00CA");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u008A");
                }
            });
            put("&infin;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C9");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0089");
                }
            });
            put("&le;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C8");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0088");
                }
            });
            put("&flech;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C7");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0087");
                }
            });
            put("&ne;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C6");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0086");
                }
            });
            put("&omgE;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C5");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0085");
                }
            });
            put("&omgC;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C4");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0084");
                }
            });
            put("&IJlig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00C0");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0080");
                }
            });
            put("&aring;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BF");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BF");
                }
            });
            put("&szlig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BE");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BE");
                }
            });
            put("&uhaak;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BD");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BD");
                }
            });
            put("&ohaak;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BC");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BC");
                }
            });
            put("&alpha;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BB");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BB");
                }
            });
            put("&eth;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00BA");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00BA");
                }
            });
            put("&pound;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B9");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B9");
                }
            });
            put("&inodot;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B8");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B8");
                }
            });
            put("&oelig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B6");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B6");
                }
            });
            put("&aelig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B5");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B5");
                }
            });
            put("&thorn;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B4");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B4");
                }
            });
            put("&dzcy;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B3");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B3");
                }
            });
            put("&oslash;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B2");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B2");
                }
            });
            put("&lstrok;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B1");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B1");
                }
            });
            put("&ayn;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00B0");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00B0");
                }
            });
            put("&Aring;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AF");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AF");
                }
            });
            put("&aleph;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AE");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AE");
                }
            });
            put("&Uhaak;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AD");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AD");
                }
            });
            put("&Ohaak;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AC");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AC");
                }
            });
            put("&plusmn;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AB");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AB");
                }
            });
            put("&reg;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00AA");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00AA");
                }
            });
            put("&flat;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A9");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A9");
                }
            });
            put("&hahog;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A8");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A8");
                }
            });
            put("&softcy;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A7");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A7");
                }
            });
            put("&OElig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A6");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A6");
                }
            });
            put("&AElig;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A5");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A5");
                }
            });
            put("&THORN;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A4");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A4");
                }
            });
            put("&DZcy;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A3");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A3");
                }
            });
            put("&Oslash;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A2");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A2");
                }
            });
            put("&Lstrok;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A1");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A1");
                }
            });
            put("&flor;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u009F");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u009F");
                }
            });
            put("&hardcy;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u009C");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u009C");
                }
            });
            put("&quot;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\"");
                    put(PicaCharDecoder.UTF_8, "\"");
                }
            });
            put("&gt;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, ">");
                    put(PicaCharDecoder.UTF_8, ">");
                }
            });
            put("&lt;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "<");
                    put(PicaCharDecoder.UTF_8, "<");
                }
            });
            put("&euro;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00D9");
                    put(PicaCharDecoder.UTF_8, "\u00C3\u0099");
                }
            });
            put("&nbsp;", new HashMap<>() {
                {
                    put(PicaCharDecoder.ISO_8859_1, "\u00A0");
                    put(PicaCharDecoder.UTF_8, "\u00C2\u00A0");
                }
            });
        }
    };

    /**
     * Decode Pica encode {@link String}.
     *
     * @param plain the plain
     * @return the string
     */
    public static String decode(final String plain) {
        return decode(plain, PicaCharDecoder.UTF_8);
    }

    /**
     * Decode Pica encode {@link String} with given encoding.
     *
     * @param plain the plain
     * @param encoding the encoding
     * @return the string
     */
    public static String decode(final String plain, final int encoding) {
        String ppDecoded = plain;

        for (Map.Entry<String, Map<Integer, String>> picaChar : PICA_CHARS.entrySet()) {
            final String htmlChar = picaChar.getKey();
            final Map<Integer, String> encCharsMap = picaChar.getValue();
            final String encChars = encCharsMap.get(encoding);

            // replace pica chars with htmlenities from given encoding
            ppDecoded = ppDecoded.replaceAll(encChars, htmlChar);
        }

        return StringEscapeUtils.unescapeHtml4(ppDecoded);
    }

    public static String asHexString(final String plain) {
        try {
            StringBuilder sb = new StringBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(plain.getBytes(StandardCharsets.UTF_8));

            while (is.available() > 0) {
                char[] line = new char[16];
                for (int i = 0; i < 16; i++) {
                    int readByte = is.read();
                    String paddingZero = (readByte < 16) ? "0" : "";
                    sb.append(paddingZero + (readByte == -1 ? "0" : Integer.toHexString(readByte)) + " ");
                    line[i] = ((readByte >= 33 && readByte <= 126) ? (char) readByte : '.');
                }
                sb.append(new String(line) + "\n");
            }
            is.close();

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
