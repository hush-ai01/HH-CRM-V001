package com.highlands.highlandscrmbackend.fleet;

import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTruckRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TruckResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTruckRequest;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TruckControllerTest {

    @Mock
    private TruckService truckService;

    @InjectMocks
    private TruckController truckController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID truckId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(truckController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        truckId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    private TruckResponse truckResponse() {
        return new TruckResponse(
                truckId,
                companyId,
                "ABC 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                TruckStatus.AVAILABLE,
                "Highlands Holdings",
                "John Doe",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    void create_shouldReturn201() throws Exception {

        CreateTruckRequest request = new CreateTruckRequest(
                "ABC 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                "Highlands Holdings",
                "John Doe"
        );

        TruckResponse response = truckResponse();

        when(truckService.create(any(CreateTruckRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/trucks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/trucks/" + truckId
                ))
                .andExpect(jsonPath("$.id").value(truckId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.registrationNumber").value("ABC 123 GP"))
                .andExpect(jsonPath("$.make").value("Volvo"))
                .andExpect(jsonPath("$.model").value("FH16"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(truckService)
                .create(any(CreateTruckRequest.class));
    }

    @Test
    void create_shouldReturn400ForInvalidRequest() throws Exception {

        CreateTruckRequest request = new CreateTruckRequest(
                "",
                "",
                "",
                null,
                null,
                null
        );

        mockMvc.perform(
                        post("/trucks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(truckService);
    }

    @Test
    void findAll_shouldReturn200() throws Exception {

        TruckResponse response = truckResponse();

        when(truckService.findAll())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/trucks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(truckId.toString()))
                .andExpect(jsonPath("$[0].registrationNumber")
                        .value("ABC 123 GP"));

        verify(truckService)
                .findAll();
    }

    @Test
    void findById_shouldReturn200() throws Exception {

        when(truckService.findById(truckId))
                .thenReturn(truckResponse());

        mockMvc.perform(
                        get("/trucks/{truckId}", truckId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(truckId.toString()))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("ABC 123 GP"));

        verify(truckService)
                .findById(truckId);
    }

    @Test
    void findById_shouldPropagateNotFoundException() throws Exception {

        when(truckService.findById(truckId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Truck not found: " + truckId
                        )
                );

        mockMvc.perform(
                        get("/trucks/{truckId}", truckId)
                )
                .andExpect(status().isNotFound());

        verify(truckService)
                .findById(truckId);
    }

    @Test
    void update_shouldReturn200() throws Exception {

        UpdateTruckRequest request = new UpdateTruckRequest(
                "ABC 999 GP",
                "Scania",
                "R500",
                new BigDecimal("38.0000"),
                "Highlands Holdings",
                "Jane Doe"
        );

        TruckResponse response = new TruckResponse(
                truckId,
                companyId,
                "ABC 999 GP",
                "Scania",
                "R500",
                new BigDecimal("38.0000"),
                TruckStatus.AVAILABLE,
                "Highlands Holdings",
                "Jane Doe",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(truckService.update(
                eq(truckId),
                any(UpdateTruckRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/trucks/{truckId}", truckId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber")
                        .value("ABC 999 GP"))
                .andExpect(jsonPath("$.make")
                        .value("Scania"))
                .andExpect(jsonPath("$.model")
                        .value("R500"));

        verify(truckService)
                .update(
                        eq(truckId),
                        any(UpdateTruckRequest.class)
                );
    }

    @Test
    void changeStatus_shouldReturn200() throws Exception {

        TruckResponse response = new TruckResponse(
                truckId,
                companyId,
                "ABC 123 GP",
                "Volvo",
                "FH16",
                new BigDecimal("34.5000"),
                TruckStatus.IN_TRANSIT,
                "Highlands Holdings",
                "John Doe",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(truckService.changeStatus(
                truckId,
                TruckStatus.IN_TRANSIT
        )).thenReturn(response);

        mockMvc.perform(
                        patch("/trucks/{truckId}/status", truckId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("\"IN_TRANSIT\"")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("IN_TRANSIT"));

        verify(truckService)
                .changeStatus(
                        truckId,
                        TruckStatus.IN_TRANSIT
                );
    }

    @Test
    void delete_shouldReturn204() throws Exception {

        doNothing()
                .when(truckService)
                .delete(truckId);

        mockMvc.perform(
                        delete("/trucks/{truckId}", truckId)
                )
                .andExpect(status().isNoContent());

        verify(truckService)
                .delete(truckId);
    }

    @Test
    void delete_shouldReturn403WhenPermissionDenied() throws Exception {

        doThrow(
                new ForbiddenException(
                        "Missing permission: TRUCK_DELETE"
                )
        ).when(truckService)
                .delete(truckId);

        mockMvc.perform(
                        delete("/trucks/{truckId}", truckId)
                )
                .andExpect(status().isForbidden());

        verify(truckService)
                .delete(truckId);
    }
}