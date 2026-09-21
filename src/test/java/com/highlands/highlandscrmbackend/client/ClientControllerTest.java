package com.highlands.highlandscrmbackend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID clientId;

    @BeforeEach
    void setUp() {

        ClientController clientController =
                new ClientController(clientService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(clientController)
                .build();

        objectMapper = new ObjectMapper();

        clientId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE CLIENT
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateClient() throws Exception {

        ClientResponse response = createClientResponse();

        CreateClientRequest request =
                new CreateClientRequest(
                        "Test Client",
                        ClientType.BUYER,
                        "Johannesburg",
                        new BigDecimal("1000"),
                        new BigDecimal("250"),
                        "30 Days",
                        null,
                        "SACD City Deep",
                        "John Doe",
                        "0821234567",
                        "john@example.com",
                        AccountStatus.PROSPECT,
                        "Follow up",
                        null,
                        null,
                        ClientVisibility.PRIVATE
                );

        when(clientService.createClient(any(CreateClientRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "/api/v1/clients/" + clientId
                        )
                )
                .andExpect(jsonPath("$.id").value(clientId.toString()))
                .andExpect(jsonPath("$.name").value("Test Client"))
                .andExpect(jsonPath("$.type").value("BUYER"));

        verify(clientService)
                .createClient(any(CreateClientRequest.class));
    }

    // -------------------------------------------------------------------------
    // GET ALL CLIENTS
    // -------------------------------------------------------------------------

    @Test
    void shouldGetAllClients() throws Exception {

        ClientResponse response = createClientResponse();

        when(clientService.getAllClients())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/clients")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(clientId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Client"));

        verify(clientService)
                .getAllClients();
    }

    // -------------------------------------------------------------------------
    // GET CLIENT BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetClientById() throws Exception {

        ClientResponse response = createClientResponse();

        when(clientService.getClientById(clientId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/clients/{id}", clientId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()))
                .andExpect(jsonPath("$.name").value("Test Client"))
                .andExpect(jsonPath("$.type").value("BUYER"));

        verify(clientService)
                .getClientById(clientId);
    }

    // -------------------------------------------------------------------------
    // UPDATE CLIENT
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateClient() throws Exception {

        ClientResponse response = createClientResponse();

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Updated Client",
                        ClientType.BUYER,
                        "Pretoria",
                        new BigDecimal("1500"),
                        new BigDecimal("300"),
                        "60 Days",
                        null,
                        "SACD City Deep",
                        "Jane Doe",
                        "0831234567",
                        "jane@example.com",
                        AccountStatus.ACTIVE,
                        "Updated follow up",
                        null,
                        null,
                        ClientVisibility.PRIVATE
                );

        ClientResponse updatedResponse =
                new ClientResponse(
                        clientId,
                        UUID.randomUUID(),
                        "Updated Client",
                        ClientType.BUYER,
                        "Pretoria",
                        new BigDecimal("1500"),
                        new BigDecimal("300"),
                        "60 Days",
                        null,
                        "SACD City Deep",
                        "Jane Doe",
                        "0831234567",
                        "jane@example.com",
                        AccountStatus.ACTIVE,
                        "Updated follow up",
                        null,
                        null,
                        ClientVisibility.PRIVATE,
                        null,
                        null,
                        null,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(clientService.updateClient(
                eq(clientId),
                any(UpdateClientRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        put("/clients/{id}", clientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Client"))
                .andExpect(jsonPath("$.area").value("Pretoria"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(clientService)
                .updateClient(
                        eq(clientId),
                        any(UpdateClientRequest.class)
                );
    }

    // -------------------------------------------------------------------------
    // UPDATE CLIENT STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateClientStatus() throws Exception {

        ClientResponse response = createClientResponse();

        ClientStatusUpdateRequest request =
                new ClientStatusUpdateRequest(
                        AccountStatus.ACTIVE
                );

        ClientResponse updatedResponse =
                new ClientResponse(
                        clientId,
                        UUID.randomUUID(),
                        "Test Client",
                        ClientType.BUYER,
                        "Johannesburg",
                        null,
                        null,
                        null,
                        null,
                        "SACD City Deep",
                        null,
                        null,
                        null,
                        AccountStatus.ACTIVE,
                        null,
                        null,
                        null,
                        ClientVisibility.PRIVATE,
                        null,
                        null,
                        null,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        when(clientService.updateClientStatus(
                eq(clientId),
                any(ClientStatusUpdateRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        patch("/clients/{id}/status", clientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(clientService)
                .updateClientStatus(
                        eq(clientId),
                        any(ClientStatusUpdateRequest.class)
                );
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectCreateClientWhenNameIsMissing() throws Exception {

        String request = """
                {
                    "type": "BUYER"
                }
                """;

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateClientWhenTypeIsMissing() throws Exception {

        String request = """
                {
                    "name": "Test Client"
                }
                """;

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateClientWhenEmailIsInvalid() throws Exception {

        String request = """
                {
                    "name": "Test Client",
                    "type": "BUYER",
                    "contactEmail": "not-an-email"
                }
                """;

        mockMvc.perform(
                        post("/clients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // INVALID PATH VARIABLE
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectInvalidClientId() throws Exception {

        mockMvc.perform(
                        get("/clients/not-a-uuid")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // RESPONSE FIXTURE
    // -------------------------------------------------------------------------

    private ClientResponse createClientResponse() {

        return new ClientResponse(
                clientId,
                UUID.randomUUID(),
                "Test Client",
                ClientType.BUYER,
                "Johannesburg",
                new BigDecimal("1000"),
                new BigDecimal("250"),
                "30 Days",
                null,
                "SACD City Deep",
                "John Doe",
                "0821234567",
                "john@example.com",
                AccountStatus.PROSPECT,
                "Follow up",
                null,
                null,
                ClientVisibility.PRIVATE,
                null,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}