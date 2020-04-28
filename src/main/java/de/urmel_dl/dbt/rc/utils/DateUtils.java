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
package de.urmel_dl.dbt.rc.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DateUtils {

    private static final String PERIOD_DATE_ZONE = "Europe/Berlin";

    private static final String PERIOD_DATE_PATTERN = "dd.MM.yyyy";

    /**
     * Return the given date with time <code>00:00:00</code>.
     * 
     * @param date the date
     * @return the date with time <code>00:00:00</code>
     */
    public static Date getStartOfDay(final Date date) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Return the given date with time <code>23:59:59</code>.
     * @param date the date
     * @return the date with time <code>23:59:59</code>
     */
    public static Date getEndOfDay(final Date date) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Parse string and return a {@link Date}. 
     * @param dateStr the date string
     * @return the parsed {@link Date}
     */
    public static Date parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PERIOD_DATE_PATTERN, Locale.GERMANY);
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.of(PERIOD_DATE_ZONE)).toInstant());
    }

    /**
     * Return a formated date string.
     * @param date the date
     * @return the formated date string
     * @see {@link DateUtils#PERIOD_DATE_PATTERN}
     */
    public static String formatDate(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PERIOD_DATE_PATTERN, Locale.GERMANY);
        LocalDate localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.of(PERIOD_DATE_ZONE));
        return localDate.format(formatter);
    }

}
