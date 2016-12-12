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
package de.urmel_dl.dbt.common;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueue;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailQueue {

    private static final MCRJobQueue MAIL_QUEUE = MCRJobQueue.getInstance(MailJob.class);

    public static void addJob(final String uri) {
        final Element xml = MCRURIResolver.instance().resolve(uri);
        if (xml.getChildren("to").isEmpty()) {
            return;
        }

        final Map<String, String> params = new HashMap<String, String>();

        if (uri.length() > 254) {
            int i = 0;
            for (String p : uri.split("(?<=\\G.{254})")) {
                params.put("uri_" + Integer.toString(i), p);
                i++;
            }
        } else {
            params.put("uri", uri);
        }

        MCRJob job = MAIL_QUEUE.getJob(params);
        if (job == null) {
            job = new MCRJob(MailJob.class);
            job.setParameters(params);
        }

        MAIL_QUEUE.offer(job);
    }
}
