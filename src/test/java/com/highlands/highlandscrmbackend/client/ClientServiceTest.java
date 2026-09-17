package com.highlands.highlandscrmbackend.client;

import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private ClientService clientService;

    private UUID companyId;
    private UUID userId;

    @BeforeEach
    void setUp() {

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();

        when(company.getId())
                .thenReturn(companyId);

        TenantContext.setCompanyId(companyId);

        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createClient_shouldCreateClientSuccessfully() {

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

        Client savedClient = clientCaptor.getValue();

        assertNotNull(savedClient);

        // Company
        assertEquals(
                company,
                savedClient.getCompany()
        );

        // Basic client information
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

        // Quantities
        assertNull(
                savedClient.getMonthlyQuantity()
        );

        assertNull(
                savedClient.getWeeklyQuantity()
        );

        // Commercial information
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

        // Contact information
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

        // Default account status
        assertEquals(
                AccountStatus.PROSPECT,
                savedClient.getAccountStatus()
        );

        // Default visibility supplied by request
        assertEquals(
                ClientVisibility.PRIVATE,
                savedClient.getVisibility()
        );

        // Ownership/provenance
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

        // Lead provenance
        assertNull(
                savedClient.getSourceLeadId()
        );

        // Next action
        assertNull(
                savedClient.getNextAction()
        );

        assertNull(
                savedClient.getNextActionAt()
        );

        // Audit timestamps
        assertNotNull(
                savedClient.getCreatedAt()
        );

        assertNotNull(
                savedClient.getUpdatedAt()
        );

        // Response
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

        // Verify tenant was used correctly
        verify(companyRepository)
                .findById(companyId);

        // Verify current authenticated user was used
        verify(currentUserService)
                .getCurrentUserId();

        // Verify client was persisted
        verify(clientRepository)
                .save(any(Client.class));

        // No owner lookup should occur because ownerUserId was null
        verifyNoInteractions(userRepository);
    }
}