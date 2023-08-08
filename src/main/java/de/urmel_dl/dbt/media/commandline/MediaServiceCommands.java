/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
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
 */
package de.urmel_dl.dbt.media.commandline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

import de.urmel_dl.dbt.media.MediaService;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "Media Service Commands")
public class MediaServiceCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger();

    @MCRCommand(syntax = "encode all media files of derivates",
        help = "encode all media files of derivates with a supported media type",
        order = 10)
    public static List<String> encodeAll() {
        return forAllDerivates("encode all media files of derivate {0}");
    }

    @MCRCommand(syntax = "force encode all media files of derivates",
        help = "force encode all media files of derivates with a supported media type",
        order = 20)
    public static List<String> forceEncodeAll() {
        return forAllDerivates("force encode all media files of derivate {0}");
    }

    @MCRCommand(syntax = "encode all media files of derivate {0}",
        help = "encode all media files of derivate {0} with a supported media type",
        order = 11)
    public static List<String> encodeMediaFilesOfDerivate(String derivateId) throws IOException {
        MCRPath derivateRoot = MCRPath.getPath(derivateId, "/");

        if (!Files.exists(derivateRoot)) {
            throw new MCRException("Derivate " + derivateId + " does not exist or is not a directory!");
        }

        return Files.walk(derivateRoot).filter(f -> !f.equals(derivateRoot) && MediaService.isMediaSupported(f))
            .map(f -> new MessageFormat("encode media file {0} of derivate {1}", Locale.ROOT)
                .format(new Object[] { f.getFileName().toString(), derivateId }))
            .collect(Collectors.toList());
    }

    @MCRCommand(syntax = "force encode all media files of derivate {0}",
        help = "force encode all media files of derivate {0} with a supported media type",
        order = 21)
    public static List<String> forceEncodeMediaFilesOfDerivate(String derivateId) throws IOException {
        MCRPath derivateRoot = MCRPath.getPath(derivateId, "/");

        if (!Files.exists(derivateRoot)) {
            throw new MCRException("Derivate " + derivateId + " does not exist or is not a directory!");
        }

        return Files.walk(derivateRoot).filter(f -> !f.equals(derivateRoot) && MediaService.isMediaSupported(f))
            .map(f -> new MessageFormat("force encode media file {0} of derivate {1}", Locale.ROOT)
                .format(new Object[] { f.getFileName().toString(), derivateId }))
            .collect(Collectors.toList());
    }

    @MCRCommand(syntax = "encode media file {1} of derivate {0}",
        help = "encode media file {1} of derivate {0}",
        order = 1)
    public static void encodeMediaFileOfDerivate(String derivateId, String fileName) {
        encodeMediaFileOfDerivate(derivateId, fileName, false);
    }

    @MCRCommand(syntax = "force encode media file {1} of derivate {0}",
        help = "force encode media file {1} of derivate {0}",
        order = 2)
    public static void forceEncodeMediaFileOfDerivate(String derivateId, String fileName) {
        encodeMediaFileOfDerivate(derivateId, fileName, true);
    }

    private static void encodeMediaFileOfDerivate(String derivateId, String fileName, boolean force) {
        MCRPath derivateRoot = MCRPath.getPath(derivateId, "/");

        if (!Files.exists(derivateRoot)) {
            throw new MCRException("Derivate " + derivateId + " does not exist or is not a directory!");
        }

        Path mediaFile = derivateRoot.resolve(fileName);
        if (Files.notExists(mediaFile)) {
            throw new MCRException("File " + fileName + " not found in derivate " + derivateId + "!");
        }

        if (!MediaService.isMediaSupported(mediaFile)) {
            LOGGER.info("Skipping encoding of " + fileName + ", because isn't supported.");
            return;
        }

        if (!force
            && MediaService.hasMediaFiles(MediaService.buildInternalId(derivateRoot.getOwner() + "_" + fileName))) {
            LOGGER.info("Skipping encoding of " + fileName + ", because it's already encoded.");
            return;
        }

        MediaService.encodeMediaFile(derivateRoot.getOwner() + "_" + fileName, mediaFile, 0);
    }

    private static List<String> forAllDerivates(String batchCommandSyntax) {
        List<String> ids = MCRXMLMetadataManager.instance().listIDsOfType("derivate");
        List<String> cmds = new ArrayList<>(ids.size());

        ids.stream().sorted(Collections.reverseOrder())
            .forEach(id -> cmds.add(new MessageFormat(batchCommandSyntax, Locale.ROOT).format(new Object[] { id })));

        return cmds;
    }
}
