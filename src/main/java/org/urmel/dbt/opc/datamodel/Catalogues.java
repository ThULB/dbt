/*
 * $Id: Catalogues.java 2165 2014-12-12 10:05:09Z adler $ 
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationDir;
import org.urmel.dbt.opc.utils.CataloguesTransformer;

@XmlRootElement(name = "catalogues")
public class Catalogues {

    private static final Logger LOGGER = LogManager.getLogger(Catalogues.class);

    private static Catalogues SINGLETON;

    private List<Catalog> catalogues = new ArrayList<Catalog>();

    public static Catalogues instance() {
        if (SINGLETON == null) {
            final URL cataloguesConfig = getCataloguesConfig();
            if (cataloguesConfig == null) {
                throw new MCRException("Could not find " + getCataloguesConfigResourceName());
            }

            Document doc = new Document();
            final SAXBuilder builder = new SAXBuilder();
            try {
                doc = builder.build(cataloguesConfig);
            } catch (final Exception e) {
                throw new MCRException("Could not load " + getCataloguesConfigResourceName());
            }

            SINGLETON = CataloguesTransformer.buildCatalogues(doc.getRootElement());
        }
        return SINGLETON;
    }

    private static URL getCataloguesConfig() {
        final File configFile = MCRConfigurationDir.getConfigFile(getCataloguesConfigResourceName());
        if (configFile != null && configFile.canRead()) {
            try {
                return configFile.toURI().toURL();
            } catch (final MalformedURLException e) {
                LOGGER.warn("Error while looking for: " + configFile, e);
            }
        }
        return MCRConfigurationDir.getConfigResource(getCataloguesConfigResourceName());
    }

    static String getCataloguesConfigResourceName() {
        return MCRConfiguration.instance().getString("DBT.OPC.CataloguesConfig", "catalogues.xml");
    }

    /**
     * @return the catalogues
     */
    @XmlElement(name = "catalog")
    public List<Catalog> getCatalogues() {
        return catalogues;
    }

    /**
     * @param catalogues the catalogues to set
     */
    public void setCatalogues(final List<Catalog> catalogues) {
        this.catalogues = catalogues;
    }

    /**
     * @param catalog the catalog to set
     */
    public void addCatalog(final Catalog catalog) {
        this.catalogues.add(catalog);
    }

    /**
     * Returns the {@link Catalog#Catalog()} for given Id.
     * 
     * @param id the catalog identifier
     * @return the {@link Catalog#Catalog()}
     */
    public Catalog getCatalogById(final String id) {
        for (Catalog catalog : catalogues) {
            if (id.equals(catalog.getIdentifier())) {
                return catalog;
            }
        }

        return null;
    }

    /**
     * Returns the {@link Catalog#Catalog()} for given ISIL.
     * 
     * @param ISIL the ISIL
     * @return the {@link Catalog#Catalog()}
     */
    public Catalog getCatalogByISIL(final String ISIL) {
        for (Catalog catalog : catalogues) {
            if (catalog.getISIL().contains(ISIL)) {
                return catalog;
            }
        }

        return null;
    }
}
