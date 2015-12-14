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
package org.urmel.dbt.rc.utils;

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
import org.urmel.dbt.rc.datamodel.slot.SlotEntryTypes;
import org.xml.sax.SAXParseException;

public abstract class SlotEntryTypesTransformer {

    private static final String ROOT_ELEMENT_NAME = "entry-types";

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private SlotEntryTypesTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(SlotEntryTypes.class.getPackage().getName(),
                    SlotEntryTypes.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final SlotEntryTypes entryTypes) {
        final MCRJAXBContent<SlotEntryTypes> content = new MCRJAXBContent<SlotEntryTypes>(JAXB_CONTEXT, entryTypes);
        try {
            final Document entryTypesXML = content.asXML();
            return entryTypesXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming SlotEntry to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final SlotEntryTypes entryTypes) {
        return getDocument(entryTypes);
    }

    public static SlotEntryTypes buildSlotEntryTypes(final Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a rc slot element.");
        }
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (SlotEntryTypes) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (final JAXBException e) {
            throw new MCRException("Exception while transforming Element to SlotEntry.", e);
        }
    }
}
