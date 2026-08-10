package com.deiconnect.erg.service;

import com.deiconnect.erg.dto.CreateErgRequest;
import com.deiconnect.erg.dto.ErgResponse;
import com.deiconnect.erg.dto.UpdateErgRequest;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ErgService {

    ErgResponse create(CreateErgRequest request);

    ErgResponse update(Long id, UpdateErgRequest request);

    Page<ErgResponse> search(ErgFocus focus, ErgStatus status, Pageable pageable);

    ErgResponse getById(Long id);

    void assertEmployeeCanAccess(Long ergId);

    void delete(Long id);

    ERG findOrThrow(Long id);

    ERG requireManageableChapter(Long ergId);

    void recomputeMemberCount(Long ergId);

    long getActiveMemberCount(String scope, String scopeValue, Long hrId);
}
