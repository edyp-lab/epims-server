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
import fr.edyp.epims.json.CellenOneSample;
import fr.edyp.epims.json.CellenOneRunJson;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.util.error.EpimServerException;
import fr.edyp.epims.util.error.EpimsErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    void testGetCellenOneManipsWithMismatchedPlatesAndPlateSizes() throws IOException {
        Files.createFile(tempDir.resolve("MANIP_MISMATCH.zip"));
        String jsonContent = """
            {
                "name": "MANIP_MISMATCH",
                "modification_date": "2026-10-06",
                "creation_date": "2026-10-05",
                "runs": [
                    {
                        "name": "Run_Bad",
                        "sources": ["1A1"],
                        "plates": ["P1", "P2"],
                        "plate_size": [{"x":8, "y":12 }],
                        "samples_count": 48
                    }
                ]
            }
            """;
        Files.writeString(tempDir.resolve("MANIP_MISMATCH.json"), jsonContent);

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        EpimServerException ex = assertThrows(EpimServerException.class, () -> controller.getCellenOneZippedManips());
        assertEquals(EpimsErrorCode.INVALID_CELLENONE_DATA, ex.getErrorCode());
    }

    @Test
    void testGetCellenOneManipsNonExistentDir() {
        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.resolve("non_existent").toAbsolutePath().toString());

        try {
            controller.getCellenOneZippedManips();
        } catch (EpimServerException e){
            assertEquals(EpimsErrorCode.CELLENONE_ROOT_ERROR, e.getErrorCode());
        }

    }

    @Test
    void testGetCellenOneZippedManipsEmptyDir() {
        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneZippedManips();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetCellenOneZippedManipsFiltering() throws IOException {
        // Zip with matching JSON and runs
        Files.createFile(tempDir.resolve("20260225_MLE15_MANIP.zip"));
        String jsonContent = """
            {
                "name": "20260225_MLE15_MANIP",
                "modification_date": "2026-10-06",
                "creation_date": "2026-10-05",
                "runs": [
                    {
                        "name": "Run_R1",
                        "sources": ["1A1"],
                        "plates": ["P1"],
                        "plate_size": [{"x":8, "y":12 }],
                        "samples_count": 48
                    }
                ]
            }
            """;
        Files.writeString(tempDir.resolve("20260225_MLE15_MANIP.json"), jsonContent);

        // Another zip with matching JSON (empty runs)
        Files.createFile(tempDir.resolve("MANIP_2024_02.ZIP"));
        Files.writeString(tempDir.resolve("MANIP_2024_02.json"), "{}");

        // Zip WITHOUT matching JSON (should be ignored)
        Files.createFile(tempDir.resolve("MANIP_NO_JSON.zip"));

        // JSON WITHOUT matching ZIP (should be ignored)
        Files.writeString(tempDir.resolve("MANIP_NO_ZIP.json"), "{}");

        // Subdirectory (should be ignored)
        Files.createDirectory(tempDir.resolve("SOME_SUBDIR"));

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneZippedManips();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<CellenOneManipJson> manips = response.getBody();
        assertEquals(2, manips.size());

        assertEquals("20260225_MLE15_MANIP", manips.get(0).getName());
        assertNotNull(manips.get(0).getDate());
        String dateString ="2026-10-05";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate expectedDate = LocalDate.parse(dateString, formatter);
        assertEquals(expectedDate, manips.get(0).getDate());
        assertNotNull(manips.get(0).getRuns());
        assertEquals(1, manips.get(0).getRuns().size());
        assertEquals("Run_R1", manips.get(0).getRuns().get(0).getName());

        assertEquals("MANIP_2024_02", manips.get(1).getName());
        assertNotNull(manips.get(1).getDate());
        assertNotNull(manips.get(1).getRuns());
        assertTrue(manips.get(1).getRuns().isEmpty());
    }

    @Test
    void testGetCellenOneZippedManipsReadsSamplesCoordinates() throws IOException {
        Files.createFile(tempDir.resolve("MANIP_SAMPLES.zip"));
        String jsonContent = """
            {
                "runs": [
                    {
                        "name": "Run_R1",
                        "sources": ["1A1", "1A2"],
                        "plates": ["P1", "P2"],
                        "plate_size": [{"x":8, "y":12 }, {"x":8, "y":12 }],
                        "plate_coordinates_samples": [
                            {
                                "plate": "P1",
                                "plate_coordinate": {"x":1, "y":1},
                                "source": "1A1",
                                "drop_samples": [{"drop_id":11, "imagefile":"one.png"}]
                            },
                            {
                                "plate": "P1",
                                "plate_coordinate": {"x":2, "y":3},
                                "source": "1A2",
                                "drop_samples": [{"drop_id":12, "imagefile":"two.png"}]
                            },
                            {
                                "plate": "P2",
                                "plate_coordinate": {"x":1, "y":5},
                                "source": "1A1",
                                "drop_samples": [{"drop_id":13, "imagefile":"three.png"}, {"drop_id":14, "imagefile":"four.png"}]
                            }
                        ]
                    }
                ]
            }
            """;
        Files.writeString(tempDir.resolve("MANIP_SAMPLES.json"), jsonContent);

        Preferences preferences = ServerEpimsPreferences.root();
        preferences.put(PreferencesKeys.CELLENONE_ROOT, tempDir.toAbsolutePath().toString());

        ResponseEntity<List<CellenOneManipJson>> response = controller.getCellenOneZippedManips();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CellenOneManipJson manip = response.getBody().get(0);
        CellenOneRunJson run = manip.getRuns().get(0);
        assertEquals(3, run.getSamplesCount());
        assertEquals(2, run.getSamplesForSource("1A1").size());
        assertEquals(2, run.getSamplesForPlate("P1").size());
        CellenOneSample p2Sample = run.getSamplesForPlate("P2").get(0);
        assertEquals("1A1", p2Sample.getSource());
        assertEquals(new Point(1, 5), p2Sample.getPlateCoordinate());
        assertEquals(13, p2Sample.getDropSamples().get(0).getDropId());
        assertEquals("three.png", p2Sample.getDropSamples().get(0).getImageFilePath());
        assertEquals(1, manip.getSamplesForSource("1A2").size());
        assertEquals(2, manip.getSamplesForPlate("P1").size());
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
