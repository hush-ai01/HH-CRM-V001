package com.highlands.highlandscrmbackend.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request
    ) {

        ClientResponse response =
                clientService.createClient(request);

        URI location = URI.create(
                "/api/v1/clients/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {

        return ResponseEntity.ok(
                clientService.getAllClients()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                clientService.getClientById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request
    ) {

        return ResponseEntity.ok(
                clientService.updateClient(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClientResponse> updateClientStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ClientStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
                clientService.updateClientStatus(id, request)
        );
    }
}