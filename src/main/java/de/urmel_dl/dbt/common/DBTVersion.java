/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2016
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

package de.urmel_dl.dbt.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.mycore.mir.common.MIRCoreVersion;

/**
 * @author Ren\u00E9 Adler (eagle)
 * @author Thomas Scheffler (yagee)
 *
 */
public class DBTVersion {
    private static Properties prop = loadVersionProperties();

    public static final String VERSION = prop.getProperty("git.build.version");

    public static final String BRANCH = prop.getProperty("git.branch");

    public static final String REVISION = prop.getProperty("git.commit.id.full");

    public static final String ABBREV = prop.getProperty("git.commit.id.abbrev");

    public static final String DESCRIBE = prop.getProperty("git.commit.id.describe");

    public static final String COMPLETE = VERSION + " " + BRANCH + ":" + DESCRIBE;

    public static String getVersion() {
        return VERSION;
    }

    private static Properties loadVersionProperties() {
        Properties props = new Properties();
        URL gitPropURL = MIRCoreVersion.class.getResource("/de/urmel_dl/dbt/git.properties");
        try (InputStream gitPropStream = getInputStream(gitPropURL);) {
            props.load(gitPropStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while initializing DBTVersion.", e);
        }
        return props;
    }

    private static InputStream getInputStream(URL gitPropURL) throws IOException {
        if (gitPropURL == null) {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            };
        }
        return gitPropURL.openStream();
    }

    public static String getBranch() {
        return BRANCH;
    }

    public static String getRevision() {
        return REVISION;
    }

    public static String getAbbRev() {
        return ABBREV;
    }

    public static String getGitDescribe() {
        return DESCRIBE;
    }

    public static String getCompleteVersion() {
        return COMPLETE;
    }

    public static Map<String,String> getVersionProperties(){
        return prop.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    public static void main(String arg[]) throws IOException {
        System.out.printf(Locale.ROOT, "DBT\tver: %s\tbranch: %s\tcommit: %s%n", VERSION, BRANCH, DESCRIBE);
        prop.store(System.out, "Values of '/de/urmel_dl/dbt/git.properties' resource");
    }
}
