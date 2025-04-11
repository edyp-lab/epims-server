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

package fr.edyp.epims.database.dao;

import fr.edyp.epims.json.ProtocolApplicationJson;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ProtocolApplicationService {

    @PersistenceContext
    private EntityManager em;

    public List<ProtocolApplicationJson> searchAcquisitions(String searchText, String acquisitionType, int instrumentId, String studyMemberActorKey, String startDate, String endDate) {




        String queryString = "SELECT new fr.edyp.epims.json.ProtocolApplicationJson(pa.id, s.name, s.actor.login, pa.actor.login, pa.name, pa.date, pa.comment, s.study.id, pa.acquisition.id, pa.acquisition.spectrometer.id, pa.acquisition.nature, pa.acquisition.durationMin) "+
                " FROM ProtocolApplication as pa "+
                " LEFT OUTER JOIN TreatmentsApplication as ta ON pa.id = ta.protocolApplication.id "+
                " LEFT OUTER JOIN Sample as s ON ta.treatments = s.treatments.id ";

        boolean firstWhere = true;

        boolean studyMemberParameter = ((studyMemberActorKey != null) && (studyMemberActorKey.length()>1));
        if (studyMemberParameter) {
            queryString += (firstWhere) ? " WHERE (" : " AND ("; firstWhere = false;
            queryString += " s.actor.login = :actor OR (s.study.id IN (SELECT DISTINCT sc.study.id from StudyContacts as sc , Actor as a WHERE a.login = :mactor AND sc.contact.id = a.contact.id ) ) )";
        }


        boolean searchTextParameter = ((searchText != null) && (searchText.length()>1));
        if (searchTextParameter) {
            searchText = "%"+searchText+"%";
            queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
            queryString += " ((LOWER(pa.name) LIKE :searchText) OR (LOWER(pa.comment) LIKE :searchText) OR (LOWER(s.name) LIKE :searchText)) ";
        }

        boolean natureParameter = ((acquisitionType != null) && (acquisitionType.length()>1));
        if (natureParameter) {
            queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
            queryString += " pa.acquisition.nature = :nature ";
        }

        boolean instrumentParameter = (instrumentId>=0);
        if (instrumentParameter) {
            queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
            queryString += " pa.acquisition.spectrometer.id = :instrumentId ";
        }

        Date startDateD = null;
        boolean startDateParameter = ((startDate!=null) && (startDate.length()>1));;
        if (startDateParameter) {
            queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
            queryString += " pa.date >= :startdate ";
            startDateD = convertToDate(startDate);
        }

        Date endDateD = null;
        boolean endDateParameter = ((endDate!=null) && (endDate.length()>1));;
        if (endDateParameter) {
            queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
            queryString += " pa.date <= :enddate ";
            endDateD = convertToDate(endDate);
            Calendar c = Calendar.getInstance();
            c.setTime(endDateD);
            c.add(Calendar.DATE, 1);
            endDateD = c.getTime();
        }


        TypedQuery query = em.createQuery(queryString, ProtocolApplicationJson.class);
        if (natureParameter) query.setParameter("nature", acquisitionType);
        if (searchTextParameter) query.setParameter("searchText", searchText.toLowerCase());
        if (instrumentParameter) query.setParameter("instrumentId", instrumentId);
        if (studyMemberParameter) query.setParameter("actor", studyMemberActorKey);
        if (studyMemberParameter) query.setParameter("mactor", studyMemberActorKey);
        if (startDateParameter) query.setParameter("startdate", startDateD);
        if (endDateParameter) query.setParameter("enddate", endDateD);

        return query.getResultList();
    }

    public List<ProtocolApplicationJson> searchAcquisitions(String acquisitionName, String instrumentName) {

        String queryString = "SELECT new fr.edyp.epims.json.ProtocolApplicationJson(pa.id, s.name, s.actor.login, pa.actor.login, pa.name, pa.date, pa.comment, s.study.id, pa.acquisition.id, pa.acquisition.spectrometer.id, pa.acquisition.nature, pa.acquisition.durationMin) "+
                " FROM ProtocolApplication as pa "+
                " LEFT OUTER JOIN TreatmentsApplication as ta ON pa.id = ta.protocolApplication.id "+
                " LEFT OUTER JOIN Sample as s ON ta.treatments = s.treatments.id ";

        queryString += " WHERE (LOWER(pa.name) = :acquisitionName) ";

        queryString += " AND pa.acquisition.spectrometer.instrument.name = :instrumentName ";

        TypedQuery query = em.createQuery(queryString, ProtocolApplicationJson.class);
        query.setParameter("acquisitionName", acquisitionName.toLowerCase());
        query.setParameter("instrumentName", instrumentName);


        return query.getResultList();
    }


    public static Date convertToDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (Exception e) {
            return null;
        }

    }
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


    /**
     *
     public List<ProtocolApplicationJson> searchAcquisitions(String searchText, String acquisitionType, int instrumentId, String studyMemberActorKey, String startDate, String endDate) {


     boolean studyMemberParameter = ((studyMemberActorKey != null) && (studyMemberActorKey.length()>1));

     String queryString = "SELECT DISTINCT new fr.edyp.epims.json.ProtocolApplicationJson(pa.id, s.name, s.actor.login, pa.actor.login, pa.name, pa.date, pa.comment, s.study.id, pa.acquisition.id, pa.acquisition.spectrometer.id, pa.acquisition.nature, pa.acquisition.durationMin) "+
     " FROM ProtocolApplication as pa ";

     if (studyMemberParameter) {
     queryString += " ,StudyContacts sc, Actor a ";
     }

     queryString += " LEFT OUTER JOIN TreatmentsApplication as ta ON pa.id = ta.protocolApplication.id "+
     " LEFT OUTER JOIN Sample as s ON ta.treatments = s.treatments.id ";

     boolean firstWhere = true;

     if (studyMemberParameter) {
     queryString += (firstWhere) ? " WHERE (" : " AND ("; firstWhere = false;
     queryString += " s.actor.login = :actor OR (sc.study.id = s.study.id AND sc.contact.id = a.contact.id AND a.login= :mactor))";
     }

     boolean searchTextParameter = ((searchText != null) && (searchText.length()>1));
     if (searchTextParameter) {
     searchText = "%"+searchText+"%";
     queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
     queryString += " (pa.name like :searchText OR pa.comment like :searchText) ";
     }

     boolean natureParameter = ((acquisitionType != null) && (acquisitionType.length()>1));
     if (natureParameter) {
     queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
     queryString += " pa.acquisition.nature = :nature ";
     }

     boolean instrumentParameter = (instrumentId>=0);
     if (instrumentParameter) {
     queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
     queryString += " pa.acquisition.spectrometer.id = :instrumentId ";
     }

     Date startDateD = null;
     boolean startDateParameter = ((startDate!=null) && (startDate.length()>1));;
     if (startDateParameter) {
     queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
     queryString += " pa.date >= :startdate ";
     startDateD = convertToDate(startDate);
     }

     Date endDateD = null;
     boolean endDateParameter = ((endDate!=null) && (endDate.length()>1));;
     if (endDateParameter) {
     queryString += (firstWhere) ? " WHERE " : " AND "; firstWhere = false;
     queryString += " pa.date <= :enddate ";
     endDateD = convertToDate(endDate);
     Calendar c = Calendar.getInstance();
     c.setTime(endDateD);
     c.add(Calendar.DATE, 1);
     endDateD = c.getTime();
     }


     TypedQuery query = em.createQuery(queryString, ProtocolApplicationJson.class);
     if (natureParameter) query.setParameter("nature", acquisitionType);
     if (searchTextParameter) query.setParameter("searchText", searchText);
     if (instrumentParameter) query.setParameter("instrumentId", instrumentId);
     if (studyMemberParameter) query.setParameter("actor", studyMemberActorKey);
     if (studyMemberParameter) query.setParameter("mactor", studyMemberActorKey);
     if (startDateParameter) query.setParameter("startdate", startDateD);
     if (endDateParameter) query.setParameter("enddate", endDateD);

     return query.getResultList();
     }
     */

}