package com.disasterconnect.service;

import com.disasterconnect.entity.Resource;
import com.disasterconnect.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(
            ResourceRepository resourceRepository) {

        this.resourceRepository = resourceRepository;
    }

    public Resource addResource(Resource resource) {

        if (resource.getStatus() == null ||
                resource.getStatus().isBlank()) {

            resource.setStatus("AVAILABLE");
        }

        return resourceRepository.save(resource);
    }

    public List<Resource> getAllResources() {

        return resourceRepository.findAll();
    }

    public Optional<Resource> getResourceById(Long id) {

        return resourceRepository.findById(id);
    }

    public List<Resource> getResourcesByCategory(
            String category) {

        return resourceRepository
                .findByCategoryIgnoreCase(category);
    }

    public List<Resource> getResourcesByStatus(
            String status) {

        return resourceRepository
                .findByStatusIgnoreCase(status);
    }

    public List<Resource> findResourcesByLocation(
            String location) {

        return resourceRepository
                .findByLocationContainingIgnoreCase(location);
    }

    public Optional<Resource> updateResourceStatus(
            Long id,
            String status) {

        Optional<Resource> optionalResource =
                resourceRepository.findById(id);

        if (optionalResource.isEmpty()) {
            return Optional.empty();
        }

        Resource resource = optionalResource.get();

        resource.setStatus(status);

        return Optional.of(
                resourceRepository.save(resource)
        );
    }

    public Optional<Resource> updateQuantity(
            Long id,
            Integer quantity) {

        if (quantity == null || quantity < 0) {
            return Optional.empty();
        }

        Optional<Resource> optionalResource =
                resourceRepository.findById(id);

        if (optionalResource.isEmpty()) {
            return Optional.empty();
        }

        Resource resource = optionalResource.get();

        resource.setQuantity(quantity);

        return Optional.of(
                resourceRepository.save(resource)
        );
    }

    public boolean deleteResource(Long id) {

        if (!resourceRepository.existsById(id)) {
            return false;
        }

        resourceRepository.deleteById(id);

        return true;
    }
}
