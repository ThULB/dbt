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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.xml.MCRURIResolver;

import de.urmel_dl.dbt.rc.datamodel.Period;
import de.urmel_dl.dbt.rc.datamodel.RCCalendar;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * Test Case for RCCalendar.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
public class TestRCCalendar extends MCRTestCase {

    private static RCCalendar calendar;

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();

        if (calendar == null) {
            calendar = RCCalendar.instance();
        }
    }

    @Test
    public void testRCCalendarExport() throws IOException {
        Document cal = new EntityFactory<>(calendar).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(cal, System.out);
        assertNotNull(cal);
    }

    @Test
    public void testRCCalendarGetPeriod() throws IOException {
        Period period = RCCalendar.getPeriod("2700", new Date());
        assertNotNull(period);

        Document p = new EntityFactory<>(period).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(p, System.out);
        assertNotNull(p);
    }

    @Test
    public void testRCCalendarGetSetable() throws IOException {
        Period period = RCCalendar.getPeriodBySetable("2700", new Date());
        assertNotNull(period);

        Document p = new EntityFactory<>(period).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(p, System.out);
        assertNotNull(p);
    }

    @Test
    public void testPeriodResolverSingle() throws IOException {
        Element input = MCRURIResolver.instance().resolve("period:areacode=0&date=now");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);
        assertNotNull(input);
    }

    @Test
    public void testPeriodResolverList() throws IOException {
        Element input = MCRURIResolver.instance().resolve("period:areacode=0&date=now&list=true");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertTrue(Boolean.parseBoolean(input.getChildren("period").get(0).getAttributeValue("setable")));
    }

    @Test
    public void testPeriodResolverListAll() throws IOException {
        Element input = MCRURIResolver.instance()
            .resolve("period:areacode=0&date=31.03.2015&onlySetable=false&list=true");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertFalse(Boolean.parseBoolean(input.getChildren("period").get(0).getAttributeValue("setable")));
    }

    @Test
    public void testPeriodResolverListFirstSemester() throws IOException, ParseException, CloneNotSupportedException {
        RCCalendar calendar = RCCalendar.instance();
        Period p = calendar.getPeriods().get(0).clone();
        p.setStartDate(new Date());
        p.setFullyQualified(true);

        Element input = MCRURIResolver.instance()
            .resolve("period:areacode=0&date=" + p.getSetableFrom() + "&list=true");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertEquals(2, input.getChildren("period").size());
    }

    @Test
    public void testPeriodResolverListSecondSemester() throws IOException, ParseException, CloneNotSupportedException {
        RCCalendar calendar = RCCalendar.instance();
        Period p = calendar.getPeriods().get(1).clone();
        p.setStartDate(new Date());
        p.setFullyQualified(true);

        Element input = MCRURIResolver.instance()
            .resolve("period:areacode=0&date=" + p.getSetableFrom() + "&list=true");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertTrue(Boolean.parseBoolean(input.getChildren("period").get(0).getAttributeValue("setable")));
        assertEquals(2, input.getChildren("period").size());
    }

    @Test
    public void testPeriodResolverListMore() throws IOException {
        Element input = MCRURIResolver.instance().resolve("period:areacode=0&date=now&list=true&numnext=2");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertEquals(3, input.getChildren("period").size());
    }

    @Test
    public void testPeriodResolverListMoreAll() throws IOException {
        Element input = MCRURIResolver.instance()
            .resolve("period:areacode=0&date=30.09.2014&list=true&onlySetable=false&numnext=1");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);

        assertFalse(Boolean.parseBoolean(input.getChildren("period").get(0).getAttributeValue("setable")));

        int numSetable = 0;
        for (Element child : input.getChildren("period")) {
            if (Boolean.parseBoolean(child.getAttributeValue("setable"))) {
                numSetable++;
            }
        }
        assertEquals(2, numSetable);
    }
}
