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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
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
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.filter.MCRSecureTokenV2FilterConfig;
import org.mycore.frontend.support.MCRSecureTokenV2;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class VideoDirectLinkFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern PATTERN_DERIVATE_ID = Pattern.compile(".+_derivate_[0-9]+");

    private boolean filterEnabled = true;

    private String hashParameter;

    private String sharedSecret;

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        filterEnabled = MCRSecureTokenV2FilterConfig.isFilterEnabled();
        hashParameter = MCRSecureTokenV2FilterConfig.getHashParameterName();
        sharedSecret = MCRSecureTokenV2FilterConfig.getSharedSecret();
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (filterEnabled) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            final String pathInfo = httpServletRequest.getPathInfo();

            if (pathInfo != null && httpServletRequest.getParameter(hashParameter) == null
                    && validateReferrer(httpServletRequest)
                    && MCRSecureTokenV2FilterConfig.requireHash(pathInfo)) {
                MCRSecureTokenV2 token = new MCRSecureTokenV2(httpServletRequest.getPathInfo().substring(1),
                        MCRFrontendUtil.getRemoteAddr(httpServletRequest), sharedSecret);
                try {
                    httpServletResponse.sendRedirect(
                            token.toURI(MCRFrontendUtil.getBaseURL() + "servlets/MCRFileNodeServlet/", hashParameter)
                                    .toASCIIString());
                    return;
                } catch (URISyntaxException e) {
                    throw new ServletException(e);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private boolean validateReferrer(final HttpServletRequest httpServletRequest) {
        final String referrer = httpServletRequest.getHeader("referer");
        if (referrer != null) {
            try {
                final String pathInfo = new URL(referrer).getPath();
                if (pathInfo.endsWith(".html")) {
                    Optional<String> optDerId = Arrays.stream(pathInfo.split("/"))
                            .filter(f -> PATTERN_DERIVATE_ID.matcher(f).matches())
                            .findFirst();

                    if (optDerId.isPresent()) {
                        final String derivateId = optDerId.get();
                        final String fileName = pathInfo.substring(pathInfo.indexOf(derivateId) + derivateId.length());
                        return Files.exists(MCRPath.getPath(derivateId, fileName));
                    }
                }
            } catch (MalformedURLException e) {
                LOGGER.error("Couldn't parse referrer " + referrer + ".", e);
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
