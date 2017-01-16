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
package de.urmel_dl.dbt.rc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;

import de.urmel_dl.dbt.rc.datamodel.Period;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * The {@link Period} test cases.
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestPeriod extends MCRTestCase {

    private static final DateFormat PERIOD_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);

    @Test
    public void testPeriod() throws IOException, ParseException {
        Period period = new Period();

        period.setFrom("01.10.");
        period.setTo("31.03.");

        assertEquals("01.10.", period.getFrom());
        assertEquals("31.03.", period.getTo());

        period.setSetableFrom("21.07.");
        period.setSetableTo("02.02.");

        assertEquals("21.07.", period.getSetableFrom());
        assertEquals("02.02.", period.getSetableTo());

        period.setLectureEnd("02.03.");
        assertEquals("02.03.", period.getLectureEnd());
    }

    @Test
    public void testPeriodFQFromAfterTo() throws IOException, ParseException {
        Period period = new Period();
        period.setFullyQualified(true);

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(new Date());

        final int year = cal.get(Calendar.YEAR);

        final Date base = PERIOD_FORMAT.parse("30.11." + year);

        period.setBaseDate(base);

        period.setFrom("01.10.");
        period.setTo("31.03.");

        assertEquals("01.10." + year, period.getFrom());
        assertEquals("31.03." + (year + 1), period.getTo());

        period.setSetableFrom("21.07.");
        period.setSetableTo("02.02.");

        assertEquals("21.07." + year, period.getSetableFrom());
        assertEquals("02.02." + (year + 1), period.getSetableTo());

        period.setLectureEnd("02.03.");
        assertEquals("02.03." + (year + 1), period.getLectureEnd());
    }

    @Test
    public void testPeriodFQToAfterFrom() throws IOException, ParseException {
        Period period = new Period();
        period.setFullyQualified(true);

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(new Date());

        final int year = cal.get(Calendar.YEAR);

        final Date base = PERIOD_FORMAT.parse("30.06." + year);

        period.setBaseDate(base);

        period.setFrom("01.04.");
        period.setTo("30.09.");

        assertEquals("01.04." + year, period.getFrom());
        assertEquals("30.09." + year, period.getTo());

        period.setSetableFrom("03.02.");
        period.setSetableTo("20.07.");

        assertEquals("03.02." + year, period.getSetableFrom());
        assertEquals("20.07." + year, period.getSetableTo());

        period.setLectureEnd("03.08.");
        assertEquals("03.08." + year, period.getLectureEnd());
    }

    @Test
    public void testPeriodFromShort() throws IOException, ParseException {
        Period period = new Period();

        period.setFrom("01.10.");
        assertEquals("01.10.", period.getFrom());

        period.setFrom("31.11.");
        assertEquals("01.12.", period.getFrom());
    }

    @Test
    public void testPeriodFromLong() throws IOException, ParseException {
        Period period = new Period();

        period.setFrom("01.10.2014");
        assertEquals("01.10.", period.getFrom());

        period.setFrom("31.11.2014");
        assertEquals("01.12.", period.getFrom());
    }

    @Test
    public void testPeriodToShort() throws IOException, ParseException {
        Period period = new Period();

        period.setTo("01.10.");
        assertEquals("01.10.", period.getTo());

        period.setTo("31.11.");
        assertEquals("01.12.", period.getTo());
    }

    @Test
    public void testPeriodToLong() throws IOException, ParseException {
        Period period = new Period();

        period.setTo("01.10.2014");
        assertEquals("01.10.", period.getTo());

        period.setTo("31.11.2014");
        assertEquals("01.12.", period.getTo());
    }

    //
    @Test
    public void testPeriodSetableFromShort() throws IOException, ParseException {
        Period period = new Period();

        period.setSetableFrom("01.10.");
        period.setSetableTo("02.10.");
        assertEquals("01.10.", period.getSetableFrom());

        period.setSetableFrom("31.11.");
        period.setSetableTo("02.12.");
        assertEquals("01.12.", period.getSetableFrom());
    }

    @Test
    public void testPeriodSetableFromLong() throws IOException, ParseException {
        Period period = new Period();

        period.setSetableFrom("01.10.2014");
        period.setSetableTo("02.10.2014");
        assertEquals("01.10.", period.getSetableFrom());

        period.setSetableFrom("31.11.2014");
        period.setSetableTo("02.12.2014");
        assertEquals("01.12.", period.getSetableFrom());
    }

    @Test
    public void testPeriodSetableToShort() throws IOException, ParseException {
        Period period = new Period();

        period.setSetableFrom("01.10.");
        period.setSetableTo("02.10.");
        assertEquals("02.10.", period.getSetableTo());

        period.setSetableFrom("30.11.");
        period.setSetableTo("31.11.");
        assertEquals("01.12.", period.getSetableTo());
    }

    @Test
    public void testPeriodSetableToLong() throws IOException, ParseException {
        Period period = new Period();

        period.setSetableFrom("01.10.2014");
        period.setSetableTo("01.10.2014");
        assertEquals("01.10.", period.getSetableTo());

        period.setSetableFrom("30.11.2014");
        period.setSetableTo("31.11.2014");
        assertEquals("01.12.", period.getSetableTo());
    }

    @Test
    public void testPeriodLectureEndShort() throws IOException, ParseException {
        Period period = new Period();

        period.setLectureEnd("01.10.");
        assertEquals("01.10.", period.getLectureEnd());

        period.setLectureEnd("31.11.");
        assertEquals("01.12.", period.getLectureEnd());
    }

    @Test
    public void testPeriodLectureEndLong() throws IOException, ParseException {
        Period period = new Period();

        period.setLectureEnd("01.10.2014");
        assertEquals("01.10.", period.getLectureEnd());

        period.setLectureEnd("31.11.2014");
        assertEquals("01.12.", period.getLectureEnd());
    }

    @Test
    public void testPeriodTransform() throws IOException {
        Period period = new Period();

        period.setMatchingLocation(".*");
        period.setFrom("01.10.");
        period.setTo("31.03.");

        period.setSetableFrom("21.07.");
        period.setSetableTo("02.02.");

        period.setLectureEnd("02.03.");

        Document p = new EntityFactory<>(period).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(p, System.out);
        assertNotNull(p);
    }

    @Test
    public void testPeriodFQTransform() throws IOException, ParseException {
        Period period = new Period();

        period.setMatchingLocation(".*");
        period.setFullyQualified(true);

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(new Date());

        final int year = cal.get(Calendar.YEAR);

        final Date base = PERIOD_FORMAT.parse("30.11." + year);

        period.setBaseDate(base);

        period.setFrom("01.10.");
        period.setTo("31.03.");

        period.setSetableFrom("21.07.");
        period.setSetableTo("02.02.");

        period.setLectureEnd("02.03.");

        Document p = new EntityFactory<>(period).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(p, System.out);
        assertNotNull(p);
    }
}
