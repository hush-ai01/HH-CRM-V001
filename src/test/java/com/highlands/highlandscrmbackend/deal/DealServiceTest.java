package com.highlands.highlandscrmbackend.deal;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.commodity.CommodityRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.grade.Grade;
import com.highlands.highlandscrmbackend.grade.GradeRepository;
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

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private GradeRepository gradeRepository;

    private DealService dealService;

    private UUID companyId;
    private UUID userId;
    private UUID clientId;
    private UUID dealId;
    private UUID commodityId;
    private UUID gradeId;

    private Company company;
    private User user;
    private Client client;
    private Commodity commodity;
    private Grade grade;

    @BeforeEach
    void setUp() {
        dealService = new DealService(
                dealRepository,
                companyRepository,
                clientRepository,
                userRepository,
                currentUserService,
                authorizationService,
                commodityRepository,
                gradeRepository
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        dealId = UUID.randomUUID();
        commodityId = UUID.randomUUID();
        gradeId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        company = mock(Company.class);
        user = mock(User.class);
        client = mock(Client.class);
        commodity = mock(Commodity.class);
        grade = mock(Grade.class);
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

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

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
                commodityId,
                gradeId,
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

        assertEquals(commodity, result.getCommodity());
        assertEquals(grade, result.getGrade());

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

        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);

        verify(gradeRepository)
                .findByIdAndCompanyId(gradeId, companyId);

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
                        commodityId,
                        gradeId,
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
                commodityRepository,
                gradeRepository,
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
                        commodityId,
                        gradeId,
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
                commodityRepository,
                gradeRepository,
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
                        commodityId,
                        gradeId,
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
                commodityRepository,
                gradeRepository,
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
                        commodityId,
                        gradeId,
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

        verifyNoInteractions(
                commodityRepository,
                gradeRepository,
                dealRepository
        );
    }

    @Test
    void create_shouldFailWhenCommodityDoesNotBelongToTenant() {

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

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        commodityId,
                        gradeId,
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);

        verifyNoInteractions(
                gradeRepository,
                dealRepository
        );
    }

    @Test
    void create_shouldFailWhenGradeDoesNotBelongToTenant() {

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

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        commodityId,
                        gradeId,
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(gradeRepository)
                .findByIdAndCompanyId(gradeId, companyId);

        verifyNoInteractions(dealRepository);
    }

    @Test
    void create_shouldFailWhenGradeDoesNotBelongToCommodity() {

        UUID differentCommodityId = UUID.randomUUID();

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

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(commodity.getId())
                .thenReturn(commodityId);

        Commodity differentCommodity = mock(Commodity.class);

        when(differentCommodity.getId())
                .thenReturn(differentCommodityId);

        when(grade.getCommodity())
                .thenReturn(differentCommodity);

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.create(
                        clientId,
                        DealType.SELL,
                        commodityId,
                        gradeId,
                        new BigDecimal("100"),
                        "KG",
                        new BigDecimal("2500"),
                        "ZAR",
                        LocalDate.of(2026, 12, 31),
                        "Test deal"
                )
        );

        verify(grade)
                .getCommodity();

        verify(dealRepository, never())
                .save(any(Deal.class));
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

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

        when(dealRepository.save(deal))
                .thenReturn(deal);

        BigDecimal quantity = new BigDecimal("200");
        BigDecimal unitPrice = new BigDecimal("3000");

        Deal result = dealService.update(
                dealId,
                commodityId,
                gradeId,
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

        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);

        verify(gradeRepository)
                .findByIdAndCompanyId(gradeId, companyId);

        verify(deal)
                .update(
                        commodity,
                        grade,
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
    void update_shouldFailWhenDealDoesNotExist() {

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.update(
                        dealId,
                        commodityId,
                        gradeId,
                        new BigDecimal("200"),
                        "TON",
                        new BigDecimal("3000"),
                        "ZAR",
                        LocalDate.of(2027, 1, 31),
                        "Updated deal"
                )
        );

        verifyNoInteractions(
                commodityRepository,
                gradeRepository
        );

        verify(dealRepository)
                .findByIdAndCompanyId(dealId, companyId);
    }

    @Test
    void update_shouldFailWhenCommodityDoesNotExist() {

        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.update(
                        dealId,
                        commodityId,
                        gradeId,
                        new BigDecimal("200"),
                        "TON",
                        new BigDecimal("3000"),
                        "ZAR",
                        LocalDate.of(2027, 1, 31),
                        "Updated deal"
                )
        );

        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);

        verifyNoInteractions(
                gradeRepository
        );

        verify(dealRepository, never())
                .save(any(Deal.class));
    }

    @Test
    void update_shouldFailWhenGradeDoesNotExist() {

        Deal deal = mock(Deal.class);

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.update(
                        dealId,
                        commodityId,
                        gradeId,
                        new BigDecimal("200"),
                        "TON",
                        new BigDecimal("3000"),
                        "ZAR",
                        LocalDate.of(2027, 1, 31),
                        "Updated deal"
                )
        );

        verify(gradeRepository)
                .findByIdAndCompanyId(gradeId, companyId);

        verify(dealRepository, never())
                .save(any(Deal.class));
    }

    @Test
    void update_shouldFailWhenGradeDoesNotBelongToCommodity() {

        Deal deal = mock(Deal.class);

        Commodity differentCommodity = mock(Commodity.class);

        UUID differentCommodityId = UUID.randomUUID();

        when(dealRepository.findByIdAndCompanyId(
                dealId,
                companyId
        )).thenReturn(Optional.of(deal));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        when(commodity.getId())
                .thenReturn(commodityId);

        when(differentCommodity.getId())
                .thenReturn(differentCommodityId);

        when(grade.getCommodity())
                .thenReturn(differentCommodity);

        assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.update(
                        dealId,
                        commodityId,
                        gradeId,
                        new BigDecimal("200"),
                        "TON",
                        new BigDecimal("3000"),
                        "ZAR",
                        LocalDate.of(2027, 1, 31),
                        "Updated deal"
                )
        );

        verify(dealRepository, never())
                .save(any(Deal.class));

        verify(deal, never())
                .update(
                        any(Commodity.class),
                        any(Grade.class),
                        any(BigDecimal.class),
                        anyString(),
                        any(BigDecimal.class),
                        anyString(),
                        any(LocalDate.class),
                        anyString()
                );
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