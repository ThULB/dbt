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
package de.urmel_dl.dbt.media.entity;

import java.time.Instant;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class ConverterJob.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "job")
public class ConverterJob {

    private String id;

    private String fileName;

    private List<File> files;

    private boolean running;

    private boolean done;

    private Instant addTime;

    private Instant startTime;

    private Instant endTime;

    private Integer exitValue;

    /**
     * Gets the id.
     *
     * @return the id
     */
    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the file name.
     *
     * @return the fileName
     */
    @XmlAttribute(name = "file")
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    @XmlElement(name = "files")
    public List<File> getFiles() {
        return files;
    }

    /**
     * Sets the files.
     *
     * @param files the files to set
     */
    public void setFiles(List<File> files) {
        this.files = files;
    }

    /**
     * Checks if is running.
     *
     * @return the running
     */
    @XmlAttribute(name = "running")
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the running.
     *
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Checks if is done.
     *
     * @return the done
     */
    @XmlAttribute(name = "done")
    public boolean isDone() {
        return done;
    }

    /**
     * Sets the done.
     *
     * @param done the done to set
     */
    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Gets the adds the time.
     *
     * @return the addTime
     */
    @XmlAttribute(name = "addTime")
    public Instant getAddTime() {
        return addTime;
    }

    /**
     * Sets the adds the time.
     *
     * @param addTime the addTime to set
     */
    public void setAddTime(Instant addTime) {
        this.addTime = addTime;
    }

    /**
     * Gets the start time.
     *
     * @return the startTime
     */
    @XmlAttribute(name = "startTime")
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the endTime
     */
    @XmlAttribute(name = "endTime")
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the endTime to set
     */
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the exit value.
     *
     * @return the exitValue
     */
    @XmlAttribute(name = "exitValue")
    public Integer getExitValue() {
        return exitValue;
    }

    /**
     * Sets the exit value.
     *
     * @param exitValue the exitValue to set
     */
    public void setExitValue(Integer exitValue) {
        this.exitValue = exitValue;
    }

    /**
     * The Class File.
     *
     * @author Ren\u00E9 Adler (eagle)
     */
    @XmlRootElement(name = "file")
    @XmlType(name = "ConverterJob.File")
    public static class File {

        private String fileName;

        private String format;

        private String scale;

        /**
         * Gets the file name.
         *
         * @return the fileName
         */
        @XmlAttribute(name = "name")
        public String getFileName() {
            return fileName;
        }

        /**
         * Sets the file name.
         *
         * @param fileName the fileName to set
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Gets the format.
         *
         * @return the format
         */
        @XmlAttribute(name = "format")
        public String getFormat() {
            return format;
        }

        /**
         * Sets the format.
         *
         * @param format the format to set
         */
        public void setFormat(String format) {
            this.format = format;
        }

        /**
         * Gets the scale.
         *
         * @return the scale
         */
        @XmlAttribute(name = "scale")
        public String getScale() {
            return scale;
        }

        /**
         * Sets the scale.
         *
         * @param scale the scale to set
         */
        public void setScale(String scale) {
            this.scale = scale;
        }

    }
}
