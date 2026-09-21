package com.highlands.highlandscrmbackend.activity;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private Company company;

    @Mock
    private Client client;

    @Mock
    private User user;

    @Mock
    private Activity activity;

    private ActivityService activityService;

    private UUID companyId;
    private UUID clientId;
    private UUID userId;
    private UUID activityId;

    @BeforeEach
    void setUp() {

        TenantContext.clear();

        activityService = new ActivityService(
                activityRepository,
                clientRepository,
                userRepository,
                currentUserService,
                authorizationService
        );

        companyId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        userId = UUID.randomUUID();
        activityId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // CREATE ACTIVITY
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateActivitySuccessfully() {

        TenantContext.setCompanyId(companyId);

        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.CALL,
                "Buyer requested revised pricing",
                "Discussed chrome concentrate pricing.",
                "Send revised offer",
                OffsetDateTime.now().plusDays(1)
        );

        OffsetDateTime nextActionAt = request.nextActionAt();

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.of(client));

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(client.getCompany())
                .thenReturn(company);

        when(activityRepository.save(any(Activity.class)))
                .thenReturn(activity);

        when(activity.getId())
                .thenReturn(activityId);

        when(activity.getCompany())
                .thenReturn(company);

        when(activity.getClient())
                .thenReturn(client);

        when(activity.getUser())
                .thenReturn(user);

        when(activity.getType())
                .thenReturn(ActivityType.CALL);

        when(activity.getOutcome())
                .thenReturn(request.outcome());

        when(activity.getNotes())
                .thenReturn(request.notes());

        when(activity.getNextAction())
                .thenReturn(request.nextAction());

        when(activity.getNextActionAt())
                .thenReturn(nextActionAt);

        OffsetDateTime createdAt = OffsetDateTime.now();

        when(activity.getCreatedAt())
                .thenReturn(createdAt);

        when(company.getId())
                .thenReturn(companyId);

        when(client.getId())
                .thenReturn(clientId);

        when(user.getId())
                .thenReturn(userId);

        ActivityResponse response =
                activityService.createActivity(
                        clientId,
                        request
                );

        assertNotNull(response);

        assertEquals(activityId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(clientId, response.clientId());
        assertEquals(userId, response.userId());
        assertEquals(ActivityType.CALL, response.type());
        assertEquals(
                "Buyer requested revised pricing",
                response.outcome()
        );
        assertEquals(
                "Discussed chrome concentrate pricing.",
                response.notes()
        );
        assertEquals(
                "Send revised offer",
                response.nextAction()
        );
        assertEquals(nextActionAt, response.nextActionAt());
        assertEquals(createdAt, response.createdAt());

        verify(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        verify(clientRepository)
                .findByIdAndCompanyId(
                        clientId,
                        companyId
                );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(activityRepository)
                .save(any(Activity.class));

        verify(client)
                .setNextAction("Send revised offer");

        verify(client)
                .setNextActionAt(nextActionAt);

        verify(client)
                .setUpdatedAt(any(OffsetDateTime.class));

        verify(clientRepository)
                .save(client);
    }

    @Test
    void shouldRejectCreateActivityWhenTenantContextIsMissing() {

        TenantContext.clear();

        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.CALL,
                "Outcome",
                "Notes",
                "Follow up",
                OffsetDateTime.now().plusDays(1)
        );

        assertThrows(
                IllegalStateException.class,
                () -> activityService.createActivity(
                        clientId,
                        request
                )
        );

        verifyNoInteractions(
                authorizationService,
                clientRepository,
                userRepository,
                activityRepository,
                currentUserService
        );
    }

    @Test
    void shouldRejectCreateActivityWhenClientDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.CALL,
                "Outcome",
                "Notes",
                "Follow up",
                OffsetDateTime.now().plusDays(1)
        );

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> activityService.createActivity(
                        clientId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        verify(clientRepository)
                .findByIdAndCompanyId(
                        clientId,
                        companyId
                );

        verifyNoInteractions(
                userRepository,
                activityRepository
        );
    }

    @Test
    void shouldRejectCreateActivityWhenUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.CALL,
                "Outcome",
                "Notes",
                "Follow up",
                OffsetDateTime.now().plusDays(1)
        );

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.of(client));

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> activityService.createActivity(
                        clientId,
                        request
                )
        );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verifyNoInteractions(activityRepository);
    }

    // -------------------------------------------------------------------------
    // GET ACTIVITIES
    // -------------------------------------------------------------------------

    @Test
    void shouldGetActivitiesForCurrentTenantAndClient() {

        TenantContext.setCompanyId(companyId);

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.of(client));

        when(activityRepository.findAllByTenantAndClient(
                companyId,
                clientId
        )).thenReturn(List.of(activity));

        when(activity.getId())
                .thenReturn(activityId);

        when(activity.getCompany())
                .thenReturn(company);

        when(activity.getClient())
                .thenReturn(client);

        when(activity.getUser())
                .thenReturn(user);

        when(company.getId())
                .thenReturn(companyId);

        when(client.getId())
                .thenReturn(clientId);

        when(user.getId())
                .thenReturn(userId);

        when(activity.getType())
                .thenReturn(ActivityType.EMAIL);

        when(activity.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        List<ActivityResponse> response =
                activityService.getActivities(clientId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(activityId, response.get(0).id());
        assertEquals(ActivityType.EMAIL, response.get(0).type());

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verify(clientRepository)
                .findByIdAndCompanyId(
                        clientId,
                        companyId
                );

        verify(activityRepository)
                .findAllByTenantAndClient(
                        companyId,
                        clientId
                );
    }

    @Test
    void shouldRejectGetActivitiesWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> activityService.getActivities(clientId)
        );

        verifyNoInteractions(
                authorizationService,
                clientRepository,
                activityRepository
        );
    }

    @Test
    void shouldRejectGetActivitiesWhenClientDoesNotBelongToTenant() {

        TenantContext.setCompanyId(companyId);

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> activityService.getActivities(clientId)
        );

        verify(clientRepository)
                .findByIdAndCompanyId(
                        clientId,
                        companyId
                );

        verify(activityRepository, never())
                .findAllByTenantAndClient(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    // -------------------------------------------------------------------------
    // GET SINGLE ACTIVITY
    // -------------------------------------------------------------------------

    @Test
    void shouldGetActivityForCurrentTenantAndClient() {

        TenantContext.setCompanyId(companyId);

        when(activityRepository.findByIdAndTenantAndClient(
                activityId,
                companyId,
                clientId
        )).thenReturn(Optional.of(activity));

        when(activity.getId())
                .thenReturn(activityId);

        when(activity.getCompany())
                .thenReturn(company);

        when(activity.getClient())
                .thenReturn(client);

        when(activity.getUser())
                .thenReturn(user);

        when(company.getId())
                .thenReturn(companyId);

        when(client.getId())
                .thenReturn(clientId);

        when(user.getId())
                .thenReturn(userId);

        when(activity.getType())
                .thenReturn(ActivityType.MEETING);

        when(activity.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        ActivityResponse response =
                activityService.getActivity(
                        clientId,
                        activityId
                );

        assertNotNull(response);

        assertEquals(activityId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals(clientId, response.clientId());
        assertEquals(userId, response.userId());
        assertEquals(ActivityType.MEETING, response.type());

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verify(activityRepository)
                .findByIdAndTenantAndClient(
                        activityId,
                        companyId,
                        clientId
                );
    }

    @Test
    void shouldRejectActivityFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(activityRepository.findByIdAndTenantAndClient(
                activityId,
                companyId,
                clientId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> activityService.getActivity(
                        clientId,
                        activityId
                )
        );

        verify(activityRepository)
                .findByIdAndTenantAndClient(
                        activityId,
                        companyId,
                        clientId
                );
    }

    @Test
    void shouldRejectActivityWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> activityService.getActivity(
                        clientId,
                        activityId
                )
        );

        verifyNoInteractions(
                authorizationService,
                activityRepository
        );
    }
}