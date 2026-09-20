package com.disasterconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geocode")
public class GeocodingController {

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("https://nominatim.openstreetmap.org")
                    .defaultHeader(
                            "User-Agent",
                            "DisasterConnect/1.0 (student project)"
                    )
                    .build();

    @GetMapping("/reverse")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lon) {

        try {

            Map<?, ?> result =
                    restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/reverse")
                                    .queryParam("format", "jsonv2")
                                    .queryParam("lat", lat)
                                    .queryParam("lon", lon)
                                    .queryParam("zoom", 13)
                                    .queryParam("addressdetails", 1)
                                    .build())
                            .retrieve()
                            .body(Map.class);

            if (result == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of(
                                "error",
                                "No location information returned"
                        ));
            }

            Map<?, ?> address =
                    result.get("address") instanceof Map
                            ? (Map<?, ?>) result.get("address")
                            : Map.of();

            List<String> parts = new ArrayList<>();

            addIfPresent(parts, address.get("neighbourhood"));
            addIfPresent(parts, address.get("suburb"));
            addIfPresent(parts, address.get("town"));
            addIfPresent(parts, address.get("city"));
            addIfPresent(parts, address.get("state_district"));
            addIfPresent(parts, address.get("state"));

            String placeName;

            if (!parts.isEmpty()) {

                placeName = String.join(", ", parts);

            } else {

                Object displayName =
                        result.get("display_name");

                placeName =
                        displayName != null
                                ? displayName.toString()
                                : "Location detected";
            }

            return ResponseEntity.ok(
                    Map.of(
                            "placeName",
                            placeName
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Unable to reverse geocode location"
                            )
                    );
        }
    }

    private void addIfPresent(
            List<String> parts,
            Object value) {

        if (value != null
                && !value.toString().isBlank()
                && !parts.contains(value.toString())) {

            parts.add(value.toString());
        }
    }
}