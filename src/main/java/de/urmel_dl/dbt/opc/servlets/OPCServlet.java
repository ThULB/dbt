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
package de.urmel_dl.dbt.opc.servlets;

import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class OPCServlet extends MCRServlet {

    private static final long serialVersionUID = -7473957009414491052L;

    private static final Catalogues CATALOGUES = Catalogues.instance();

    @Override
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
                    final Result result = opc.search(URLDecoder.decode(request, "UTF-8"));
                    result.setCatalog(catalog);

                    getLayoutService().doLayout(req, res,
                        new MCRJDOMContent(new EntityFactory<>(result).toDocument()));
                    return;
                } else if ("record".equals(action)) {
                    final Record record = opc.getRecord(request);

                    getLayoutService().doLayout(req, res,
                        new MCRJDOMContent(new EntityFactory<>(record).toDocument()));
                    return;
                }
            }
        }

        res.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
