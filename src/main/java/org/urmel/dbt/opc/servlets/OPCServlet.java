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
package org.urmel.dbt.opc.servlets;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.opc.OPCConnector;
import org.urmel.dbt.opc.datamodel.Catalog;
import org.urmel.dbt.opc.datamodel.Catalogues;
import org.urmel.dbt.opc.datamodel.pica.Record;
import org.urmel.dbt.opc.datamodel.pica.Result;
import org.urmel.dbt.opc.utils.RecordTransformer;
import org.urmel.dbt.opc.utils.ResultTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class OPCServlet extends MCRServlet {

    private static final long serialVersionUID = -7473957009414491052L;

    private static final Catalogues CATALOGUES = Catalogues.instance();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();
        final HttpServletResponse res = job.getResponse();

        final String path = req.getPathInfo();

        if (path != null) {
            final StringTokenizer st = new StringTokenizer(path, "/");

            Catalog catalog = null;
            String action = null;
            String request = null;

            while (st.hasMoreTokens()) {
                final String token = st.nextToken();

                if (catalog == null) {
                    catalog = CATALOGUES.getCatalogById(token);
                    if (catalog == null) {
                        catalog = CATALOGUES.getCatalogByISIL(token);
                    }
                } else if (action == null) {
                    action = token;
                } else {
                    request = token;
                }
            }

            if (catalog != null) {
                final OPCConnector opc = catalog.getOPCConnector();
                if ("search".equals(action)) {
                    final Result result = opc.search(request);
                    result.setCatalog(catalog);

                    getLayoutService().doLayout(req, res,
                            new MCRJDOMContent(ResultTransformer.buildExportableXML(result)));
                    return;
                } else if ("record".equals(action)) {
                    final Record record = opc.getRecord(request);

                    getLayoutService().doLayout(req, res,
                            new MCRJDOMContent(RecordTransformer.buildExportableXML(record)));
                    return;
                }
            }
        }

        res.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
