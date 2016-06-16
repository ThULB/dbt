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
package org.urmel.dbt.filter;

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
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DerivateServletFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern PATTERN_DERIVATE_XML = Pattern.compile(".*/Derivate-[0-9]+\\.xml$");

    private static final Pattern PATTERN_DERIVATE_ID = Pattern.compile("^Derivate-([0-9]+)\\.xml");

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

        if (requestURL != null) {
            if (PATTERN_DERIVATE_XML.matcher(requestURL).matches()) {
                LOGGER.info(requestURL.substring(requestURL.lastIndexOf("/") + 1));
                final Matcher matcher = PATTERN_DERIVATE_ID
                        .matcher(requestURL.substring(requestURL.lastIndexOf("/") + 1));
                if (matcher.find()) {
                    final String derId = matcher.group(1);
                    MCRObjectID derivateId = MCRObjectID.getInstance("dbt_derivate_" + derId);
                    MCRObjectID objectId = MCRMetadataManager.getObjectId(derivateId, 10, TimeUnit.MINUTES);
                    if (objectId != null) {
                        final String redirectURL = MCRFrontendUtil.getBaseURL(request) + "receive/"
                                + objectId.toString();
                        LOGGER.info("Redirect to " + redirectURL);
                        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                        httpServletResponse.setHeader("Location", redirectURL);
                        return;
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
