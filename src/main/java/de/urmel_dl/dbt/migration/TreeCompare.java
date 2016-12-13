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

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TreeCompare implements FileVisitor<Path> {

    private static final Logger LOGGER = Logger.getLogger(TreeCompare.class);

    private final Path source;

    private final Path target;

    public TreeCompare(Path source, Path target) throws NoSuchFileException {
        if (Files.notExists(target)) {
            throw new NoSuchFileException(target.toString(), null, "Target directory does not exist.");
        }
        this.source = source;
        this.target = target;
    }

    /* (non-Javadoc)
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path newdir = target.resolve(toTargetFS(source.relativize(dir)));
        if (!Files.exists(newdir)) {
            LOGGER.error(newdir.toString() + " directory is missing.");
        }
        return FileVisitResult.CONTINUE;
    }

    /* (non-Javadoc)
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path targetFile = target.resolve(toTargetFS(source.relativize(file)));

        if (!Files.exists(targetFile)) {
            LOGGER.error(targetFile.toString() + " file is missing.");
        } else {
            BasicFileAttributes tattrs = Files.readAttributes(targetFile, BasicFileAttributes.class);
            if (attrs.size() != tattrs.size()) {
                LOGGER.error("Size of " + file.toString() + " and " + targetFile.toString() + " differs.");
            }
        }

        return FileVisitResult.CONTINUE;
    }

    /* (non-Javadoc)
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc instanceof FileSystemLoopException) {
            LOGGER.error("cycle detected: " + file);
        } else {
            LOGGER.error("Unable to copy: " + file, exc);
        }
        return FileVisitResult.CONTINUE;
    }

    /* (non-Javadoc)
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private Path toTargetFS(Path source) {
        if (target.getFileSystem().equals(source.getFileSystem())) {
            return source;
        }
        String[] nameParts = new String[source.getNameCount() - 1];
        for (int i = 0; i < nameParts.length; i++) {
            nameParts[i] = source.getName(i + 1).toString();
        }
        return target.getFileSystem().getPath(source.getName(0).toString(), nameParts);
    }

}
