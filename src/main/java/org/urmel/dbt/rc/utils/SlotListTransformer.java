/*
 * $Id$
 */
package org.urmel.dbt.rc.utils;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.xml.sax.SAXParseException;

public abstract class SlotListTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private SlotListTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(SlotList.class.getPackage().getName(), SlotList.class.getClassLoader());
        } catch (JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(SlotList slotList) {
        MCRJAXBContent<SlotList> content = new MCRJAXBContent<SlotList>(JAXB_CONTEXT, slotList);
        try {
            Document SlotListXML = content.asXML();
            return SlotListXML;
        } catch (SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming SlotList to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(SlotList slotList) {
        return getDocument(slotList);
    }
}
