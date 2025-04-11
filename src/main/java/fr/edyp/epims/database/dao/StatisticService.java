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

import fr.edyp.epims.json.AcquisitionStatisticJson;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class StatisticService {

    @PersistenceContext
    private EntityManager em;

    public List<AcquisitionStatisticJson> acquistionsStatistics(int year) {

        String sql =
        "select count(acq.a) as Acq_All, sum(sizeMo) as Acq_ALL_Size, sum(case when acq.nature = 'Recherche' THEN 1 ELSE 0 END) as Acq_Rech, "+
                "sum(case when acq.nature = 'Recherche' THEN sizeMo ELSE 0 END) as Acq_Rech_Size, "+
                "acq.week, acq.year,acq.spectro as Spectro "+

        " from "+
        " (SELECT "+
        "    a.nature, "+
        "    a , "+
        "    protocol_application.date, "+
        "    date_part('week',protocol_application.date) as week, "+
        "    date_part('year',protocol_application.date) as year, "+
        "    attached_file.size_mo as sizeMo, "+
        "    instrument.name AS spectro "+
        "    FROM "+
        "       public.acquisition a, "+
        "       public.instrument, "+
        "       public.protocol_application, "+
        "       public.attached_file, "+
        "       public.file_link "+
        "    WHERE "+
        "       a.spectrometer = instrument.id AND "+
        "       a.id = file_link.id_fk AND "+
        "       protocol_application.id = a.id AND "+
        "       file_link.attached_file = attached_file.id AND "+
        "       file_link.associated_entity = 'acquisition' "+
        " ) as acq "+
        " where year = "+year+
        " group by week, year, spectro "+
        " order by spectro, year, week ";

        List<Object[]> acqStatisticList = em.createNativeQuery(sql ).getResultList();


        List<AcquisitionStatisticJson> resultList = new ArrayList<>(acqStatisticList.size());
        for (Object[] values : acqStatisticList) {

            int acquisitionTotal = ((BigInteger)values[0]).intValue();
            double acquisitionSizeTotal = ((Double)values[1]).doubleValue();
            int researchAcquisitionTotal = ((BigInteger)values[2]).intValue();
            double researchAcquisitionSizeTotal = ((Double)values[3]).doubleValue();
            int week = (int) Math.round(((Double)values[4]).doubleValue());
            int yearAcq = (int) Math.round(((Double)values[5]).doubleValue());
            String instrument = (String) values[6];

            AcquisitionStatisticJson acquisitionStatisticJson = new AcquisitionStatisticJson(acquisitionTotal, acquisitionSizeTotal, researchAcquisitionTotal, researchAcquisitionSizeTotal, week, yearAcq, instrument);
            resultList.add(acquisitionStatisticJson);
        }


        return resultList;
    }
}
