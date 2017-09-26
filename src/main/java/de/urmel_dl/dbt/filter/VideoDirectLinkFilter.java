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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
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

    private static final Pattern PATTERN_ALLOWED_REFERRER = Pattern.compile("(?i).*\\.(htm|html)$");

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
                    final URI uri = token.toURI(MCRFrontendUtil.getBaseURL() + "servlets/MCRFileNodeServlet/",
                            hashParameter);
                    LOGGER.info("Redirect to " + uri.toString());
                    httpServletResponse.sendRedirect(uri.toString());
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
        if (referrer == null || referrer.trim().isEmpty()) {
            return false;
        }
        try {
            final String pathInfo = new URL(referrer).getPath();
            if (PATTERN_ALLOWED_REFERRER.matcher(pathInfo).matches()) {
                Optional<String> optDerId = Arrays.stream(pathInfo.split("/"))
                    .filter(f -> PATTERN_DERIVATE_ID.matcher(f).matches())
                    .findFirst();

                if (optDerId.isPresent()) {
                    final String derivateId = optDerId.get();
                    final String fileName = URLDecoder.decode(
                        pathInfo.substring(pathInfo.indexOf(derivateId) + derivateId.length()), "UTF-8");
                    return Files.exists(MCRPath.getPath(derivateId, fileName));
                }
            }
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error("Couldn't parse referrer " + referrer + ".", e);
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
