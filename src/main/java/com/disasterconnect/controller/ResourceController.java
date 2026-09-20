package com.disasterconnect.controller;

import com.disasterconnect.entity.Resource;
import com.disasterconnect.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(
            ResourceService resourceService) {

        this.resourceService = resourceService;
    }

    /*
     * ============================================
     * ADD RESOURCE
     * ============================================
     */

    @PostMapping("/resource/add")
    public String addResource(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam Integer quantity,
            @RequestParam String unit,
            @RequestParam String location) {

        Resource resource = new Resource(
                name,
                category,
                quantity,
                unit,
                location
        );

        resourceService.addResource(resource);

        return "redirect:/dashboard.html";
    }


    /*
     * ============================================
     * GET ALL RESOURCES
     * ============================================
     */

    @GetMapping("/api/resources")
    @ResponseBody
    public ResponseEntity<List<Resource>> getAllResources() {

        return ResponseEntity.ok(
                resourceService.getAllResources()
        );
    }


    /*
     * ============================================
     * GET RESOURCE BY ID
     * ============================================
     */

    @GetMapping("/api/resources/{id}")
    @ResponseBody
    public ResponseEntity<Resource> getResourceById(
            @PathVariable Long id) {

        Optional<Resource> resource =
                resourceService.getResourceById(id);

        return resource
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    /*
     * ============================================
     * GET BY CATEGORY
     * ============================================
     */

    @GetMapping("/api/resources/category/{category}")
    @ResponseBody
    public ResponseEntity<List<Resource>>
    getResourcesByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                resourceService
                        .getResourcesByCategory(category)
        );
    }


    /*
     * ============================================
     * GET BY STATUS
     * ============================================
     */

    @GetMapping("/api/resources/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Resource>>
    getResourcesByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                resourceService
                        .getResourcesByStatus(status)
        );
    }


    /*
     * ============================================
     * SEARCH BY LOCATION
     * ============================================
     */

    @GetMapping("/api/resources/location")
    @ResponseBody
    public ResponseEntity<List<Resource>>
    findResourcesByLocation(
            @RequestParam String location) {

        return ResponseEntity.ok(
                resourceService
                        .findResourcesByLocation(location)
        );
    }


    /*
     * ============================================
     * UPDATE STATUS
     * ============================================
     */

    @PutMapping("/api/resources/{id}/status")
    @ResponseBody
    public ResponseEntity<Resource> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Optional<Resource> resource =
                resourceService.updateResourceStatus(
                        id,
                        status
                );

        return resource
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    /*
     * ============================================
     * UPDATE QUANTITY
     * ============================================
     */

    @PutMapping("/api/resources/{id}/quantity")
    @ResponseBody
    public ResponseEntity<Resource> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        Optional<Resource> resource =
                resourceService.updateQuantity(
                        id,
                        quantity
                );

        return resource
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    /*
     * ============================================
     * DELETE RESOURCE
     * ============================================
     */

    @DeleteMapping("/api/resources/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id) {

        boolean deleted =
                resourceService.deleteResource(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}