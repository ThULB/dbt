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
package org.urmel.dbt.rc.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailResourcePermission implements MCRResourceAccessChecker {

    private static final String PERMISSION_MAIL = "rcmail";

    /* (non-Javadoc)
     * @see org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker#isPermitted(com.sun.jersey.spi.container.ContainerRequest)
     */
    @Override
    public boolean isPermitted(ContainerRequest request) {
        String uri = request.getEntity(String.class);
        try {
            if (!MCRAccessManager.checkPermission(PERMISSION_MAIL)) {
                return false;
            }

            return true;
        } catch (Exception exc) {
            throw new WebApplicationException(exc,
                    Response.status(Status.INTERNAL_SERVER_ERROR)
                            .entity("Unable to check permission for request " + request.getRequestUri()
                                    + " containing entity value " + uri)
                            .build());
        }
    }
}
