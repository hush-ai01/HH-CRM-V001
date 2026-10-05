package com.highlands.highlandscrmbackend.inventory;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.commodity.CommodityRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.grade.Grade;
import com.highlands.highlandscrmbackend.grade.GradeRepository;
import com.highlands.highlandscrmbackend.inventory.dto.CreateInventoryRequest;
import com.highlands.highlandscrmbackend.inventory.dto.InventoryResponse;
import com.highlands.highlandscrmbackend.inventory.dto.UpdateInventoryRequest;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.ForbiddenException;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationService authorizationService;

    private InventoryService inventoryService;

    private UUID companyId;
    private UUID commodityId;
    private UUID gradeId;
    private UUID supplierClientId;
    private UUID inventoryId;

    private Company company;
    private Commodity commodity;
    private Grade grade;
    private Client supplierClient;
    private Inventory inventory;

    @BeforeEach
    void setUp() {

        TenantContext.clear();

        companyId = UUID.randomUUID();
        commodityId = UUID.randomUUID();
        gradeId = UUID.randomUUID();
        supplierClientId = UUID.randomUUID();
        inventoryId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        inventoryService = new InventoryService(
                inventoryRepository,
                companyRepository,
                commodityRepository,
                gradeRepository,
                clientRepository,
                userRepository,
                authorizationService
        );

        company = mock(Company.class);
        commodity = mock(Commodity.class);
        grade = mock(Grade.class);
        supplierClient = mock(Client.class);
        inventory = mock(Inventory.class);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    void create_shouldCreateInventorySuccessfully() {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        supplierClientId,
                        new BigDecimal("500.0000"),
                        " MT ",
                        " Mooinooi ",
                        " Lefa Wash Plant ",
                        " ABC Minerals ",
                        " Chrome stock "
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        // Required by validateCommodityGrade()
        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

        when(clientRepository.findByIdAndCompanyId(
                supplierClientId,
                companyId
        )).thenReturn(Optional.of(supplierClient));

        when(inventoryRepository.save(
                any(Inventory.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        InventoryResponse response =
                inventoryService.create(request);

        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository)
                .save(captor.capture());

        Inventory savedInventory =
                captor.getValue();

        assertSame(
                company,
                savedInventory.getCompany()
        );

        assertSame(
                commodity,
                savedInventory.getCommodity()
        );

        assertSame(
                grade,
                savedInventory.getGrade()
        );

        assertSame(
                supplierClient,
                savedInventory.getSupplierClient()
        );

        assertEquals(
                new BigDecimal("500.0000"),
                savedInventory.getQuantity()
        );

        assertEquals(
                "MT",
                savedInventory.getUnit()
        );

        assertEquals(
                "Mooinooi",
                savedInventory.getLocation()
        );

        assertEquals(
                "Lefa Wash Plant",
                savedInventory.getWashPlant()
        );

        assertEquals(
                "ABC Minerals",
                savedInventory.getOwner()
        );

        assertEquals(
                "Chrome stock",
                savedInventory.getNotes()
        );

        assertEquals(
                "AVAILABLE",
                savedInventory.getStatus()
        );

        assertFalse(
                savedInventory.isPortalSubmitted()
        );

        assertNotNull(response);
    }

    @Test
    void create_shouldAllowNullSupplierClient() {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        // Required by validateCommodityGrade()
        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

        when(inventoryRepository.save(
                any(Inventory.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        InventoryResponse response =
                inventoryService.create(request);

        verify(
                clientRepository,
                never()
        ).findByIdAndCompanyId(
                any(),
                any()
        );

        assertNotNull(response);
    }

    @Test
    void create_shouldRejectGradeThatDoesNotBelongToCommodity() {

        Commodity anotherCommodity =
                mock(Commodity.class);

        UUID anotherCommodityId =
                UUID.randomUUID();

        when(commodity.getId())
                .thenReturn(commodityId);

        when(anotherCommodity.getId())
                .thenReturn(anotherCommodityId);

        when(grade.getCommodity())
                .thenReturn(anotherCommodity);

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        assertThrows(
                InvalidInventoryCommodityGradeException.class,
                () -> inventoryService.create(request)
        );

        verify(
                inventoryRepository,
                never()
        ).save(any());
    }

    @Test
    void create_shouldRejectMissingCommodity() {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.create(request)
        );

        verify(
                gradeRepository,
                never()
        ).findByIdAndCompanyId(
                any(),
                any()
        );

        verify(
                inventoryRepository,
                never()
        ).save(any());
    }

    @Test
    void create_shouldRejectMissingGrade() {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

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
                () -> inventoryService.create(request)
        );

        verify(
                inventoryRepository,
                never()
        ).save(any());
    }

    @Test
    void create_shouldRejectSupplierClientFromAnotherTenant() {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        supplierClientId,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        // Required so validation reaches supplier-client validation.
        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

        when(clientRepository.findByIdAndCompanyId(
                supplierClientId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.create(request)
        );

        verify(
                inventoryRepository,
                never()
        ).save(any());
    }

    // =========================================================================
    // FIND ALL
    // =========================================================================

    @Test
    void findAll_shouldReturnCurrentTenantInventory() {

        when(inventoryRepository
                .findAllByCompanyIdOrderByCreatedAtDesc(
                        companyId
                ))
                .thenReturn(List.of(inventory));

        when(inventory.getCompany())
                .thenReturn(company);

        when(inventory.getCommodity())
                .thenReturn(commodity);

        when(inventory.getGrade())
                .thenReturn(grade);

        List<InventoryResponse> result =
                inventoryService.findAll();

        assertEquals(
                1,
                result.size()
        );

        verify(inventoryRepository)
                .findAllByCompanyIdOrderByCreatedAtDesc(
                        companyId
                );
    }

    // =========================================================================
    // FIND BY ID
    // =========================================================================

    @Test
    void findById_shouldReturnInventory() {

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.of(inventory));

        when(inventory.getCompany())
                .thenReturn(company);

        when(inventory.getCommodity())
                .thenReturn(commodity);

        when(inventory.getGrade())
                .thenReturn(grade);

        InventoryResponse response =
                inventoryService.findById(
                        inventoryId
                );

        assertNotNull(response);

        verify(inventoryRepository)
                .findByIdAndCompanyId(
                        inventoryId,
                        companyId
                );
    }

    @Test
    void findById_shouldRejectInventoryFromAnotherTenant() {

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.findById(
                        inventoryId
                )
        );
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    void update_shouldUpdateInventorySuccessfully() {

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(
                        commodityId,
                        gradeId,
                        supplierClientId,
                        new BigDecimal("750.0000"),
                        "MT",
                        "Rustenburg",
                        "Updated Plant",
                        "Updated Owner",
                        "Updated notes"
                );

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.of(inventory));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        // Required by validateCommodityGrade()
        when(commodity.getId())
                .thenReturn(commodityId);

        when(grade.getCommodity())
                .thenReturn(commodity);

        when(clientRepository.findByIdAndCompanyId(
                supplierClientId,
                companyId
        )).thenReturn(Optional.of(supplierClient));

        when(inventoryRepository.save(
                inventory
        )).thenReturn(inventory);

        when(inventory.getCompany())
                .thenReturn(company);

        when(inventory.getCommodity())
                .thenReturn(commodity);

        when(inventory.getGrade())
                .thenReturn(grade);

        InventoryResponse response =
                inventoryService.update(
                        inventoryId,
                        request
                );

        verify(inventory).update(
                commodity,
                grade,
                supplierClient,
                new BigDecimal("750.0000"),
                "MT",
                "Rustenburg",
                "Updated Plant",
                "Updated Owner",
                "Updated notes"
        );

        verify(inventoryRepository)
                .save(inventory);

        assertNotNull(response);
    }

    @Test
    void update_shouldRejectInvalidCommodityGradeCombination() {

        Commodity anotherCommodity =
                mock(Commodity.class);

        UUID anotherCommodityId =
                UUID.randomUUID();

        when(commodity.getId())
                .thenReturn(commodityId);

        when(anotherCommodity.getId())
                .thenReturn(anotherCommodityId);

        when(grade.getCommodity())
                .thenReturn(anotherCommodity);

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("750.0000"),
                        "MT",
                        "Rustenburg",
                        null,
                        null,
                        null
                );

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.of(inventory));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository.findByIdAndCompanyId(
                gradeId,
                companyId
        )).thenReturn(Optional.of(grade));

        assertThrows(
                InvalidInventoryCommodityGradeException.class,
                () -> inventoryService.update(
                        inventoryId,
                        request
                )
        );

        verify(
                inventory,
                never()
        ).update(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(
                inventoryRepository,
                never()
        ).save(any());
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    void delete_shouldDeleteCurrentTenantInventory() {

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.of(inventory));

        inventoryService.delete(
                inventoryId
        );

        verify(inventoryRepository)
                .delete(inventory);
    }

    @Test
    void delete_shouldNotDeleteInventoryFromAnotherTenant() {

        when(inventoryRepository.findByIdAndCompanyId(
                inventoryId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.delete(
                        inventoryId
                )
        );

        verify(
                inventoryRepository,
                never()
        ).delete(any());
    }

    // =========================================================================
    // PERMISSIONS
    // =========================================================================

    @Test
    void create_shouldRequireCreatePermission() {

        doThrow(
                new ForbiddenException(
                        "Permission denied"
                )
        )
                .when(authorizationService)
                .requirePermission(
                        "INVENTORY_CREATE"
                );

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        null,
                        new BigDecimal("100.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        assertThrows(
                ForbiddenException.class,
                () -> inventoryService.create(
                        request
                )
        );

        verify(authorizationService)
                .requirePermission(
                        "INVENTORY_CREATE"
                );

        verifyNoInteractions(
                companyRepository,
                commodityRepository,
                gradeRepository,
                clientRepository,
                inventoryRepository
        );
    }

    @Test
    void delete_shouldRequireDeletePermission() {

        doThrow(
                new ForbiddenException(
                        "Permission denied"
                )
        )
                .when(authorizationService)
                .requirePermission(
                        "INVENTORY_DELETE"
                );

        assertThrows(
                ForbiddenException.class,
                () -> inventoryService.delete(
                        inventoryId
                )
        );

        verify(authorizationService)
                .requirePermission(
                        "INVENTORY_DELETE"
                );

        verifyNoInteractions(
                inventoryRepository
        );
    }
}