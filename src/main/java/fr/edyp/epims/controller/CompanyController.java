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

import fr.edyp.epims.database.dao.CompanyRepository;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.CompanyJson;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api")
public class CompanyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    CompanyRepository companyRepository;


    @Transactional
    @PostMapping("/modifycompany")
    public ResponseEntity<CompanyJson> modifyCompany(@RequestBody CompanyJson companyJson) {

        try {

            Optional<Company> companyOpt = companyRepository.findByName(companyJson.getName());
            if (! companyOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Company company = companyOpt.get();
            company.setManager(companyJson.getManager());
            company.setAddress(companyJson.getAddress());
            company.setPostalCode(companyJson.getPostalCode());

            company = companyRepository.save(company);


            CompanyJson companyJsonModified = Converter.convert(company);

            DatabaseVersionManager.getSingleton().bumpVersion(CompanyJson.class, null);

            return new ResponseEntity(companyJsonModified, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/modifycompany", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createcompany")
    public ResponseEntity<CompanyJson> createCompany(@RequestBody CompanyJson companyJson) {

        try {

            // check that the company does not already exist
            String name = companyJson.getName();
            Optional<Company> companyOpt = companyRepository.findByName(name);
            if (companyOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

            Company company = new Company(companyJson.getName(),companyJson.getManager(), companyJson.getAddress(), companyJson.getPostalCode(), null);
            company = companyRepository.save(company);

            DatabaseVersionManager.getSingleton().bumpVersion(CompanyJson.class, null);

            return new ResponseEntity(Converter.convert(company), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/createcompany", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/companies")
    public ResponseEntity<List<CompanyJson>> getAllCompanies() {
        try {
            List<Company> companies = new ArrayList<>();

            companyRepository.findAll().forEach(companies::add);

            if (companies.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            ArrayList<CompanyJson> companyJsonArrayList = new ArrayList();
            for (Company c : companies) {
                CompanyJson companyJson = new CompanyJson(c.getName(), c.getManager(), c.getAddress(), c.getPostalCode());

                companyJsonArrayList.add(companyJson);
            }


            return ControllerUtil.createResponseWithVersion(companyJsonArrayList, CompanyJson.class);

        } catch (Exception e) {
            LOGGER.error("error in /api/companies", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

