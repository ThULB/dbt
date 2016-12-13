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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jdom2.Element;
import org.mycore.common.MCRMailer;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailJob extends MCRJobAction {

    public MailJob() {
    }

    /**
     * @param job the mail job
     */
    public MailJob(MCRJob job) {
        super(job);
    }

    /* (non-Javadoc)
     * @see org.mycore.services.queuedjob.MCRJobAction#isActivated()
     */
    @Override
    public boolean isActivated() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.mycore.services.queuedjob.MCRJobAction#name()
     */
    @Override
    public String name() {
        return MailJob.class.getSimpleName();
    }

    /* (non-Javadoc)
     * @see org.mycore.services.queuedjob.MCRJobAction#execute()
     */
    @Override
    public void execute() throws ExecutionException {
        try {
            final Element xml = MCRURIResolver.instance().resolve(buildURI(job.getParameters()));

            if (!xml.getChildren("to").isEmpty()) {
                MCRMailer.send(xml, true);
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.mycore.services.queuedjob.MCRJobAction#rollback()
     */
    @Override
    public void rollback() {
        // nothing to do
    }

    private String buildURI(final Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (params.containsKey("uri")) {
            sb.append(params.get("uri"));
        } else {
            List<String> up = new ArrayList<String>();
            for (String key : params.keySet()) {
                if (key.startsWith("uri_")) {
                    int i = Integer.parseInt(key.split("_")[1]);
                    up.add(i, params.get(key));
                }
            }

            if (!up.isEmpty()) {
                for (String p : up) {
                    sb.append(p);
                }
            }
        }

        return sb.toString();
    }
}
