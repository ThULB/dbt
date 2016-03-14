/*
 * $Id$ 
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.urmel.dbt.opc.datamodel;

import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.urmel.dbt.opc.OPCConnector;

/**
 * 
 * @author René Adler (eagle)
 */
@XmlRootElement(name = "catalog")
public class Catalog {
    private String identifier;

    private String name;

    private String description;

    private boolean enabled;

    private OPACURL opc;

    private OPCConnector opcConnector;

    private List<String> ISIL;

    public Catalog() {
        this.identifier = UUID.randomUUID().toString();
        this.enabled = true;
    }

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
     * @return the identifier
     */
    @XmlAttribute(name = "identifier", required = true)
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the name
     */
    @XmlElement(name = "name", required = true)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    @XmlElement(name = "description")
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return the enabled
     */
    @XmlAttribute(name = "enabled")
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the opc configuration
     * @see OPACURL#OPACURL()
     */
    @XmlElement(name = "opc")
    public OPACURL getOPC() {
        return opc;
    }

    /**
     * @param opc the opc configuration to set
     * @see OPACURL#OPACURL()
     */
    public void setOPC(final OPACURL opc) {
        this.opc = opc;
    }

    /**
     * @param url the URL to set
     * @param db the db to set
     * @throws MalformedURLException thrown on malformed url
     */
    public void setOPC(final String url, final String db) throws MalformedURLException {
        this.opc = new OPACURL(url, db);
    }

    /**
     * @return the iSIL
     */
    @XmlElement(name="ISIL")
    public List<String> getISIL() {
        return ISIL;
    }

    /**
     * @param iSIL the iSIL to set
     */
    public void setISIL(final List<String> iSIL) {
        ISIL = iSIL;
    }
}
