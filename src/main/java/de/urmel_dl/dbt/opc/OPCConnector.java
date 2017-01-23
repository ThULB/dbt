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
package de.urmel_dl.dbt.opc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.urmel_dl.dbt.opc.datamodel.IKT;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.PPField;
import de.urmel_dl.dbt.opc.datamodel.pica.PPSubField;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;
import de.urmel_dl.dbt.opc.utils.PicaCharDecoder;

/**
 * The PICA OPC Connector based on hidden XML interface and the plain PICA+ output.
 *
 * @author René Adler (eagle)
 *
 */
public class OPCConnector {
    private static final Logger LOGGER = LogManager.getLogger(OPCConnector.class);

    private static final Cache<Object, Object> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    private String db;

    private URL url = new URL("http://gso.gbv.de/");

    private int maxhits = 500;

    private int maxread = 250;

    private int connectionTimeout = 10000;

    /**
     * Creates a new OPC connection.
     *
     * @param url the URL
     * @param db the database
     * @throws Exception thrown on malformed url
     */
    public OPCConnector(final URL url, final String db) throws Exception {
        this.url = url;
        this.db = (db == null) ? "1" : db;
    }

    /**
     * Creates a new OPC connection.
     *
     * @param url the URL
     * @param db the database
     * @throws MalformedURLException thrown on malformed url
     */
    public OPCConnector(final String url, final String db) throws MalformedURLException {
        this.url = new URL(url);
        this.db = (db == null) ? "1" : db;
    }

    /**
     * Creates a new OPC connection with default values.
     *
     * @see #OPCConnector(String, String)
     * @throws MalformedURLException thrown on malformed url
     */
    public OPCConnector() throws MalformedURLException {
        this.url = new URL("http://gso.gbv.de");
        this.db = "2.1";
    }

    /**
     * Set the database.
     *
     * @param db the database
     */
    public void setDB(final String db) {
        this.db = db;
    }

    /**
     * Return the set database.
     *
     * @return db the database
     */
    public String getDB() {
        return db;
    }

    /**
     * Set the URL.
     *
     * @param url the URL
     * @throws MalformedURLException thrown on malformed url
     */
    public void setURL(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * Set the URL
     *
     * @param url the URL
     */
    public void setURL(final URL url) {
        this.url = url;
    }

    /**
     * Return the OPC URL.
     *
     * @return the OPC URL
     */
    public URL getURL() {
        return url;
    }

    /**
     * Set number of max hits to return.
     *
     * @param maxhits the maxhits to return
     */
    public void setMaxHits(final int maxhits) {
        this.maxhits = maxhits;
    }

    /**
     * Return the max hits that was set.
     *
     * @return the maxhits were returned
     */
    public int getMaxHits() {
        return maxhits;
    }

    /**
     * Set the number of hits that were read at once.
     *
     * @param maxread the max hits to read at once
     */
    public void setMaxRead(final int maxread) {
        this.maxread = maxread;
    }

    /**
     * Return the max hits that were read at once.
     *
     * @return the max hits were read at once
     */
    public int getMaxRead() {
        return maxread;
    }

    /**
     * Set the connection timeout.
     *
     * @param connectionTimeout the connectionTimeout to set
     */
    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Return the set connection timeout.
     *
     * @return the connectionTimeout
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Returns the list of IKTs for the OPC.
     *
     * @return the list of ikts
     * @see IKTList#IKTList()
     * @throws ExecutionException if cache couldn't generated
     */
    public IKTList getIKTList() throws ExecutionException {
        if (this.url == null) {
            throw new IllegalArgumentException("No OPC URL was set.");
        }

        final URL url = this.url;

        IKTList iktList = (IKTList) CACHE.get(generateCacheKey("iktlist"), () -> {
            final URL pageURL = new URL(url + "/XML=1.0/MENUIKTLIST");

            final Document xml = new SAXBuilder().build(pageURL);

            final IKTList ikts = new IKTList();

            final List<Element> entries = xml.getRootElement().getChildren("IKTLIST");
            if (entries.size() == 1) {
                final Element entry = entries.get(0);

                final List<Element> children = entry.getChildren();

                for (int k = 0; k < children.size(); ++k) {
                    final Element child = children.get(k);

                    final IKT ikt = new IKT();

                    ikt.setKey(child.getText());
                    ikt.setMnemonic(child.getAttributeValue("mnemonic"));
                    ikt.setDescription(child.getAttributeValue("description"));

                    ikts.addIKT(ikt);
                }
            }

            return ikts;
        });

        return iktList;
    }

    /**
     * Search for given term within given ikt.
     *
     * @param trm the search term
     * @param ikt the ikt key
     * @return the result
     * @throws ExecutionException if cache couldn't generated
     * @see Result#Result(OPCConnector)
     */
    public Result search(final String trm, final String ikt) throws ExecutionException {
        if (this.url == null) {
            throw new IllegalArgumentException("No OPC URL was set.");
        }

        final URL url = this.url;
        final String db = this.db;
        final int maxread = this.maxread;

        Result result = (Result) CACHE.get(generateCacheKey(trm + "_" + ikt), () -> {
            LOGGER.info("Search OPAC for \"" + trm + "\" with IKT \"" + ikt + "\"...");

            final URL pageURL = new URL(url + "/XML=1.0/DB=" + db + "/SET=1/TTL=1/CMD?ACT=SRCHA&IKT=" + ikt
                + "&SRT=YOP&SHRTST=" + maxread + "&TRM=" + URLEncoder.encode(trm, "UTF-8"));

            final Document xml = new SAXBuilder().build(pageURL);

            return parseResult(xml);
        });

        return result;
    }

    /**
     * Search for given term within all index.
     *
     * @param trm the search term
     * @return the result
     * @throws ExecutionException if cache couldn't generated
     * @see OPCConnector#search(String, String)
     * @see Result#Result(OPCConnector)
     */
    public Result search(final String trm) throws ExecutionException {
        return search(trm, "1016");
    }

    /**
     * Return all related entries for given PPN.
     *
     * @param PPN the PPN
     * @return the result
     * @throws ExecutionException if cache couldn't generated
     * @see Result#Result(OPCConnector)
     */
    public Result family(final String PPN) throws ExecutionException {
        if (this.url == null) {
            throw new IllegalArgumentException("No OPC URL was set.");
        }

        final URL url = this.url;
        final String db = this.db;
        final int maxread = this.maxread;

        Result result = (Result) CACHE.get(generateCacheKey("fam_" + PPN), () -> {
            LOGGER.info("Enumarates member publications of PPN " + PPN);

            final URL pageURL = new URL(url + "/XML=1.0/DB=" + db + "/FAM?PPN=" + PPN + "&SHRTST=" + maxread);

            final Document xml = new SAXBuilder().build(pageURL);

            return parseResult(xml);
        });

        return result;
    }

    private Result parseResult(final Document xml) throws JDOMException, IOException {
        List<Element> entries = xml.getRootElement().getChildren("SET");
        if (entries.size() == 1) {
            Element entry = entries.get(0);

            int hits = Integer.valueOf(entry.getAttributeValue("hits")).intValue();
            hits = hits > this.maxhits ? this.maxhits : hits;

            final Result result = new Result(this);

            List<Element> titElements = entry.getChildren("SHORTTITLE");
            for (Element titElement : titElements) {
                if (titElement.getAttributeValue("PPN") != null) {
                    result.addRecord(new Record(titElement.getAttributeValue("PPN")));
                }
            }

            if (hits > this.maxread) {
                final int nums = (int) Math.floor(hits / this.maxread + (double) (hits % this.maxread > 0 ? 1 : 0));
                LOGGER.info(" ...found " + hits + " (num reads " + (nums - 1) + " on max. " + this.maxread
                    + "/Session) entries");

                String sessionpart = "";
                final Element session = xml.getRootElement().getChildren("SESSION").get(0);

                final StringBuffer buf = new StringBuffer();
                final List<Element> sessVars = session.getChildren("SESSIONVAR");
                for (int c = 0; c < sessVars.size(); ++c) {
                    final Element sessVar = sessVars.get(c);
                    buf.append("/" + sessVar.getAttributeValue("name") + "="
                        + URLEncoder.encode(sessVar.getText(), "UTF-8"));
                }
                sessionpart = buf.toString();

                int pos = 1;
                for (int rc = 1; rc < nums; ++rc) {
                    pos += this.maxread;
                    LOGGER.info("  ...read part " + rc + "/" + (nums - 1) + " - " + pos + " to "
                        + (pos + this.maxread > hits ? hits : pos + this.maxread));

                    final URL pageURL = new URL(this.url + sessionpart + "/XML=1.0/NXT?FRST=" + pos + "&SHRTST="
                        + this.maxread + "&NORND=ON");
                    final Document doc = new SAXBuilder().build(pageURL);

                    entries = doc.getRootElement().getChildren("SET");
                    if (entries.size() == 1) {
                        entry = entries.get(0);

                        titElements = entry.getChildren("SHORTTITLE");
                        for (Element titElement : titElements) {
                            if (titElement.getAttributeValue("PPN") != null) {
                                result.addRecord(new Record(titElement.getAttributeValue("PPN")));
                            }
                        }
                    }
                }
            } else {
                LOGGER.info(" ...found " + hits + " entries");
            }

            return result;
        }

        return new Result(this);
    }

    private String getPicaPlus(final String PPN) throws ExecutionException {
        final URL url = this.url;
        final String db = this.db;

        String ppraw = (String) CACHE.get(generateCacheKey("raw_" + PPN),
            () -> readWebPageFromUrl(url + "/DB=" + db + "/PPN?PLAIN=ON&PPN=" + PPN));
        return ppraw;
    }

    /**
     * Return a set of {@link PPField#PPField()}.
     *
     * @param PPN the PPN
     * @return a set of PICA+ fields
     * @throws ExecutionException if cache couldn't generated
     */
    public List<PPField> getPPFields(final String PPN) throws ExecutionException {
        final String ppraw = getPicaPlus(PPN);

        if (ppraw != null) {
            final String[] pp = ppraw.split("\036");

            if (pp.length > 1) {
                final List<PPField> fields = new ArrayList<>();

                for (int c = 0; c < pp.length; ++c) {
                    final String[] line = pp[c].split(" ", 2);

                    if (line.length == 2) {
                        final PPField ppField = new PPField();

                        final String[] field = line[0].split("/", 2);
                        if (field.length == 2) {
                            ppField.setTag(field[0]);
                            ppField.setOccurrence(field[1]);
                        } else {
                            ppField.setTag(field[0]);
                        }

                        final Pattern p = Pattern.compile("\037([a-zA-Z0-9])([^\037]+)");
                        final Matcher matcher = p.matcher(line[1]);
                        while (matcher.find()) {
                            final PPSubField ppSubfield = new PPSubField();
                            ppSubfield.setCode(matcher.group(1));
                            ppSubfield.setContent(PicaCharDecoder.decode(matcher.group(2)));
                            ppField.addSubfield(ppSubfield);
                        }

                        fields.add(ppField);
                    }
                }

                return fields;
            }
        }

        return null;
    }

    /**
     * Return a new {@link Record#Record()}.
     *
     * @param PPN the PPN
     * @return a new {@link Record#Record()}
     * @throws ExecutionException if cache couldn't generated
     */
    public Record getRecord(final String PPN) throws ExecutionException {
        List<PPField> fields = getPPFields(PPN);
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        final Record record = new Record(this, PPN);
        record.setFields(fields);
        return record;
    }

    /**
     * Returns the PPN from given barcode.
     *
     * @param barcode the barcode
     * @return the PPN
     * @throws ExecutionException if cache couldn't generated
     */
    public String getPPNFromBarcode(final String barcode) throws ExecutionException {
        final Result result = search("bar " + barcode);

        if (result.getRecords().size() == 1) {
            return result.getRecords().get(0).getPPN();
        }

        return null;
    }

    private String removeLocationFromShelfMark(final String shelfMark) {
        String shelf = shelfMark;

        final int offsetLoc = shelf.indexOf(":");
        if (offsetLoc != -1) {
            shelf = shelf.substring(offsetLoc + 1);
        }

        return shelf;
    }

    private Boolean ifShelfEquals(final Record record, final String shelf) {
        record.load();

        final List<PPField> ppFields = record.getFieldsByTag("209A");
        for (PPField ppField : ppFields) {
            final PPSubField ppSubfield = ppField.getSubfieldByCode("a");
            if (ppSubfield != null && shelf.equals(ppSubfield.getContent())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the PPN from given shelf mark.
     *
     * @param shelfMark the shelf mark
     * @param withLoc true to search with location within sehlfMark
     * @return the PPN
     * @throws ExecutionException if cache couldn't generated
     */
    public String getPPNFromShelfMark(final String shelfMark, final boolean withLoc) throws ExecutionException {
        String shelf = shelfMark;
        if (withLoc) {
            shelf = removeLocationFromShelfMark(shelfMark);
        }

        String shelfShort = shelf;

        if (shelf.indexOf("(") != -1) {
            shelfShort = shelf.substring(0, shelf.indexOf("("));
        }

        final Result result = search("num " + shelfShort + "*" + " or sgn " + shelfShort + "*");

        if (result.getRecords().size() == 1) {
            if (ifShelfEquals(result.getRecords().get(0), removeLocationFromShelfMark(shelfMark))) {
                return result.getRecords().get(0).getPPN();
            }
        } else {
            for (Record record : result.getRecords()) {
                if (ifShelfEquals(record, removeLocationFromShelfMark(shelfMark))) {
                    return record.getPPN();
                }
            }
        }

        return null;
    }

    /**
     * Return the PPN from given shelf mark and title.
     *
     * @param shelfMark the shelf mark
     * @param withLoc true to search with location within sehlfMark
     * @param title the title
     * @return the PPN
     * @throws ExecutionException if cache couldn't generated
     */
    public String getPPNFromShelfMarkAndTitle(final String shelfMark, final boolean withLoc, final String title)
        throws ExecutionException {
        String shelf = shelfMark;
        if (withLoc) {
            shelf = removeLocationFromShelfMark(shelfMark);
        }

        final String t = title.indexOf(": ") != -1 ? title.substring(0, title.indexOf(": ")) : title;

        final Result result = search("sgn \"" + shelf + "\" and tit " + t);

        if (result != null) {
            if (result.getRecords().size() == 1) {
                if (ifShelfEquals(result.getRecords().get(0), removeLocationFromShelfMark(shelfMark))) {
                    return result.getRecords().get(0).getPPN();
                }
            } else {
                for (Record record : result.getRecords()) {
                    if (ifShelfEquals(record, removeLocationFromShelfMark(shelfMark))) {
                        return record.getPPN();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Return the PPN from given shelf mark or title.
     *
     * @param shelfMark the shelf mark
     * @param withLoc true to search with location within sehlfMark
     * @param title the title
     * @return the PPN
     * @throws ExecutionException if cache couldn't generated
     */
    public String getPPNFromShelfMarkOrTitle(final String shelfMark, final boolean withLoc, final String title)
        throws ExecutionException {
        String shelf = shelfMark;
        if (withLoc) {
            shelf = removeLocationFromShelfMark(shelfMark);
        }

        final String t = title.indexOf(": ") != -1 ? title.substring(0, title.indexOf(": ")) : title;

        final Result result = search("sgn \"" + shelf + "\" or tit " + t);

        if (result != null) {
            if (result.getRecords().size() == 1) {
                if (ifShelfEquals(result.getRecords().get(0), removeLocationFromShelfMark(shelfMark))) {
                    return result.getRecords().get(0).getPPN();
                }
            } else {
                for (Record record : result.getRecords()) {
                    if (ifShelfEquals(record, removeLocationFromShelfMark(shelfMark))) {
                        return record.getPPN();
                    }
                }
            }
        }

        return null;
    }

    private String readWebPageFromUrl(final String urlString) throws IOException {
        String content = "";

        final URL url = new URL(urlString);

        LOGGER.debug("Open URL: " + urlString);

        final URLConnection urlConn = url.openConnection();
        urlConn.setConnectTimeout(this.connectionTimeout);
        urlConn.setDoInput(true);
        urlConn.setUseCaches(false);

        final String encoding = (urlConn.getContentEncoding() != null ? urlConn.getContentEncoding()
            : "ISO-8859-1");
        LOGGER.debug("Encoding set to: " + encoding);

        final BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), encoding));

        final StringBuffer buf = new StringBuffer();
        String s;
        while ((s = dis.readLine()) != null) {
            buf.append(s);
        }
        dis.close();
        content = buf.toString();

        return content;
    }

    private String generateCacheKey(final String key) {
        return this.url.getHost() + "_" + this.db + "-" + key;
    }

    static {
        final TrustManager[] trustAllCerts = { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
            }
        } };
        try {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
