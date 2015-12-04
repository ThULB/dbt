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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
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
     * @param job
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
            MCRSessionMgr.getCurrentSession().setUserInformation(MCRSystemUserInformation.getSuperUserInstance());
            
            final Element xml = MCRURIResolver.instance().resolve(buildURI(job.getParameters()));

            if (!xml.getChildren("to").isEmpty()) {
                Mailer.send(xml, true);
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
