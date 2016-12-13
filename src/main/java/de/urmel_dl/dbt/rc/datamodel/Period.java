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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.urmel_dl.dbt.rc.utils.DateUtils;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "period")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "period", propOrder = { "from", "to", "lectureEnd", "matchingLocation", "setableFrom", "setableTo",
    "labels", "warnings" })
public class Period implements Serializable, Comparable<Period>, Cloneable {

    private static final long serialVersionUID = -1389892190013532300L;

    /** DateFormat.MEDIUM, Locale.GERMANY: dd.MM.yyyy */
    private static final DateFormat PERIOD_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);

    private static final String SHORT_FORMAT = "[0-3][0-9]\\.[0-1][0-9]\\.";

    private boolean fqDate = false;

    private Date baseDate;

    private String matchingLocation;

    private String fromShort;

    private String setableFromShort;

    private String toShort;

    private String setableToShort;

    private String lectureEndShort;

    private List<Label> labels;

    private List<Warning> warnings;

    /**
     * Returns the set base date or the current date if'n was set.
     * 
     * @return the base
     * @see #setBaseDate(Date)
     */
    public Date getBaseDate() {
        if (baseDate == null) {
            baseDate = new Date();
        }
        return baseDate;
    }

    /**
     * Set the base date for this {@link Period}. 
     * If {@link #getFrom()} or {@link #getTo()} resp.
     * {@link #getSetableFrom()} or {@link #getSetableTo()}  
     * returns <code>null</code>, the period isn't a valid on.
     * 
     * @param base the base to set
     */
    public void setBaseDate(final Date base) {
        if (base != null) {
            this.baseDate = new Date(base.getTime());
        } else {
            this.baseDate = base;
        }
    }

    /**
     * Sets this date to the values combined of fromShort and the year of target date.
     * 
     * @param base the basic date
     * @throws ParseException should never occurs
     */
    public void setStartDate(final Date base) throws ParseException {
        this.baseDate = getPeriodDate(base, fromShort, toShort, false);
        if (this.baseDate == null) {
            this.baseDate = getPeriodDate(base, setableFromShort, setableToShort, true);
        }
    }

    /**
     * @param enabled <code>true</code> for fully qualified date output
     */
    public void setFullyQualified(final boolean enabled) {
        fqDate = enabled;
    }

    /**
     * @return is fully qualified date output is enabled
     */
    public boolean isFullyQualified() {
        return fqDate;
    }

    /**
     * @return the matchingLocation
     */
    @XmlAttribute(name = "matchingLocation")
    public String getMatchingLocation() {
        return matchingLocation;
    }

    /**
     * @param matchingLocation the matchingLocation to set
     */
    public void setMatchingLocation(final String matchingLocation) {
        this.matchingLocation = matchingLocation;
    }

    /**
     * Returns the from date as short string or 
     * if {@link #isFullyQualified()} equals <code>true</code> a fully qualified date string.
     * 
     * @return the from date
     * @throws ParseException should never occur
     */
    @XmlAttribute(name = "from")
    public String getFrom() throws ParseException {
        return fqDate ? constructDateString(getPeriodDate(getBaseDate(), false)) : fromShort;
    }

    /**
     * Returns the from date as Date.
     * 
     * @return the from date
     * @throws ParseException should never occur
     * @see #getFromDate(Date)
     */
    public Date getFromDate() throws ParseException {
        return getFromDate(getBaseDate());
    }

    /**
     * Returns the from date as Date.
     * 
     * @param base the basis date
     * @return the from date
     * @throws ParseException should never occur
     */
    public Date getFromDate(final Date base) throws ParseException {
        return getPeriodDate(base, false);
    }

    /**
     * Set the from date by given string.
     * The input string should be as short format or as fully qualified date string.  
     * 
     * @param from the from date
     */
    public void setFrom(final String from) {
        setFromShort(constructShort(from));
    }

    /**
     * Set the from date by given date.  
     * 
     * @param from the from date
     */
    public void setFrom(final Date from) {
        setFromShort(constructShort(from));
    }

    /**
     * @return the fromShort
     */
    protected String getFromShort() {
        return fromShort;
    }

    /**
     * @param fromShort the fromShort to set
     */
    private void setFromShort(final String fromShort) {
        if (fromShort == null || !fromShort.matches(SHORT_FORMAT)) {
            throw new IllegalArgumentException("invalid from format given");
        }
        this.fromShort = fromShort;
    }

    /**
     * Returns the setableFrom date as short string or 
     * if {@link #isFullyQualified()} equals <code>true</code> a fully qualified date string.
     * 
     * @return the setableFrom date
     * @throws ParseException should never occur
     */
    @XmlAttribute(name = "setableFrom")
    public String getSetableFrom() throws ParseException {
        final Date date = getPeriodDate(getBaseDate(), setableFromShort, setableToShort, false);
        return fqDate && date != null ? constructDateString(date) : setableFromShort;
    }

    /**
     * Returns the setableFrom date as Date.
     * 
     * @param base the basis date
     * @return the setableFrom date
     * @throws ParseException should never occur
     */
    public Date getSetableFromDate(final Date base) throws ParseException {
        return getPeriodDate(base, setableFromShort, setableToShort, false);
    }

    /**
     * Set the setableFrom date by given string.
     * The input string should be as short format or as fully qualified date string.  
     * 
     * @param setableFrom the setableFrom date
     */
    public void setSetableFrom(final String setableFrom) {
        setSetableFromShort(constructShort(setableFrom));
    }

    /**
     * Set the setableFrom date by given date.  
     * 
     * @param setableFrom the setableFrom date
     */
    public void setSetableFrom(final Date setableFrom) {
        setSetableFromShort(constructShort(setableFrom));
    }

    /**
     * @return the setableFromShort
     */
    protected String getSetableFromShort() {
        return setableFromShort;
    }

    /**
     * @param setableFromShort the setableFromShort to set
     */
    private void setSetableFromShort(final String setableFromShort) {
        if (setableFromShort == null || !setableFromShort.matches(SHORT_FORMAT)) {
            throw new IllegalArgumentException("invalid setableFrom format given");
        }
        this.setableFromShort = setableFromShort;
    }

    /**
     * Returns the to date as short string or 
     * if {@link #isFullyQualified()} equals <code>true</code> a fully qualified date string.
     * 
     * @return the to date
     * @throws ParseException should never occur
     */
    @XmlAttribute(name = "to")
    public String getTo() throws ParseException {
        return fqDate ? constructDateString(getPeriodDate(getBaseDate(), true)) : toShort;
    }

    /**
     * Returns the to date as Date.
     * 
     * @return the to date
     * @throws ParseException should never occur
     * @see #getToDate(Date)
     */
    public Date getToDate() throws ParseException {
        return getToDate(getBaseDate());
    }

    /**
     * Returns the to date as Date.
     * 
     * @param base the basis date
     * @return the to date
     * @throws ParseException should never occur
     */
    public Date getToDate(final Date base) throws ParseException {
        return getPeriodDate(base, true);
    }

    /**
     * Set the to date by given string.
     * The input string should be as short format or as fully qualified date string.  
     * 
     * @param to the to date
     */
    public void setTo(final String to) {
        setToShort(constructShort(to));
    }

    /**
     * Set the to date by given date.  
     * 
     * @param to the to date
     */
    public void setTo(final Date to) {
        setToShort(constructShort(to));
    }

    /**
     * @return the toShort
     */
    protected String getToShort() {
        return toShort;
    }

    /**
     * @param toShort the toShort to set
     */
    private void setToShort(final String toShort) {
        if (toShort == null || !toShort.matches(SHORT_FORMAT)) {
            throw new IllegalArgumentException("invalid to format given");
        }
        this.toShort = toShort;
    }

    /**
     * Returns the setableTo date as Date.
     * 
     * @param base the basis date
     * @return the setableTo date
     * @throws ParseException should never occur
     */
    public Date getSetableToDate(final Date base) throws ParseException {
        return getPeriodDate(base, setableFromShort, setableToShort, true);
    }

    /**
     * Returns the setableTo date as short string or 
     * if {@link #isFullyQualified()} equals <code>true</code> a fully qualified date string.
     * 
     * @return the setableTo date
     * @throws ParseException should never occur
     */
    @XmlAttribute(name = "setableTo")
    public String getSetableTo() throws ParseException {
        final Date date = getPeriodDate(getBaseDate(), setableFromShort, setableToShort, true);
        return fqDate && date != null ? constructDateString(date) : setableToShort;
    }

    /**
     * Set the setableTo date by given string.
     * The input string should be as short format or as fully qualified date string.  
     * 
     * @param setableTo the setableTo date
     */
    public void setSetableTo(final String setableTo) {
        setSetableToShort(constructShort(setableTo));
    }

    /**
     * Set the setableTo date by given date.  
     * 
     * @param setableTo the setableTo date
     */
    public void setSetableTo(final Date setableTo) {
        setSetableToShort(constructShort(setableTo));
    }

    /**
     * @return the setableToShort
     */
    protected String getSetableToShort() {
        return setableToShort;
    }

    /**
     * @param setableToShort the setableToShort to set
     */
    private void setSetableToShort(final String setableToShort) {
        if (setableToShort == null || !setableToShort.matches(SHORT_FORMAT)) {
            throw new IllegalArgumentException("invalid setableTo format given");
        }
        this.setableToShort = setableToShort;
    }

    @XmlAttribute(name = "setable", required = false)
    public boolean isSetable() throws ParseException {
        final Date today = new Date();
        final Date to = getToDate();

        if (to != null && today.after(to)) {
            return false;
        } else {
            Date setTo = getPeriodDate(getBaseDate(), setableFromShort, setableToShort, true);
            if (setTo == null)
                setTo = getPeriodDate(today, setableFromShort, setableToShort, true);

            if (setTo == null || today.after(setTo)) {
                return false;
            }
        }

        return true;
    }

    public boolean isSetable(final Date base) throws ParseException {
        return getPeriodDate(base, setableFromShort, setableToShort, false) != null;
    }

    /**
     * Returns the lectureEnd date as short string or 
     * if {@link #isFullyQualified()} equals <code>true</code> a fully qualified date string.
     * 
     * @return the lectureEnd date
     * @throws ParseException should never occur
     */
    @XmlAttribute(name = "lectureEnd")
    public String getLectureEnd() throws ParseException {
        return fqDate ? constructDateString(getLectureEndDate(getBaseDate())) : lectureEndShort;
    }

    /**
     * Returns the lecture end of a period. 
     * 
     * @return The {@link Date} of the end of the lecture if one could be found,
     *         <code>null</code> otherwise
     * @throws ParseException should never occur
     * @see #getLectureEndDate(Date)
     */
    public Date getLectureEndDate() throws ParseException {
        return getLectureEndDate(getBaseDate());
    }

    /**
     * The lecture end of a period is within the limits of from and to defined
     * in this period. According to the year by given date, <b>from</b> and
     * <b>to</b> may vary. See also {@link #getPeriodDate(Date, boolean)} for
     * some logic explanation. The same logic occurs when calculating the
     * lecture end of a period. The lecture is validated against <b>from</b> and
     * <b>to</b> calculated by {@link #getFrom()} and {@link #getTo()}.
     * 
     * @param date the date
     * @return The {@link Date} of the end of the lecture if one could be found,
     *         <code>null</code> otherwise
     * @throws ParseException should never occur
     */
    public Date getLectureEndDate(final Date date) throws ParseException {
        Date result = null;

        final Calendar inDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        inDate.setTime(date);

        final int year = inDate.get(Calendar.YEAR);

        // calculate _from and _to according to given date. _from is before _to!
        final Date _from = getFromDate(date);
        final Date _to = getToDate(date);

        Date _lEnd = PERIOD_FORMAT.parse(this.lectureEndShort + year);

        if (_from != null && _to != null) {
            /*
             * same validation as in getPeriodEnd. the following tests lead to a
             * null result only if the lecture is outside of the period. ex.:
             * _from = 01.10.(2010|11); _to = 31.03.(2011|12); _lEnd = 22.04.
             * _from = 01.04.(2011); _to = 30.09.(2011); _lEnd = 22.04.
             */

            if (isInsideExclusive(_from, _to, _lEnd)) {
                result = _lEnd;
            } else {
                // test later period
                final int yearInc = year + 1;
                _lEnd = PERIOD_FORMAT.parse(this.lectureEndShort + yearInc);
                if (isInsideExclusive(_from, _to, _lEnd)) {
                    result = _lEnd;
                } else {
                    // test earlier period
                    final int yearDec = year - 1;
                    _lEnd = PERIOD_FORMAT.parse(this.lectureEndShort + yearDec);
                    if (isInsideExclusive(_from, _to, _lEnd)) {
                        result = _lEnd;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Set the lectureEnd date by given string.
     * The input string should be as short format or as fully qualified date string.  
     * 
     * @param lectureEnd the lectureEnd date
     */
    public void setLectureEnd(final String lectureEnd) {
        setLectureEndShort(constructShort(lectureEnd));
    }

    /**
     * Set the lectureEnd date by given date.  
     * 
     * @param lectureEnd the lectureEnd date
     */
    public void setLectureEnd(final Date lectureEnd) {
        setLectureEndShort(constructShort(lectureEnd));
    }

    /**
     * @return the lectureEndShort
     */
    protected String getLectureEndShort() {
        return lectureEndShort;
    }

    /**
     * @param lectureEndShort the lectureEndShort to set
     */
    private void setLectureEndShort(final String lectureEndShort) {
        if (lectureEndShort == null || !lectureEndShort.matches(SHORT_FORMAT)) {
            throw new IllegalArgumentException("invalid lectureEnd format given");
        }
        this.lectureEndShort = lectureEndShort;
    }

    /**
     * @return the labels
     * @throws ParseException  should never occur
     */
    @XmlElement(name = "label")
    public List<Label> getLabels() throws ParseException {
        if (labels != null) {
            for (Label label : labels) {
                if (fqDate) {
                    setStartDate(getBaseDate());
                    label.setDescription(label.getText() + " " + getLabelExtension(getFromDate(getBaseDate())));
                } else {
                    label.setDescription(null);
                }
            }
        }
        return labels;
    }

    /**
     * @param labels the labels to set
     */
    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    /**
     * @return the warnings
     */
    @XmlElementWrapper(name = "warnings")
    @XmlElement(name = "warning")
    public List<Warning> getWarnings() {
        if (warnings != null) {
            for (Warning warning : warnings) {
                warning.setPeriod(this);
            }
        }
        return warnings;
    }

    /**
     * @param warnings the warnings to set
     */
    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }

    /**
     * Returns the actual {@link Warning} for given {@link Date}.
     * 
     * @param date the {@link Date} to check
     * @return {@link Warning} or <code>null</code> if none matches
     */
    public Warning getWarning(final Date date) {
        return getWarning(date, null);
    }

    /**
     * Returns the actual {@link Warning} for given {@link Date} and {@link WarningType}.
     * 
     * @param date the {@link Date} to check
     * @param warningType the {@link WarningType}
     * @return {@link Warning} or <code>null</code> if none matches
     */
    public Warning getWarning(final Date date, final WarningType warningType) {
        if (warnings != null && !getWarnings().isEmpty()) {
            try {
                Warning warning = null;
                for (Warning w : getWarnings()) {
                    if ((warningType == null || w.getType() == warningType)
                        && (date.after(w.getWarningDate()) || date.equals(w.getWarningDate()))
                        && (warning == null || w.getWarningDate().after(warning.getWarningDate()))) {
                        warning = w;
                    }
                }
                return warning;
            } catch (IllegalArgumentException | ParseException | CloneNotSupportedException e) {
                return null;
            }
        }

        return null;
    }

    private String getLabelExtension(Date date) throws ParseException {
        String result = null;

        Calendar fromDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        Calendar toDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

        Date from = getFromDate(date);
        Date to = getToDate(date);

        if (from != null && to != null) {
            fromDate.setTime(from);
            toDate.setTime(to);

            int fromYear = fromDate.get(Calendar.YEAR);
            int toYear = toDate.get(Calendar.YEAR);

            result = fromYear < toYear ? Integer.toString(fromYear) + "/" + Integer.toString(toYear)
                : Integer.toString(fromYear);
        } else {
            Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            c.setTime(date);
            result = Integer.toString(c.get(Calendar.YEAR));
        }

        return result;
    }

    private String constructShort(final Date inputDate) {
        if (inputDate != null) {
            final Date date = new Date(inputDate.getTime());
            final String dateStr = PERIOD_FORMAT.format(date);
            return dateStr.substring(0, dateStr.length() - dateStr.replaceFirst(SHORT_FORMAT, "").length());
        }

        throw new IllegalArgumentException("invalid input date \"" + inputDate + "\"");
    }

    private String constructShort(final String inputStr) {
        if (inputStr != null) {
            Date date;
            if (inputStr.matches(SHORT_FORMAT)) {
                try {
                    date = constructDate(inputStr);
                } catch (final ParseException e) {
                    date = null;
                }
            } else {
                try {
                    date = PERIOD_FORMAT.parse(inputStr);
                } catch (final ParseException e) {
                    date = null;
                }
            }

            if (date != null) {
                return constructShort(date);
            }
        }

        throw new IllegalArgumentException("invalid input string \"" + inputStr + "\"");
    }

    protected String constructDateString(final Date date) throws ParseException {
        return PERIOD_FORMAT.format(date);
    }

    private Date constructDate(final String fieldShort) throws ParseException {
        final Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(getBaseDate());
        final int year = cal.get(Calendar.YEAR);

        return PERIOD_FORMAT.parse(fieldShort + year);
    }

    /**
     * Checks if target is in the interval of start and end including these
     * dates. Validation of given dates is not done.
     * 
     * @param start the start date
     * @param end the end date
     * @param target the target date
     * @return <code>true</code> if target is inside start and end,
     *         <code>false</code> otherwise.
     */
    private static boolean isInsideInclusive(final Date start, final Date end, final Date target) {
        final boolean isStartBeforeTarget = start.before(target);
        final boolean isStartEqualToTarget = start.equals(target);
        final boolean isEndAfterTarget = end.after(target);
        final boolean isEndEqualToTarget = end.equals(target);

        return (isStartBeforeTarget || isStartEqualToTarget) && (isEndAfterTarget || isEndEqualToTarget);
    }

    /**
     * Checks if target is in the interval of start and end excluding these
     * dates. Validation of given dates is not done.
     * 
     * @param start the start date
     * @param end the end date
     * @param target the target date
     * @return <code>true</code> if target is inside start and end,
     *         <code>false</code> otherwise.
     */
    private static boolean isInsideExclusive(final Date start, final Date end, final Date target) {
        return start.before(target) && end.after(target);
    }

    /**
     * See {@linkplain #getPeriodDate(Date, String, String, boolean)}.
     * 
     * @param base the base date
     * @param end
     *            <code>true</code> if the end of the period should be returned,
     *            <code>false</code> for the beginning
     * @return A new {@link Date} with the begin or the end of a period if one
     *         could be calculated, <code>null</code> otherwise
     * @throws ParseException should never occur
     */
    private Date getPeriodDate(final Date base, final boolean end) throws ParseException {
        return getPeriodDate(base, fromShort, toShort, end);
    }

    /**
     * Calculates the beginning or the ending {@link Date} of a {@link Period}
     * based on target {@link Date}. Use <code>true</code> if you want to get
     * the end of the period, <code>false</code> for the beginning. Because a
     * period is initialized only with month and day data, the period is created
     * through the year of target date. There are three different chances, that
     * the date can be calculated, based upon target from and to.
     * <ol>
     * <li>If month and day of "to" is after "from" the period is inside one
     * single year. Therefore the result is the last day of the period.</li>
     * <li>If month and day of "to" is before "from" the period can be on two
     * different years.
     * <ol>
     * <li>The first period is calculated by adding one year to the end of the
     * period. The validation is now the same as mentioned earlier.</li>
     * <li>The second period is calculated by subtracting one year to the
     * beginning of the period. The validation is now the same as mentioned
     * earlier.</li>
     * </ol>
     * </li>
     * </ol>
     * If no date could be calculated, target date is not inside a period.
     * 
     * @param base the base date
     * @param from the from date part
     * @param to the to date part
     * @param end
     *            <code>true</code> if the end of the period should be returned,
     *            <code>false</code> for the beginning
     * @return A new {@link Date} with the begin or the end of a period if one
     *         could be calculated, <code>null</code> otherwise
     * @throws ParseException should never occurs
     */
    private static Date getPeriodDate(final Date base, final String from, final String to, final boolean end)
        throws ParseException {
        Date result = null;

        final Calendar inDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        inDate.setTime(base);

        final int year = inDate.get(Calendar.YEAR);

        Date _from = DateUtils.getStartOfDay(PERIOD_FORMAT.parse(from + year));
        Date _to = DateUtils.getEndOfDay(PERIOD_FORMAT.parse(to + year));

        // check if the period is inside one single year
        if (_from.before(_to)) {
            /*
             * if date is inside of the interval of _from and _to, then the
             * result is _to. if date is outside, the result is null
             */
            if (isInsideInclusive(_from, _to, base)) {
                result = end ? DateUtils.getEndOfDay(_to) : DateUtils.getStartOfDay(_from);
            }
        } else {
            /*
             * _to is earlier than _from. this occurs when the period is at the
             * turn of the year. here we have to check two different periods,
             * plus and minus one year based on the dates given in from and
             * to
             */

            // check the later period -> add one year to _to
            final int yearInc = year + 1;
            _to = DateUtils.getEndOfDay(PERIOD_FORMAT.parse(to + yearInc));
            if (isInsideInclusive(_from, _to, base)) {
                result = end ? DateUtils.getEndOfDay(_to) : DateUtils.getStartOfDay(_from);
            } else {
                // check the earlier period -> substract one year of _from
                final int yearDec = year - 1;
                _from = DateUtils.getStartOfDay(PERIOD_FORMAT.parse(from + yearDec));
                _to = DateUtils.getEndOfDay(PERIOD_FORMAT.parse(to + year));
                if (isInsideInclusive(_from, _to, base)) {
                    result = end ? DateUtils.getEndOfDay(_to) : DateUtils.getStartOfDay(_from);
                }
            }
        }

        return result;
    }

    /**
     * A {@link Period} is equal to another period if both {@link #fromShort}
     * and {@link #toShort} are equal and {@link #getFrom()} and
     * {@link #getTo()} return the same {@link Date}s.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Period) {
            Period other = (Period) obj;
            if (!fromShort.equals(other.fromShort) || !toShort.equals(other.toShort))
                return false;

            try {
                Date from = getFromDate();
                Date otherFrom = other.getFromDate();
                Date to = getToDate();
                Date otherTo = other.getToDate();
                return from.equals(otherFrom) && to.equals(otherTo);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else
            return false;
    }

    @Override
    public int compareTo(final Period other) {
        return this.fromShort.compareTo(other.fromShort) | this.toShort.compareTo(other.toShort);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Period clone() throws CloneNotSupportedException {
        final Period clone = new Period();

        clone.baseDate = this.baseDate;
        clone.fromShort = this.fromShort;
        clone.toShort = this.toShort;
        clone.setableFromShort = this.setableFromShort;
        clone.setableToShort = this.setableToShort;
        clone.lectureEndShort = this.lectureEndShort;
        clone.labels = this.labels;
        clone.setWarnings(this.warnings);

        return clone;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Period [");
        if (baseDate != null) {
            builder.append("baseDate=");
            builder.append(baseDate);
            builder.append(", ");
        }
        if (matchingLocation != null) {
            builder.append("matchingLocation=");
            builder.append(matchingLocation);
            builder.append(", ");
        }
        if (fromShort != null) {
            builder.append("fromShort=");
            builder.append(fromShort);
            builder.append(", ");
        }
        if (toShort != null) {
            builder.append("toShort=");
            builder.append(toShort);
            builder.append(", ");
        }
        if (setableFromShort != null) {
            builder.append("setableFromShort=");
            builder.append(setableFromShort);
            if (setableToShort != null) {
                builder.append(", ");
            }
        }
        if (setableToShort != null) {
            builder.append("setableToShort=");
            builder.append(setableToShort);
        }
        builder.append("]");
        return builder.toString();
    }
}
