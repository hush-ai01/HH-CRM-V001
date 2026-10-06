package com.highlands.highlandscrmbackend.logistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.logistics.dto.ChangeTripStatusRequest;
import com.highlands.highlandscrmbackend.logistics.dto.CreateTripRequest;
import com.highlands.highlandscrmbackend.logistics.dto.TripResponse;
import com.highlands.highlandscrmbackend.logistics.dto.UpdateTripRequest;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID tripId;
    private UUID companyId;
    private UUID driverId;
    private UUID truckId;
    private UUID trailerId;
    private UUID gradeId;

    private OffsetDateTime dispatchDate;
    private OffsetDateTime scheduledPickup;
    private OffsetDateTime eta;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(tripController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        tripId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        truckId = UUID.randomUUID();
        trailerId = UUID.randomUUID();
        gradeId = UUID.randomUUID();

        dispatchDate = OffsetDateTime.now();
        scheduledPickup = dispatchDate.plusHours(2);
        eta = dispatchDate.plusHours(8);
    }

    @Test
    void shouldCreateTrip() throws Exception {

        CreateTripRequest request = createRequest();

        TripResponse response = createResponse();

        when(tripService.create(any(CreateTripRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.dispatchRef").value("DISP-001"))
                .andExpect(jsonPath("$.origin").value("Johannesburg"))
                .andExpect(jsonPath("$.destination").value("City Deep"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        verify(tripService).create(any(CreateTripRequest.class));
    }

    @Test
    void shouldFindAllTrips() throws Exception {

        TripResponse response = createResponse();

        when(tripService.findAll())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(tripId.toString()))
                .andExpect(jsonPath("$[0].dispatchRef").value("DISP-001"));

        verify(tripService).findAll();
    }

    @Test
    void shouldFindTripById() throws Exception {

        when(tripService.findById(tripId))
                .thenReturn(createResponse());

        mockMvc.perform(
                        get("/trips/{tripId}", tripId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.dispatchRef").value("DISP-001"));

        verify(tripService).findById(tripId);
    }

    @Test
    void shouldUpdateTrip() throws Exception {

        UpdateTripRequest request = new UpdateTripRequest(
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                null,
                null,
                null,
                driverId,
                truckId,
                trailerId,
                gradeId
        );

        TripResponse response = createResponse();

        when(tripService.update(
                eq(tripId),
                any(UpdateTripRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/trips/{tripId}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()))
                .andExpect(jsonPath("$.dispatchRef").value("DISP-001"));

        verify(tripService)
                .update(eq(tripId), any(UpdateTripRequest.class));
    }

    @Test
    void shouldChangeTripStatus() throws Exception {

        ChangeTripStatusRequest request =
                new ChangeTripStatusRequest(TripStatus.IN_TRANSIT);

        TripResponse response = createResponse();

        when(tripService.changeStatus(
                eq(tripId),
                any(ChangeTripStatusRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch("/trips/{tripId}/status", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()));

        verify(tripService)
                .changeStatus(
                        eq(tripId),
                        any(ChangeTripStatusRequest.class)
                );
    }

    @Test
    void shouldDeleteTrip() throws Exception {

        mockMvc.perform(
                        delete("/trips/{tripId}", tripId)
                )
                .andExpect(status().isNoContent());

        verify(tripService).delete(tripId);
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid()
            throws Exception {

        CreateTripRequest request = new CreateTripRequest(
                "",
                null,
                null,
                null,
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(
                        post("/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid()
            throws Exception {

        UpdateTripRequest request = new UpdateTripRequest(
                null,
                null,
                null,
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(
                        put("/trips/{tripId}", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsMissing()
            throws Exception {

        String request = "{}";

        mockMvc.perform(
                        patch("/trips/{tripId}/status", tripId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnForbiddenWhenServiceRejectsCreate()
            throws Exception {

        CreateTripRequest request = createRequest();

        doThrow(new ForbiddenException("Forbidden"))
                .when(tripService)
                .create(any(CreateTripRequest.class));

        mockMvc.perform(
                        post("/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenTripDoesNotExist()
            throws Exception {

        when(tripService.findById(tripId))
                .thenThrow(new ResourceNotFoundException("Trip not found"));

        mockMvc.perform(
                        get("/trips/{tripId}", tripId)
                )
                .andExpect(status().isNotFound());
    }

    private CreateTripRequest createRequest() {

        return new CreateTripRequest(
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                "Johannesburg",
                "https://tracking.example.com/DISP-001",
                "Handle with care",
                driverId,
                truckId,
                trailerId,
                gradeId
        );
    }

    private TripResponse createResponse() {

        return new TripResponse(
                tripId,
                companyId,
                "DISP-001",
                dispatchDate,
                scheduledPickup,
                eta,
                "Johannesburg",
                "City Deep",
                "Johannesburg",
                "https://tracking.example.com/DISP-001",
                "Handle with care",
                dispatchDate,
                driverId,
                truckId,
                trailerId,
                gradeId,
                new BigDecimal("30000.0000"),
                new BigDecimal("10000.0000"),
                new BigDecimal("20000.0000"),
                new BigDecimal("5.0000"),
                TripStatus.ASSIGNED,
                dispatchDate,
                dispatchDate
        );
    }
}