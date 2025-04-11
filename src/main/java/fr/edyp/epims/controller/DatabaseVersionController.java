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


import fr.edyp.epims.json.DatabaseVersionJson;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class DatabaseVersionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVersionController.class);

	@GetMapping("/databaseversion")
	public ResponseEntity<DatabaseVersionJson> databaseversion() {


		DatabaseVersionJson databaseVersionJson = DatabaseVersionManager.getSingleton().getVersion();

		LOGGER.info(databaseVersionJson.toString());

		return new ResponseEntity(databaseVersionJson, HttpStatus.OK);
	}

}
