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
package de.urmel_dl.dbt.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import de.urmel_dl.dbt.annotation.EnumValue;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class EnumResolver implements URIResolver {

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            String clsName = href.split(":")[1];

            this.getClass();
            Class<?> cls = Class.forName(clsName);
            if (cls.isEnum()) {
                Element root = new Element("enum");
                root.setAttribute("name", cls.getSimpleName());
                for (Object obj : cls.getEnumConstants()) {
                    final Method m = obj.getClass().getDeclaredMethod("value");
                    final String value = (String) m.invoke(obj);

                    EnumValue enumValue = getAnnotation(cls, value);

                    if (enumValue == null || enumValue.visible()) {
                        Element elm = new Element("value");
                        elm.setText(value);
                        if (enumValue != null && enumValue.disabled()) {
                            elm.setAttribute("disabled", Boolean.toString(enumValue.disabled()));
                        }
                        root.addContent(elm);
                    }
                }

                return new JDOMSource(root);
            }

            return null;
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }

    private EnumValue getAnnotation(final Class<?> cls, final String value) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(value)) {
                return field.getAnnotation(EnumValue.class);
            }
        }
        return null;
    }
}
