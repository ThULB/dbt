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
package de.urmel_dl.dbt.rc.datamodel.slot.entries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * The Class TextEntry.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "text")
@XmlAccessorType(XmlAccessType.NONE)
public class TextEntry implements Serializable {

    private static final long serialVersionUID = 933967545791166846L;

    private Format format;

    private String text;

    /**
     * Gets the format.
     *
     * @return the type
     */
    @XmlAttribute(name = "format", required = true)
    public Format getFormat() {
        return format;
    }

    /**
     * Sets the format.
     *
     * @param type the type to set
     */
    public void setFormat(final Format type) {
        this.format = type;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    @XmlValue
    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     *
     * @param text the text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TextEntry";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TextEntry)) {
            return false;
        }
        final TextEntry other = (TextEntry) obj;
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }

        return format != other.format;
    }

    /**
     * The Enum Format.
     *
     * @author Ren\u00E9 Adler (eagle)
     */
    @XmlType(name = "textFormat")
    @XmlEnum
    public enum Format {

        /** The plain. */
        @XmlEnumValue("plain")
        PLAIN("plain"),

        /** The preformatted. */
        @XmlEnumValue("preformatted")
        PREFORMATTED("preformatted"),

        /** The html. */
        @XmlEnumValue("html")
        HTML("html");

        private final String value;

        /**
         * Instantiates a new format.
         *
         * @param value the value
         */
        Format(final String value) {
            this.value = value;
        }

        /**
         * Returns the set text format.
         *
         * @return the set text format
         */
        public String value() {
            return value;
        }

        /**
         * Returns the text format from given value.
         *
         * @param value the text format value
         * @return the text format
         */
        public static Format fromValue(final String value) {
            for (Format type : Format.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }
}
