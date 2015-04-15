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
package org.urmel.dbt.opc.resolver;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.urmel.dbt.opc.OPCConnector;
import org.urmel.dbt.opc.datamodel.Catalogues;
import org.urmel.dbt.opc.datamodel.pica.Record;
import org.urmel.dbt.opc.datamodel.pica.Result;
import org.urmel.dbt.opc.utils.IKTListTransformer;
import org.urmel.dbt.opc.utils.RecordTransformer;
import org.urmel.dbt.opc.utils.ResultTransformer;

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
            final HashMap<String, String> params = new HashMap<String, String>();
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

            OPCConnector opc = null;

            if (params.get("catalogId") != null) {
                opc = Catalogues.instance().getCatalogById(params.get("catalogId")).getOPCConnector();
            } else if (params.get("isil") != null) {
                opc = Catalogues.instance().getCatalogByISIL(params.get("isil")).getOPCConnector();
            } else {
                opc = new OPCConnector(params.get("url"), params.get("db"));
            }

            final MCRSession currentSession = MCRSessionMgr.getCurrentSession();

            if (params.containsKey("iktList")) {
                return new JDOMSource(IKTListTransformer.buildExportableXML(opc.getIKTList()));
            } else if (params.containsKey("search")) {
                Result result = null;
                if (params.containsKey("ikt")) {
                    result = (Result) currentSession.get(params.get("search") + "_" + params.get("ikt"));
                    if (result == null) {
                        result = opc.search(params.get("search"), params.get("ikt"));
                    }
                } else {
                    result = (Result) currentSession.get(params.get("search"));
                    if (result == null) {
                        result = opc.search(params.get("search"));
                    }
                }

                return new JDOMSource(ResultTransformer.buildExportableXML(result));
            } else if (params.containsKey("family")) {
                Result result = (Result) currentSession.get(params.get("family"));
                if (result == null) {
                    result = opc.search(params.get("family"));
                }
                return new JDOMSource(ResultTransformer.buildExportableXML(result));
            } else if (params.containsKey("record")) {
                Record record = (Record) currentSession.get(params.get("record"));
                if (record == null) {
                    record = opc.getRecord(params.get("record"));
                    currentSession.put(params.get("record"), record);
                }
                return new JDOMSource(RecordTransformer.buildExportableXML(record));
            } else if (params.containsKey("barcode")) {
                Record record = (Record) currentSession.get(params.get("barcode"));
                if (record == null) {
                    record = opc.getRecord(opc.getPPNFromBarcode(params.get("barcode")));
                    currentSession.put(params.get("barcode"), record);
                }
                return new JDOMSource(RecordTransformer.buildExportableXML(record));
            } else {
                throw new TransformerException("Couldn't resolve " + href);
            }
        } catch (final Exception ex) {
            throw new TransformerException("Exception resolving " + href, ex);
        }
    }
}
