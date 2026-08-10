package com.deiconnect.erg.repository;

import com.deiconnect.erg.entity.ERGEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErgEventRepository extends JpaRepository<ERGEvent, Long> {

    Page<ERGEvent> findByErg_Id(Long ergId, Pageable pageable);
}
