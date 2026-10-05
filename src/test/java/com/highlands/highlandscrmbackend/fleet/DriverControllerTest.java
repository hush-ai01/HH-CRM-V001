package com.highlands.highlandscrmbackend.fleet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.fleet.dto.CreateDriverRequest;
import com.highlands.highlandscrmbackend.fleet.dto.DriverResponse;
import com.highlands.highlandscrmbackend.fleet.dto.UpdateDriverRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID driverId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DriverController(driverService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_shouldReturn201Created() throws Exception {

        CreateDriverRequest request = new CreateDriverRequest(
                "John",
                "Doe",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "0821234567"
        );

        DriverResponse response = driverResponse();

        when(driverService.create(any(CreateDriverRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/drivers/" + driverId
                ))
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.identificationType")
                        .value("SOUTH_AFRICAN_ID"))
                .andExpect(jsonPath("$.identificationNumber")
                        .value("9001015009087"))
                .andExpect(jsonPath("$.phone").value("0821234567"));

        verify(driverService).create(any(CreateDriverRequest.class));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {

        CreateDriverRequest request = new CreateDriverRequest(
                "",
                "",
                null,
                "",
                ""
        );

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(driverService);
    }

    @Test
    void findAll_shouldReturn200() throws Exception {

        when(driverService.findAll())
                .thenReturn(List.of(driverResponse()));

        mockMvc.perform(get("/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(driverId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(driverService).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {

        when(driverService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(driverService).findAll();
    }

    @Test
    void findById_shouldReturn200() throws Exception {

        when(driverService.findById(driverId))
                .thenReturn(driverResponse());

        mockMvc.perform(get("/drivers/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(driverService).findById(driverId);
    }

    @Test
    void findById_shouldReturn404WhenDriverDoesNotExist() throws Exception {

        when(driverService.findById(driverId))
                .thenThrow(new ResourceNotFoundException("Driver not found"));

        mockMvc.perform(get("/drivers/{driverId}", driverId))
                .andExpect(status().isNotFound());

        verify(driverService).findById(driverId);
    }

    @Test
    void update_shouldReturn200() throws Exception {

        UpdateDriverRequest request = new UpdateDriverRequest(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        DriverResponse response = driverResponse(
                "Jane",
                "Doe",
                DriverIdentificationType.PASSPORT,
                "AB123456",
                "0839876543"
        );

        when(driverService.update(
                eq(driverId),
                any(UpdateDriverRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/drivers/{driverId}", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.identificationType")
                        .value("PASSPORT"))
                .andExpect(jsonPath("$.identificationNumber")
                        .value("AB123456"))
                .andExpect(jsonPath("$.phone")
                        .value("0839876543"));

        verify(driverService).update(
                eq(driverId),
                any(UpdateDriverRequest.class)
        );
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {

        UpdateDriverRequest request = new UpdateDriverRequest(
                "",
                "",
                null,
                "",
                ""
        );

        mockMvc.perform(put("/drivers/{driverId}", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(driverService);
    }

    @Test
    void delete_shouldReturn204() throws Exception {

        doNothing().when(driverService).delete(driverId);

        mockMvc.perform(delete("/drivers/{driverId}", driverId))
                .andExpect(status().isNoContent());

        verify(driverService).delete(driverId);
    }

    private DriverResponse driverResponse() {
        return driverResponse(
                "John",
                "Doe",
                DriverIdentificationType.SOUTH_AFRICAN_ID,
                "9001015009087",
                "0821234567"
        );
    }

    private DriverResponse driverResponse(
            String firstName,
            String lastName,
            DriverIdentificationType identificationType,
            String identificationNumber,
            String phone) {

        OffsetDateTime now = OffsetDateTime.now();

        return new DriverResponse(
                driverId,
                companyId,
                firstName,
                lastName,
                identificationType,
                identificationNumber,
                phone,
                now,
                now
        );
    }
}