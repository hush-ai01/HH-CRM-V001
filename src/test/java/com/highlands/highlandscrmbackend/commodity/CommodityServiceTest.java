package com.highlands.highlandscrmbackend.commodity;

import com.highlands.highlandscrmbackend.common.exception.CommodityAlreadyExistsException;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommodityServiceTest {

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthorizationService authorizationService;

    private CommodityService commodityService;

    private UUID companyId;
    private UUID otherCompanyId;
    private UUID commodityId;

    private Company company;
    private Commodity commodity;

    @BeforeEach
    void setUp() {
        commodityService = new CommodityService(
                commodityRepository,
                companyRepository,
                authorizationService
        );

        companyId = UUID.randomUUID();
        otherCompanyId = UUID.randomUUID();
        commodityId = UUID.randomUUID();

        company = new Company(
                "Highlands Holdings",
                "HH"
        );

        commodity = new Commodity(
                company,
                "Gold",
                "GOLD",
                "Gold commodity"
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateCommoditySuccessfully() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.existsByCompanyIdAndCode(
                    companyId,
                    "GOLD"
            )).thenReturn(false);

            when(companyRepository.findById(companyId))
                    .thenReturn(Optional.of(company));

            when(commodityRepository.save(any(Commodity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Commodity result = commodityService.create(
                    " Gold ",
                    " gold ",
                    " Gold commodity "
            );

            assertNotNull(result);
            assertEquals("Gold", result.getName());
            assertEquals("GOLD", result.getCode());
            assertEquals("Gold commodity", result.getDescription());
            assertTrue(result.isActive());
            assertSame(company, result.getCompany());

            verify(authorizationService)
                    .requirePermission("COMMODITY_CREATE");

            verify(commodityRepository)
                    .existsByCompanyIdAndCode(companyId, "GOLD");

            verify(companyRepository)
                    .findById(companyId);

            verify(commodityRepository)
                    .save(any(Commodity.class));
        }
    }

    @Test
    void shouldRejectDuplicateCommodityCode() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.existsByCompanyIdAndCode(
                    companyId,
                    "GOLD"
            )).thenReturn(true);

            assertThrows(
                    CommodityAlreadyExistsException.class,
                    () -> commodityService.create(
                            "Gold",
                            "gold",
                            "Gold commodity"
                    )
            );

            verify(authorizationService)
                    .requirePermission("COMMODITY_CREATE");

            verify(commodityRepository)
                    .existsByCompanyIdAndCode(companyId, "GOLD");

            verify(companyRepository, never())
                    .findById(any());

            verify(commodityRepository, never())
                    .save(any());
        }
    }

    @Test
    void shouldThrowWhenCompanyDoesNotExist() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.existsByCompanyIdAndCode(
                    companyId,
                    "GOLD"
            )).thenReturn(false);

            when(companyRepository.findById(companyId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> commodityService.create(
                            "Gold",
                            "GOLD",
                            "Gold commodity"
                    )
            );

            verify(commodityRepository, never())
                    .save(any());
        }
    }

    @Test
    void shouldReturnAllCommoditiesForCurrentTenant() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            List<Commodity> commodities = List.of(commodity);

            when(commodityRepository
                    .findAllByCompanyIdOrderByNameAsc(companyId))
                    .thenReturn(commodities);

            List<Commodity> result = commodityService.findAll();

            assertEquals(1, result.size());
            assertSame(commodity, result.get(0));

            verify(authorizationService)
                    .requirePermission("COMMODITY_READ");

            verify(commodityRepository)
                    .findAllByCompanyIdOrderByNameAsc(companyId);
        }
    }

    @Test
    void shouldFindCommodityByIdWithinCurrentTenant() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            Commodity result =
                    commodityService.findById(commodityId);

            assertSame(commodity, result);

            verify(authorizationService)
                    .requirePermission("COMMODITY_READ");

            verify(commodityRepository)
                    .findByIdAndCompanyId(
                            commodityId,
                            companyId
                    );
        }
    }

    @Test
    void shouldNotReturnCommodityFromAnotherTenant() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> commodityService.findById(commodityId)
            );

            verify(commodityRepository)
                    .findByIdAndCompanyId(
                            commodityId,
                            companyId
                    );
        }
    }

    @Test
    void shouldUpdateCommoditySuccessfully() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            when(commodityRepository.existsByCompanyIdAndCodeAndIdNot(
                    companyId,
                    "CHROME",
                    commodityId
            )).thenReturn(false);

            when(commodityRepository.save(commodity))
                    .thenReturn(commodity);

            Commodity result = commodityService.update(
                    commodityId,
                    " Chrome ",
                    " chrome ",
                    " Chrome commodity "
            );

            assertSame(commodity, result);
            assertEquals("Chrome", commodity.getName());
            assertEquals("CHROME", commodity.getCode());
            assertEquals(
                    "Chrome commodity",
                    commodity.getDescription()
            );

            verify(authorizationService)
                    .requirePermission("COMMODITY_UPDATE");

            verify(commodityRepository)
                    .findByIdAndCompanyId(
                            commodityId,
                            companyId
                    );

            verify(commodityRepository)
                    .existsByCompanyIdAndCodeAndIdNot(
                            companyId,
                            "CHROME",
                            commodityId
                    );

            verify(commodityRepository)
                    .save(commodity);
        }
    }

    @Test
    void shouldRejectDuplicateCodeWhenUpdatingCommodity() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            when(commodityRepository.existsByCompanyIdAndCodeAndIdNot(
                    companyId,
                    "CHROME",
                    commodityId
            )).thenReturn(true);

            assertThrows(
                    CommodityAlreadyExistsException.class,
                    () -> commodityService.update(
                            commodityId,
                            "Chrome",
                            "chrome",
                            "Chrome commodity"
                    )
            );

            verify(commodityRepository, never())
                    .save(any());
        }
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingCommodity() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> commodityService.update(
                            commodityId,
                            "Chrome",
                            "CHROME",
                            "Chrome commodity"
                    )
            );

            verify(commodityRepository, never())
                    .save(any());
        }
    }

    @Test
    void shouldActivateCommodity() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            commodity.changeStatus(false);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            when(commodityRepository.save(commodity))
                    .thenReturn(commodity);

            Commodity result =
                    commodityService.changeStatus(
                            commodityId,
                            true
                    );

            assertSame(commodity, result);
            assertTrue(result.isActive());

            verify(authorizationService)
                    .requirePermission("COMMODITY_UPDATE");

            verify(commodityRepository)
                    .save(commodity);
        }
    }

    @Test
    void shouldDeactivateCommodity() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            when(commodityRepository.save(commodity))
                    .thenReturn(commodity);

            Commodity result =
                    commodityService.changeStatus(
                            commodityId,
                            false
                    );

            assertFalse(result.isActive());

            verify(authorizationService)
                    .requirePermission("COMMODITY_UPDATE");

            verify(commodityRepository)
                    .save(commodity);
        }
    }

    @Test
    void shouldDeleteCommodityWithinCurrentTenant() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.of(commodity));

            commodityService.delete(commodityId);

            verify(authorizationService)
                    .requirePermission("COMMODITY_DELETE");

            verify(commodityRepository)
                    .findByIdAndCompanyId(
                            commodityId,
                            companyId
                    );

            verify(commodityRepository)
                    .delete(commodity);
        }
    }

    @Test
    void shouldThrowWhenDeletingNonExistingCommodity() {
        try (MockedStatic<TenantContext> tenantContext =
                     mockStatic(TenantContext.class)) {

            tenantContext.when(TenantContext::requireCompanyId)
                    .thenReturn(companyId);

            when(commodityRepository.findByIdAndCompanyId(
                    commodityId,
                    companyId
            )).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> commodityService.delete(commodityId)
            );

            verify(commodityRepository, never())
                    .delete(any());
        }
    }
}