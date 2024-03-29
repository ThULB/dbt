/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2017
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
package de.urmel_dl.dbt.commandline;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.datamodel.niofs.utils.MCRFileCollectingFileVisitor;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * The Class RepairCommands.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@MCRCommandGroup(name = "Repair Commands")
public class RepairCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * repair institution fields.
     *
     * @return the list
     */
    @MCRCommand(syntax = "repair institution fields",
        help = "repairs all mods:name with @valueURI but without @authorityURI")
    public static List<String> updateInstitutions() {
        URL styleFile = RepairCommands.class.getResource("/xsl/repair/repair-institutions.xsl");
        if (styleFile == null) {
            LOGGER.error("Could not find migration stylesheet. File a bug!");
            return null;
        }
        TreeSet<String> ids = new TreeSet<>(MCRXMLMetadataManager.instance().listIDsOfType("mods"));
        ArrayList<String> cmds = new ArrayList<>(ids.size());
        for (String id : ids) {
            cmds.add("xslt " + id + " with file " + styleFile.toString());
        }
        return cmds;
    }

    @MCRCommand(syntax = "set main file on first derivate",
        help = "set the main file on first derviate with only one file")
    public static void setMainFile() {
        TreeSet<String> ids = new TreeSet<>(MCRXMLMetadataManager.instance().listIDsOfType("mods"));
        ids.forEach(id -> {
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
            Optional.ofNullable(obj.getStructure().getDerivates())
                .ifPresent(ol -> ol.stream().filter(l -> l != null).findFirst().ifPresent(d -> {
                    String derid = d.getXLinkHref();
                    MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derid));
                    String mainFile = der.getDerivate().getInternals().getMainDoc();
                    LOGGER.info("Check derivate {} ({})", derid, mainFile);
                    if (mainFile == null || mainFile.isEmpty()) {
                        MCRPath rootPath = MCRPath.getPath(derid, "/");
                        try {
                            DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, "*.*");
                            List<Path> files = new ArrayList<>();
                            stream.forEach(f -> {
                                if (!Files.isDirectory(f)) {
                                    files.add(f);
                                }
                            });
                            if (!files.isEmpty() && files.size() == 1) {
                                Path file = files.get(0);
                                LOGGER.info("set main file {} on derivate {}", file.getFileName().toString(), derid);
                                der.getDerivate().getInternals().setMainDoc(file.getFileName().toString());
                                MCRMetadataManager.update(der);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        } catch (MCRAccessException | MCRPersistenceException e) {
                            throw new MCRException(e);
                        }
                    }
                }));
        });
    }

    @MCRCommand(syntax = "set main file on derivates {0} if missing",
        help = "set the main file on derviate {0} when this is missing")
    public static void setMainFileIfMissing(String derid) throws MCRAccessException {
        MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derid));
        String mainFile = der.getDerivate().getInternals().getMainDoc();
        LOGGER.info("Check derivate {} ({})", derid, mainFile);
        if (mainFile == null || mainFile.isEmpty()) {
            Optional<String> defaultMainFile = getDefaultMainFile(der);
            if (defaultMainFile.isPresent()) {
                MCRDerivateCommands.setMainFile(der.getId().toString(), defaultMainFile.get());
            }
        }
    }

    private static Optional<String> getDefaultMainFile(MCRDerivate derivate) {
        MCRPath path = MCRPath.getPath(derivate.getId().toString(), "/");
        List<String> ignoreMainfileList = MCRConfiguration2.getString("MCR.Upload.NotPreferredFiletypeForMainfile")
            .map(MCRConfiguration2::splitValue)
            .map(s -> s.collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
        try {
            MCRFileCollectingFileVisitor<Path> visitor = new MCRFileCollectingFileVisitor<>();
            Files.walkFileTree(path, visitor);

            //sort files by name
            ArrayList<java.nio.file.Path> paths = visitor.getPaths();
            paths.sort(Comparator.comparing(java.nio.file.Path::getNameCount)
                .thenComparing(java.nio.file.Path::getFileName));
            //extract first file, before filtering
            MCRPath firstPath = MCRPath.toMCRPath(paths.get(0));

            //filter files, remove files that should be ignored for mainfile
            return paths.stream()
                .map(MCRPath.class::cast)
                .filter(p -> ignoreMainfileList.stream().noneMatch(p.getOwnerRelativePath()::endsWith))
                .findFirst()
                .or(() -> Optional.of(firstPath))
                .map(MCRPath::getOwnerRelativePath);
        } catch (IOException e) {
            LOGGER.error("Could not get main file!", e);
        }
        return Optional.empty();
    }

}
