/*
 * Copyright (C) 2024
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.edyp.epims.controller;

import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.json.CellenOneRunJson;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.util.error.EpimServerException;
import fr.edyp.epims.util.error.EpimsErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
//@AutoConfigureMockMvc
class CellenOneControllerTest {

//    @Autowired
//    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    private CellenOneController controller;
    private String originalPrefValue;

    @BeforeEach
    void setUp() {
        ServerEpimsPreferences.initPreferences(tempDir.toAbsolutePath().toString());
        controller = new CellenOneController();
    }

    @AfterEach
    void tearDown() {
        Preferences preferences = ServerEpimsPreferences.root();
        if (originalPrefValue != null) {
            preferences.put(PreferencesKeys.CELLENONE_ROOT, originalPrefValue);
        } else {
            preferences.remove(PreferencesKeys.CELLENONE_ROOT);
        }
    }

    @Test
    void testGetCellenOneManipsEmptyDir() {
        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneManips();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetCellenOneManipsWithSubfolders() throws IOException {
        Path manip1 = Files.createDirectory(tempDir.resolve("MANIP_2024_01"));
        Path manip2 = Files.createDirectory(tempDir.resolve("MANIP_2024_02"));
        // Create a regular file to make sure non-directories are ignored
        Files.createFile(tempDir.resolve("some_file.txt"));

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneManips();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<CellenOneManipJson> manips = response.getBody();
        assertEquals(2, manips.size());

        assertEquals("MANIP_2024_01", manips.get(0).getName());
        assertNotNull(manips.get(0).getDate());
        assertNotNull(manips.get(0).getRuns());
        assertTrue(manips.get(0).getRuns().isEmpty());

        assertEquals("MANIP_2024_02", manips.get(1).getName());
        assertNotNull(manips.get(1).getDate());
        assertNotNull(manips.get(1).getRuns());
        assertTrue(manips.get(1).getRuns().isEmpty());
    }

    @Test
    void testGetCellenOneManipsWithRunsJson() throws IOException {
        Path manipDir = Files.createDirectory(tempDir.resolve("20260225_MLE15_MANIP"));
        String jsonContent = """
            {
                "Name": "20260225_MLE15_MANIP",
                "Runs": [
                    {
                        "Name": "Run_R1",
                        "Sources": ["1A1"],
                        "Plates": ["P1"],
                        "Plate Size": [{"X":8, "Y":12 }],
                        "Nb Samples": 48
                    },
                    {
                        "Name": "Run_R2",
                        "Sources": ["1A1", "A2"],
                        "Plates": ["P1", "P2"],
                        "Plate Size": [{"X":8, "Y":12 }, {"X":8, "Y":12 }],
                        "Nb Samples": 62
                    }
                ]
            }
            """;
        Files.writeString(manipDir.resolve("20260225_MLE15_MANIP.json"), jsonContent);

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneManips();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<CellenOneManipJson> manips = response.getBody();
        assertEquals(1, manips.size());

        CellenOneManipJson manip = manips.get(0);
        assertEquals("20260225_MLE15_MANIP", manip.getName());
        assertNotNull(manip.getRuns());
        assertEquals(2, manip.getRuns().size());

        CellenOneRunJson run1 = manip.getRuns().get(0);
        assertEquals("Run_R1", run1.getName());
        assertEquals(List.of("1A1"), run1.getSources());
        assertEquals(List.of("P1"), run1.getPlates());
        assertEquals(1, run1.getPlateSizes().size());
        assertEquals(8, run1.getPlateSizes().get(0).x);
        assertEquals(12, run1.getPlateSizes().get(0).y);
        assertEquals(Integer.valueOf(48), run1.getSamplesCount());

        CellenOneRunJson run2 = manip.getRuns().get(1);
        assertEquals("Run_R2", run2.getName());
        assertEquals(List.of("1A1", "A2"), run2.getSources());
        assertEquals(List.of("P1", "P2"), run2.getPlates());
        assertEquals(2, run2.getPlateSizes().size());
        assertEquals(8, run2.getPlateSizes().get(0).x);
        assertEquals(12, run2.getPlateSizes().get(0).y);
        assertEquals(8, run2.getPlateSizes().get(1).x);
        assertEquals(12, run2.getPlateSizes().get(1).y);
        assertEquals(Integer.valueOf(62), run2.getSamplesCount());
    }

    @Test
    void testGetCellenOneManipsWithMismatchedPlatesAndPlateSizes() throws IOException {
        Path manipDir = Files.createDirectory(tempDir.resolve("MANIP_MISMATCH"));
        String jsonContent = """
            {
                "Name": "MANIP_MISMATCH",
                "Runs": [
                    {
                        "Name": "Run_Bad",
                        "Sources": ["1A1"],
                        "Plates": ["P1", "P2"],
                        "Plate Size": [{"X":8, "Y":12 }],
                        "Nb Samples": 48
                    }
                ]
            }
            """;
        Files.writeString(manipDir.resolve("MANIP_MISMATCH.json"), jsonContent);

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        EpimServerException ex = assertThrows(EpimServerException.class, () -> controller.getCellenOneManips());
        assertEquals(EpimsErrorCode.INVALID_STUDY_DATA, ex.getErrorCode());
    }

    @Test
    void testGetCellenOneManipsNonExistentDir() {
        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.resolve("non_existent").toAbsolutePath().toString());

        try {
            controller.getCellenOneManips();
        } catch (EpimServerException e){
            assertEquals(EpimsErrorCode.CELLENONE_ROOT_ERROR, e.getErrorCode());
        }

    }

    // VDS Should configure H2 database ...
//    @Test
//    void testGetCellenOneManipsNonExistentDir() throws Exception {
//        Preferences preferences = ServerEpimsPreferences.root();
//        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.resolve("non_existent").toAbsolutePath().toString());
//
//        mockMvc.perform(post("/api/cellenonemanips")).andExpect(status().isNotFound()); // or whatever status your handler returns
//    }
}
