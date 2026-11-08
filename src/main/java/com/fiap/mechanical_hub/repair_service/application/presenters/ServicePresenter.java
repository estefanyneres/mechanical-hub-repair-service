package com.fiap.mechanical_hub.repair_service.application.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;

import java.util.List;

public interface ServicePresenter {
    ServiceResponse toResponse(ServiceData serviceData);
    List<ServiceResponse> toResponseList(List<ServiceData> services);
}
