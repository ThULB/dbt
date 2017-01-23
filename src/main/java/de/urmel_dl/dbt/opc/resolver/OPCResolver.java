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
package de.urmel_dl.dbt.opc.resolver;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class OPCResolver implements URIResolver {

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        try {
            final String options = href.substring(href.indexOf(":") + 1);
            final HashMap<String, String> params = new HashMap<>();
            String[] param;
            final StringTokenizer tok = new StringTokenizer(options, "&");
            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                if (param.length == 1) {
                    params.put(param[0], "");
                } else {
                    params.put(param[0], param[1]);
                }
            }

            OPCConnector opc = new OPCConnector();

            if (params.get("catalogId") != null) {
                opc = Catalogues.instance().getCatalogById(params.get("catalogId")).getOPCConnector();
            } else if (params.get("isil") != null) {
                opc = Catalogues.instance().getCatalogByISIL(params.get("isil")).getOPCConnector();
            } else if (params.get("url") != null) {
                opc = new OPCConnector(params.get("url"), params.get("db"));
            }

            if (params.containsKey("iktList")) {
                return new JDOMSource(new EntityFactory<>(opc.getIKTList()).toDocument());
            } else if (params.containsKey("search")) {
                if (params.containsKey("ikt")) {
                    return new JDOMSource(new EntityFactory<>(opc.search(params.get("search"),
                        params.get("ikt"))).toDocument());
                } else {
                    return new JDOMSource(new EntityFactory<>(opc.search(params.get("search"))).toDocument());
                }
            } else if (params.containsKey("family")) {
                return new JDOMSource(new EntityFactory<>(opc.family(params.get("family"))).toDocument());
            } else if (params.containsKey("record")) {
                final Record record = opc.getRecord(params.get("record"));
                return new JDOMSource(new EntityFactory<>(!params.containsKey("copys")
                    || Boolean.parseBoolean(params.get("copys")) ? record : record.getBasicCopy()).toDocument());
            } else if (params.containsKey("barcode")) {
                final Record record = opc.getRecord(opc.getPPNFromBarcode(params.get("barcode")));
                return new JDOMSource(new EntityFactory<>(!params.containsKey("copys")
                    || Boolean.parseBoolean(params.get("copys")) ? record : record.getBasicCopy()).toDocument());
            } else {
                throw new TransformerException("Couldn't resolve " + href);
            }
        } catch (final Exception ex) {
            throw new TransformerException("Exception resolving " + href, ex);
        }
    }
}
