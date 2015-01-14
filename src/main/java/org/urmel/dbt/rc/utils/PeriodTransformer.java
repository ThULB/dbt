/*
 * $Id: PeriodTransformer.java 2116 2014-10-01 12:14:43Z adler $
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
import org.urmel.dbt.rc.datamodel.Period;
import org.xml.sax.SAXParseException;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public abstract class PeriodTransformer {

    /**
     * The JAXB context.
     */
    public static final JAXBContext JAXB_CONTEXT = initContext();

    private static final String ROOT_ELEMENT_NAME = "period";

    private PeriodTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Period.class.getPackage().getName(), Period.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final Period period) {
        final MCRJAXBContent<Period> content = new MCRJAXBContent<Period>(JAXB_CONTEXT, period);
        try {
            final Document periodXML = content.asXML();
            return periodXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Period to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Period period) {
        return getDocument(period);
    }

    public static Period buildPeriod(final Element element) {
        if (!element.getName().equals(ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element is not a Period element.");
        }
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (Period) unmarshaller.unmarshal(new JDOMSource(element));
        } catch (final JAXBException e) {
            throw new MCRException("Exception while transforming Element to Period.", e);
        }
    }
}
