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
import javax.xml.bind.Unmarshaller;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.xml.sax.SAXParseException;

import de.urmel_dl.dbt.opc.datamodel.Catalogues;

public abstract class CataloguesTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private static final String ROOT_ELEMENT_NAME = "catalogues";

    private CataloguesTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Catalogues.class.getPackage().getName(), Catalogues.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final Catalogues catalogues) {
        final MCRJAXBContent<Catalogues> content = new MCRJAXBContent<Catalogues>(JAXB_CONTEXT, catalogues);
        try {
            final Document cataloguesXML = content.asXML();
            return cataloguesXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Catalogues to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Catalogues catalogues) {
        return getDocument(catalogues);
    }

    public static Catalogues buildCatalogues(final Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a catalogues element.");
        }
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (Catalogues) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (final JAXBException e) {
            throw new MCRException("Exception while transforming Element to Catalogues.", e);
        }
    }
}
