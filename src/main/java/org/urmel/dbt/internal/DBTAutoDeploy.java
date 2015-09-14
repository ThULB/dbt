/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the DBT repository software.
 * Copyright (C) 2011 MyCoRe developer team
 * See http://www-db-thueringen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package org.urmel.dbt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.common.events.MCRStartupHandler;

/**
 * Default {@link ServletContextListener} for DBT.
 * 
 * @author René Adler
 * 
 */
public class DBTAutoDeploy implements MCRStartupHandler.AutoExecutable, MCRShutdownHandler.Closeable {

    private static final Logger LOGGER = LogManager.getLogger(DBTAutoDeploy.class);

    private static final String HANDLER_NAME = DBTAutoDeploy.class.getName();

    private static final String RESOURCE_DIR = "META-INF/resources";

    private static final String WEB_FRAGMENT = "META-INF/web-fragment.xml";

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public void prepareClose() {
    }

    @Override
    public void close() {
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public void startUp(final ServletContext servletContext) {
        if (servletContext != null) {
            registerServlets(servletContext);

            final String webRoot = servletContext.getRealPath("/");
            if (webRoot != null) {
                final String jarFile = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                JarFile jar;
                try {
                    LOGGER.info("Deploy DBT web resources to \"" + webRoot + "\"...");

                    jar = new JarFile(jarFile);
                    final Enumeration<JarEntry> enumEntries = jar.entries();
                    while (enumEntries.hasMoreElements()) {
                        final JarEntry file = (JarEntry) enumEntries.nextElement();
                        if (file.getName().startsWith(RESOURCE_DIR)) {
                            final String fileName = file.getName().substring(RESOURCE_DIR.length());
                            LOGGER.debug("...deploy " + fileName);

                            final File f = new File(webRoot + File.separator + fileName);
                            if (file.isDirectory()) {
                                f.mkdir();
                                continue;
                            }

                            final InputStream is = jar.getInputStream(file);
                            final FileOutputStream fos = new FileOutputStream(f);
                            while (is.available() > 0) {
                                fos.write(is.read());
                            }
                            fos.close();
                            is.close();
                        }
                    }
                    LOGGER.info("...done.");
                } catch (final IOException e) {
                    LOGGER.error("Couldn't parse JAR!");
                }
            }
        }
    }

    private void registerServlets(final ServletContext servletContext) {
        final String jarFile = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        JarFile jar;
        try {
            Document doc = null;
            jar = new JarFile(jarFile);
            final Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                final JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().equals(WEB_FRAGMENT)) {
                    final SAXBuilder builder = new SAXBuilder();

                    final InputStream is = jar.getInputStream(file);
                    doc = builder.build(is);
                    is.close();

                    break;
                }
            }

            if (doc != null) {
                final Element root = doc.getRootElement();
                final Namespace ns = root.getNamespace();
                final List<Element> servlets = root.getChildren("servlet", ns);
                final List<Element> mappings = root.getChildren("servlet-mapping", ns);

                for (Element servlet : servlets) {
                    final String name = servlet.getChildText("servlet-name", ns);
                    final String clazz = servlet.getChildText("servlet-class", ns);

                    final List<String> urlPattern = new ArrayList<String>();
                    for (Element mapping : mappings) {
                        final String n = mapping.getChildText("servlet-name", ns);
                        if (n.equals(name)) {
                            final List<Element> ups = mapping.getChildren("url-pattern", ns);
                            for (Element up : ups) {
                                urlPattern.add(up.getTextTrim());
                            }
                            break;
                        }
                    }

                    LOGGER.info("Register Servlet " + name + " (" + clazz + ")...");
                    final ServletRegistration sr = servletContext.addServlet(name, clazz);
                    if (sr != null) {
                        for (String up : urlPattern) {
                            sr.addMapping(up);
                        }
                    } else {
                        LOGGER.error("Couldn't register " + name + "!");
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
