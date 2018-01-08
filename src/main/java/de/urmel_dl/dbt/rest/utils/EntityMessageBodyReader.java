/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2017
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
package de.urmel_dl.dbt.rest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;

import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * The Class EntityMessageBodyReader.
 *
 * @author Ren\u00E9 Adler (eagle)
 * @param <T> the generic type
 */
@Provider
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class EntityMessageBodyReader<T> implements MessageBodyReader<T> {

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) || mediaType.equals(MediaType.APPLICATION_XML_TYPE))
            && type.getAnnotation(XmlRootElement.class) != null;
    }

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
        throws IOException, WebApplicationException {
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            return new EntityFactory<>(type).fromJSON(entityStream);
        }

        return new EntityFactory<>(type).fromXML(entityStream);
    }

}
