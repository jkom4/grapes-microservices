package grapes.microservices.authservice.services.eid;

import grapes.microservices.authservice.dto.EIDCardInfo;

public interface EIDCardService {
    EIDCardInfo readCard() throws Exception;
}