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
package org.urmel.dbt.rc.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.urmel.dbt.rc.datamodel.slot.entries.OPCRecordEntry;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotListTransformer;
import org.urmel.dbt.rc.utils.SlotTransformer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class ClientServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(ClientServlet.class);

    private static final String TOKEN = "rctoken";
    private static final String SESSION_TOKEN = "rcsession";

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    public void doGetPost(final MCRServletJob job) throws Exception {
        final HttpServletRequest req = job.getRequest();
        final HttpServletResponse res = job.getResponse();

        final MCRSession session = getSession(req);

        final String path = req.getPathInfo();
        final String token = (String) session.get(TOKEN);
        final String sessionToken = (String) session.get(SESSION_TOKEN);

        if (path != null) {
            final StringTokenizer st = new StringTokenizer(path, "/");

            final String action = st.hasMoreTokens() ? st.nextToken() : null;

            final SlotList slotList = SLOT_MGR.getSlotList();

            if ("token".equals(action)) {
                LOGGER.info("Request token...");

                final String newToken = UUID.randomUUID().toString();

                session.put(TOKEN, newToken);

                writeToResponse(res, Base64.getEncoder().encode(newToken.getBytes(StandardCharsets.UTF_8)), null);
                return;
            } else if (token != null && !token.isEmpty() && "session".equals(action)) {
                if ("text/plain".equals(req.getContentType()) && req.getInputStream() != null) {
                    LOGGER.info("Register session...");

                    session.put(SESSION_TOKEN, ClientData.decrypt(token, req.getInputStream()));

                    LOGGER.info("...impersonate superuser");
                    session.setUserInformation(MCRSystemUserInformation.getSuperUserInstance());

                    res.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            } else if (sessionToken != null && !sessionToken.isEmpty()) {
                if ("list".equals(action)) {
                    writeToResponse(res, ClientData.encrypt(sessionToken,
                            new XMLOutputter()
                                    .outputString(SlotListTransformer.buildExportableXML(slotList.getBasicSlots()))),
                            null);
                    return;
                } else if (action != null) {
                    final Slot slot = SLOT_MGR.getSlotById(action);

                    String jsonStr = ClientData.decrypt(sessionToken, req.getInputStream());
                    if (jsonStr != null) {
                        final JsonParser jsonParser = new JsonParser();
                        final JsonObject jsonObj = jsonParser.parse(jsonStr).getAsJsonObject();

                        final String jobAction = jsonObj.get("action").getAsString();

                        if (jobAction.contains("register")) {
                            final String entryId = jsonObj.get("entryId").getAsString();
                            String epn = jsonObj.get("epn").getAsString();

                            if ("register".equals(jobAction)) {
                                LOGGER.info("Register copy with EPN " + epn + " on entry with id " + entryId + ".");
                            } else if ("deregister".equals(jobAction)) {
                                LOGGER.info("Deregister copy with EPN " + epn + " on entry with id " + entryId + ".");
                                epn = null;
                            }

                            final SlotEntry<?> entry = slot.getEntryById(entryId);
                            final OPCRecordEntry record = (OPCRecordEntry) entry.getEntry();

                            record.setEPN(epn);
                            SLOT_MGR.saveOrUpdate(slot);

                            res.setStatus(HttpServletResponse.SC_OK);
                            return;
                        }
                    } else {
                        writeToResponse(res,
                                ClientData.encrypt(sessionToken,
                                        new XMLOutputter().outputString(SlotTransformer.buildExportableXML(slot))),
                                null);
                        return;
                    }
                }
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    private static void writeToResponse(HttpServletResponse res, String data, String contentType) throws IOException {
        writeToResponse(res, data.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private static void writeToResponse(HttpServletResponse res, byte[] data, String contentType) throws IOException {
        res.setContentType(contentType != null ? contentType : "text/plain");
        res.getOutputStream().write(data);
        res.getOutputStream().flush();
    }

    private static class ClientData {
        private static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
        private static int KEY_SIZE = 128;
        private static int ITERATIONS = 100;

        private static byte[] generateIV(String passphrase) throws NoSuchAlgorithmException {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(passphrase.getBytes(StandardCharsets.UTF_8));
        }

        private static SecretKey generateKey(String passphrase, byte[] saltBytes) throws GeneralSecurityException {
            KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), saltBytes, ITERATIONS, KEY_SIZE);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey secretKey = keyFactory.generateSecret(keySpec);

            return new SecretKeySpec(secretKey.getEncoded(), "AES");
        }

        private static String convertStreamToString(InputStream is) {
            final StringBuffer sb = new StringBuffer();
            final Scanner s = new Scanner(is);
            while (s.hasNext()) {
                sb.append(s.next());
            }
            s.close();
            return sb.toString();
        }

        public static String encrypt(String passphrase, String data) throws GeneralSecurityException, IOException {
            final ByteArrayOutputStream bao = new ByteArrayOutputStream();
            final Random rnd = new SecureRandom();

            byte[] ivBytes = generateIV(passphrase);
            byte[] saltBytes = new byte[KEY_SIZE / 32];
            rnd.nextBytes(saltBytes);

            try {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
                SecretKeySpec sKey = (SecretKeySpec) generateKey(passphrase, saltBytes);

                Cipher c = Cipher.getInstance(CIPHER_TRANSFORMATION);
                c.init(Cipher.ENCRYPT_MODE, sKey, ivParameterSpec);

                byte[] cipherBytes = c.doFinal(data.getBytes(StandardCharsets.UTF_8));

                bao.write(saltBytes);
                bao.write(cipherBytes);

                return new String(Base64.getEncoder().encode(bao.toByteArray()), StandardCharsets.ISO_8859_1);
            } finally {
                bao.close();
            }
        }

        public static String decrypt(String passphrase, String encrypted) throws GeneralSecurityException {
            if (encrypted == null || encrypted.isEmpty())
                return null;

            byte[] cipherBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.ISO_8859_1));

            byte[] ivBytes = generateIV(passphrase);
            byte[] saltBytes = Arrays.copyOf(cipherBytes, KEY_SIZE / 32);
            cipherBytes = Arrays.copyOfRange(cipherBytes, KEY_SIZE / 32, cipherBytes.length);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec sKey = (SecretKeySpec) generateKey(passphrase, saltBytes);

            Cipher c = Cipher.getInstance(CIPHER_TRANSFORMATION);
            c.init(Cipher.DECRYPT_MODE, sKey, ivParameterSpec);

            return new String(c.doFinal(cipherBytes), StandardCharsets.UTF_8);
        }

        public static String decrypt(String passphrase, InputStream is) throws GeneralSecurityException, IOException {
            return decrypt(passphrase, convertStreamToString(is));
        }
    }
}