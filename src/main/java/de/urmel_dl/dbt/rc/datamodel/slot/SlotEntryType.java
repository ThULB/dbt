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
package de.urmel_dl.dbt.rc.datamodel.slot;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class SlotEntryType.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "entry-type")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotEntryType implements Serializable {

    private static final long serialVersionUID = 6722322996282291627L;

    private String cls;

    private I18N i18n;

    /**
     * Gets the cls.
     *
     * @return the cls
     */
    @XmlAttribute(name = "class")
    public String getCls() {
        return cls;
    }

    /**
     * @param cls the cls to set
     */
    protected void setCls(final String cls) {
        this.cls = cls;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlAttribute(name = "name")
    public String getName() {
        try {
            final Class<?> clazz = getEntryClass();
            final XmlRootElement rootElm = clazz.getAnnotation(XmlRootElement.class);
            return rootElm.name();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the entry class.
     *
     * @return the entry class
     * @throws ClassNotFoundException thrown if class was not found
     */
    public Class<?> getEntryClass() throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(getCls());
    }

    /**
     * Gets the i18n.
     *
     * @return the i18n
     */
    @XmlElement(name = "i18n")
    public I18N getI18n() {
        return i18n;
    }

    /**
     * @param i18n the i18n to set
     */
    protected void setI18n(final I18N i18n) {
        this.i18n = i18n;
    }

    /**
     * The Class I18N.
     *
     * @author Ren\u00E9 Adler (eagle)
     */

    @XmlRootElement(name = "i18n")
    protected static class I18N {
        @XmlAttribute
        public String single;

        @XmlAttribute
        public String multiple;
    }
}
