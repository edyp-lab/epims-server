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
import fr.edyp.epims.util.error.EpimServerException;
import fr.edyp.epims.util.error.EpimsErrorCode;
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

        String actorId = programJson.getResponsible();
        Optional<Actor> actorOpt = actorRepository.findById(actorId);
        if (actorOpt.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Actor Key: " + actorId);
        }
        //Actor actor = actorOpt.get();

        Set<Actor> actors = new HashSet<>();
        for (String actorKey : programJson.getActorsKey()) {
            actorOpt =actorRepository.findById(actorKey);
            if (actorOpt.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Member Key: " + actorKey);
            }
            actors.add(actorOpt.get());
        }

        Optional<Program> existingProgByNomenclature = programRepository.findByNomenclatureTitle(programJson.getNomenclatureTitle());
        if (existingProgByNomenclature.isPresent()) {
            throw new EpimServerException(EpimsErrorCode.DUPLICATE_NOMENCLATURE,
                    "A program with nomenclature title '" + programJson.getNomenclatureTitle() + "' already exists");
        }

        Optional<Program> existingProgramByTitle = programRepository.findByTitle(programJson.getTitle());
        if (existingProgramByTitle.isPresent()) {
            throw new EpimServerException(EpimsErrorCode.DUPLICATE_TITLE,
                    "A Program with title '" + programJson.getTitle() + "' already exists");
        }


        Set<ProgContacts> progContactsSet = new HashSet<>();

        Program program = new Program(-1, programJson.getTitle(), programJson.getNomenclatureTitle(),
            programJson.getLongTitle(), programJson.getDescription(), programJson.getContractualFrame(), actorId,
            null, programJson.getCreationDate(), programJson.getConfidential(), null, actors,
            progContactsSet);

        Program programSaved = programRepository.save(program);

        for (Integer contactKey : programJson.getContactsKey()) {
            Optional<Contact> contactOpt = contactRepository.findById(contactKey);
            if (contactOpt.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.CONTACT_NOT_FOUND, "Contact Key: " + contactKey);
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
        boolean success = f.mkdir();
        if(!success){
            throw new EpimServerException(EpimsErrorCode.PROGRAM_DIRECTORY_CREATION_FAILED,"Program Key: " + program.getNomenclatureTitle());
        }
        DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

        return new ResponseEntity<>(Converter.convert(programSaved), HttpStatus.OK);
    }


    @Transactional
    @PostMapping("/programclose/{programId}")
    public ResponseEntity<ProgramJson> programclose(@RequestBody Date closingDate, @PathVariable("programId") int programId) {
        Optional<Program> p = programRepository.findById(programId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROGRAM_NOT_FOUND, "Program Key: " + programId);
        }

        Program program = p.get();
        program.setClosingDate(closingDate);

        Program programSaved = programRepository.save(program);

        DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

        return new ResponseEntity<>(Converter.convert(programSaved), HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/programaddmember/{programId}")
    public ResponseEntity<ProgramJson> addMember(@RequestBody List<String> actorIds, @PathVariable("programId") int programId) {

        Optional<Program> p = programRepository.findById(programId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROGRAM_NOT_FOUND, "Program Key: " + programId);
        }

        for (String actorId : actorIds) {

            Optional<Actor> a = actorRepository.findById(actorId);
            if (a.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.ACTOR_NOT_FOUND, "Actor Key: " + actorId);
            }

            p.get().addMember(a.get());
        }

        Program programSaved = programRepository.save(p.get());

        DatabaseVersionManager.getSingleton().bumpVersion(ProgramJson.class, null);

        return new ResponseEntity<>(Converter.convert(programSaved), HttpStatus.OK);
    }


    @Transactional
    @PostMapping("/programaddcontact/{programId}")
    public ResponseEntity<ProgramJson> addContact(@RequestBody List<Integer> contactIds, @PathVariable("programId") int programId) {

        Optional<Program> p = programRepository.findById(programId);
        if (p.isEmpty()) {
            throw new EpimServerException(EpimsErrorCode.PROGRAM_NOT_FOUND, "Program Key: " + programId);
        }

        Set<ProgContacts> progContactsSet = null;

        ProgContacts progContact = null;
        for (Integer contactId : contactIds) {

            Optional<Contact> c = contactRepository.findById(contactId);
            if (c.isEmpty()) {
                throw new EpimServerException(EpimsErrorCode.CONTACT_NOT_FOUND, "Contact Key: " + contactId);
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

        return new ResponseEntity<>(Converter.convert(program), HttpStatus.OK);

    }

  //  @Transactional(readOnly = true)
    @GetMapping("/programs")
    public ResponseEntity<List<ProgramJson>> getAllPrograms() {

        List<Program> programs = new ArrayList<>(programRepository.findAll());
        if (programs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        ArrayList<ProgramJson> programJsonArrayList = new ArrayList<>();
        for (Program program : programs) {
            programJsonArrayList.add(Converter.convert(program));
        }

        Collections.sort(programJsonArrayList);

        return ControllerUtil.createResponseWithVersion(programJsonArrayList, ProgramJson.class);
    }
}
