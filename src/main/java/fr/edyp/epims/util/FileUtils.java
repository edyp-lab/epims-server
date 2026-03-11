/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Adler32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static void secureCopy(File src, File dest) throws IOException {

        if (src.isDirectory()) {
            dest.mkdir();
            File[] content = src.listFiles();
            for (int i = 0; i < Objects.requireNonNull(content).length; i++) {
                File f = content[i];
                File d = new File(dest, f.getName());
                LOGGER.debug(" copy file {}", f.getName());
                secureCopy(f, d);
            }

        } else {
            FileInputStream in = new FileInputStream(src);
            CheckedInputStream cin = new CheckedInputStream(in, new Adler32());
            FileOutputStream out = new FileOutputStream(dest);
            CheckedOutputStream cout = new CheckedOutputStream(out, new Adler32());
            byte[] buf = new byte[64*1024];
            int c;
            while ((c = cin.read(buf)) != -1)
                cout.write(buf, 0, c);

            if (cin.getChecksum().getValue() != cout.getChecksum().getValue()) {
                cin.close();
                cout.close();
                throw new IOException("Erreur de checksum sur la copie du fichier "+src.getName());
            }
            cin.close();
            cout.close();

            if (src.length() != dest.length()) {
                throw new IOException("Erreur de taille du fichier copié :"+src.length()+" attendu, "+dest.length()+" constaté");
            }
        }
    }

    public static void append(File file, String data) throws IOException {

        LOGGER.debug("Appending data to file: {}", file.getName());

        try (FileOutputStream out = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write("\n");
            writer.write(data);
            writer.flush();

        } catch (IOException e) {
            LOGGER.error("Error appending data to file: {}", file.getName(), e);
            throw e;
        }
    }

}
