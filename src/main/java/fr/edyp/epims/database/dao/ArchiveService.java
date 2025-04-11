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



import fr.edyp.epims.json.ControlAcquisitionArchivableJson;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ArchiveService {

    @PersistenceContext
    private EntityManager em;

    public List<ControlAcquisitionArchivableJson> controlToBeArchivedByMonthYearInstrument() {

        String sql = " SELECT DISTINCT cast( extract ('month' from af.\"date\") as int) as mois, cast(extract ('year' from af.\"date\") as int) as annee, i.\"name\" instrum \n " +
                "FROM acquisition a, file_link fl, attached_file af, instrument i \n " +
                "WHERE a.nature <> 'Recherche' AND \n " +
                "a.id = fl.id_fk and \n" +
                "fl.associated_entity = 'acquisition' AND \n" +
                "af.id = fl.attached_file AND \n" +
                "af.archived is not true AND \n" +
                "i.id=a.spectrometer order by instrum, annee, mois ";


        List<Object[]> resultSQLList = em.createNativeQuery(sql).getResultList();


        List<ControlAcquisitionArchivableJson> resultList = new ArrayList<>(resultSQLList.size());
        for (Object[] values : resultSQLList) {

            ControlAcquisitionArchivableJson dataToArchiveJson = new ControlAcquisitionArchivableJson();

            dataToArchiveJson.month = ((Integer)values[0]);
            dataToArchiveJson.year = ((Integer)values[1]);
            dataToArchiveJson.instrument = (String) values[2];

            resultList.add(dataToArchiveJson);
        }


        return resultList;
    }



    public List<AcquisitionToArchive> controlToBeArchived(ControlAcquisitionArchivableJson data) {

        String sql = " SELECT  a.nature, af.id, af.name \n" +
                "FROM acquisition a, file_link fl, attached_file af, instrument i \n" +
                "WHERE a.nature <> 'Recherche' AND \n" +
                "a.id = fl.id_fk AND \n" +
                "fl.associated_entity = 'acquisition' AND \n" +
                "af.id = fl.attached_file AND \n" +
                "af.archived is not true AND \n" +
                "i.id=a.spectrometer AND \n" +
                "extract ('year' from af.\"date\") = "+data.getYear()+" AND \n" +
                "extract ('month' from af.\"date\") = "+data.getMonth()+" AND \n" +
                "i.\"name\" = '"+data.getInstrument()+"'";


        List<Object[]> resultSQLList = em.createNativeQuery(sql).getResultList();


        List<AcquisitionToArchive> resultList = new ArrayList<>(resultSQLList.size());
        for (Object[] values : resultSQLList) {

            AcquisitionToArchive acquisitionToArchive = new AcquisitionToArchive();

            acquisitionToArchive.nature = (String) values[0];
            acquisitionToArchive.attachedFileId = ((Integer)values[1]);
            acquisitionToArchive.fileName = (String) values[2];

            resultList.add(acquisitionToArchive);
        }


        return resultList;
    }


    public class AcquisitionToArchive {
        public String nature;
        public int attachedFileId;
        public String fileName;
    }



}
