/*
 * $Id: SlotEntryTransformer.java 2126 2014-11-25 09:20:21Z adler $
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
import org.urmel.dbt.rc.datamodel.SlotEntry;
import org.xml.sax.SAXParseException;

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

    private static Document getDocument(SlotEntry<?> slot) {
        MCRJAXBContent<SlotEntry<?>> content = new MCRJAXBContent<SlotEntry<?>>(JAXB_CONTEXT, slot);
        try {
            Document SlotXML = content.asXML();
            return SlotXML;
        } catch (SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming SlotEntry to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(SlotEntry<?> slot) {
        return getDocument(slot);
    }

    public static SlotEntry<?> buildSlotEntry(Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a rc slot element.");
        }
        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (SlotEntry<?>) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (JAXBException e) {
            throw new MCRException("Exception while transforming Element to SlotEntry.", e);
        }
    }
}
