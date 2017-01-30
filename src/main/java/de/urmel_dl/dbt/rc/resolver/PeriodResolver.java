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
package de.urmel_dl.dbt.rc.resolver;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;

import de.urmel_dl.dbt.rc.datamodel.Period;
import de.urmel_dl.dbt.rc.datamodel.RCCalendar;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * This resolver can be used to resolve the Period from given
 * <code>areaCode</code> with an optional date string or "now" for
 * the current date.
 * <br>
 * <br>
 * Syntax:
 * <ul>
 * <li><code>period:areacode=areaCode[&amp;date={now|31.12.2011}]</code> get (set able) period for given date</li>
 * <li><code>period:areacode=areaCode[&amp;date={now|31.12.2011}][&amp;fq=true]</code> get (fq = full qualified) period for given date</li>
 * <li><code>period:areacode=areaCode[&amp;date={now|31.12.2011}][&amp;list=true][&amp;onlySetable=true][&amp;numnext=1]</code> get periods (+ next) for given date</li>
 * </ul>
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class PeriodResolver implements URIResolver {

    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        try {
            final DateFormat parser = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);

            final String options = href.substring(href.indexOf(":") + 1);
            final HashMap<String, String> params = new HashMap<>();
            String[] param;
            final StringTokenizer tok = new StringTokenizer(options, "&");
            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                if (param.length == 1) {
                    params.put(param[0], "");
                } else {
                    params.put(param[0], param[1]);
                }
            }

            final String areaCode = params.get("areacode");
            final String dateStr = params.get("date") != null ? params.get("date") : "now";
            boolean fq = params.get("fq") != null ? Boolean.parseBoolean(params.get("fq")) : false;
            boolean list = params.get("list") != null ? Boolean.parseBoolean(params.get("list")) : false;
            boolean onlySetable = params.get("onlySetable") != null ? Boolean.parseBoolean(params.get("onlySetable"))
                : true;
            int numNext = params.get("numnext") != null ? Integer.parseInt(params.get("numnext")) : 1;

            Date date = new Date();
            if (!"now".equalsIgnoreCase(dateStr)) {
                try {
                    date = parser.parse(dateStr);
                } catch (ParseException pe) {
                    date = new Date();
                }
            }

            if (!list) {
                final Period period = fq ? RCCalendar.getPeriod(areaCode, date)
                    : RCCalendar.getPeriodBySetable(areaCode, date);
                return new JDOMSource(new EntityFactory<>(period).toDocument());
            } else {
                final RCCalendar calendar = RCCalendar.getPeriodList(areaCode, date, onlySetable, numNext);
                return new JDOMSource(new EntityFactory<>(calendar).toDocument());
            }
        } catch (final Exception ex) {
            throw new TransformerException("Exception resolving " + href, ex);
        }
    }
}
