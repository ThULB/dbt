/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2018
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.servlets;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.jersey.MCRJWTUtil;
import org.mycore.frontend.jersey.resources.MCRJWTResource;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DBTLoginEndpointServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see org.mycore.frontend.servlets.MCRServlet#doGetPost(org.mycore.frontend.servlets.MCRServletJob)
     */
    @Override
    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String token = getToken(req);

        if (token != null) {
            String name = req.getParameter("name");
            if (name == null) {
                res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                return;
            }

            String url = MCRConfiguration.instance().getString("DBT.LoginEndpoint." + name);
            res.sendRedirect(res.encodeRedirectURL(url.replace("{TOKEN}", token)));
            return;
        }

        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String getToken(HttpServletRequest req) throws UnsupportedEncodingException {
        final MCRSession mcrSession = Objects.requireNonNull(getSession(req));
        MCRUserInformation userInformation = mcrSession.getUserInformation();
        String issuer = req.getRequestURL().toString();
        return MCRJWTUtil.getJWTBuilder(userInformation)
            .withJWTId(mcrSession.getID())
            .withIssuer(issuer)
            .withAudience(MCRJWTResource.AUDIENCE)
            .withClaim(MCRJWTUtil.JWT_CLAIM_IP, mcrSession.getCurrentIP())
            .sign(MCRJWTUtil.getJWTAlgorithm());
    }
}
