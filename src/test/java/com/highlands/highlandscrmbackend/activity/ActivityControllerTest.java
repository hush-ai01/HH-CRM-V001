package com.highlands.highlandscrmbackend.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID clientId;
    private UUID activityId;
    private UUID companyId;
    private UUID userId;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new ActivityController(activityService)
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        clientId = UUID.randomUUID();
        activityId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE ACTIVITY
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateActivitySuccessfully() throws Exception {

        CreateActivityRequest request =
                new CreateActivityRequest(
                        ActivityType.CALL,
                        "Buyer requested revised pricing",
                        "Discussed chrome concentrate pricing.",
                        "Send revised offer",
                        OffsetDateTime.now().plusDays(1)
                );

        ActivityResponse response =
                new ActivityResponse(
                        activityId,
                        companyId,
                        clientId,
                        userId,
                        ActivityType.CALL,
                        "Buyer requested revised pricing",
                        "Discussed chrome concentrate pricing.",
                        "Send revised offer",
                        request.nextActionAt(),
                        OffsetDateTime.now()
                );

        when(activityService.createActivity(
                eq(clientId),
                any(CreateActivityRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/clients/{clientId}/activities", clientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(activityId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.clientId")
                        .value(clientId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.type")
                        .value("CALL"))
                .andExpect(jsonPath("$.outcome")
                        .value("Buyer requested revised pricing"))
                .andExpect(jsonPath("$.notes")
                        .value("Discussed chrome concentrate pricing."))
                .andExpect(jsonPath("$.nextAction")
                        .value("Send revised offer"));

        verify(activityService)
                .createActivity(
                        eq(clientId),
                        any(CreateActivityRequest.class)
                );
    }

    // -------------------------------------------------------------------------
    // GET ACTIVITIES
    // -------------------------------------------------------------------------

    @Test
    void shouldGetActivitiesSuccessfully() throws Exception {

        ActivityResponse firstActivity =
                new ActivityResponse(
                        UUID.randomUUID(),
                        companyId,
                        clientId,
                        userId,
                        ActivityType.CALL,
                        "Called buyer",
                        "Discussed pricing",
                        "Send quote",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now()
                );

        ActivityResponse secondActivity =
                new ActivityResponse(
                        UUID.randomUUID(),
                        companyId,
                        clientId,
                        userId,
                        ActivityType.EMAIL,
                        "Quote sent",
                        "Sent revised offer",
                        null,
                        null,
                        OffsetDateTime.now()
                );

        when(activityService.getActivities(clientId))
                .thenReturn(List.of(
                        firstActivity,
                        secondActivity
                ));

        mockMvc.perform(
                        get("/clients/{clientId}/activities", clientId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type")
                        .value("CALL"))
                .andExpect(jsonPath("$[1].type")
                        .value("EMAIL"));

        verify(activityService)
                .getActivities(clientId);
    }

    // -------------------------------------------------------------------------
    // GET SINGLE ACTIVITY
    // -------------------------------------------------------------------------

    @Test
    void shouldGetActivitySuccessfully() throws Exception {

        ActivityResponse response =
                new ActivityResponse(
                        activityId,
                        companyId,
                        clientId,
                        userId,
                        ActivityType.MEETING,
                        "Meeting completed",
                        "Discussed supply requirements.",
                        "Send proposal",
                        OffsetDateTime.now().plusDays(2),
                        OffsetDateTime.now()
                );

        when(activityService.getActivity(
                clientId,
                activityId
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/clients/{clientId}/activities/{activityId}",
                                clientId,
                                activityId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(activityId.toString()))
                .andExpect(jsonPath("$.clientId")
                        .value(clientId.toString()))
                .andExpect(jsonPath("$.type")
                        .value("MEETING"))
                .andExpect(jsonPath("$.outcome")
                        .value("Meeting completed"));

        verify(activityService)
                .getActivity(
                        clientId,
                        activityId
                );
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectCreateActivityWhenTypeIsMissing()
            throws Exception {

        String request = """
                {
                    "outcome": "Buyer contacted",
                    "notes": "Discussed pricing",
                    "nextAction": "Send quote"
                }
                """;

        mockMvc.perform(
                        post("/clients/{clientId}/activities", clientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(activityService);
    }

    @Test
    void shouldRejectCreateActivityWhenRequestBodyIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/clients/{clientId}/activities", clientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid-json}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(activityService);
    }

    // -------------------------------------------------------------------------
    // INVALID PATH VARIABLES
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectInvalidClientId() throws Exception {

        mockMvc.perform(
                        get("/clients/not-a-uuid/activities")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(activityService);
    }

    @Test
    void shouldRejectInvalidActivityId() throws Exception {

        mockMvc.perform(
                        get(
                                "/clients/{clientId}/activities/not-a-uuid",
                                clientId
                        )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(activityService);
    }

    // -------------------------------------------------------------------------
    // NOT FOUND
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNotFoundWhenActivityDoesNotExist()
            throws Exception {

        when(activityService.getActivity(
                clientId,
                activityId
        )).thenThrow(
                new ResourceNotFoundException(
                        "Activity with id '" + activityId + "' not found"
                )
        );

        mockMvc.perform(
                        get(
                                "/clients/{clientId}/activities/{activityId}",
                                clientId,
                                activityId
                        )
                )
                .andExpect(status().isNotFound());

        verify(activityService)
                .getActivity(
                        clientId,
                        activityId
                );
    }

    // -------------------------------------------------------------------------
    // FORBIDDEN
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnForbiddenWhenUserLacksPermission()
            throws Exception {

        when(activityService.getActivities(clientId))
                .thenThrow(
                        new ForbiddenException(
                                "You do not have permission to perform this action"
                        )
                );

        mockMvc.perform(
                        get("/clients/{clientId}/activities", clientId)
                )
                .andExpect(status().isForbidden());

        verify(activityService)
                .getActivities(clientId);
    }
}