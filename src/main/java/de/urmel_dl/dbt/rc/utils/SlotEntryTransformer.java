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
package de.urmel_dl.dbt.rc.utils;

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

import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;

public abstract class SlotEntryTransformer {

    private static final String ROOT_ELEMENT_NAME = "entry";

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private SlotEntryTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(SlotEntry.class.getPackage().getName(), SlotEntry.class.getClassLoader());
        } catch (JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(SlotEntry<?> slotEntry) {
        MCRJAXBContent<SlotEntry<?>> content = new MCRJAXBContent<SlotEntry<?>>(JAXB_CONTEXT, slotEntry);
        try {
            Document slotEntryXML = content.asXML();
            return slotEntryXML;
        } catch (SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming SlotEntry to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(SlotEntry<?> slotEntry) {
        return getDocument(slotEntry);
    }

    public static SlotEntry<?> buildSlotEntry(Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a rc entry element.");
        }
        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (SlotEntry<?>) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (JAXBException e) {
            throw new MCRException("Exception while transforming Element to SlotEntry.", e);
        }
    }
}
