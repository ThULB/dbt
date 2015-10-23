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
package org.urmel.dbt.rc.servlets;

import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Element;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotListTransformer;
import org.urmel.dbt.rc.utils.SlotTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class ClientServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final String RC_TOKEN = "rctoken";

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();
        final HttpServletResponse res = job.getResponse();

        final MCRSession session = MCRSessionMgr.getCurrentSession();

        final String path = req.getPathInfo();
        final String token = req.getParameter("token");

        if (path != null) {
            final StringTokenizer st = new StringTokenizer(path, "/");

            final String action = st.hasMoreTokens() ? st.nextToken() : null;

            final SlotList slotList = SLOT_MGR.getSlotList();

            if ("token".equals(action)) {
                final String newToken = UUID.randomUUID().toString();

                session.put(RC_TOKEN, newToken);

                final Element root = new Element("token");
                root.setText(newToken);

                getLayoutService().sendXML(req, res, new MCRJDOMContent(root));
                return;
            } else if (token != null && token.equals(session.get(RC_TOKEN))) {
                if ("list".equals(action)) {
                    getLayoutService().sendXML(req, res,
                            new MCRJDOMContent(SlotListTransformer.buildExportableXML(slotList.getBasicSlots())));
                    return;
                } else if (action != null) {
                    final Slot slot = SLOT_MGR.getSlotById(action);

                    getLayoutService().sendXML(req, res, new MCRJDOMContent(SlotTransformer.buildExportableXML(slot)));
                    return;
                }
            } else {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
}
