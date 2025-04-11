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

import fr.edyp.epims.database.entities.RobotPlanning;
import fr.edyp.epims.database.entities.TreatmentsApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreatmentsApplicationRepository  extends JpaRepository<TreatmentsApplication, Integer> {


    //@Query(value = "SELECT max(treatApp.rank) FROM TreatmentsApplication as treatApp WHERE (treatApp.treatments.id = :treatmentsId) ")
    //public Integer maxRank(@Param("treatmentsId") Integer treatmentsId);

    @Query(value = "SELECT max(treatApp.rank) FROM treatments_application as treatApp WHERE (treatApp.treatments = :treatmentsId) ", nativeQuery = true)
    public Integer maxRank(@Param("treatmentsId") Integer treatmentsId);


}

