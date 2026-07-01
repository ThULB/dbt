/*
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
package de.urmel_dl.dbt.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRSourceContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRXMLParserFactory;

/**
 * @author René Adler (eagle)
 *
 */
public class DebugResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String target = href.substring(href.indexOf(":") + 1);
        String subUri = target.substring(target.indexOf(":") + 1);

        LOGGER.info("target: {}", target);

        if (subUri.isEmpty()) {
            return new JDOMSource(new Element("null"));
        }

        try {
            Source result = MCRURIResolver.obtainInstance().resolve(target, base);
            if (result != null) {
                MCRContent content = new MCRSourceContent(result).getBaseContent();
                Document document = MCRXMLParserFactory.getParser(false, true).parseXML(content);

                LOGGER.info(new XMLOutputter(Format.getPrettyFormat()).outputString(document));

                return new JDOMSource(document.getRootElement().detach());
            } else {
                return new JDOMSource(new Element("null"));
            }
        } catch (Exception ex) {
            LOGGER.info("DebugResolver caught exception: {}", ex.getLocalizedMessage());
            LOGGER.debug(ex.getStackTrace());
            return new JDOMSource(new Element("null"));
        }
    }

}
