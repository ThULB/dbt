/*
 * $Id$ 
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
package org.urmel.dbt.resolver;

import java.lang.reflect.Method;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class EnumResolver implements URIResolver {

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            String clsName = href.split(":")[1];

            this.getClass();
            Class<?> cls = Class.forName(clsName);
            if (cls.isEnum()) {
                Element root = new Element("enum");
                root.setAttribute("name", cls.getSimpleName());
                for (Object obj : cls.getEnumConstants()) {
                    Method m = obj.getClass().getDeclaredMethod("value");
                    Element elm = new Element("value");
                    elm.setText((String) m.invoke(obj));
                    root.addContent(elm);
                }

                return new JDOMSource(root);
            }

            return null;
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }

}
