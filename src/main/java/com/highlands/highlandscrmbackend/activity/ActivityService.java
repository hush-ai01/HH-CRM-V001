package com.highlands.highlandscrmbackend.activity;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public ActivityService(
            ActivityRepository activityRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService
    ) {
        this.activityRepository = activityRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public ActivityResponse createActivity(
            UUID clientId,
            CreateActivityRequest request
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_UPDATE");

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository
                .findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client with id '" + clientId + "' not found"
                        )
                );

        User user = userRepository
                .findByIdAndCompanyId(currentUserId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id '" + currentUserId + "' not found"
                        )
                );

        Activity activity = new Activity(
                client.getCompany(),
                client,
                user,
                request.type(),
                request.outcome(),
                request.notes(),
                request.nextAction(),
                request.nextActionAt()
        );

        Activity savedActivity = activityRepository.save(activity);

        /*
         * The latest activity determines the client's
         * next planned action when one is supplied.
         */
        if (request.nextAction() != null) {
            client.setNextAction(request.nextAction());
        }

        if (request.nextActionAt() != null) {
            client.setNextActionAt(request.nextActionAt());
        }

        client.setUpdatedAt(OffsetDateTime.now());

        clientRepository.save(client);

        return ActivityResponse.from(savedActivity);
    }

    @Transactional
    public List<ActivityResponse> getActivities(UUID clientId) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_READ");

        /*
         * First establish that the requested client belongs
         * to the current tenant.
         */
        clientRepository
                .findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client with id '" + clientId + "' not found"
                        )
                );

        return activityRepository
                .findAllByTenantAndClient(companyId, clientId)
                .stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @Transactional
    public ActivityResponse getActivity(
            UUID clientId,
            UUID activityId
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_READ");

        Activity activity = activityRepository
                .findByIdAndTenantAndClient(
                        activityId,
                        companyId,
                        clientId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Activity with id '" + activityId + "' not found"
                        )
                );

        return ActivityResponse.from(activity);
    }
}