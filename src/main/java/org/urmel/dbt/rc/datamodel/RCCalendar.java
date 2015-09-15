/*
 * $Id: RCCalendar.java 2134 2014-12-08 14:37:17Z adler $ 
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRFileContent;
import org.mycore.common.content.MCRSourceContent;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.urmel.dbt.rc.utils.RCCalendarTransformer;
import org.xml.sax.SAXException;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "calendar")
@XmlAccessorType(XmlAccessType.NONE)
public final class RCCalendar implements Serializable, Iterable<Period> {

    static final String RESOURCE_URI = "resource:rccalendar.xml";

    static final String URI_CFG_KEY = "DBT.RC.Calendar.URI";

    private static final long serialVersionUID = -812825621316872737L;

    private static final Logger LOGGER = LogManager.getLogger(RCCalendar.class);

    private static RCCalendar singleton;

    private static URI calendarURI;

    private static File calendarFile;

    private List<Period> periods;

    static {
        MCRConfiguration config = MCRConfiguration.instance();
        String dataDirProperty = "MCR.datadir";
        String dataDir = config.getString(dataDirProperty, null);
        if (dataDir == null) {
            LOGGER.warn(dataDirProperty + " is undefined.");
            try {
                calendarURI = new URI(config.getString(URI_CFG_KEY, RESOURCE_URI));
            } catch (URISyntaxException e) {
                throw new MCRException(e);
            }
        } else {
            File dataDirFile = new File(dataDir);
            String calendarCfg = config.getString(URI_CFG_KEY, dataDirFile.toURI().toString() + "rccalendar.xml");
            try {
                calendarURI = new URI(calendarCfg);
                LOGGER.info("Using rc calendar defined in " + calendarURI);
                if ("file".equals(calendarURI.getScheme())) {
                    calendarFile = new File(calendarURI);
                    LOGGER.info("Loading rc calendar from file: " + calendarFile);
                } else {
                    LOGGER.info("Try loading rc calendar with URIResolver for scheme " + calendarURI.toString());
                }
            } catch (URISyntaxException e) {
                throw new MCRException(e);
            }
        }
    }

    private RCCalendar() {
    }

    /**
     * Returns a {@link RCCalendar} instance.
     * 
     * @return a instance of {@link RCCalendar}
     */
    public static RCCalendar instance() {
        if (singleton == null) {
            Element xml;
            try {
                xml = getCalendar().getRootElement();
                if (xml != null) {
                    singleton = RCCalendarTransformer.buildRCCalendar(xml);
                }
            } catch (JDOMException | TransformerException | SAXException | IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return singleton;
    }

    private static Document getCalendar() throws JDOMException, TransformerException, SAXException, IOException {
        if (calendarFile == null) {
            return MCRSourceContent.getInstance(calendarURI.toASCIIString()).asXML();
        }
        if (!calendarFile.exists() || calendarFile.length() == 0) {
            LOGGER.info("Creating " + calendarFile.getAbsolutePath() + "...");
            MCRSourceContent realmsContent = MCRSourceContent.getInstance(RESOURCE_URI);
            realmsContent.sendTo(calendarFile);
        }
        return MCRXMLParserFactory.getNonValidatingParser().parseXML(new MCRFileContent(calendarFile));
    }

    /**
     * @return the periods
     */
    @XmlElement(name = "period")
    public List<Period> getPeriods() {
        return periods;
    }

    /**
     * Tries to obtain a {@link Period}, which was defined inside RC periods, by
     * calculating <b>from</b> and <b>to</b> dates with the use of target date.
     * 
     * @param areaCode
     *            specific area code for the period
     * @param date
     *            date which is inside the period to search for
     * @return the targeting {@link Period} if one could be calculated,
     *         <code>null</code> otherwise
     */
    public static Period getPeriod(final String areaCode, final Date date) {
        try {
            for (Period period : instance().iterable(areaCode)) {
                if (period.getFromDate(date) != null && period.getToDate(date) != null) {
                    final Period p = period.clone();
                    p.setStartDate(date);
                    p.setFullyQualified(true);
                    return p;
                }
            }
        } catch (final Throwable e) {
            // Period is NULL
            LOGGER.info("no period given for date " + date);
        }

        return null;
    }

    /**
     * Returns a list of setable {@link Period} for given <code>areaCode</code>, <code>date</code> and 
     * <code>numNext</code> (number of next periods).
     * 
     * @param areaCode specific area code for the period
     * @param date date as starting point which should return period(s)
     * @param numNext number of periods after starting one
     * @return a list of {@link Period}
     */
    public static RCCalendar getPeriodList(final String areaCode, final Date date, int numNext) {
        return getPeriodList(areaCode, date, true, numNext);
    }

    /**
     * Returns a list of {@link Period} for given <code>areaCode</code>, <code>date</code> and 
     * <code>numNext</code> (number of next periods).
     * 
     * @param areaCode specific area code for the period
     * @param date date as starting point which should return period(s)
     * @param onlySetable if <code>true</code> only setable periods are listed  
     * @param numNext number of periods after first setable one
     * @return a list of {@link Period}
     */
    public static RCCalendar getPeriodList(final String areaCode, final Date date, boolean onlySetable, int numNext) {
        try {
            final Iterable<Period> periods = instance().iterable(areaCode);

            final RCCalendar calendar = new RCCalendar();
            calendar.periods = new ArrayList<Period>();

            Date lastDate = date;

            int pos = 0;
            while (pos < numNext + 1) {
                for (Period period : periods) {
                    period.setBaseDate(lastDate);
                    if (!onlySetable && period.getFromDate(lastDate) == null && period.getToDate(lastDate) == null) {
                        continue;
                    }

                    if (pos < numNext + 1 && ((period.isSetable(lastDate) && onlySetable) || (!onlySetable && (period
                            .isSetable(lastDate)
                            || (period.getFromDate(lastDate) != null && period.getToDate(lastDate) != null))))) {
                        final Period p = period.clone();

                        if (onlySetable) {
                            p.setStartDate(lastDate);
                        }

                        p.setFullyQualified(true);

                        final Calendar nextDay = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                        nextDay.setTime(p.getToDate());
                        nextDay.add(Calendar.DATE, 1);

                        lastDate = nextDay.getTime();

                        boolean setAble = p.isSetable();

                        if (setAble && onlySetable || !onlySetable) {
                            calendar.periods.add(p);
                        }

                        if (setAble) {
                            pos++;
                        }
                    }
                }
            }

            return calendar;
        } catch (final Throwable e) {
            // Period is NULL
            LOGGER.info("no periods given for date " + date);
        }

        return null;
    }

    /**
     * Tries to obtain a {@link Period}, which was defined inside RC periods, by
     * calculating <b>setableFrom</b> and <b>setableTo</b> dates with the use of target date.
     * 
     * @param areaCode
     *            specific area code for the period
     * @param date
     *            date which is inside the period to search for
     * @return the targeting {@link Period} if one could be calculated,
     *         <code>null</code> otherwise
     */
    public static Period getPeriodBySetable(final String areaCode, final Date date) {
        try {
            for (Period period : instance().iterable(areaCode)) {
                if (period.getSetableFromDate(date) != null && period.getSetableToDate(date) != null) {
                    final Period p = period.clone();
                    p.setStartDate(date);
                    p.setFullyQualified(true);
                    return p;
                }
            }
        } catch (final Throwable e) {
            // Period is NULL
            LOGGER.info("no period given for date " + date);
        }

        return null;
    }

    /**
     * @param periods the periods to set
     */
    protected void setPeriods(final List<Period> periods) {
        this.periods = periods;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Period> iterator() {
        return periods.iterator();
    }

    /**
     * Returns an iterator over a set of elements of type {@link Period} witch match the given location.
     * 
     * @param areaCode the which location
     * @return the iterator
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Period> iterator(final String areaCode) {
        return new Iterator<Period>() {
            Iterator<Period> it = periods.iterator();

            Period next = null;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }

                while (it.hasNext()) {
                    final Period period = it.next();
                    if (period.getMatchingLocation() == null || areaCode.matches(period.getMatchingLocation())) {
                        next = period;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public Period next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    final Period period = next;
                    next = null;
                    return period;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Implementing this interface allows an object to be the target of the "foreach" statement with given location.
     * 
     * @param areaCode the which location
     * @return the iterable
     * 
     * @see java.lang.Iterable
     */
    public Iterable<Period> iterable(final String areaCode) {
        return new Iterable<Period>() {
            public Iterator<Period> iterator() {
                return RCCalendar.this.iterator(areaCode);
            }
        };
    }
}
