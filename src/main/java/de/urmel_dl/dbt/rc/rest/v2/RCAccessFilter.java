/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2018
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.rc.rest.v2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.user2.MCRUserManager;

import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.rc.rest.v2.annotation.RCAccessCheck;
import de.urmel_dl.dbt.rest.utils.EntityMessageBodyReader;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Provider
@Priority(Priorities.ENTITY_CODER + 10)
public class RCAccessFilter implements ReaderInterceptor, WriterInterceptor {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    HttpServletRequest httpServletRequest;

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.ReaderInterceptor#aroundReadFrom(javax.ws.rs.ext.ReaderInterceptorContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        if (context.getType().isAnnotationPresent(RCAccessCheck.class)) {
            Arrays.stream(context.getAnnotations()).forEach(LOGGER::info);

            MessageBodyReader er = new EntityMessageBodyReader();

            Object entity = er.readFrom(context.getType(), context.getGenericType(), context.getAnnotations(),
                context.getMediaType(), context.getHeaders(), context.getInputStream());

            if (entity != null) {
                LOGGER.info(entity);
            }
        }

        return context.proceed();
    }

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.WriterInterceptor#aroundWriteTo(javax.ws.rs.ext.WriterInterceptorContext)
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (context.getType().isAnnotationPresent(RCAccessCheck.class)) {
            Object entity = context.getEntity();

            if (entity != null) {
                MCRSessionMgr.unlock();
                MCRSession currentSession = MCRSessionMgr.getCurrentSession();

                if (!MCRSystemUserInformation.getGuestInstance().equals(currentSession.getUserInformation())) {
                    // inject the user
                    Optional.ofNullable(MCRUserManager.getUser(currentSession.getUserInformation().getUserID()))
                        .ifPresent(currentSession::setUserInformation);
                }

                if (entity instanceof Attendees) {
                    Attendees attendees = Attendees.class.cast(entity);
                    Slot slot = SlotManager.instance().getSlotById(attendees.slotId);

                    boolean allowed = checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_WRITE);

                    LOGGER.info("has access {} for {}", allowed, entity.getClass());

                    if (!allowed) {
                        throw new WebApplicationException(Response.Status.FORBIDDEN);
                    }
                } else if (entity instanceof Slot) {
                    Slot slot = Slot.class.cast(entity);

                    boolean allowed = checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ);

                    LOGGER.info("has access {} for {}", allowed, entity.getClass());

                    if (!allowed) {
                        context.setEntity(slot.getBasicCopy());
                    } else {
                        context.setEntity(slot.getExportableCopy());
                    }
                } else if (entity instanceof FileEntry) {
                    Slot slot = Optional.ofNullable(context.getProperty(Slot.class.getName()))
                        .map(o -> Slot.class.cast(o))
                        .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));

                    boolean allowed = checkPermission(slot.getMCRObjectID(), MCRAccessManager.PERMISSION_READ);

                    LOGGER.info("has access {} for {}", allowed, entity.getClass());

                    if (!allowed) {
                        throw new WebApplicationException(Response.Status.FORBIDDEN);
                    }
                }
            }
        }

        context.proceed();
    }

    private boolean checkPermission(MCRObjectID id, String permission) {
        return SlotManager.checkPermission(id, permission) || MCRAccessManager.checkPermission(id, permission);
    }

}
