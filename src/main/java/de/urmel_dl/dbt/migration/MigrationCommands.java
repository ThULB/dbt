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
package de.urmel_dl.dbt.migration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.datamodel.niofs.utils.MCRTreeCopier;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.xml.sax.SAXParseException;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "Migration Commands")
public class MigrationCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = Logger.getLogger(MigrationCommands.class);

    @MCRCommand(syntax = "fix objects for base {0} with file {1}", help = "transforms all mycore objects for base {0} with the given file or URL {1}")
    public static List<String> xsltObjects(final String base, final String xslFile) throws Exception {
        URL styleFile = MigrationCommands.class.getResource("/xsl/" + xslFile);
        if (styleFile == null) {
            final File file = new File(xslFile);

            if (!file.exists()) {
                LOGGER.error("Could not find the stylesheet \"" + xslFile + "\".");
                return null;
            }

            styleFile = file.toURI().toURL();
        }

        List<String> cmds = new ArrayList<String>();

        MCRMetadataStore store = MCRXMLMetadataManager.instance().getStore(base);
        Iterator<Integer> IDs = store.listIDs(true);
        while (IDs.hasNext()) {
            final String id = MCRObjectID.formatID(base, IDs.next());
            cmds.add(new MessageFormat("xslt {0} with file {1}", Locale.ROOT)
                .format(new Object[] { id, styleFile.toString() }));
        }

        return cmds;
    }

    @MCRCommand(syntax = "repair derivate from file {0}", help = "try to repair a derivate from given file {0}")
    public static boolean repairDerivate(final String from)
        throws SAXParseException, IOException, MCRPersistenceException, MCRAccessException {
        File file = new File(from);

        if (!file.getName().endsWith(".xml")) {
            LOGGER.warn(file + " ignored, does not end with *.xml");
            return false;
        }

        if (!file.isFile()) {
            LOGGER.warn(file + " ignored, is not a file.");
            return false;
        }

        LOGGER.info("Reading file " + file + " ...");

        MCRDerivate derivate = new MCRDerivate(file.toURI());
        derivate.setImportMode(true);

        // Replace relative path with absolute path of files
        if (derivate.getDerivate().getInternals() != null) {
            String path = derivate.getDerivate().getInternals().getSourcePath();
            path = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            if (path.trim().length() <= 1) {
                // the path is the path name plus the name of the derivate -
                path = derivate.getId().toString();
            }
            File sPath = new File(path);

            if (!sPath.isAbsolute()) {
                // only change path to absolute path when relative
                String prefix = file.getParent();

                if (prefix != null) {
                    path = prefix + File.separator + path;
                }
            }

            derivate.getDerivate().getInternals().setSourcePath(path);
            LOGGER.info("Source path --> " + path);
        }

        LOGGER.info("Label --> " + derivate.getLabel());

        repairDerivate(derivate);

        return true;
    }

    private static void repairDerivate(final MCRDerivate mcrDerivate)
        throws MCRPersistenceException, IOException {
        if (!mcrDerivate.isValid()) {
            throw new MCRPersistenceException("The derivate " + mcrDerivate.getId() + " is not valid.");
        }
        final MCRObjectID objid = mcrDerivate.getDerivate().getMetaLink().getXLinkHrefID();

        // prepare the derivate metadata and store under the XML table
        if (mcrDerivate.getService().getDate("createdate") == null || !mcrDerivate.isImportMode()) {
            mcrDerivate.getService().setDate("createdate");
        }
        if (mcrDerivate.getService().getDate("modifydate") == null || !mcrDerivate.isImportMode()) {
            mcrDerivate.getService().setDate("modifydate");
        }

        // handle events
        try {
            LOGGER.info("Fire create event.");
            fireEvent(mcrDerivate, null, MCREvent.CREATE_EVENT);
        } catch (MCRException ex) {
            LOGGER.info("Fire update event.");
            fireEvent(mcrDerivate, null, MCREvent.UPDATE_EVENT);
        }

        // add the link to metadata
        final MCRMetaLinkID der = new MCRMetaLinkID();
        der.setReference(mcrDerivate.getId().toString(), null, mcrDerivate.getLabel());
        der.setSubTag("derobject");

        try {
            LOGGER.debug("adding Derivate in data store");
            MCRMetadataManager.addOrUpdateDerivateToObject(objid, der);
        } catch (final Exception e) {
            // throw final exception
            throw new MCRPersistenceException("Error while creatlink to MCRObject " + objid + ".", e);
        }

        // create data in IFS
        if (mcrDerivate.getDerivate().getInternals() != null) {
            MCRObjectID derId = mcrDerivate.getId();
            MCRPath rootPath = MCRPath.getPath(derId.toString(), "/");
            if (mcrDerivate.getDerivate().getInternals().getSourcePath() == null) {
                rootPath.getFileSystem().createRoot(rootPath.getOwner());
                BasicFileAttributes attrs = Files.readAttributes(rootPath, BasicFileAttributes.class);
                if (!(attrs.fileKey() instanceof String)) {
                    throw new MCRPersistenceException(
                        "Cannot get ID from newely created directory, as it is not a String." + rootPath);
                }
                mcrDerivate.getDerivate().getInternals().setIFSID(attrs.fileKey().toString());
            } else {
                final String sourcepath = mcrDerivate.getDerivate().getInternals().getSourcePath();
                final File f = new File(sourcepath);
                if (f.exists()) {
                    try {
                        LOGGER.debug("Starting File-Import");
                        importDerivate(derId.toString(), f.toPath());
                        BasicFileAttributes attrs = Files.readAttributes(rootPath, BasicFileAttributes.class);
                        if (!(attrs.fileKey() instanceof String)) {
                            throw new MCRPersistenceException(
                                "Cannot get ID from newely created directory, as it is not a String." + rootPath);
                        }
                        mcrDerivate.getDerivate().getInternals().setIFSID(attrs.fileKey().toString());
                    } catch (final Exception e) {
                        throw new MCRPersistenceException("Can't add derivate to the IFS", e);
                    }
                } else {
                    LOGGER.warn("Empty derivate, the File or Directory -->" + sourcepath + "<--  was not found.");
                }
            }
        }
    }

    private static void importDerivate(String derivateID, Path sourceDir) throws NoSuchFileException, IOException {
        MCRPath rootPath = MCRPath.getPath(derivateID, "/");
        if (Files.exists(rootPath)) {
            LOGGER.info("Derivate does already exist: " + derivateID);
        } else {
            rootPath.getFileSystem().createRoot(derivateID);
        }
        Files.walkFileTree(sourceDir, new MCRTreeCopier(sourceDir, rootPath));
    }

    private static void fireEvent(MCRBase base, MCRBase oldBase, String eventType) {
        boolean objectEvent = base instanceof MCRObject;
        String type = objectEvent ? MCREvent.OBJECT_TYPE : MCREvent.DERIVATE_TYPE;
        final MCREvent evt = new MCREvent(type, eventType);
        if (objectEvent) {
            evt.put(MCREvent.OBJECT_KEY, base);
        } else {
            evt.put(MCREvent.DERIVATE_KEY, base);
        }
        Optional.ofNullable(oldBase)
            .ifPresent(b -> evt.put(objectEvent ? MCREvent.OBJECT_OLD_KEY : MCREvent.DERIVATE_OLD_KEY, b));
        if (MCREvent.DELETE_EVENT.equals(eventType)) {
            MCREventManager.instance().handleEvent(evt, MCREventManager.BACKWARD);
        } else {
            MCREventManager.instance().handleEvent(evt);
        }
    }

    @MCRCommand(syntax = "check all derivates from directory {0}", help = "check all derivate from given directory {0} has missing files")
    public static List<String> checkDerivates(final String directory) {
        File dir = new File(directory);

        if (!dir.isDirectory()) {
            LOGGER.warn(directory + " ignored, is not a directory.");
            return null;
        }

        File[] list = dir.listFiles();

        if (list.length == 0) {
            LOGGER.warn("No files found in directory " + directory);
            return null;
        }

        List<String> cmds = new ArrayList<String>();
        for (File file : list) {
            String name = file.getName();
            if (!(name.endsWith(".xml") && name.contains("derivate"))) {
                continue;
            }
            name = name.substring(0, name.length() - 4); // remove ".xml"
            File contentDir = new File(dir, name);
            if (!(contentDir.exists() && contentDir.isDirectory())) {
                continue;
            }
            cmds.add("check derivate from file " + file.getAbsolutePath());
        }

        return cmds;
    }

    @MCRCommand(syntax = "check derivate from file {0}", help = "check derivate from given file {0} has missing files")
    public static boolean checkDerivate(final String from) throws SAXParseException, IOException {
        File file = new File(from);

        if (!file.getName().endsWith(".xml")) {
            LOGGER.warn(file + " ignored, does not end with *.xml");
            return false;
        }

        if (!file.isFile()) {
            LOGGER.warn(file + " ignored, is not a file.");
            return false;
        }

        LOGGER.info("Reading file " + file + " ...");

        MCRDerivate derivate = new MCRDerivate(file.toURI());

        // Replace relative path with absolute path of files
        if (derivate.getDerivate().getInternals() != null) {
            String path = derivate.getDerivate().getInternals().getSourcePath();
            path = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            if (path.trim().length() <= 1) {
                // the path is the path name plus the name of the derivate -
                path = derivate.getId().toString();
            }
            File sPath = new File(path);

            if (!sPath.isAbsolute()) {
                // only change path to absolute path when relative
                String prefix = file.getParent();

                if (prefix != null) {
                    path = prefix + File.separator + path;
                }
            }

            derivate.getDerivate().getInternals().setSourcePath(path);
        }

        checkDerivate(derivate);

        return true;
    }

    private static void checkDerivate(final MCRDerivate mcrDerivate) {
        if (!mcrDerivate.isValid()) {
            throw new MCRPersistenceException("The derivate " + mcrDerivate.getId() + " is not valid.");
        }

        if (mcrDerivate.getDerivate().getInternals() != null) {
            MCRObjectID derId = mcrDerivate.getId();
            MCRPath rootPath = MCRPath.getPath(derId.toString(), "/");
            if (mcrDerivate.getDerivate().getInternals().getSourcePath() != null) {
                LOGGER.info("Check derivate " + derId.toString() + "...");

                final String sourcepath = mcrDerivate.getDerivate().getInternals().getSourcePath();
                final File f = new File(sourcepath);
                if (f.exists()) {
                    try {
                        if (!Files.exists(rootPath)) {
                            LOGGER.error("Derivate does not exist: " + derId);
                            return;
                        }
                        Files.walkFileTree(f.toPath(), new TreeCompare(f.toPath(), rootPath));
                    } catch (final Exception e) {
                        LOGGER.error("Can't check derivate on IFS", e);
                    }
                }

                LOGGER.info("...done.");
            }
        }
    }
}
