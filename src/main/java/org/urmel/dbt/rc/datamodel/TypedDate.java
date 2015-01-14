/*
 * $Id: TypedDate.java 2126 2014-11-25 09:20:21Z adler $ 
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
package org.urmel.dbt.rc.datamodel;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author René Adler (eagle)
 */
@XmlRootElement(name = "date")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "date", propOrder = { "type", "format" })
public class TypedDate implements Serializable {

    private static final long serialVersionUID = -8422581526966266251L;

    public static final String SHORT_DATE_FORMAT = "dd.MM.yyyy";

    public static final String LONG_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private Type type;

    private Date date;

    private String format;

    /**
     * Default Constructor for transforming.
     */
    protected TypedDate() {

    }

    /**
     * Creates a new {@link TypedDate} with given type.
     * 
     * @param type the date type
     */
    public TypedDate(final Type type) {
        this(type, LONG_DATE_FORMAT);
    }

    /**
     * Creates a new {@link TypedDate} with given type and specific format.
     * 
     * @param type the date type
     * @param format the date format
     */
    public TypedDate(final Type type, final String format) {
        this(type, format, null);
    }

    /**
     * Creates a new {@link TypedDate} with given type and date.
     * 
     * @param type the date type
     * @param date the date
     */
    public TypedDate(final Type type, final Date date) {
        this(type, LONG_DATE_FORMAT, date);
    }

    /**
     * Creates a new {@link TypedDate} with given type, format and date.
     *  
     * @param type the date type
     * @param format the date format
     * @param date the date
     */
    public TypedDate(final Type type, final String format, final Date date) {
        setType(type);
        setFormat(format);
        setDate(date);
    }

    /**
     * @return the type
     */
    @XmlAttribute(name = "type", required = true)
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        if (date == null) {
            return;
        }
        this.date = new Date(date.getTime());
    }

    /**
     * @return the formated date
     */
    @XmlValue
    public String getFormatedDate() {
        if (date == null) {
            return null;
        }
        DateFormat df = new SimpleDateFormat(format, Locale.ROOT);
        return df.format(date);
    }

    /**
     * @param date the formated date
     * @throws ParseException
     */
    public void setFormatedDate(final String date) throws ParseException {
        if (date == null) {
            return;
        }

        DateFormat df;
        if (format == null) {
            df = new SimpleDateFormat(LONG_DATE_FORMAT, Locale.ROOT);
            try {
                this.date = df.parse(date);
                format = LONG_DATE_FORMAT;
            } catch (final ParseException pe) {
                df = new SimpleDateFormat(SHORT_DATE_FORMAT, Locale.ROOT);
                this.date = df.parse(date);
                format = SHORT_DATE_FORMAT;
            }
        } else {
            df = new SimpleDateFormat(format, Locale.ROOT);
            this.date = df.parse(date);
        }
    }

    /**
     * @return the format
     */
    @XmlAttribute(name = "format")
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "TypedDate [type=%s, date=%s, format=%s]", type, date, format);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TypedDate other = (TypedDate) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (format == null) {
            if (other.format != null) {
                return false;
            }
        } else if (!format.equals(other.format)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @XmlType(name = "dateType")
    @XmlEnum
    public enum Type {
        @XmlEnumValue("created")
        CREATED("created"),

        @XmlEnumValue("modified")
        MODIFIED("modified");

        private final String value;

        Type(final String value) {
            this.value = value;
        }

        /**
         * Returns the set date type.
         * 
         * @return the set date type
         */
        public String value() {
            return value;
        }

        /**
         * Returns the date type from given value.
         * 
         * @param value the date type value
         * @return the date type
         */
        public static Type fromValue(final String value) {
            for (Type type : Type.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }
}
