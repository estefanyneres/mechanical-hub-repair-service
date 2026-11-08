package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.service.UpsertServiceRequest;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.ServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.ServicePresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;

import java.util.List;
import java.util.UUID;

public class ServiceUseCase {

    private final ServiceGateway serviceGateway;
    private final OrderTaskGateway orderTaskGateway;
    private final ServicePresenter servicePresenter;

    public ServiceUseCase(ServiceGateway serviceGateway, OrderTaskGateway orderTaskGateway,
                          ServicePresenter servicePresenter) {
        this.serviceGateway = serviceGateway;
        this.orderTaskGateway = orderTaskGateway;
        this.servicePresenter = servicePresenter;
    }

    public ServiceResponse create(UpsertServiceRequest request) {
        ServiceData serviceData = ServiceData.create(
                request.name(),
                request.description(),
                request.laborCost()
        );

        serviceGateway.save(serviceData);
        return servicePresenter.toResponse(serviceData);
    }

    public ServiceResponse update(UUID id, UpsertServiceRequest request) {
        ServiceData serviceData = serviceGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado com ID: " + id));

        serviceData.update(
                request.name(),
                request.description(),
                request.laborCost()
        );

        serviceGateway.save(serviceData);
        return servicePresenter.toResponse(serviceData);
    }

    public ServiceResponse findById(UUID id) {
        ServiceData serviceData = serviceGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado com ID: " + id));
        return servicePresenter.toResponse(serviceData);
    }

    public ServiceData findServiceById(UUID id) {
        return serviceGateway.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado com ID: " + id));
    }

    public List<ServiceResponse> findAll() {
        return servicePresenter.toResponseList(serviceGateway.findAll());
    }

    public void delete(UUID id) {
        if (orderTaskGateway.existsByServiceId(id)) {
            throw new BusinessRuleException("Serviço está vinculado a ordens de serviço e não pode ser deletado");
        }
        serviceGateway.deleteById(id);
    }

    public List<ServiceData> findAll(List<UUID> serviceIds) {
        return serviceGateway.findByIds(serviceIds);
    }
}
