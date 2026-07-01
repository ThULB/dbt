/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2018
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
package de.urmel_dl.dbt.viewer;

import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.viewer.configuration.MCRViewerConfiguration;
import org.mycore.viewer.configuration.MCRViewerDefaultConfigurationStrategy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Provides Bootstrap JavaScript and CSS in DBT colors
 */
public class DBTViewerConfigurationStrategy extends MCRViewerDefaultConfigurationStrategy {
    @Override
    public MCRViewerConfiguration get(HttpServletRequest request) {
        MCRViewerConfiguration viewerConfiguration = super.get(request);
        String baseURL = MCRFrontendUtil.getBaseURL(request);

        if (!MCRXMLFunctions.isMobileDevice(request.getHeader("User-Agent"))) {
            // Default Stylesheet
            viewerConfiguration.addCSS(baseURL + "rsc/sass/scss/layout.min.css");

            if (request.getParameter("embedded") != null) {
                viewerConfiguration.setProperty("permalink.updateHistory", false);
                viewerConfiguration.setProperty("chapter.showOnStart", false);
            } else {
                // Default JS
                viewerConfiguration
                    .addScript(baseURL + "assets/bootstrap/js/bootstrap.min.js", false);
            }
        }
        return viewerConfiguration;
    }
}
