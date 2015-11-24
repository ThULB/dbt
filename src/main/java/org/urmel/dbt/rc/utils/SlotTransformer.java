/*
 * $Id$
 */
package org.urmel.dbt.rc.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.mycore.common.content.MCRVFSContent;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.xml.sax.SAXParseException;

public abstract class SlotTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private static final String ROOT_ELEMENT_NAME = "slot";

    private SlotTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Slot.class.getPackage().getName(), Slot.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final Slot slot) {
        final MCRJAXBContent<Slot> content = new MCRJAXBContent<Slot>(JAXB_CONTEXT, slot);
        try {
            final Document slotXML = content.asXML();
            return slotXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Slot to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Slot slot) {
        return getDocument(slot);
    }

    public static void sendTo(final Slot slot, final File target) {
        final MCRJAXBContent<Slot> content = new MCRJAXBContent<Slot>(JAXB_CONTEXT, slot);
        try {
            content.sendTo(target);
        } catch (final IOException e) {
            throw new MCRException("Exception while transforming Slot to File.", e);
        }
    }

    public static Slot buildSlot(final Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a rc slot element.");
        }
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (Slot) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (final JAXBException e) {
            throw new MCRException("Exception while transforming Element to Slot.", e);
        }
    }

    public static Slot buildSlot(final URI uri) {
        try {
            Document jdom = MCRXMLParserFactory.getParser(false).parseXML(new MCRVFSContent(uri));
            return buildSlot(jdom.getRootElement());
        } catch (MCRException | SAXParseException | IOException e) {
            throw new MCRException("Exception while transforming URI to Slot.", e);
        }
    }
}
