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
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

@RestController
@RequestMapping("/api")
public class ProgramController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramController.class);

    @Autowired
    ProgramRepository programRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ProgContactsRepository progContactsRepository;


    @Autowired
    ActorRepository actorRepository;

    @Autowired
    private Environment env;

    @Transactional
    @PostMapping("/addprogram")
    public ResponseEntity<ProgramJson> addprogram(@RequestBody ProgramJson programJson) {

        try {


            Optional<Actor> actorOpt = actorRepository.findById(programJson.getResponsible());
            if (!actorOpt.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Actor actor = actorOpt.get();

            Set<Actor> actors = new HashSet<>();
            for (String actorKey : programJson.getActorsKey()) {
                actorOpt =actorRepository.findById(actorKey);
                if (!actorOpt.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                actors.add(actorOpt.get());
            }

            Set<ProgContacts> progContactsSet = new HashSet();

            Program program = new Program(-1, programJson.getTitle(), programJson.getNomenclatureTitle(),
                    programJson.getLongTitle(), programJson.getDescription(), programJson.getContractualFrame(), programJson.getResponsible(),
                    null, programJson.getCreationDate(), programJson.getConfidential(), null, actors,
                    progContactsSet);



            Program programSaved = programRepository.save(program);

            for (Integer contactKey : programJson.getContactsKey()) {
                Optional<Contact> contactOpt = contactRepository.findById(contactKey);
                if (!contactOpt.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Contact contact = contactOpt.get();
                ProgContacts progContact = new ProgContacts(contact, programSaved);
                progContact = progContactsRepository.save(progContact);
                progContactsSet.add(progContact);
            }
            programSaved.setProgContactses(progContactsSet);


            Preferences preferences = ServerEpimsPreferences.root();
            String epimsRepository = preferences.get(PreferencesKeys.PIMS_ROOT, env.getProperty("epims.repository"));

            String subABCDirectory = preferences.get(PreferencesKeys.REPOSITORY_DEFAULT_ABC_LETTER, "b");
            File f = new File(epimsRepository+"/"+subABCDirectory+"/"+program.getNomenclatureTitle());
            f.mkdir();


            DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);



            return new ResponseEntity(Converter.convert(programSaved), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/addprogram", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Transactional
    @PostMapping("/programclose/{programId}")
    public ResponseEntity<ProgramJson> programclose(@RequestBody Date closingDate, @PathVariable("programId") int programId) {

        try {

            Optional<Program> p = programRepository.findById(programId);
            if (!p.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Program program = p.get();
            program.setClosingDate(closingDate);

            Program programSaved = programRepository.save(program);

            DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

            return new ResponseEntity(Converter.convert(programSaved), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/programclose", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/programaddmember/{programId}")
    public ResponseEntity<ProgramJson> addMember(@RequestBody List<String> actorIds, @PathVariable("programId") int programId) {

        try {

            Optional<Program> p = programRepository.findById(programId);
            if (!p.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }


            for (String actorId : actorIds) {

                Optional<Actor> a = actorRepository.findById(actorId);
                if (!a.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                p.get().addActor(a.get());

            }
            Program programSaved = programRepository.save(p.get());

            DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

            return new ResponseEntity(Converter.convert(programSaved), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("error in /api/programaddmember", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/programaddcontact/{programId}")
    public ResponseEntity<ProgramJson> addContact(@RequestBody List<Integer> contactIds, @PathVariable("programId") int programId) {

        try {

            Optional<Program> p = programRepository.findById(programId);
            if (!p.isPresent()) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Set<ProgContacts> progContactsSet = null;

            ProgContacts progContact = null;
            for (Integer contactId : contactIds) {

                Optional<Contact> c = contactRepository.findById(contactId);
                if (!c.isPresent()) {
                    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                progContact = new ProgContacts(c.get(), p.get());
                progContact = progContactsRepository.save(progContact);


                if (progContactsSet == null) {
                    Program program = progContact.getProgram();
                    progContactsSet = program.getProgContactses();
                    if (progContactsSet == null) {
                        progContactsSet = new HashSet<>();
                    }
                }

                progContactsSet.add(progContact);

            }

            Program program = progContact.getProgram();
            program.setProgContactses (progContactsSet);

            DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

            return new ResponseEntity(Converter.convert(program), HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("error in /api/programaddcontact", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/programs")
    public ResponseEntity<List<ProgramJson>> getAllPrograms() {
        try {
            List<Program> programs = new ArrayList<>();

            programRepository.findAll().forEach(programs::add);

            if (programs.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            ArrayList<ProgramJson> programJsonArrayList = new ArrayList();
            for (Program program : programs) {

                programJsonArrayList.add(Converter.convert(program));
            }

            Collections.sort(programJsonArrayList);

            return ControllerUtil.createResponseWithVersion(programJsonArrayList, ProgramJson.class);
        } catch (Exception e) {
            LOGGER.error("error in /api/programs", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
