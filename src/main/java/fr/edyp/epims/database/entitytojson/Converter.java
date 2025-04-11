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

package fr.edyp.epims.database.entitytojson;

import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.json.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class Converter {

    public static InstrumentJson convert(Instrument instrument) {
        InstrumentJson instrumentJson = new InstrumentJson(instrument.getId(), instrument.getName(), instrument.getManufacturer(), instrument.getModel(), instrument.getStatus(), instrument.getSpectrometer() != null);
        return instrumentJson;
    }

    public static ActorJson convert(Actor a) {

        Contact contact = a.getContact();

        ContactJson contactJson = convert(contact);

        String roleKey = "user";
        Set<ActorRole> roles = a.getActorRoles();
        for (ActorRole role : roles) {
            String roleCur = role.getId().getRole();
            if (roleCur.equals("admin")) {
                roleKey = roleCur;
                break;
            } else if (roleCur.equals("admin user")) {
                roleKey = roleCur;
            } else if (roleCur.equals("robot user")) {
                roleKey = roleCur;
            }

        }

        ActorJson aj = new ActorJson(a.getLogin(), contactJson, roleKey, null); // password not given to the client

        return aj;

    }

    public static ContactJson convert(Contact contact) {

        Company company = contact.getCompany();
        String companyName = (company != null) ? company.getName() : null;

        ContactJson contactJson = new ContactJson(
                contact.getId(),
                companyName,
                contact.getLastName(),
                contact.getFirstName(),
                contact.getTelephoneNumber(),
                contact.getFaxNumber(),
                contact.getEmail()
        );


        return contactJson;

    }


    public static CompanyJson convert(Company company) {

        CompanyJson companyJson = new CompanyJson(company.getName(), company.getManager(), company.getAddress(), company.getPostalCode());

        return companyJson;

    }

    public static ProgramJson convert(Program program) {
        ProgramJson programJson = new ProgramJson(
                program.getId(),
                program.getTitle(),
                program.getNomenclatureTitle(),
                program.getLongTitle(),
                program.getDescription(),
                program.getContractualFrame(),
                program.getResponsible(),
                program.getClosingDate(),
                program.getCreationDate(),
                program.getConfidential());

        ArrayList<Integer> projectsKeysList = new ArrayList<>();
        if (program.getProjects() != null) {
            for (Project project : program.getProjects()) {

                projectsKeysList.add(project.getId());

            }
        }
        programJson.setProjectsKeys(projectsKeysList);


        ArrayList<String> actorsKey = new ArrayList<String>(0);
        if (program.getActors() != null) {
            for (Actor a : program.getActors()) {
                String actorKey = a.getLogin();
                actorsKey.add(actorKey);
            }
        }
        programJson.setActorsKey(actorsKey);

        ArrayList<Integer> contactsKey = new ArrayList<Integer>(0);
        if (program.getProgContactses() != null) {
            for (ProgContacts pc : program.getProgContactses()) {
                contactsKey.add(pc.getContact().getId());
            }
        }
        programJson.setContactsKey(contactsKey);

        return programJson;
    }

    public static ProjectJson convert(Project project) {

        Program program = project.getProgram();

        ProjectJson projectJson = new ProjectJson(
                project.getId(),
                project.getActor().getLogin(),
                (program == null) ? -1 : program.getId(),
                project.getTitle(),
                project.getNomenclatureTitle(),
                project.getLongTitle(),
                project.getDescription(),
                project.getContractualFrame(),
                project.getIdentificationType(),
                project.getCreationDate(),
                project.getClosingDate(),
                project.getConfidential()
        );

        ArrayList<String> actorsKey = new ArrayList<String>(0);
        if (project.getActors() != null) {
            for (Actor a : project.getActors()) {
                String actorKey = a.getLogin();
                actorsKey.add(actorKey);
            }
        }
        projectJson.setActorsKey(actorsKey);

        ArrayList<Integer> contactsKey = new ArrayList<Integer>(0);
        if (project.getProjContactses() != null) {
            for (ProjContacts pc : project.getProjContactses()) {
                contactsKey.add(pc.getContact().getId());
            }
        }
        projectJson.setContactsKey(contactsKey);

        ArrayList<Integer> studyKeyList = new ArrayList<>();
        if (project.getStudies() != null) {
            for (Study study : project.getStudies()) {
                studyKeyList.add(study.getId());
            }
        }
        projectJson.setStudiesKeys(studyKeyList);

        return projectJson;
    }


    public static StudyJson convert(Study study) {

        if (study == null) {
            return null;
        }

        Project project = study.getProject();

        StudyJson studyJson = new StudyJson(
                study.getId(),
                study.getActor().getLogin(),
                (project == null) ? -1 : project.getId(),
                study.getTitle(),
                study.getNomenclatureTitle(),
                study.getLongTitle(),
                study.getDescription(),
                study.getIdentificationType(),
                study.getContractualFrame(),
                study.getBilled(),
                study.getCreationDate(),
                study.getClosingDate(),
                study.getEstimatedClosingDate(),
                study.getStatus(),
                study.getConfidential(),
                study.getComment()
        );

        ArrayList<String> actorsKey = new ArrayList<>(0);
        if (study.getActors() != null) {
            for (Actor a : study.getActors()) {
                String actorKey = a.getLogin();
                actorsKey.add(actorKey);
            }
        }
        studyJson.setActorsKey(actorsKey);

        ArrayList<Integer> contactsKey = new ArrayList<>(0);
        if (study.getStudyContactses() != null) {
            for (StudyContacts sc : study.getStudyContactses()) {
                contactsKey.add(sc.getContact().getId());
            }
        }
        studyJson.setContactsKey(contactsKey);

        return studyJson;
    }

    public static StudyPathJson convert(Study study, String path) {

        if (study == null) {
            return null;
        }

        Project project = study.getProject();
        Program program = (project == null) ? null : project.getProgram();

        StudyPathJson studyPathJson = new StudyPathJson(
                study.getId(),
                study.getActor().getLogin(),
                (project == null) ? -1 : project.getId(),
                study.getTitle(),
                study.getNomenclatureTitle(),
                study.getLongTitle(),
                study.getDescription(),
                study.getIdentificationType(),
                study.getContractualFrame(),
                study.getBilled(),
                study.getCreationDate(),
                study.getClosingDate(),
                study.getEstimatedClosingDate(),
                study.getStatus(),
                study.getConfidential(),
                path,
                (project == null) ? null : project.getNomenclatureTitle(),
                (program == null) ? null : program.getNomenclatureTitle()
                );

        ArrayList<String> actorsKey = new ArrayList<>(0);
        if (study.getActors() != null) {
            for (Actor a : study.getActors()) {
                String actorKey = a.getLogin();
                actorsKey.add(actorKey);
            }
        }
        studyPathJson.setActorsKey(actorsKey);

        ArrayList<Integer> contactsKey = new ArrayList<>(0);
        if (study.getStudyContactses() != null) {
            for (StudyContacts sc : study.getStudyContactses()) {
                contactsKey.add(sc.getContact().getId());
            }
        }
        studyPathJson.setContactsKey(contactsKey);

        return studyPathJson;
    }

    public static SampleJson convert(Sample sample, ArrayList<ProtocolApplicationJson> orderedProtocolApplications) {

        String actorKey = (sample.getActor() == null) ? null : sample.getActor().getLogin();

        SampleJson sampleJson = new SampleJson(
                sample.getName(), actorKey, convert(sample.getBiologicOrigin()), sample.getStudy().getId(),
                sample.getDescription(), sample.getVolume(), sample.getStatus(), sample.getQuantity(),
                sample.getOriginalName(), sample.getRadioactivity(), sample.getToxicity(), sample.getCreationDate(),
                orderedProtocolApplications);

        return sampleJson;
    }


    public static BiologicOriginJson convert(BiologicOrigin biologicOrigin) {

        if (biologicOrigin == null) {
            return null;
        }

        Integer sampleKind = (biologicOrigin.getSampleKind() == null) ? null : biologicOrigin.getSampleKind().getId();
        Integer sampleSubcellularLocalisation = (biologicOrigin.getSampleSubcellularLocalisation() == null) ? null : biologicOrigin.getSampleSubcellularLocalisation().getId();
        Integer sampleType = (biologicOrigin.getSampleType() == null) ? null : biologicOrigin.getSampleType().getId();


        BiologicOriginJson biologicOriginJson = new BiologicOriginJson(
                biologicOrigin.getId(), sampleKind, biologicOrigin.getSampleSpecies().getId(),
                sampleSubcellularLocalisation, sampleType, biologicOrigin.getCommentOrigin());

        return biologicOriginJson;
    }

    public static SampleSpeciesJson convert(SampleSpecies sampleSpecies) {

        if (sampleSpecies == null) {
            return null;
        }

        SampleSpeciesJson sampleSpeciesJson = new SampleSpeciesJson(
                sampleSpecies.getId(), sampleSpecies.getName());

        return sampleSpeciesJson;
    }

    public static SampleTypeJson convert(SampleType sampleType) {

        if (sampleType == null) {
            return null;
        }

        SampleTypeJson sampleTypeJson = new SampleTypeJson(
                sampleType.getId(), sampleType.getName());

        return sampleTypeJson;
    }

}
