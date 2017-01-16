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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;

/**
 * <p>This object represents a warning within or outside a period to be send as
 * mail to the lecturer and/or the library staff.<br>
 * It held informations about the warning Date btw. an Integer value to be
 * subtract, as day(s), from the period end date of the Reserve Collection.<br>
 * Also an XSL template can be defined witch is used to build an mail from given
 * XML Element.</p>
 *
 * <p>Use the following example to define a warning:</p>
 *
 * <pre>
 * {@code
 * <period>
 *   <warnings>
 *     <warning type="validTo|periodEnd|lectureEnd" at="number|dd.MM." template="name of the xsl template" />
 *   </warnings>
 * </period>
 * }
 * </pre>
 *
 * <p>The "at"-attribute should contain a number or a date with the given format. A
 * number is used in combination with the expiration date of a {@link Slot}. X
 * days, where x is the number, before expiration a warning can be generated.</p>
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "warning")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "warning", propOrder = { "type", "at", "template", "groupTemplate" })
public class Warning implements Serializable {

    public static final String NUMBER_FORMAT = "[0-9]+";

    public static final String AT_DATE_FORMAT = "[0-3][0-9]\\.[0-1][0-9]\\.";

    private static final long serialVersionUID = -7623469646489474951L;

    private Period period;

    private WarningType type;

    private String at;

    private String template;

    private String groupTemplate;

    /**
     * @return the period
     */
    @XmlTransient
    public Period getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    protected void setPeriod(final Period period) {
        this.period = period;
    }

    /**
     * @return the type
     */
    @XmlAttribute(name = "type")
    public WarningType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final WarningType type) {
        this.type = type;
    }

    /**
     * @return the at
     * @throws CloneNotSupportedException thrown to indicate clone is not supported
     * @throws ParseException thrown if date couldn't constructed
     * @throws IllegalArgumentException thrown on missing argument
     */
    @XmlAttribute(name = "at")
    public String getAt() throws IllegalArgumentException, ParseException, CloneNotSupportedException {
        return period.isFullyQualified() ? period.constructDateString(getWarningDate()) : at;
    }

    /**
     * @param at the at to set
     */
    public void setAt(final String at) {
        this.at = at;
    }

    /**
     * @return the template
     */
    @XmlAttribute(name = "template")
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(final String template) {
        this.template = template;
    }

    /**
     * @return the groupTemplate
     */
    @XmlAttribute(name = "groupTemplate")
    public String getGroupTemplate() {
        return groupTemplate;
    }

    /**
     * @param groupTemplate the groupTemplate to set
     */
    public void setGroupTemplate(final String groupTemplate) {
        this.groupTemplate = groupTemplate;
    }

    /**
     * Creates the warning date according to the format of this objects
     * {@link #at} and the different dates of the period this warning belongs to.
     *
     * @return Date the warning date
     * @throws ParseException thrown if date couldn't parsed
     * @throws CloneNotSupportedException thrown to indicate clone is not supported
     */
    public Date getWarningDate() throws IllegalArgumentException, ParseException, CloneNotSupportedException {
        Date result = null;

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);
        Date toDate = this.type == WarningType.PERIODEND ? period.getToDate() // end of current period
            : this.type == WarningType.LECTUREEND ? period.getLectureEndDate() // lecture end of current period
                : period.getBaseDate(); // if all goes wrong we use the base date

        if (at.matches(NUMBER_FORMAT)) {

            /*
             * if the at attribute within the configuration is an Integer value
             * it will be used to subtract from the period end date as days.
             */
            int rollAmount = Integer.parseInt(at);

            /*
             * It may happen, that the calculated date is in a period before
             * this period
             */
            Date warningDate = rollDate(toDate, Calendar.DAY_OF_MONTH, rollAmount * -1);

            return warningDate;
        } else if (at.matches(AT_DATE_FORMAT)) {

            if (this.type == WarningType.VALIDTO) {
                throw new IllegalArgumentException("A date value isn't allowed for type validTo!");
            }

            Calendar inDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

            inDate.setTime(toDate);

            int year = inDate.get(Calendar.YEAR);

            Date atDate = df.parse(at + year);

            Period periodAt = period.clone();
            periodAt.setBaseDate(atDate);

            if (period.equals(periodAt)) {
                result = atDate;
            } else if (atDate.before(period.getFromDate())) {
                Calendar fDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

                fDate.setTime(toDate);
                atDate = df.parse(at + fDate.get(Calendar.YEAR));

                periodAt = period.clone();
                periodAt.setBaseDate(atDate);

                if (period.equals(periodAt)) {
                    result = atDate;
                }
            } else if (atDate.after(toDate)) {
                Calendar tDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

                tDate.setTime(period.getFromDate());
                atDate = df.parse(at + tDate.get(Calendar.YEAR));

                periodAt = period.clone();
                periodAt.setBaseDate(atDate);

                if (period.equals(periodAt)) {
                    result = atDate;
                }
            }
        } else {
            throw new IllegalArgumentException("invalid value given in at. use \"[0-9]+\" or \"dd.MM.\"");
        }

        return result;
    }

    /**
     * Helper method for rolling given date with a field defined inside
     * {@link Calendar} by given amount.
     *
     * @param date
     * @param field
     *            field defined in {@link Calendar} like {@link Calendar#MONTH}
     * @param amount
     * @return
     */
    private static Date rollDate(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

        calendar.setTime(date);
        calendar.add(field, amount);

        return calendar.getTime();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Warning [");
        if (type != null) {
            builder.append("type=").append(type).append(", ");
        }
        if (at != null) {
            builder.append("at=").append(at).append(", ");
        }
        if (template != null) {
            builder.append("template=").append(template).append(", ");
        }
        if (groupTemplate != null) {
            builder.append("groupTemplate=").append(groupTemplate);
        }
        builder.append("]");
        return builder.toString();
    }
}
