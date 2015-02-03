/*
 * $Id: SlotEntryTypesResolver.java 2134 2014-12-08 14:37:17Z adler $ 
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
package org.urmel.dbt.rc.resolver;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;
import org.urmel.dbt.rc.datamodel.slot.SlotEntryTypes;
import org.urmel.dbt.rc.utils.SlotEntryTypesTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class SlotEntryTypesResolver implements URIResolver {

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        final String options = href.substring(href.indexOf(":") + 1);
        final HashMap<String, String> params = new HashMap<String, String>();
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

        final SlotEntryTypes entryTypes = SlotEntryTypes.instance();

        return new JDOMSource(SlotEntryTypesTransformer.buildExportableXML(entryTypes));
    }

}
