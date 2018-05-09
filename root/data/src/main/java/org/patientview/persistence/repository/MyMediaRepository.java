package org.patientview.persistence.repository;

import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This gets all the news associated with a user. News can be link via
 * group or role.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MyMediaRepository extends JpaRepository<MyMedia, Long> {

    @Query("SELECT c FROM MyMedia c WHERE c.creator = :user AND c.deleted = :deleted")
    Page<List<MyMedia>> getByCreator(@Param("user") User user,
                                     @Param("deleted") Boolean deleted,
                                     Pageable pageable);

    @Query("SELECT SUM(octet_length(c.content))/1000000 FROM MyMedia c WHERE c.creator = :user AND c.deleted = :deleted")
    Long getUserTotal(@Param("user") User user, @Param("deleted") Boolean deleted);

}
