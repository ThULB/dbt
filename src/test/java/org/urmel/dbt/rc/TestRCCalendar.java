/*
 * $Id: TestRCCalendar.java 2134 2014-12-08 14:37:17Z adler $ 
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
package org.urmel.dbt.rc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.xml.MCRURIResolver;
import org.urmel.dbt.rc.datamodel.Period;
import org.urmel.dbt.rc.datamodel.RCCalendar;
import org.urmel.dbt.rc.utils.PeriodTransformer;
import org.urmel.dbt.rc.utils.RCCalendarTransformer;

/**
 * Test Case for RCCalendar.
 * 
 * @author Ren\u00E9 Adler (eagle)
 */
public class TestRCCalendar extends MCRTestCase {

    private static RCCalendar calendar;

    @Before()
    public void setUp() throws Exception {
        super.setUp();

        if (calendar == null) {
            calendar = RCCalendar.instance();
        }
    }

    @Test
    public void testRCCalendarExport() throws IOException {
        new XMLOutputter(Format.getPrettyFormat()).output(RCCalendarTransformer.buildExportableXML(calendar),
            System.out);
    }

    @Test
    public void testRCCalendarGetPeriod() throws IOException {
        Period period = RCCalendar.getPeriod("2700", new Date());
        assertNotNull(period);

        new XMLOutputter(Format.getPrettyFormat()).output(PeriodTransformer.buildExportableXML(period), System.out);
    }

    @Test
    public void testRCCalendarGetSetable() throws IOException {
        Period period = RCCalendar.getPeriodBySetable("2700", new Date());
        assertNotNull(period);

        new XMLOutputter(Format.getPrettyFormat()).output(PeriodTransformer.buildExportableXML(period), System.out);
    }

    @Test
    public void testPeriodResolverSingle() throws IOException {
        Element input = MCRURIResolver.instance().resolve("period:areacode=0&date=now");
        new XMLOutputter(Format.getPrettyFormat()).output(input, System.out);
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
