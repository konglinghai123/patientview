package org.patientview.persistence.repository;

import org.patientview.persistence.model.ContactPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ContactPointRepository extends JpaRepository<ContactPoint, Long> {
}
