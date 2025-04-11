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

import fr.edyp.epims.database.dao.StatisticService;
import fr.edyp.epims.json.AcquisitionStatisticJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StatisticController {


    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticController.class);

    @Autowired
    StatisticService statisticService;

    @GetMapping("/acquisitionsStatistic/{year}")
    public ResponseEntity<List<AcquisitionStatisticJson>> getAcquisitionsStatistic(@PathVariable("year") int year) {
        try {

            List<AcquisitionStatisticJson> resultList = statisticService.acquistionsStatistics(year);


            return new ResponseEntity<>(resultList, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error on acquisitionsStatistic", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
