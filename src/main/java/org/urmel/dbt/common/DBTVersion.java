/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the DBT repository software.
 * Copyright (C) 2011 MyCoRe developer team
 * See http://www-db-thueringen.de/ and http://www.mycore.de/
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
 **/

package org.urmel.dbt.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.mycore.common.config.MCRConfigurationDir;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DBTVersion {

    private static Properties prop = loadVersionProperties();

    public static final String VERSION = prop.getProperty("dbt.version");

    public static final String BRANCH = prop.getProperty("git.branch");

    public static final String HASH = prop.getProperty("git.hash");

    public static final String HASH_SHORT = prop.getProperty("git.hash-short");

    public static final String COMPLETE = VERSION + " " + BRANCH + ":" + HASH;

    public static String getVersion() {
        return VERSION;
    }

    private static Properties loadVersionProperties() {
        Properties props = new Properties();
        URL propURL = DBTVersion.class.getResource("/org/urmel/dbt/version.properties");
        try {
            InputStream propStream = propURL.openStream();
            try {
                props.load(propStream);
            } finally {
                propStream.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error while initializing DBTVersion.", e);
        }
        return props;
    }

    public static String getHash() {
        return HASH;
    }
    
    public static String getShortHash() {
        return HASH_SHORT;
    }

    public static String getCompleteVersion() {
        return COMPLETE;
    }

    public static void main(String arg[]) {
        System.out.printf(Locale.ROOT, "DBT\tver: %s\tbranch: %s\tcommit: %s%n", VERSION, BRANCH, HASH);
        System.out.printf(Locale.ROOT, "Config directory: %s\n", MCRConfigurationDir.getConfigurationDirectory());
    }
}
