package com.highlands.highlandscrmbackend.client;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public ClientService(
            ClientRepository clientRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService
    ) {
        this.clientRepository = clientRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public ClientResponse createClient(CreateClientRequest request) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_CREATE");

        UUID currentUserId = currentUserService.getCurrentUserId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company with id '" + companyId + "' not found"
                        )
                );

        User owner = null;

        if (request.ownerUserId() != null) {
            owner = userRepository
                    .findByIdAndCompanyId(
                            request.ownerUserId(),
                            companyId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Owner user with id '"
                                            + request.ownerUserId()
                                            + "' not found"
                            )
                    );
        }

        Client client = new Client();

        client.setCompany(company);
        client.setName(request.name());
        client.setType(request.type());
        client.setArea(request.area());
        client.setMonthlyQuantity(request.monthlyQuantity());
        client.setWeeklyQuantity(request.weeklyQuantity());
        client.setPaymentTerms(request.paymentTerms());
        client.setDeliveryTerms(request.deliveryTerms());
        client.setWarehouse(request.warehouse());
        client.setContactName(request.contactName());
        client.setContactPhone(request.contactPhone());
        client.setContactEmail(request.contactEmail());

        client.setAccountStatus(
                request.accountStatus() != null
                        ? request.accountStatus()
                        : AccountStatus.PROSPECT
        );

        client.setNextAction(request.nextAction());
        client.setNextActionAt(request.nextActionAt());
        client.setOwner(owner);

        client.setVisibility(
                request.visibility() != null
                        ? request.visibility()
                        : ClientVisibility.PRIVATE
        );

        client.setCreatedByUserId(currentUserId);
        client.setOriginatorUserId(currentUserId);

        OffsetDateTime now = OffsetDateTime.now();

        client.setCreatedAt(now);
        client.setUpdatedAt(now);

        Client savedClient = clientRepository.save(client);

        return toResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_READ");

        return clientRepository
                .findAllByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID clientId) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_READ");

        Client client = clientRepository
                .findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client with id '" + clientId + "' not found"
                        )
                );

        return toResponse(client);
    }

    public ClientResponse updateClient(
            UUID clientId,
            UpdateClientRequest request
    ) {

        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("CLIENT_UPDATE");

        Client client = clientRepository
                .findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client with id '" + clientId + "' not found"
                        )
                );

        if (request.name() != null) {
            client.setName(request.name());
        }

        if (request.type() != null) {
            client.setType(request.type());
        }

        if (request.area() != null) {
            client.setArea(request.area());
        }

        if (request.monthlyQuantity() != null) {
            client.setMonthlyQuantity(
                    request.monthlyQuantity()
            );
        }

        if (request.weeklyQuantity() != null) {
            client.setWeeklyQuantity(
                    request.weeklyQuantity()
            );
        }

        if (request.paymentTerms() != null) {
            client.setPaymentTerms(
                    request.paymentTerms()
            );
        }

        if (request.deliveryTerms() != null) {
            client.setDeliveryTerms(
                    request.deliveryTerms()
            );
        }

        if (request.warehouse() != null) {
            client.setWarehouse(
                    request.warehouse()
            );
        }

        if (request.contactName() != null) {
            client.setContactName(
                    request.contactName()
            );
        }

        if (request.contactPhone() != null) {
            client.setContactPhone(
                    request.contactPhone()
            );
        }

        if (request.contactEmail() != null) {
            client.setContactEmail(
                    request.contactEmail()
            );
        }

        if (request.accountStatus() != null) {
            client.setAccountStatus(
                    request.accountStatus()
            );
        }

        if (request.nextAction() != null) {
            client.setNextAction(
                    request.nextAction()
            );
        }

        if (request.nextActionAt() != null) {
            client.setNextActionAt(
                    request.nextActionAt()
            );
        }

        if (request.visibility() != null) {
            client.setVisibility(
                    request.visibility()
            );
        }

        if (request.ownerUserId() != null) {

            User owner = userRepository
                    .findByIdAndCompanyId(
                            request.ownerUserId(),
                            companyId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Owner user with id '"
                                            + request.ownerUserId()
                                            + "' not found"
                            )
                    );

            client.setOwner(owner);
        }

        client.setUpdatedAt(OffsetDateTime.now());

        Client updatedClient =
                clientRepository.save(client);

        return toResponse(updatedClient);
    }

    private ClientResponse toResponse(Client client) {

        return new ClientResponse(
                client.getId(),
                client.getCompany().getId(),
                client.getName(),
                client.getType(),
                client.getArea(),
                client.getMonthlyQuantity(),
                client.getWeeklyQuantity(),
                client.getPaymentTerms(),
                client.getDeliveryTerms(),
                client.getWarehouse(),
                client.getContactName(),
                client.getContactPhone(),
                client.getContactEmail(),
                client.getAccountStatus(),
                client.getNextAction(),
                client.getNextActionAt(),
                client.getOwner() != null
                        ? client.getOwner().getId()
                        : null,
                client.getVisibility(),
                client.getSourceLeadId(),
                client.getOriginatorUserId(),
                client.getCreatedByUserId(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}