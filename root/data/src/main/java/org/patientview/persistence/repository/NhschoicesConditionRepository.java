package org.patientview.persistence.repository;

import org.patientview.persistence.model.NhschoicesCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/06/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NhschoicesConditionRepository extends JpaRepository<NhschoicesCondition, Long> {

    @Query("SELECT c FROM NhschoicesCondition c WHERE c.code = :code")
    NhschoicesCondition findOneByCode(@Param("code") String code);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NhschoicesCondition WHERE code IN :codes")
    void deleteByCode(@Param("codes") List<String> codes);
}
