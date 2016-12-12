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
package de.urmel_dl.dbt.rc.datamodel;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.jdom2.DataConversionException;

/**
 * The Slot warning date.
 * 
 * @author René Adler (eagle)
 *
 */
@XmlRootElement(name = "warning")
public class WarningDate implements Serializable, Comparable<WarningDate> {

    /**
     * Default warning date format.
     */
    public static final String DEFAULT_FORMAT = "dd.MM.yyyy";

    private static final long serialVersionUID = -8460652776713463836L;

    private Date warningDate;

    private String format;

    WarningDate() {
    }

    public WarningDate(final Date date) {
        setWarningDate(date);
    }

    /**
     * Returns the warning date.
     * 
     * @return the warning date 
     */
    @XmlValue
    public String getWarningDate() {
        final DateFormat df = new SimpleDateFormat(getFormat(), Locale.ROOT);
        return df.format(warningDate);
    }

    /**
     * Returns the warning date as {@link Date#Date()}.
     * 
     * @return the warning date
     */
    public Date getWarningDateAsDate() {
        return warningDate;
    }

    /**
     * Set the warning date.
     * 
     * @param warningDate the warning date to set
     */
    public void setWarningDate(final Date warningDate) {
        this.warningDate = new Date(warningDate.getTime());
    }

    /**
     * Set the warning date.
     * 
     * @param warningDate the warning date to set
     * @throws DataConversionException if set date have an invalid format
     */
    public void setWarningDate(final String warningDate) throws DataConversionException {
        try {
            final DateFormat df = new SimpleDateFormat(getFormat(), Locale.ROOT);
            this.warningDate = df.parse(warningDate);
        } catch (final ParseException e) {
            throw new DataConversionException("warningDate", "java.util.Date");
        }
    }

    /**
     * Returns the set format of warning date.
     * 
     * @return the warning date format
     */
    @XmlAttribute(name = "format")
    public String getFormat() {
        if (format == null) {
            format = DEFAULT_FORMAT;
        }
        return format;
    }

    /**
     * Set the format of warning date.
     * 
     * @param format the warning date format
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    public int compareTo(WarningDate o) {
        return warningDate.compareTo(o.warningDate);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WarningDate [");
        if (warningDate != null) {
            builder.append("warningDate=");
            builder.append(warningDate);
            builder.append(", ");
        }
        builder.append("format=");
        builder.append(getFormat());
        builder.append("]");
        return builder.toString();
    }

}
