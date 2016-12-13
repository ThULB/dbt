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
package de.urmel_dl.dbt.rc.resources;

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

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("rcmail")
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
