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
package de.urmel_dl.dbt.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration2;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * The <code>EntityFactory</code> class marshals/unmarshals a specified entity class
 * with shorthand methods or by marshal/unmarshal functions.
 * <br>
 * <br>
 * <b>Examples:</b>
 * <p>
 * Marshalling a entity to output:
 * </p>
 * <blockquote>
 * <pre>
 * new EntityFactory&lt;&gt;(entity).toJSON()
 * new EntityFactory&lt;&gt;(entity).marshal(entity -&gt; { return ...; })
 * </pre>
 * </blockquote>
 * <p>
 * Unmarshalling a source to entity:
 * </p>
 * <blockquote>
 * <pre>
 * new EntityFactory&lt;Entity&gt;(Entity.class).fromJSON(json)
 * new EntityFactory&lt;Entity&gt;(Entity.class).unmarshal(json, source -&gt; { return ...; })
 * </pre>
 * </blockquote>
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class EntityFactory<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final static String CONFIG_PREFIX = "DBT.EntityFactory.";

    private final static String CONFIG_MARSHALLER = "Marshaller.";

    private final static String CONFIG_UNMARSHALLER = "Unmarshaller.";

    private final static Map<String, Class<?>[]> CACHED_ENTITIES = new ConcurrentHashMap<>();

    private Class<T> entityType;

    private T entity;

    private Function<T, String> toJSON = entity -> {
        try {
            StringWriter sw = new StringWriter();

            Marshaller marshaller = marshaller();
            try {
                marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            } catch (PropertyException e) {
                LOGGER.warn("Property \"{}\" couldn't set.", MarshallerProperties.MEDIA_TYPE);
            }
            marshaller.marshal(entity, sw);

            return sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Couldn't marshal " + entity.getClass().getName() + " to JSON.", e);
        }
    };

    private Function<String, T> fromJSON = source -> {
        try {
            StringReader sr = new StringReader(source);
            Unmarshaller unmarshaller = unmarshaller();
            try {
                unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            } catch (PropertyException e) {
                LOGGER.warn("Property \"{}\" couldn't set.", MarshallerProperties.MEDIA_TYPE);
            }
            return entityType.cast(unmarshaller.unmarshal(sr));
        } catch (JAXBException e) {
            throw new RuntimeException(
                "Couldn't unmarshal " + source.getClass().getName() + " as JSON to " + entityType + ".", e);
        }
    };

    private Function<T, String> toXML = entity -> {
        try {
            StringWriter sw = new StringWriter();
            marshaller().marshal(entity, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Couldn't marshal " + entity.getClass().getName() + " to XML.", e);
        }
    };

    private Function<String, T> fromXML = source -> {
        try {
            StringReader sr = new StringReader(source);
            Unmarshaller unmarshaller = unmarshaller();
            return entityType.cast(unmarshaller.unmarshal(sr));
        } catch (JAXBException e) {
            throw new RuntimeException(
                "Couldn't unmarshal " + source.getClass().getName() + " as XML to " + entityType + ".", e);
        }
    };

    private Function<T, Document> toDocument = entity -> {
        try {
            JDOMResult r = new JDOMResult();
            marshaller().marshal(entity, r);
            return r.getDocument();
        } catch (JAXBException e) {
            throw new RuntimeException("Couldn't marshal " + entity.getClass().getName() + " to Document.", e);
        }
    };

    private Function<Document, T> fromDocument = source -> {
        try {
            JDOMSource js = new JDOMSource(source);
            Unmarshaller unmarshaller = unmarshaller();
            return entityType.cast(unmarshaller.unmarshal(js.getInputSource()));
        } catch (JAXBException e) {
            throw new RuntimeException("Couldn't unmarshal " + source.getClass().getName() + " to " + entityType + ".",
                e);
        }
    };

    /**
     * Instantiates a new entity factory.
     *
     * @param entityType the entity class
     */
    public EntityFactory(Class<T> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Entity class was null!");
        }

        this.entityType = entityType;
    }

    /**
     * Instantiates a new entity factory.
     *
     * @param entity the entity object
     */
    @SuppressWarnings("unchecked")
    public EntityFactory(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity was null!");
        }

        this.entityType = (Class<T>) entity.getClass();
        this.entity = entity;
    }

    /**
     * Marshals a entity with specified marshal function.
     *
     * @param <V> the value type
     * @param marshalFunc the marshal function
     * @return the value type
     */
    public <V> V marshal(Function<T, V> marshalFunc) {
        return marshalFunc.apply(entity);
    }

    /**
     * Unmarshals a entity with specified unmarshal function.
     *
     * @param <V> the value type
     * @param source the source
     * @param unmarshalFunc the unmarshal function
     * @return the entity class
     */
    public <V> T unmarshal(V source, Function<V, T> unmarshalFunc) {
        return unmarshalFunc.apply(source);
    }

    /**
     * Marshals the entity to JSON.
     *
     * @return the JSON string
     */
    public String toJSON() {
        return marshal(toJSON);
    }

    /**
     * Unmarshals the JSON source to entity.
     *
     * @param json the json
     * @return the entity class
     */
    public T fromJSON(String json) {
        return unmarshal(json, fromJSON);
    }

    /**
     * Marshals the entity to XML.
     *
     * @return the XML string
     */
    public String toXML() {
        return marshal(toXML);
    }

    /**
     * Unmarshals the XML source to entity.
     *
     * @param xml the xml
     * @return the entity class
     */
    public T fromXML(String xml) {
        return unmarshal(xml, fromXML);
    }

    /**
     * Marshals the entity to {@link Document}.
     *
     * @return the {@link Document}
     */
    public Document toDocument() {
        return marshal(toDocument);
    }

    /**
     * Unmarshals the {@link Document} to entity.
     *
     * @param doc the doc
     * @return the entity class
     */
    public T fromDocument(Document doc) {
        return unmarshal(doc, fromDocument);
    }

    /**
     * Marshals the entity to {@link Element}.
     *
     * @return the {@link Element}
     */
    public Element toElement() {
        return toDocument().getRootElement().clone();
    }

    /**
     * Unmarshals the {@link Element} to entity.
     *
     * @param elm the elm
     * @return the entity class
     */
    public T fromElement(Element elm) {
        return unmarshal(new Document(elm.clone()), fromDocument);
    }

    /**
     * Returns the marshaled entity.
     *
     * @param mediaType the optional mediaType
     * @return the entity as string
     */
    public String marshalByMediaType(Optional<String> mediaType) {
        if (mediaType.isPresent()) {
            if (mediaType.get().contains(MediaType.APPLICATION_JSON)) {
                return toJSON();
            } else if (mediaType.get().contains(MediaType.APPLICATION_XML)
                || mediaType.get().contains(MediaType.TEXT_XML)) {
                return toXML();
            }
        }

        return entity.toString();
    }

    /**
     * Returns the unmarshaled entity.
     *
     * @param source the source
     * @param mediaType the mediaType
     * @return the entity
     */
    public T unmarshalByMediaType(String source, String mediaType) {
        if (mediaType.contains(MediaType.APPLICATION_JSON)) {
            return fromJSON(source);
        } else if (mediaType.contains(MediaType.APPLICATION_XML)
            || mediaType.contains(MediaType.TEXT_XML)) {
            return fromXML(source);
        }

        return null;
    }

    private Marshaller marshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(populateEntities());
        Marshaller marshaller = context.createMarshaller();

        properties(CONFIG_MARSHALLER).forEach((k, v) -> {
            try {
                marshaller.setProperty(k, v);
            } catch (PropertyException e) {
                LOGGER.warn("Property \"{}\" couldn't set.", k);
            }
        });

        return marshaller;
    }

    private Unmarshaller unmarshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(populateEntities());
        Unmarshaller unmarshaller = context.createUnmarshaller();

        properties(CONFIG_UNMARSHALLER).forEach((k, v) -> {
            try {
                unmarshaller.setProperty(k, v);
            } catch (PropertyException e) {
                LOGGER.warn("Property \"{}\" couldn't set.", k);
            }
        });

        return unmarshaller;
    }

    private Map<String, ?> properties(String propType) {
        Function<String, String> keyFunc = k -> k.substring(k.indexOf(propType) + propType.length());
        Function<String, ?> valueFunc = v -> {
            if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                return Boolean.parseBoolean(v);
            }
            return v;
        };

        return Stream.of(MCRConfiguration2.getPropertiesMap(CONFIG_PREFIX + propType),
            MCRConfiguration2.getPropertiesMap(CONFIG_PREFIX + entityType.getPackage().getName() + "." + propType))
            .map(Map::entrySet).flatMap(Collection::stream)
            .collect(
                Collectors.toMap(e -> keyFunc.apply(e.getKey()), e -> valueFunc.apply(e.getValue()), (v1, v2) -> v2));
    }

    private Class<?>[] populateEntities() {
        final String pkgName = entityType.getPackage().getName();

        if (!CACHED_ENTITIES.containsKey(pkgName)) {
            Class<?>[] classes;
            try {
                classes = ClassPath.from(entityType.getClassLoader()).getAllClasses()
                    .stream()
                    .filter(ci -> ci.getName().startsWith(pkgName)
                        && ci.load().isAnnotationPresent(XmlRootElement.class))
                    .map(ClassInfo::load).toArray(Class<?>[]::new);
            } catch (IOException e) {
                classes = entityType.getClasses();
            }
            CACHED_ENTITIES.put(pkgName, classes);
        }

        return CACHED_ENTITIES.get(pkgName);
    }
}
