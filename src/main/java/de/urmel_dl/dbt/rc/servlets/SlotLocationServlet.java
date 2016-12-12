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
package de.urmel_dl.dbt.rc.servlets;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotLocationServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final String LAYOUT_ELEMENT_KEY = SlotLocationServlet.class.getName() + ".layoutElement";

    private MCRCategoryDAO categoryDao;

    /* (non-Javadoc)
     * @see org.mycore.frontend.servlets.MCRServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        categoryDao = MCRCategoryDAOFactory.getInstance();
    }

    /* (non-Javadoc)
     * @see org.mycore.frontend.servlets.MCRServlet#think(org.mycore.frontend.servlets.MCRServletJob)
     */
    @Override
    protected void think(final MCRServletJob job) throws Exception {
        final HttpServletRequest request = job.getRequest();
        final String action = getProperty(request, "action");
        if ("chooseCategory".equals(action)) {
            chooseCategory(request);
        } else {
            chooseRoot(request);
        }
    }

    private void chooseRoot(final HttpServletRequest request) {
        final Element rootElement = getRootElement(request);
        rootElement.addContent(getLocationElements());
        request.setAttribute(LAYOUT_ELEMENT_KEY, new Document(rootElement));
    }

    private Collection<Element> getLocationElements() {
        final MCRCategoryID categID = MCRCategoryID.rootID(Slot.CLASSIF_ROOT_LOCATION);

        final ArrayList<Element> list = new ArrayList<Element>();
        final Element location = new Element("location");
        location.setAttribute("categID", categID.toString());
        final MCRCategory category = categoryDao.getCategory(categID, 0);

        if (category != null) {
            category.getCurrentLabel().ifPresent(label -> location.setAttribute("label", label.getText()));
            list.add(location);
        }

        return list;
    }

    private static Element getRootElement(final HttpServletRequest request) {
        final Element rootElement = new Element("locations");
        rootElement.setAttribute("queryParams", request.getQueryString());
        return rootElement;
    }

    private static void chooseCategory(final HttpServletRequest request) {
        MCRCategoryID categoryID;
        final String categID = getProperty(request, "categID");
        if (categID != null) {
            categoryID = MCRCategoryID.fromString(categID);
        } else {
            final String rootID = getProperty(request, "classID");
            categoryID = (rootID == null) ? MCRCategoryID.rootID(Slot.CLASSIF_ROOT_LOCATION)
                    : MCRCategoryID.rootID(rootID);
        }
        final Element rootElement = getRootElement(request);
        rootElement.setAttribute("classID", categoryID.getRootID());
        if (!categoryID.isRootID()) {
            rootElement.setAttribute("categID", categoryID.getID());
        }
        request.setAttribute(LAYOUT_ELEMENT_KEY, new Document(rootElement));
    }

    /* (non-Javadoc)
     * @see org.mycore.frontend.servlets.MCRServlet#render(org.mycore.frontend.servlets.MCRServletJob, java.lang.Exception)
     */
    @Override
    protected void render(final MCRServletJob job, final Exception ex) throws Exception {
        if (ex != null) {
            //do not handle error here
            throw ex;
        }
        getLayoutService().doLayout(job.getRequest(), job.getResponse(),
                new MCRJDOMContent((Document) job.getRequest().getAttribute(LAYOUT_ELEMENT_KEY)));
    }
}
