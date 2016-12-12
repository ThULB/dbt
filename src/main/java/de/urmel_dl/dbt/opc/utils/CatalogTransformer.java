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
package de.urmel_dl.dbt.opc.utils;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.xml.sax.SAXParseException;

import de.urmel_dl.dbt.opc.datamodel.Catalog;

public abstract class CatalogTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private CatalogTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Catalog.class.getPackage().getName(), Catalog.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final Catalog catalog) {
        final MCRJAXBContent<Catalog> content = new MCRJAXBContent<Catalog>(JAXB_CONTEXT, catalog);
        try {
            final Document catalogXML = content.asXML();
            return catalogXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Catalog to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Catalog catalog) {
        return getDocument(catalog);
    }
}
