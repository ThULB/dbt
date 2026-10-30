/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2026
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 */
package de.urmel_dl.dbt.podcast.servlets;

import java.io.Serial;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * Renders the MIR shell used as the mount point for the podcast Vue application.
 *
 * <p>The servlet deliberately does not interpret {@code pathInfo}. Every URL below
 * {@code /podcast/} receives the same shell; client-side routing is exclusively
 * handled by Vue Router.</p>
 */
public class PodcastServlet extends MCRServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void doGetPost(final MCRServletJob job) throws Exception {
        final Document document = new Document(new Element("podcast"));
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(document));
    }
}
