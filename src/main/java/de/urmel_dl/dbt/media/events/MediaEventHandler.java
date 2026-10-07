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
package de.urmel_dl.dbt.media.events;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mods.MCRMODSWrapper;

import de.urmel_dl.dbt.media.MediaService;

/**
 * @author René Adler (eagle)
 *
 */
public class MediaEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger();

    /** DBT holds language codes of both RFCs, they are equal for simple languages like en. */
    private static final String LANGUAGE_TERM_XPATH = "mods:language/mods:languageTerm[@type='code']"
        + "[@authority='rfc5646' or @authority='rfc4646']";

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathUpdated(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }
        // one task, so the old files are always deleted before the new subtitle is imported
        MCRSessionMgr.getCurrentSession().onCommit(() -> {
            deleteFile(MCRPath.ofPath(path));
            handleFile(MCRPath.ofPath(path), 0);
        });
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathDeleted(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }

        MCRSessionMgr.getCurrentSession().onCommit(() -> deleteFile(MCRPath.ofPath(path)));
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handlePathCreated(org.mycore.common.events.MCREvent, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        if (!(path instanceof MCRPath)) {
            return;
        }
        MCRSessionMgr.getCurrentSession().onCommit(() -> handleFile(MCRPath.ofPath(path), 10));
    }

    private void deleteFile(MCRPath path) {
        try {
            if (MediaService.isSubtitleSupported(path)) {
                Optional<Path> mediaFile = MediaService.findMediaFile(path);

                if (mediaFile.isPresent()) {
                    MediaService.deleteImportedSubtitleFile(internalMediaId(path, mediaFile.get()));
                }

                return;
            }

            String id = internalMediaId(path, path);

            // an imported subtitle can exist even if the media file was never encoded
            MediaService.deleteImportedSubtitleFile(id);

            if (MediaService.hasMediaFiles(id)) {
                MediaService.deleteMediaFiles(id);
            }
        } catch (IOException | RuntimeException e) {
            // the file itself was deleted fine, so a failing cleanup must not fail the request
            LOGGER.error("Could not delete media files or subtitle of {}.", path, e);
        }
    }

    private void handleFile(MCRPath path, int priority) {
        try {
            if (MediaService.isMediaSupported(path)) {

                MediaService.encodeMediaFile( mediaId(path, path), path, priority);

                // the subtitle may have been uploaded before the media file
                Optional<Path> subtitleFile = MediaService.findSubtitleFile(path);

                if (subtitleFile.isPresent()) {
                    MediaService.importSubtitleFile(internalMediaId(path, path), subtitleFile.get(),
                        language(path));
                }
            } else if (MediaService.isSubtitleSupported(path)) {
                Optional<Path> mediaFile = MediaService.findMediaFile(path);

                if (mediaFile.isPresent()) {
                    MediaService.importSubtitleFile(internalMediaId(path, mediaFile.get()), path, language(path));
                }
            }
        } catch (IOException | RuntimeException e) {
            // the file itself was stored fine, so a failing encoding or import must not fail the upload
            LOGGER.error("Could not handle media file or subtitle {}.", path, e);
        }
    }

    /**
     * Returns the language code of the object, that owns the derivate of given path
     */
    private static String language(MCRPath path) {
        try {
            MCRObjectID objectId = MCRMetadataManager
                .retrieveMCRDerivate(MCRObjectID.getInstance(path.getOwner())).getOwnerID();
            Element term = new MCRMODSWrapper(MCRMetadataManager.retrieveMCRObject(objectId))
                .getElement(LANGUAGE_TERM_XPATH);

            return term != null ? term.getTextTrim() : null;
        } catch (RuntimeException e) {
            // a subtitle without language is still better than no subtitle
            LOGGER.warn("Could not read the language of {}, the subtitle stays unlabelled.", path, e);
            return null;
        }
    }

    /**
     * Returns the media id of given media file, e.g. <code>dbt_derivate_00000001_video.mp4</code>.
     * It's used by the encoder service as job id. The derivate is taken from <code>path</code>, so
     * a sibling file can be passed too.
     */
    private static String mediaId(MCRPath path, Path mediaFile) {
        return path.getOwner() + "_" + mediaFile.getFileName().toString();
    }

    /**
     * Returns the internal media id of given media file, that is used to address the media, thumb
     * and subtitle store.
     */
    private static String internalMediaId(MCRPath path, Path mediaFile) {
        return MediaService.buildInternalId(mediaId(path, mediaFile));
    }

}
