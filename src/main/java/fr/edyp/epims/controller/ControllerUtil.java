/*
 * Copyright (C) 2021
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

import fr.edyp.epims.version.DatabaseVersionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class ControllerUtil {

    public static ResponseEntity createResponseWithVersion(Object body, Class versionClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        headers.add("VersionClass", versionClass.getSimpleName());
        headers.add("Version", DatabaseVersionManager.getSingleton().getVersion(versionClass).toString());
        headers.add("VersionLogin", DatabaseVersionManager.getSingleton().getLogin(versionClass));


        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    public static boolean checkVersion(String versionClassAsString, String versionAsString) {
        if (versionAsString == null) {
            return true; // should not happen
        }
        Integer version = Integer.parseInt(versionAsString);

        Integer versionCur = DatabaseVersionManager.getSingleton().getVersion().getVersion(versionClassAsString);

        return (version.intValue() == versionCur.intValue());
    }
}
