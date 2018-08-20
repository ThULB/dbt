package de.urmel_dl.dbt.viewer;

import javax.servlet.http.HttpServletRequest;

import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.viewer.configuration.MCRViewerConfiguration;
import org.mycore.viewer.configuration.MCRViewerDefaultConfigurationStrategy;

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
            viewerConfiguration.addCSS(baseURL + "dbt/css/layout.min.css");

            if (request.getParameter("embedded") != null) {
                viewerConfiguration.setProperty("permalink.updateHistory", false);
                viewerConfiguration.setProperty("chapter.showOnStart", false);
            } else {
                // Default JS
                viewerConfiguration
                    .addScript(baseURL + "assets/bootstrap/js/bootstrap.min.js");
            }
        }
        return viewerConfiguration;
    }
}
