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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DateUtils {

    public static final String TIME_ZONE = "Europe/Berlin";

    public static final String DATE_PATTERN = "dd.MM.yyyy";

    /**
     * Return the given date with time <code>00:00:00</code>.
     * 
     * @param date the date
     * @return the date with time <code>00:00:00</code>
     */
    public static Date getStartOfDay(final Date date) {
        LocalDate localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.of(TIME_ZONE));
        return Date.from(localDate.atStartOfDay(ZoneId.of(TIME_ZONE)).toInstant());
    }

    /**
     * Return the given date with time <code>23:59:59</code>.
     * @param date the date
     * @return the date with time <code>23:59:59</code>
     */
    public static Date getEndOfDay(final Date date) {
        LocalDate localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.of(TIME_ZONE));
        return Date.from(LocalDateTime.of(localDate, LocalTime.MAX).atZone(ZoneId.of(TIME_ZONE)).toInstant());
    }

    /**
     * Return the year for given {@link Date}.
     * @param date the date
     * @return the year
     */
    public static int getYear(final Date date) {
        LocalDate localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.of(TIME_ZONE));
        return localDate.getYear();
    }

    /**
     * Parse string and return a {@link Date}. 
     * @param dateStr the date string
     * @return the parsed {@link Date}
     */
    public static Date parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.GERMANY);
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.of(TIME_ZONE)).toInstant());
    }

    /**
     * Return a formated date string.
     * @param date the date
     * @return the formated date string
     * @see {@link DateUtils#DATE_PATTERN}
     */
    public static String formatDate(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.GERMANY);
        LocalDate localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.of(TIME_ZONE));
        return localDate.format(formatter);
    }

}
