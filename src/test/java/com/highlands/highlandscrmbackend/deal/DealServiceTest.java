package com.highlands.highlandscrmbackend.deal;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuthorizationService authorizationService;

    private DealService dealService;

    private UUID companyId;
    private UUID userId;
    private UUID clientId;
    private UUID dealId;

    private Company company;
    private User user;
    private Client client;

    @BeforeEach
    void setUp() {
        dealService = new DealService(
                dealRepository,
                companyRepository,
                clientRepository,
                userRepository,
                currentUserService,
                authorizationService
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        dealId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        company = mock(Company.class);
        user = mock(User.class);
        client = mock(Client.class);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateDealForCurrentTenant() {
        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.of(client));

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(dealRepository.existsByCompanyIdAndDealNumber(
                eq(companyId),
                anyString()
        )).thenReturn(false);

        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal quantity = new BigDecimal("100.0000");
        BigDecimal unitPrice = new BigDecimal("2500.0000");

        Deal result = dealService.create(
                clientId,
                DealType.SELL,
                "Gold",
                "24K",
                quantity,
                "KG",
                unitPrice,
                "ZAR",
                LocalDate.of(2026, 12, 31),
                "Test deal"
        );

        assertNotNull(result);

        assertEquals(company, result.getCompany());
        assertEquals(client, result.getClient());
        assertEquals(user, result.getOwner());

        assertEquals(DealType.SELL, result.getType());
        assertEquals(DealStatus.DRAFT, result.getStatus());

        assertEquals("Gold", result.getCommodity());
        assertEquals("24K", result.getGrade());
        assertEquals(quantity, result.getQuantity());
        assertEquals("KG", result.getUnit());

        assertEquals(unitPrice, result.getUnitPrice());
        assertEquals("ZAR", result.getCurrency());

        assertEquals(
                new BigDecimal("250000.00000000"),
                result.getTotalValue()
        );

        assertEquals(
                LocalDate.of(2026, 12, 31),
                result.getExpectedCloseDate()
        );

        assertEquals("Test deal", result.getNotes());

        assertNotNull(result.getDealNumber());
        assertTrue(result.getDealNumber().startsWith("DL-2026-"));

        verify(authorizationService)
                .requirePermission("DEAL_CREATE");

        verify(dealRepository)
                .save(any(Deal.class));
    }

    @Test
    void create_shouldRequireTenant() {
        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        "Gold",
                        "24K",
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verifyNoInteractions(
                authorizationService,
                companyRepository,
                clientRepository,
                userRepository,
                dealRepository
        );
    }

    @Test
    void create_shouldFailWhenCompanyDoesNotExist() {
        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        "Gold",
                        "24K",
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(companyRepository)
                .findById(companyId);

        verifyNoInteractions(
                clientRepository,
                userRepository,
                dealRepository
        );
    }

    @Test
    void create_shouldFailWhenClientDoesNotBelongToTenant() {
        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(clientRepository.findByIdAndCompanyId(
                clientId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        "Gold",
                        "24K",
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(clientRepository)
                .findByIdAndCompanyId(clientId, companyId);

        verifyNoInteractions(
                userRepository,
                dealRepository
        );
    }

    @Test
    void create_shouldFailWhenCurrentUserDoesNotBelongToTenant() {
        when(currentUserService.getCurrentUserId())
                .thenReturn(userId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

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
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        "Gold",
                        "24K",
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(userRepository)
                .findByIdAndCompanyId(userId, companyId);

        verifyNoInteractions(dealRepository);
    }

    @Test
    void findAll_shouldReturnTenantDeals() {
        Deal deal = mock(Deal.class);

        when(dealRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId))
                .thenReturn(List.of(deal));

        List<Deal> result = dealService.findAll();

        assertEquals(1, result.size());
        assertEquals(deal, result.get(0));

        verify(authorizationService)
                .requirePermission("DEAL_READ");

        verify(dealRepository)
                .findAllByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    @Test
    void findById_shouldReturnTenantDeal() {
        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        Deal result = dealService.findById(dealId);

        assertEquals(deal, result);

        verify(authorizationService)
                .requirePermission("DEAL_READ");

        verify(dealRepository)
                .findByIdAndCompanyId(dealId, companyId);
    }

    @Test
    void findById_shouldFailWhenDealDoesNotExistInTenant() {
        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.findById(dealId)
        );

        verify(dealRepository)
                .findByIdAndCompanyId(dealId, companyId);
    }

    @Test
    void update_shouldUpdateDeal() {
        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        when(dealRepository.save(deal))
                .thenReturn(deal);

        BigDecimal quantity = new BigDecimal("200");
        BigDecimal unitPrice = new BigDecimal("3000");

        Deal result = dealService.update(
                dealId,
                "Chrome",
                "Grade A",
                quantity,
                "TON",
                unitPrice,
                "ZAR",
                LocalDate.of(2027, 1, 31),
                "Updated deal"
        );

        assertEquals(deal, result);

        verify(authorizationService)
                .requirePermission("DEAL_UPDATE");

        verify(deal)
                .update(
                        "Chrome",
                        "Grade A",
                        quantity,
                        "TON",
                        unitPrice,
                        "ZAR",
                        LocalDate.of(2027, 1, 31),
                        "Updated deal"
                );

        verify(dealRepository)
                .save(deal);
    }

    @Test
    void changeStatus_shouldUpdateDealStatus() {
        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        when(dealRepository.save(deal))
                .thenReturn(deal);

        Deal result = dealService.changeStatus(
                dealId,
                DealStatus.OPEN
        );

        assertEquals(deal, result);

        verify(authorizationService)
                .requirePermission("DEAL_UPDATE");

        verify(deal)
                .changeStatus(DealStatus.OPEN);

        verify(dealRepository)
                .save(deal);
    }

    @Test
    void delete_shouldDeleteTenantDeal() {
        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        dealService.delete(dealId);

        verify(authorizationService)
                .requirePermission("DEAL_DELETE");

        verify(dealRepository)
                .findByIdAndCompanyId(dealId, companyId);

        verify(dealRepository)
                .delete(deal);
    }

    @Test
    void delete_shouldFailWhenDealDoesNotExistInTenant() {
        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.delete(dealId)
        );

        verify(dealRepository)
                .findByIdAndCompanyId(dealId, companyId);

        verify(dealRepository, never())
                .delete(any());
    }
}