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
package de.urmel_dl.dbt.opc.datamodel;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The Class OPACURL.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "opacURL")
public class OPACURL {

    private URL url;

    private String db;

    protected OPACURL() {
    }

    /**
     * Instantiates a new opacurl.
     *
     * @param url the url
     * @param db the db
     */
    public OPACURL(final URL url, final String db) {
        this.url = url;
        this.db = db;
    }

    /**
     * Instantiates a new opacurl.
     *
     * @param url the url
     * @param db the db
     * @throws MalformedURLException the malformed URL exception
     */
    public OPACURL(final String url, final String db) throws MalformedURLException {
        this(new URL(url), db);
    }

    /**
     * Returns the URL.
     *
     * @return the url
     */
    @XmlValue
    public URL getURL() {
        return url;
    }

    /**
     * Set the URL.
     *
     * @param url the url to set
     */
    public void setURL(final URL url) {
        this.url = url;
    }

    /**
     * Returns the DB.
     *
     * @return the db
     */
    @XmlAttribute(name = "db", required = true)
    public String getDB() {
        return db;
    }

    /**
     * Set the DB.
     *
     * @param db the db to set
     */
    public void setDB(final String db) {
        this.db = db;
    }
}
