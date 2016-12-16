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
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.urmel_dl.dbt.opc.OPCConnector;

/**
 * The Class Catalog.
 *
 * @author René Adler (eagle)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "catalog")
public class Catalog {
    private String identifier;

    private String name;

    private String description;

    private boolean enabled;

    private OPACURL opc;

    private OPCConnector opcConnector;

    private List<String> ISIL;

    /**
     * Instantiates a new catalog.
     */
    public Catalog() {
        this.identifier = UUID.randomUUID().toString();
        this.enabled = true;
    }

    /**
     * Gets the OPC connector.
     *
     * @return the OPC connector
     */
    public OPCConnector getOPCConnector() {
        if (opcConnector == null) {
            try {
                opcConnector = new OPCConnector(opc.getURL().toString(), opc.getDB());
            } catch (final Exception e) {
                return null;
            }
        }

        return opcConnector;
    }

    /**
     * Returns the identifier.
     *
     * @return the identifier
     */
    @XmlAttribute(name = "identifier", required = true)
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the identifier.
     *
     * @param identifier the identifier to set
     */
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    @XmlElement(name = "name", required = true)
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns is enabled.
     *
     * @return the enabled
     */
    @XmlAttribute(name = "enabled")
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set is enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the OPC configuration.
     *
     * @return the opc configuration
     * @see OPACURL#OPACURL()
     */
    @XmlElement(name = "opc")
    public OPACURL getOPC() {
        return opc;
    }

    /**
     * Set the OPC configuration.
     *
     * @param opc the opc configuration to set
     * @see OPACURL#OPACURL()
     */
    public void setOPC(final OPACURL opc) {
        this.opc = opc;
    }

    /**
     * Set the OPC configuration by params.
     *
     * @param url the URL to set
     * @param db the db to set
     * @throws MalformedURLException thrown on malformed url
     */
    public void setOPC(final String url, final String db) throws MalformedURLException {
        this.opc = new OPACURL(url, db);
    }

    /**
     * Returns a list of ISILs.
     *
     * @return the iSIL
     */
    @XmlElement(name = "ISIL")
    public List<String> getISIL() {
        return ISIL;
    }

    /**
     * Set a list of ISILs.
     *
     * @param iSIL the iSIL to set
     */
    public void setISIL(final List<String> iSIL) {
        ISIL = iSIL;
    }
}
