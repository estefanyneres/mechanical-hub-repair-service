package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.application.presenters.ServicePresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServicePresenterImpl implements ServicePresenter {

    @Override
    public ServiceResponse toResponse(ServiceData serviceData) {
        return new ServiceResponse(
                serviceData.getId(),
                serviceData.getName(),
                serviceData.getDescription(),
                serviceData.getLaborCost(),
                serviceData.getTotalPrice(),
                serviceData.isActive(),
                serviceData.getCreatedAt(),
                serviceData.getUpdatedAt()
        );
    }

    @Override
    public List<ServiceResponse> toResponseList(List<ServiceData> services) {
        return services.stream().map(this::toResponse).toList();
    }
}
