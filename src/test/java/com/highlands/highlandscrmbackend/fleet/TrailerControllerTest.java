package com.highlands.highlandscrmbackend.fleet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.fleet.dto.ChangeTrailerStatusRequest;
import com.highlands.highlandscrmbackend.fleet.dto.CreateTrailerRequest;
import com.highlands.highlandscrmbackend.fleet.dto.TrailerResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateTrailerRequest;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrailerControllerTest {

    @Mock
    private TrailerService trailerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID trailerId;
    private UUID companyId;
    private UUID truckId;

    private TrailerResponse trailerResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrailerController(trailerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        trailerId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        truckId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now();

        trailerResponse = new TrailerResponse(
                trailerId,
                companyId,
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("35.0000"),
                "AVAILABLE",
                now,
                now
        );
    }

    @Test
    void create_shouldReturn201() throws Exception {

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("35.0000")
        );

        when(trailerService.create(any(CreateTrailerRequest.class)))
                .thenReturn(trailerResponse);

        mockMvc.perform(
                        post("/trailers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/trailers/" + trailerId
                ))
                .andExpect(jsonPath("$.id").value(trailerId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.truckId").value(truckId.toString()))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("TRAILER-001"))
                .andExpect(jsonPath("$.type")
                        .value("Side tipper"))
                .andExpect(jsonPath("$.capacity")
                        .value(35.0000))
                .andExpect(jsonPath("$.status")
                        .value("AVAILABLE"));

        verify(trailerService)
                .create(any(CreateTrailerRequest.class));
    }

    @Test
    void findAll_shouldReturn200() throws Exception {

        when(trailerService.findAll())
                .thenReturn(List.of(trailerResponse));

        mockMvc.perform(get("/trailers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(trailerId.toString()))
                .andExpect(jsonPath("$[0].registrationNumber")
                        .value("TRAILER-001"));

        verify(trailerService).findAll();
    }

    @Test
    void findById_shouldReturn200() throws Exception {

        when(trailerService.findById(trailerId))
                .thenReturn(trailerResponse);

        mockMvc.perform(
                        get("/trailers/{trailerId}", trailerId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(trailerId.toString()))
                .andExpect(jsonPath("$.truckId")
                        .value(truckId.toString()))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("TRAILER-001"));

        verify(trailerService).findById(trailerId);
    }

    @Test
    void findByTruck_shouldReturn200() throws Exception {

        when(trailerService.findByTruck(truckId))
                .thenReturn(List.of(trailerResponse));

        mockMvc.perform(
                        get("/trailers/truck/{truckId}", truckId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].truckId")
                        .value(truckId.toString()));

        verify(trailerService).findByTruck(truckId);
    }

    @Test
    void update_shouldReturn200() throws Exception {

        UpdateTrailerRequest request = new UpdateTrailerRequest(
                truckId,
                "TRAILER-002",
                "Side tipper",
                new BigDecimal("40.0000")
        );

        TrailerResponse updatedResponse = new TrailerResponse(
                trailerId,
                companyId,
                truckId,
                "TRAILER-002",
                "Side tipper",
                new BigDecimal("40.0000"),
                "AVAILABLE",
                trailerResponse.createdAt(),
                OffsetDateTime.now()
        );

        when(trailerService.update(
                eq(trailerId),
                any(UpdateTrailerRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        put("/trailers/{trailerId}", trailerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(trailerId.toString()))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("TRAILER-002"))
                .andExpect(jsonPath("$.capacity")
                        .value(40.0000));

        verify(trailerService).update(
                eq(trailerId),
                any(UpdateTrailerRequest.class)
        );
    }

    @Test
    void changeStatus_shouldReturn200() throws Exception {

        TrailerResponse loadingResponse = new TrailerResponse(
                trailerId,
                companyId,
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("35.0000"),
                "LOADING",
                trailerResponse.createdAt(),
                OffsetDateTime.now()
        );

        ChangeTrailerStatusRequest request =
                new ChangeTrailerStatusRequest("LOADING");

        when(trailerService.changeStatus(trailerId, "LOADING"))
                .thenReturn(loadingResponse);

        mockMvc.perform(
                        patch("/trailers/{trailerId}/status", trailerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(trailerId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("LOADING"));

        verify(trailerService)
                .changeStatus(trailerId, "LOADING");
    }

    @Test
    void delete_shouldReturn204() throws Exception {

        doNothing().when(trailerService).delete(trailerId);

        mockMvc.perform(
                        delete("/trailers/{trailerId}", trailerId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(trailerService).delete(trailerId);
    }

    @Test
    void create_shouldReturn400ForInvalidRequest() throws Exception {

        CreateTrailerRequest request = new CreateTrailerRequest(
                null,
                "",
                "",
                new BigDecimal("0")
        );

        mockMvc.perform(
                        post("/trailers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trailerService);
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {

        when(trailerService.findById(trailerId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Trailer not found: " + trailerId
                        )
                );

        mockMvc.perform(
                        get("/trailers/{trailerId}", trailerId)
                )
                .andExpect(status().isNotFound());

        verify(trailerService).findById(trailerId);
    }

    @Test
    void create_shouldReturn409WhenTrailerAlreadyExists() throws Exception {

        CreateTrailerRequest request = new CreateTrailerRequest(
                truckId,
                "TRAILER-001",
                "Side tipper",
                new BigDecimal("35.0000")
        );

        when(trailerService.create(any(CreateTrailerRequest.class)))
                .thenThrow(
                        new TrailerAlreadyExistsException(
                                "A trailer with registration number 'TRAILER-001' already exists"
                        )
                );

        mockMvc.perform(
                        post("/trailers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(trailerService)
                .create(any(CreateTrailerRequest.class));
    }
}