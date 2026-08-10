package com.deiconnect.erg.repository;

import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ErgRepository extends JpaRepository<ERG, Long> {

    @Query("select e from ERG e where "
            + "(:focus is null or e.focus = :focus) and "
            + "(:status is null or e.status = :status) and "
            + "(:creatorManagerId is null or e.creatorManagerId = :creatorManagerId)")
    Page<ERG> search(@Param("focus") ErgFocus focus,
                     @Param("status") ErgStatus status,
                     @Param("creatorManagerId") Long creatorManagerId,
                     Pageable pageable);

    @Query("select e from ERG e where "
            + "(:focus is null or e.focus = :focus) and "
            + "(:status is null or e.status = :status) and "
            + "(e.creatorManagerId is null or e.creatorManagerId = :managerId)")
    Page<ERG> searchVisibleToEmployee(@Param("focus") ErgFocus focus,
                                      @Param("status") ErgStatus status,
                                      @Param("managerId") Long managerId,
                                      Pageable pageable);

    @Query("select e from ERG e where "
            + "(:focus is null or e.focus = :focus) and "
            + "(:status is null or e.status = :status) and "
            + "e.ergLeadId = :ergLeadId")
    Page<ERG> searchForLead(@Param("focus") ErgFocus focus,
                            @Param("status") ErgStatus status,
                            @Param("ergLeadId") Long ergLeadId,
                            Pageable pageable);
}
