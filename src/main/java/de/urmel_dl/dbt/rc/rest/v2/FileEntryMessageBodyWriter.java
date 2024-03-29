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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Provider
@Produces("*/*")
public class FileEntryMessageBodyWriter implements MessageBodyWriter<FileEntry> {

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return FileEntry.class.isAssignableFrom(type);
    }

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(FileEntry fileEntry, Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException, WebApplicationException {

        if (fileEntry.getPath() != null) {
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename = \"" + fileEntry.getName() + "\"");
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.add(HttpHeaders.ETAG, fileEntry.getHash());
            Files.copy(fileEntry.getPath(), entityStream);
            return;
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
