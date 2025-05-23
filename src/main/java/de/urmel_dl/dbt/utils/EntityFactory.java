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
package de.urmel_dl.dbt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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

import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

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
 * @param <T> the generic type
 */
public class EntityFactory<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final static String CONFIG_PREFIX = "DBT.EntityFactory.";

    protected final static String CONFIG_MARSHALLER = "Marshaller.";

    protected final static String CONFIG_UNMARSHALLER = "Unmarshaller.";

    private final static Map<String, Class<?>[]> CACHED_ENTITIES = new ConcurrentHashMap<>();

    private final static Map<String, String> MARSHALLER_JSON_PROPERTIES = new HashMap<>();

    private final static Map<String, String> UNMARSHALLER_JSON_PROPERTIES = new HashMap<>();

    static {
        MARSHALLER_JSON_PROPERTIES.put(MarshallerProperties.MEDIA_TYPE, "application/json");
        UNMARSHALLER_JSON_PROPERTIES.put(UnmarshallerProperties.MEDIA_TYPE, "application/json");
    }

    private Class<T> entityType;

    private T entity;

    private BiConsumer<Callable<Marshaller>, Object> marshal = (marshallerCaller, output) -> {
        Marshaller marshaller;
        try {
            marshaller = marshallerCaller.call();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't build marshaller.", e);
        }

        Class<? extends Marshaller> marshallerClass = marshaller.getClass();
        Class<?> outputType = output.getClass();

        Method method = findMethod(marshallerClass, "marshal", Object.class, outputType).orElseThrow(
            () -> new IllegalArgumentException(
                "Couldn't find a marshal method for marshaller " + marshallerClass + " with output type "
                    + outputType + "."));

        try {
            method.invoke(marshaller, entity, output);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(
                "Couldn't marshal " + entityType + " to output " + outputType + ".", e);
        }
    };

    private BiFunction<Callable<Unmarshaller>, Object, T> unmarshal = (unmarshallerCaller, input) -> {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = unmarshallerCaller.call();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't build unmarshaller.", e);
        }

        Class<? extends Unmarshaller> unmarshallerClass = unmarshaller.getClass();
        Class<?> inputType = input.getClass();

        Method method = findMethod(unmarshallerClass, "unmarshal", inputType).orElseThrow(
            () -> new IllegalArgumentException(
                "Couldn't find a unmarshal method for unmarshaller " + unmarshallerClass + " with input type "
                    + inputType + "."));

        try {
            return entityType.cast(method.invoke(unmarshaller, input));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(
                "Couldn't unmarshal " + inputType + " to " + entityType + ".", e);
        }
    };

    private BiFunction<Callable<Unmarshaller>, Object, T> jsonUnmarshal = (unmarshallerCaller, input) -> {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = unmarshallerCaller.call();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't build unmarshaller.", e);
        }

        try {
            return unmarshaller.unmarshal(toSource(input), entityType).getValue();
        } catch (IllegalArgumentException | JAXBException e) {
            throw new RuntimeException(
                "Couldn't unmarshal " + input.getClass() + " to " + entityType + ".", e);
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
     * Check if entity can be handled.
     * 
     * @return <code>true</code> if can be handled
     */
    public boolean canHandle() {
        return entityType != null && MCRConfiguration2.getPropertiesMap().entrySet().stream()
            .filter(e -> e.getKey().startsWith(CONFIG_PREFIX))
            .anyMatch(e -> Stream.of(CONFIG_MARSHALLER, CONFIG_UNMARSHALLER).map(a -> e.getKey().indexOf(a))
                .filter(i -> i != -1 && CONFIG_PREFIX.length() < i)
                .map(i -> e.getKey().substring(CONFIG_PREFIX.length(), i - 1))
                .anyMatch(p -> entityType.getPackage().getName().contains(p) || entityType.getName().contains(p)));
    }

    /**
     * Marshals a entity with specified marshal function.
     *
     * @param <R> the return type
     * @param f the f
     * @return the return type
     */
    public <R> R marshal(Function<T, R> f) {
        return f.apply(entity);
    }

    /**
     * Unmarshals a entity with specified unmarshal function.
     *
     * @param <S> the source type
     * @param s the source
     * @param f the unmarshal function
     * @return the entity class
     */
    public <S> T unmarshal(S s, Function<S, T> f) {
        return f.apply(s);
    }

    /**
     * Marshals the entity to JSON.
     *
     * @return the JSON string
     */
    public String toJSON() {
        StringWriter sw = new StringWriter();
        toJSON(sw);

        return sw.toString();
    }

    /**
     *  Marshals the entity to JSON.
     *
     * @param <S> the source type
     * @param source the source
     */
    public <S> void toJSON(S source) {
        marshal.accept(() -> marshaller(Optional.of(MARSHALLER_JSON_PROPERTIES)), source);
    }

    /**
     * Unmarshals the JSON string to entity.
     *
     * @param json the json string
     * @return the entity class
     */
    public T fromJSON(String json) {
        StringReader sr = new StringReader(json);
        return fromJSON(sr);
    }

    /**
     * Unmarshals the JSON source object to entity.
     *
     * @param source the json source
     * @return the entity class
     */
    public T fromJSON(Object source) {
        return jsonUnmarshal.apply(() -> unmarshaller(Optional.of(UNMARSHALLER_JSON_PROPERTIES)), source);
    }

    /**
     * Marshals the entity to XML.
     *
     * @return the XML string
     */
    public String toXML() {
        StringWriter sw = new StringWriter();
        toXML(sw);
        return sw.toString();
    }

    /**
     *  Marshals the entity to XML.
     *
     * @param <S> the source type
     * @param source the source
     */
    public <S> void toXML(S source) {
        marshal.accept(() -> marshaller(Optional.empty()), source);
    }

    /**
     * Unmarshals the XML string to entity.
     *
     * @param xml the xml
     * @return the entity class
     */
    public T fromXML(String xml) {
        StringReader sr = new StringReader(xml);
        return fromXML(sr);
    }

    /**
     * Unmarshals the XML source to entity.
     *
     * @param source the source
     * @return the entity class
     */
    public T fromXML(Object source) {
        return unmarshal.apply(() -> unmarshaller(Optional.empty()), source);
    }

    /**
     * Marshals the entity to {@link Document}.
     *
     * @return the {@link Document}
     */
    public Document toDocument() {
        JDOMResult r = new JDOMResult();
        marshal.accept(() -> marshaller(Optional.empty()), r);
        return r.getDocument();
    }

    /**
     * Unmarshals the {@link Document} to entity.
     *
     * @param doc the doc
     * @return the entity class
     */
    public T fromDocument(Document doc) {
        JDOMSource source = new JDOMSource(doc);
        return unmarshal.apply(() -> unmarshaller(Optional.empty()), source.getInputSource());
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
        return fromDocument(new Document(elm.clone()));
    }

    /**
     * Returns the marshaled entity. By default outputs entity as XML.
     *
     * @param mediaType the optional mediaType
     * @return the marshaled entity
     */
    public String marshalByMediaType(Optional<String> mediaType) {
        if (mediaType.isPresent() && mediaType.get().contains(MediaType.APPLICATION_JSON)) {
            return toJSON();
        }

        return toXML();
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

    private Marshaller marshaller(Optional<Map<String, ?>> extraProperties) throws JAXBException {
        JAXBContext context = getContext();
        Marshaller marshaller = context.createMarshaller();

        Map<String, ?> props = extraProperties.orElse(new HashMap<>());

        Stream.of(props, properties(CONFIG_MARSHALLER)).map(Map::entrySet).flatMap(Collection::stream)
            .forEach((e) -> {
                try {
                    marshaller.setProperty(e.getKey(), e.getValue());
                } catch (PropertyException ex) {
                    LOGGER.warn("Property \"{}\" couldn't set.", e.getKey());
                }
            });

        return marshaller;
    }

    private JAXBContext getContext() throws JAXBException {
        return JAXBContext.newInstance(populateEntities(),
            Map.of(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory"));
    }

    private Unmarshaller unmarshaller(Optional<Map<String, ?>> extraProperties) throws JAXBException {
        JAXBContext context = getContext();
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Map<String, ?> props = extraProperties.orElse(new HashMap<>());

        Stream.of(props, properties(CONFIG_UNMARSHALLER)).map(Map::entrySet).flatMap(Collection::stream).forEach(e -> {
            try {
                unmarshaller.setProperty(e.getKey(), e.getValue());
            } catch (PropertyException ex) {
                LOGGER.warn("Property \"{}\" couldn't set.", e.getKey());
            }
        });

        return unmarshaller;
    };

    private Optional<Method> findMethod(Class<?> cls, String name, Class<?>... parameterTypes) {
        return Arrays.stream(cls.getMethods()).filter(m -> name.equals(m.getName())).filter(m -> {
            Iterator<Class<?>> it1 = Arrays.stream(m.getParameterTypes()).iterator();
            Iterator<Class<?>> it2 = Arrays.stream(parameterTypes).iterator();
            while (it1.hasNext() && it2.hasNext()) {
                if (!it1.next().isAssignableFrom(it2.next())) {
                    return false;
                }
            }
            return !it1.hasNext() && !it2.hasNext();
        }).findFirst();
    }

    private Source toSource(Object input) {
        Class<?> inputType = input.getClass();

        Source src;
        if (Source.class.isAssignableFrom(inputType)) {
            src = (Source) input;
        } else if (Reader.class.isAssignableFrom(inputType)) {
            src = new StreamSource((Reader) input);
        } else if (InputStream.class.isAssignableFrom(inputType)) {
            src = new StreamSource((InputStream) input);
        } else {
            throw new IllegalArgumentException("Couldn't use " + inputType + " to unmarshal json.");
        }

        return src;
    }

    protected Map<String, ?> properties(String propType) {
        Function<String, String> keyFunc = k -> k.substring(k.indexOf(propType) + propType.length());
        Function<String, ?> valueFunc = v -> {
            if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                return Boolean.parseBoolean(v);
            }
            return v;
        };

        List<String> propNames = Arrays.asList(CONFIG_PREFIX + propType,
            CONFIG_PREFIX + entityType.getPackage().getName() + "." + propType,
            CONFIG_PREFIX + entityType.getName() + "." + propType);

        return MCRConfiguration2.getPropertiesMap().entrySet().stream()
            .filter(e -> propNames.stream().anyMatch(n -> e.getKey().startsWith(n))).sorted(Comparator.comparing(
                Entry::getKey, (k1, k2) -> {
                    String fpn = keyFunc.apply(k1);
                    if (fpn.equals(keyFunc.apply(k2))) {
                        int pi1 = propNames.indexOf(k1.replace(fpn, ""));
                        int pi2 = propNames.indexOf(k2.replace(fpn, ""));
                        return pi1 == pi2 ? 0 : pi1 < pi2 ? -1 : 1;
                    }
                    return k1.compareTo(k2);
                }))
            .collect(
                Collectors.toMap(e -> keyFunc.apply(e.getKey()), e -> valueFunc.apply(e.getValue()), (v1, v2) -> v2));
    }

    protected Class<?>[] populateEntities() {
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
