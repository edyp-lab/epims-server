package fr.edyp.epims.util;

import java.io.*;
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
            for (int i = 0; i < content.length; i++) {
                File f = content[i];
                File d = new File(dest, f.getName());
                LOGGER.debug(" copy file "+f.getName());
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
}
