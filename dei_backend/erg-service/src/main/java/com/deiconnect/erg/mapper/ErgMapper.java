package com.deiconnect.erg.mapper;

import com.deiconnect.erg.client.UserClient;
import com.deiconnect.erg.dto.ErgResponse;
import com.deiconnect.erg.entity.ERG;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErgMapper {

    private final UserClient userClient;

    public ErgResponse toResponse(ERG erg) {
        String creatorManagerName = null;
        if (erg.getCreatorManagerId() != null) {
            try {
                creatorManagerName = userClient.getByIdInternal(erg.getCreatorManagerId()).name();
            } catch (Exception e) {
                creatorManagerName = "System User (Offline)";
            }
        }
        return new ErgResponse(
                erg.getId(),
                erg.getErgName(),
                erg.getFocus(),
                erg.getMission(),
                erg.getExecutiveSponsorId(),
                erg.getErgLeadId(),
                erg.getMemberCount(),
                erg.getFoundedDate(),
                erg.getStatus(),
                erg.getCreatedDate(),
                erg.getLastModifiedDate(),
                erg.getCreatorManagerId(),
                creatorManagerName);
    }
}
