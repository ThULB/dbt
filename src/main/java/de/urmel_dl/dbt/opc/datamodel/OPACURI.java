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
import java.net.URI;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * The Class OPACURI.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "opacURI")
public class OPACURI {

    private URI uri;

    private String db;

    protected OPACURI() {
    }

    /**
     * Instantiates a new opacuri.
     *
     * @param uri the uri
     * @param db the db
     */
    public OPACURI(final URI uri, final String db) {
        this.uri = uri;
        this.db = db;
    }

    /**
     * Instantiates a new opacuri.
     *
     * @param uri the uri
     * @param db the db
     * @throws MalformedURLException the malformed URL exception
     */
    public OPACURI(final String uri, final String db)  {
        this(URI.create(uri), db);
    }

    /**
     * Returns the URI.
     *
     * @return the uri
     */
    @XmlValue
    public URI getURI() {
        return uri;
    }

    /**
     * Set the URI.
     *
     * @param uri the uri to set
     */
    public void setURI(final URI uri) {
        this.uri = uri;
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
