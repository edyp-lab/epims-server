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

import fr.edyp.epims.database.dao.*;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.*;
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
public class ActorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActorController.class);

    @Autowired
    ActorRepository actorRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ContactRepository contactRepository;


    @Autowired
    ActorRoleRepository actorRoleRepository;

    @Transactional
    @PostMapping("/modifyactor")
    public ResponseEntity<ActorJson> modifyActor(@RequestBody ActorJson actorJson) {

        try {

            // JPM.WART : we need to drop the role constraint : this line can be suppressed when the new epims
            // has worked one time on the production server and an actor has been created.
            //actorRoleRepository.dropRoleConstraint();

            // get the current actor
            String login = actorJson.getLogin();
            Optional<Actor> actorOpt = actorRepository.findByLogin(login);
            if (! actorOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Actor actor = actorOpt.get();

            // modify password
            String password = actorJson.getPasswd();
            if ((password != null) && !password.isEmpty()) {
                actor.setPasswd(actorJson.getPasswd());
                actor = actorRepository.save(actor);
            }

            // modify role
            Set<ActorRole> roles = actor.getActorRoles();
            ActorRole actorRole = roles.iterator().next();
            actorRoleRepository.delete(actorRole);
            ActorRoleId actorRoleId = new ActorRoleId(actor.getLogin(), actorJson.getRole());
            actorRole = new ActorRole(actorRoleId, actor);
            actorRole = actorRoleRepository.save(actorRole);

            // modify contact
            Contact contact = actor.getContact();
            ContactJson contactJson = actorJson.getContact();
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


            ActorJson actorJsonModified = Converter.convert(actor);
            actorJsonModified.setRole(actorJson.getRole());

            DatabaseVersionManager.getSingleton().bumpVersion(ContactJson.class, null);
            DatabaseVersionManager.getSingleton().bumpVersion(ActorJson.class, null);

            return new ResponseEntity(actorJsonModified, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/modifyactor", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createactor")
    public ResponseEntity<ActorJson> createActor(@RequestBody ActorJson actorJson) {

        try {

            // JPM.WART : we need to drop the role constraint : this line can be suppressed when the new epims
            // has worked one time on the production server and an actor has been created.
            //actorRoleRepository.dropRoleConstraint();

            // check that the actor does not alreay exist
            String login = actorJson.getLogin();
            Optional<Actor> actorOpt = actorRepository.findByLogin(login);
            if (actorOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

            ContactJson contactJson = actorJson.getContact();
            String companyKey = contactJson.getCompany();

            Optional<Company> companyOpt = companyRepository.findByName(companyKey);
            if (! companyOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Contact contact = new Contact(companyOpt.get(), contactJson.getLastName(),
                    contactJson.getFirstName(), contactJson.getTelephoneNumber(),
                    contactJson.getFaxNumber(), contactJson.getEmail(), null, null, null, null);

            contact = contactRepository.save(contact);

            ActorRoleId actorRoleId = new ActorRoleId(actorJson.getLogin(), actorJson.getRole());


            Actor actor = new Actor(actorJson.getLogin(), contact, actorJson.getPasswd());
            actor = actorRepository.save(actor);

            ActorRole actorRole = new ActorRole(actorRoleId, actor);
            actorRole = actorRoleRepository.save(actorRole);

            DatabaseVersionManager.getSingleton().bumpVersion(ActorJson.class, null);
            DatabaseVersionManager.getSingleton().bumpVersion(ContactJson.class, null);

            return new ResponseEntity(Converter.convert(actor), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/createactor", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/actors")
    public ResponseEntity<List<ActorJson>> getAllActors() {
        try {
            List<Actor> actors = new ArrayList<Actor>();

            actorRepository.findAll().forEach(actors::add);


            if (actors.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            ArrayList<ActorJson> list = new ArrayList();
            for (Actor a : actors) {

                ActorJson actorJson = Converter.convert(a);
                list.add(actorJson);

            }

            Collections.sort(list);

            return ControllerUtil.createResponseWithVersion(list, ActorJson.class);

        } catch (Exception e) {
            LOGGER.error("error in /api/actors", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
