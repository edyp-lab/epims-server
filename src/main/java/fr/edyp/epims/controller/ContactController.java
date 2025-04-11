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
import fr.edyp.epims.database.dao.ContactRepository;

import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.ContactJson;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/api")
public class ContactController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    CompanyRepository companyRepository;

    @RequestMapping({  "/login"})
    public String index() {
        return "forward:/";
    }


    @Transactional
    @PostMapping("/modifycontact")
    public ResponseEntity<ContactJson> modifyContact(@RequestBody ContactJson contactJson) {

        try {

            // modify contact
            Optional<Contact> contactOpt = contactRepository.findById(contactJson.getId());
            if (! contactOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Contact contact = contactOpt.get();
            contact.setFirstName(contactJson.getFirstName());
            contact.setLastName(contactJson.getLastName());
            contact.setFaxNumber(contactJson.getFaxNumber());
            contact.setTelephoneNumber(contactJson.getTelephoneNumber());
            contact.setEmail(contactJson.getEmail());

            Optional<Company> companyOpt = companyRepository.findByName(contactJson.getCompany());
            if (! companyOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            contact.setCompany(companyOpt.get());
            contact = contactRepository.save(contact);


            ContactJson contactJsonModified = Converter.convert(contact);

            DatabaseVersionManager.getSingleton().bumpVersion(ContactJson.class, null);

            return new ResponseEntity(contactJsonModified, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/modifycontact", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createcontact")
    public ResponseEntity<ContactJson> createActor(@RequestBody ContactJson contactJson) {

        try {


            // check that the contact does not already exist
            Optional<Contact> contactOpt = contactRepository.findById(contactJson.getId());
            if (contactOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String companyKey = contactJson.getCompany();

            Optional<Company> companyOpt = companyRepository.findByName(companyKey);
            if (! companyOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Contact contact = new Contact(companyOpt.get(), contactJson.getLastName(),
                    contactJson.getFirstName(), contactJson.getTelephoneNumber(),
                    contactJson.getFaxNumber(), contactJson.getEmail(), null, null, null, null);

            contact = contactRepository.save(contact);

            DatabaseVersionManager.getSingleton().bumpVersion(ContactJson.class, null);

            return new ResponseEntity(Converter.convert(contact), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/createcontact", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/contacts")
    public ResponseEntity<List<ContactJson>> getAllContacts(@RequestParam(required = false) String lastName) {
        try {



            List<Contact> contacts = new ArrayList<Contact>();

            if (lastName == null)
                contactRepository.findAll().forEach(contacts::add);
            else
                contactRepository.findByLastName(lastName).forEach(contacts::add);

            if (contacts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            ArrayList<ContactJson> list = new ArrayList();
            for (Contact c : contacts) {
                Company company = c.getCompany();
                String companyName = (company == null) ? null : company.getName();
                ContactJson cj = new ContactJson(c.getId(), companyName, c.getLastName(), c.getFirstName(), c.getTelephoneNumber(), c.getFaxNumber(), c.getEmail());
                list.add(cj);
            }

            return ControllerUtil.createResponseWithVersion(list, ContactJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /api/contacts", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/contacts/{id}")
    public ResponseEntity<ContactJson> getContactById(@PathVariable("id") int id) {
        Optional<Contact> contactData = contactRepository.findById(id);

        if (contactData.isPresent()) {
            Contact c = contactData.get();
            Company company = c.getCompany();
            String companyName = (company == null) ? null : company.getName();
            ContactJson cj = new ContactJson(c.getId(), companyName, c.getLastName(), c.getFirstName(), c.getTelephoneNumber(), c.getFaxNumber(), c.getEmail());


            return new ResponseEntity<>(cj, HttpStatus.OK);
        } else {
            LOGGER.warn("error in /api/contacts/{id}");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }




}
