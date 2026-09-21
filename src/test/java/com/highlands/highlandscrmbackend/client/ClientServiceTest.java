package com.highlands.highlandscrmbackend.client;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private Company company;

    @Mock
    private User owner;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private ClientService clientService;

    private UUID companyId;
    private UUID anotherCompanyId;
    private UUID userId;
    private UUID anotherUserId;
    private UUID clientId;

    @BeforeEach
    void setUp() {

        companyId = UUID.randomUUID();
        anotherCompanyId = UUID.randomUUID();

        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();

        clientId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================================
    // CREATE CLIENT
    // =========================================================================

    @Test
    void createClient_shouldCreateClientSuccessfully() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_CREATE");

        when(company.getId())
                .thenReturn(companyId);

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        CreateClientRequest request = new CreateClientRequest(
                "Test Mining Supplier",
                ClientType.SUPPLIER,
                "Limpopo",
                null,
                null,
                "50% deposit, balance on POD",
                DeliveryTerm.FOT,
                "SACD City Deep",
                "John Smith",
                "+27 82 000 0000",
                "john@example.com",
                null,
                null,
                null,
                null,
                ClientVisibility.PRIVATE
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ClientResponse response =
                clientService.createClient(request);

        ArgumentCaptor<Client> clientCaptor =
                ArgumentCaptor.forClass(Client.class);

        verify(clientRepository)
                .save(clientCaptor.capture());

        Client savedClient =
                clientCaptor.getValue();

        assertNotNull(savedClient);

        assertEquals(
                company,
                savedClient.getCompany()
        );

        assertEquals(
                "Test Mining Supplier",
                savedClient.getName()
        );

        assertEquals(
                ClientType.SUPPLIER,
                savedClient.getType()
        );

        assertEquals(
                "Limpopo",
                savedClient.getArea()
        );

        assertNull(
                savedClient.getMonthlyQuantity()
        );

        assertNull(
                savedClient.getWeeklyQuantity()
        );

        assertEquals(
                "50% deposit, balance on POD",
                savedClient.getPaymentTerms()
        );

        assertEquals(
                DeliveryTerm.FOT,
                savedClient.getDeliveryTerms()
        );

        assertEquals(
                "SACD City Deep",
                savedClient.getWarehouse()
        );

        assertEquals(
                "John Smith",
                savedClient.getContactName()
        );

        assertEquals(
                "+27 82 000 0000",
                savedClient.getContactPhone()
        );

        assertEquals(
                "john@example.com",
                savedClient.getContactEmail()
        );

        assertEquals(
                AccountStatus.PROSPECT,
                savedClient.getAccountStatus()
        );

        assertEquals(
                ClientVisibility.PRIVATE,
                savedClient.getVisibility()
        );

        assertEquals(
                userId,
                savedClient.getCreatedByUserId()
        );

        assertEquals(
                userId,
                savedClient.getOriginatorUserId()
        );

        assertNull(
                savedClient.getOwner()
        );

        assertNull(
                savedClient.getSourceLeadId()
        );

        assertNull(
                savedClient.getNextAction()
        );

        assertNull(
                savedClient.getNextActionAt()
        );

        assertNotNull(
                savedClient.getCreatedAt()
        );

        assertNotNull(
                savedClient.getUpdatedAt()
        );

        assertNotNull(response);

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                "Test Mining Supplier",
                response.name()
        );

        assertEquals(
                ClientType.SUPPLIER,
                response.type()
        );

        assertEquals(
                "Limpopo",
                response.area()
        );

        assertEquals(
                "50% deposit, balance on POD",
                response.paymentTerms()
        );

        assertEquals(
                DeliveryTerm.FOT,
                response.deliveryTerms()
        );

        assertEquals(
                "SACD City Deep",
                response.warehouse()
        );

        assertEquals(
                "John Smith",
                response.contactName()
        );

        assertEquals(
                "+27 82 000 0000",
                response.contactPhone()
        );

        assertEquals(
                "john@example.com",
                response.contactEmail()
        );

        assertEquals(
                AccountStatus.PROSPECT,
                response.accountStatus()
        );

        assertEquals(
                ClientVisibility.PRIVATE,
                response.visibility()
        );

        assertEquals(
                userId,
                response.createdByUserId()
        );

        assertEquals(
                userId,
                response.originatorUserId()
        );

        assertNull(
                response.ownerUserId()
        );

        assertNull(
                response.sourceLeadId()
        );

        assertNotNull(
                response.createdAt()
        );

        assertNotNull(
                response.updatedAt()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_CREATE");

        verify(companyRepository)
                .findById(companyId);

        verify(currentUserService)
                .getCurrentUserId();

        verify(clientRepository)
                .save(any(Client.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void createClient_shouldRejectWithoutCreatePermission() {

        CreateClientRequest request = new CreateClientRequest(
                "Test Mining Supplier",
                ClientType.SUPPLIER,
                "Limpopo",
                null,
                null,
                "50% deposit",
                DeliveryTerm.FOT,
                "SACD City Deep",
                "John Smith",
                "+27 82 000 0000",
                "john@example.com",
                null,
                null,
                null,
                null,
                ClientVisibility.PRIVATE
        );

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        ))
                .when(authorizationService)
                .requirePermission("CLIENT_CREATE");

        assertThrows(
                ForbiddenException.class,
                () -> clientService.createClient(request)
        );

        verify(authorizationService)
                .requirePermission("CLIENT_CREATE");

        verifyNoInteractions(companyRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(currentUserService);

        verify(clientRepository, never())
                .save(any(Client.class));
    }

    @Test
    void createClient_shouldRejectOwnerFromAnotherCompany() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_CREATE");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        CreateClientRequest request =
                new CreateClientRequest(
                        "Test Mining Supplier",
                        ClientType.SUPPLIER,
                        "Limpopo",
                        null,
                        null,
                        "50% deposit",
                        DeliveryTerm.FOT,
                        "SACD City Deep",
                        "John Smith",
                        "+27 82 000 0000",
                        "john@example.com",
                        null,
                        null,
                        null,
                        anotherUserId,
                        ClientVisibility.PRIVATE
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.findByIdAndCompanyId(
                anotherUserId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.createClient(request)
        );

        verify(authorizationService)
                .requirePermission("CLIENT_CREATE");

        verify(userRepository)
                .findByIdAndCompanyId(
                        anotherUserId,
                        companyId
                );

        verify(clientRepository, never())
                .save(any(Client.class));
    }

    // =========================================================================
    // GET ALL CLIENTS
    // =========================================================================

    @Test
    void getAllClients_shouldQueryVisibleClientsForCurrentUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(company.getId())
                .thenReturn(companyId);

        Client clientOne = mock(Client.class);
        Client clientTwo = mock(Client.class);

        when(clientOne.getId())
                .thenReturn(UUID.randomUUID());

        when(clientOne.getCompany())
                .thenReturn(company);

        when(clientOne.getName())
                .thenReturn("Company A Client");

        when(clientOne.getType())
                .thenReturn(ClientType.SUPPLIER);

        when(clientOne.getAccountStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(clientOne.getVisibility())
                .thenReturn(ClientVisibility.PRIVATE);

        when(clientTwo.getId())
                .thenReturn(UUID.randomUUID());

        when(clientTwo.getCompany())
                .thenReturn(company);

        when(clientTwo.getName())
                .thenReturn("Company A Buyer");

        when(clientTwo.getType())
                .thenReturn(ClientType.BUYER);

        when(clientTwo.getAccountStatus())
                .thenReturn(AccountStatus.PROSPECT);

        when(clientTwo.getVisibility())
                .thenReturn(ClientVisibility.MANAGEMENT);

        when(clientRepository.findAllVisibleToUser(
                companyId,
                userId,
                false
        )).thenReturn(List.of(clientOne, clientTwo));

        List<ClientResponse> responses =
                clientService.getAllClients();

        assertNotNull(responses);

        assertEquals(
                2,
                responses.size()
        );

        assertEquals(
                companyId,
                responses.get(0).companyId()
        );

        assertEquals(
                companyId,
                responses.get(1).companyId()
        );

        assertEquals(
                "Company A Client",
                responses.get(0).name()
        );

        assertEquals(
                "Company A Buyer",
                responses.get(1).name()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verify(currentUserService)
                .getCurrentUserId();

        verify(currentUserService)
                .isManagementUser();

        verify(clientRepository)
                .findAllVisibleToUser(
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findAllByCompanyId(any(UUID.class));
    }

    @Test
    void getAllClients_shouldPassManagementFlagForManagementUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(true);

        when(clientRepository.findAllVisibleToUser(
                companyId,
                userId,
                true
        )).thenReturn(List.of());

        List<ClientResponse> responses =
                clientService.getAllClients();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(clientRepository)
                .findAllVisibleToUser(
                        companyId,
                        userId,
                        true
                );
    }

    @Test
    void getAllClients_shouldRejectWithoutReadPermission() {

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        ))
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        assertThrows(
                ForbiddenException.class,
                () -> clientService.getAllClients()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verifyNoInteractions(clientRepository);
        verifyNoInteractions(currentUserService);
    }

    // =========================================================================
    // GET CLIENT BY ID
    // =========================================================================

    @Test
    void getClientById_shouldReturnVisibleClientFromCurrentCompany() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(company.getId())
                .thenReturn(companyId);

        Client client = mock(Client.class);

        when(client.getId())
                .thenReturn(clientId);

        when(client.getCompany())
                .thenReturn(company);

        when(client.getName())
                .thenReturn("Company A Client");

        when(client.getType())
                .thenReturn(ClientType.SUPPLIER);

        when(client.getAccountStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(client.getVisibility())
                .thenReturn(ClientVisibility.PRIVATE);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.of(client));

        ClientResponse response =
                clientService.getClientById(clientId);

        assertNotNull(response);

        assertEquals(
                clientId,
                response.id()
        );

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                "Company A Client",
                response.name()
        );

        assertEquals(
                ClientType.SUPPLIER,
                response.type()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                response.accountStatus()
        );

        assertEquals(
                ClientVisibility.PRIVATE,
                response.visibility()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verify(currentUserService)
                .getCurrentUserId();

        verify(currentUserService)
                .isManagementUser();

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findByIdAndCompanyId(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void getClientById_shouldRejectClientNotVisibleToUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> clientService.getClientById(clientId)
                );

        assertEquals(
                "Client with id '" + clientId + "' not found",
                exception.getMessage()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findByIdAndCompanyId(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void getClientById_shouldUseManagementVisibilityForManagementUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(true);

        Client client = mock(Client.class);

        when(client.getId())
                .thenReturn(clientId);

        when(client.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(client.getName())
                .thenReturn("Management Client");

        when(client.getType())
                .thenReturn(ClientType.BUYER);

        when(client.getAccountStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(client.getVisibility())
                .thenReturn(ClientVisibility.MANAGEMENT);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                true
        )).thenReturn(Optional.of(client));

        ClientResponse response =
                clientService.getClientById(clientId);

        assertNotNull(response);

        assertEquals(
                clientId,
                response.id()
        );

        assertEquals(
                ClientVisibility.MANAGEMENT,
                response.visibility()
        );

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        true
                );
    }

    @Test
    void getClientById_shouldRejectWithoutReadPermission() {

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        ))
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        assertThrows(
                ForbiddenException.class,
                () -> clientService.getClientById(clientId)
        );

        verify(authorizationService)
                .requirePermission("CLIENT_READ");

        verifyNoInteractions(clientRepository);
        verifyNoInteractions(currentUserService);
    }

    // =========================================================================
    // UPDATE CLIENT
    // =========================================================================

    @Test
    void updateClient_shouldUpdateVisibleClientFromCurrentCompany() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(company.getId())
                .thenReturn(companyId);

        Client client = mock(Client.class);

        when(client.getId())
                .thenReturn(clientId);

        when(client.getCompany())
                .thenReturn(company);

        when(client.getName())
                .thenReturn("Updated Mining Supplier");

        when(client.getType())
                .thenReturn(ClientType.SUPPLIER);

        when(client.getAccountStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(client.getVisibility())
                .thenReturn(ClientVisibility.PRIVATE);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.of(client));

        when(clientRepository.save(client))
                .thenReturn(client);

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Updated Mining Supplier",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        AccountStatus.ACTIVE,
                        null,
                        null,
                        null,
                        null
                );

        ClientResponse response =
                clientService.updateClient(
                        clientId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                clientId,
                response.id()
        );

        assertEquals(
                companyId,
                response.companyId()
        );

        assertEquals(
                "Updated Mining Supplier",
                response.name()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                response.accountStatus()
        );

        verify(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        verify(currentUserService)
                .getCurrentUserId();

        verify(currentUserService)
                .isManagementUser();

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository)
                .save(client);

        verify(client)
                .setName("Updated Mining Supplier");

        verify(client)
                .setAccountStatus(AccountStatus.ACTIVE);
    }

    @Test
    void updateClient_shouldRejectClientNotVisibleToUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Attempted Update",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.updateClient(
                        clientId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .save(any(Client.class));
    }

    @Test
    void updateClient_shouldUseManagementVisibilityForManagementUser() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(true);

        Client client = mock(Client.class);

        when(client.getId())
                .thenReturn(clientId);

        when(client.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(companyId);

        when(client.getName())
                .thenReturn("Management Client");

        when(client.getType())
                .thenReturn(ClientType.BUYER);

        when(client.getAccountStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(client.getVisibility())
                .thenReturn(ClientVisibility.MANAGEMENT);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                true
        )).thenReturn(Optional.of(client));

        when(clientRepository.save(client))
                .thenReturn(client);

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Updated Management Client",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        ClientResponse response =
                clientService.updateClient(
                        clientId,
                        request
                );

        assertNotNull(response);

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        true
                );

        verify(client)
                .setName("Updated Management Client");

        verify(clientRepository)
                .save(client);
    }

    @Test
    void updateClient_shouldRejectWithoutUpdatePermission() {

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Attempted Update",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        doThrow(new ForbiddenException(
                "You do not have permission to perform this action"
        ))
                .when(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        assertThrows(
                ForbiddenException.class,
                () -> clientService.updateClient(
                        clientId,
                        request
                )
        );

        verify(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        verify(clientRepository, never())
                .findVisibleById(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class),
                        anyBoolean()
                );

        verify(clientRepository, never())
                .save(any(Client.class));

        verifyNoInteractions(currentUserService);
    }

    // =========================================================================
    // TENANT ISOLATION
    // =========================================================================

    @Test
    void getAllClients_shouldAlwaysUseCurrentTenant() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(clientRepository.findAllVisibleToUser(
                companyId,
                userId,
                false
        )).thenReturn(List.of());

        clientService.getAllClients();

        verify(clientRepository)
                .findAllVisibleToUser(
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findAllVisibleToUser(
                        anotherCompanyId,
                        userId,
                        false
                );
    }

    @Test
    void getClientById_shouldAlwaysUseCurrentTenant() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_READ");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.getClientById(clientId)
        );

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findVisibleById(
                        clientId,
                        anotherCompanyId,
                        userId,
                        false
                );
    }

    @Test
    void updateClient_shouldAlwaysUseCurrentTenant() {

        doNothing()
                .when(authorizationService)
                .requirePermission("CLIENT_UPDATE");

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(currentUserService.isManagementUser())
                .thenReturn(false);

        UpdateClientRequest request =
                new UpdateClientRequest(
                        "Attempted Cross Tenant Update",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        when(clientRepository.findVisibleById(
                clientId,
                companyId,
                userId,
                false
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.updateClient(
                        clientId,
                        request
                )
        );

        verify(clientRepository)
                .findVisibleById(
                        clientId,
                        companyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .findVisibleById(
                        clientId,
                        anotherCompanyId,
                        userId,
                        false
                );

        verify(clientRepository, never())
                .save(any(Client.class));
    }
}