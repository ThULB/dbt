/*
 * $Id$
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
import org.urmel.dbt.rc.datamodel.RCCalendar;
import org.xml.sax.SAXParseException;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public abstract class RCCalendarTransformer {

    /**
     * The JAXB context.
     */
    public static final JAXBContext JAXB_CONTEXT = initContext();

    private static final String ROOT_ELEMENT_NAME = "calendar";

    private RCCalendarTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(RCCalendar.class.getPackage().getName(), RCCalendar.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final RCCalendar calendar) {
        final MCRJAXBContent<RCCalendar> content = new MCRJAXBContent<RCCalendar>(JAXB_CONTEXT, calendar);
        try {
            final Document calendarXML = content.asXML();
            return calendarXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming RCCalendar to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final RCCalendar calendar) {
        return getDocument(calendar);
    }

    public static RCCalendar buildRCCalendar(final Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a RCCalendar element.");
        }
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (RCCalendar) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (final JAXBException e) {
            throw new MCRException("Exception while transforming Element to RCCalendar.", e);
        }
    }
}
