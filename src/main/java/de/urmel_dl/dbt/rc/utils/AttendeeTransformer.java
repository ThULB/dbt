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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.xml.sax.SAXParseException;

import de.urmel_dl.dbt.rc.datamodel.Attendee;
import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class AttendeeTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Attendee.class.getPackage().getName(), Attendee.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    public static Document buildExportableXML(final Attendee attendee) {
        final MCRJAXBContent<Attendee> content = new MCRJAXBContent<Attendee>(JAXB_CONTEXT, attendee);
        try {
            final Document xml = content.asXML();
            return xml;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Attendee to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Slot slot, final List<Attendee> list) {
        return buildExportableXML(slot.getSlotId(), list);
    }

    public static Document buildExportableXML(final String slotId, final List<Attendee> list) {
        Attendees attendees = new Attendees();

        final MCRJAXBContent<Attendees> content = new MCRJAXBContent<Attendees>(JAXB_CONTEXT, attendees);
        try {
            attendees.slotId = slotId;
            attendees.attendees = list;
            final Document xml = content.asXML();
            return xml;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Attendees to JDOM document.", e);
        }
    }
}
