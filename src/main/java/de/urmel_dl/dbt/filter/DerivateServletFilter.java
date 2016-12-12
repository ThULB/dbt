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
package de.urmel_dl.dbt.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DerivateServletFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern PATTERN_DERIVATE_XML = Pattern
        .compile(".*/Derivate-[0-9]+\\.xml$|.*/Derivate-[0-9]+$");

    private static final Pattern PATTERN_DERIVATE_ID = Pattern.compile("^Derivate-([0-9]+).*");

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        final String requestURL = httpServletRequest.getRequestURI();

        if (requestURL != null && PATTERN_DERIVATE_XML.matcher(requestURL).matches()) {
            final String lp = requestURL.substring(requestURL.lastIndexOf("/") + 1);
            final Matcher matcher = PATTERN_DERIVATE_ID.matcher(lp);
            if (matcher.find()) {
                try {
                    final String derId = matcher.group(1);
                    MCRObjectID derivateId = MCRObjectID.getInstance("dbt_derivate_" + derId);
                    if (MCRMetadataManager.exists(derivateId)) {
                        String redirectURL = null;
                        if (lp.endsWith(".xml")) {
                            MCRObjectID objectId = MCRMetadataManager.getObjectId(derivateId, 10, TimeUnit.MINUTES);
                            if (objectId != null) {
                                redirectURL = MCRFrontendUtil.getBaseURL(request) + "receive/"
                                    + objectId.toString();

                            }
                        } else {
                            redirectURL = MCRFrontendUtil.getBaseURL(request) + "servlets/MCRFileNodeServlet/"
                                + derivateId.toString() + "/"
                                + MCRMetadataManager.retrieveMCRDerivate(derivateId).getDerivate()
                                    .getInternals()
                                    .getMainDoc();
                        }

                        if (redirectURL != null && !redirectURL.isEmpty()) {
                            LOGGER.info("Redirect to " + redirectURL);
                            httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                            httpServletResponse.setHeader("Location", redirectURL);
                            return;
                        }
                    }
                } finally {
                    if (MCRSessionMgr.hasCurrentSession()) {
                        if (httpServletRequest.getSession(false) == null) {
                            MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
                            MCRSessionMgr.releaseCurrentSession();
                            mcrSession.close();
                        } else {
                            MCRSessionMgr.releaseCurrentSession();
                        }
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
