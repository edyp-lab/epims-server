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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void testSecureCopyFile(@TempDir Path tempDir) throws IOException {
        Path srcFile = tempDir.resolve("source.txt");
        Path destFile = tempDir.resolve("dest.txt");
        String content = "Hello Secure Copy";
        Files.writeString(srcFile, content);

        FileUtils.secureCopy(srcFile, destFile);

        assertTrue(Files.exists(destFile));
        assertEquals(content, Files.readString(destFile));
    }

    @Test
    void testSecureCopyDirectory(@TempDir Path tempDir) throws IOException {
        Path srcDir = tempDir.resolve("srcDir");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("file1.txt"), "Content 1");
        Path subDir = srcDir.resolve("sub");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("file2.txt"), "Content 2");

        Path destDir = tempDir.resolve("destDir");

        FileUtils.secureCopy(srcDir, destDir);

        assertTrue(Files.exists(destDir.resolve("file1.txt")));
        assertTrue(Files.exists(destDir.resolve("sub").resolve("file2.txt")));
        assertEquals("Content 1", Files.readString(destDir.resolve("file1.txt")));
        assertEquals("Content 2", Files.readString(destDir.resolve("sub").resolve("file2.txt")));
    }

    @Test
    void testSecureCopyFileObjects(@TempDir Path tempDir) throws IOException {
        File srcFile = tempDir.resolve("source_file.txt").toFile();
        File destFile = tempDir.resolve("dest_file.txt").toFile();
        String content = "Testing File object overload";
        org.apache.commons.io.FileUtils.writeStringToFile(srcFile, content, "UTF-8");

        FileUtils.secureCopy(srcFile, destFile);

        assertTrue(destFile.exists());
        assertEquals(content, org.apache.commons.io.FileUtils.readFileToString(destFile, "UTF-8"));
    }

    @Test
    void testSecureMove(@TempDir Path tempDir) throws IOException {
        Path srcFile = tempDir.resolve("move_source.txt");
        Path destFile = tempDir.resolve("move_dest.txt");
        String content = "Moving content securely";
        Files.writeString(srcFile, content);

        FileUtils.secureMove(srcFile, destFile);

        assertFalse(Files.exists(srcFile));
        assertTrue(Files.exists(destFile));
        assertEquals(content, Files.readString(destFile));
    }

    @Test
    void testAppend(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("append.txt").toFile();
        FileUtils.append(file, "Line 1");
        FileUtils.append(file, "Line 2");

        String content = org.apache.commons.io.FileUtils.readFileToString(file, "UTF-8");
        assertTrue(content.contains("Line 1"));
        assertTrue(content.contains("Line 2"));
    }
}
