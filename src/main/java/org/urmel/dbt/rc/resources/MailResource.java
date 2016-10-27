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

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.mycore.common.content.MCRSourceContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.sun.jersey.spi.resource.Singleton;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("rcmail")
@Singleton
public class MailResource {

    @POST()
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("*/*")
    @MCRRestrictedAccess(MailResourcePermission.class)
    public Response resolve(String uri) {
        try {
            final StreamingOutput so = (OutputStream os) -> {
                try {
                    new MCRSourceContent(MCRURIResolver.instance().resolve(uri, null)).sendTo(os);
                } catch (TransformerException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            };
            return Response.ok().status(Response.Status.OK).entity(so)
                    .build();
        } catch (Exception e) {
            final StreamingOutput so = (OutputStream os) -> e
                    .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.name()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }
}
