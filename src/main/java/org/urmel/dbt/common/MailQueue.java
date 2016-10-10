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
package org.urmel.dbt.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueue;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailQueue {

    private static Logger LOGGER = LogManager.getLogger();

    private static final MCRJobQueue MAIL_QUEUE = MCRJobQueue.getInstance(MailJob.class);

    public static void addJob(final String uri) {
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

            try {
                MailJob mjob = new MailJob(job);
                mjob.execute();
                return;
            } catch (ExecutionException ex) {
                LOGGER.warn(ex.getMessage(), ex);
            }
        }

        MAIL_QUEUE.offer(job);
    }
}
