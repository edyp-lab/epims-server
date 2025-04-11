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

import fr.edyp.epims.database.entities.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SampleRepository extends JpaRepository<Sample, String> {

    List<Sample> findByStudy(Integer id);

    Optional<Sample> findByName(String name);

    @Query(value = "SELECT s FROM Sample as s, TreatmentsApplication as ta WHERE s.treatments.id=ta.treatments.id and ta.protocolApplication.id= :paId) ")
    List<Sample> findByProtocolApplication(@Param("paId") Integer paId);

}
