/*
 * $Id$ 
 * $Revision$
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
package org.urmel.dbt.opc.utils;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRJAXBContent;
import org.urmel.dbt.opc.datamodel.pica.Result;
import org.xml.sax.SAXParseException;

public abstract class ResultTransformer {

    public static final JAXBContext JAXB_CONTEXT = initContext();

    private ResultTransformer() {
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance(Result.class.getPackage().getName(), Result.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new MCRException("Could not instantiate JAXBContext.", e);
        }
    }

    private static Document getDocument(final Result result) {
        final MCRJAXBContent<Result> content = new MCRJAXBContent<Result>(JAXB_CONTEXT, result);
        try {
            final Document resultXML = content.asXML();
            return resultXML;
        } catch (final SAXParseException | JDOMException | IOException e) {
            throw new MCRException("Exception while transforming Result to JDOM document.", e);
        }
    }

    public static Document buildExportableXML(final Result result) {
        return getDocument(result);
    }
}
