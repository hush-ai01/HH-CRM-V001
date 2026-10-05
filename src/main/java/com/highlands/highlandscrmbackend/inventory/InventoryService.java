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
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private static final String INVENTORY_CREATE = "INVENTORY_CREATE";
    private static final String INVENTORY_READ = "INVENTORY_READ";
    private static final String INVENTORY_UPDATE = "INVENTORY_UPDATE";
    private static final String INVENTORY_DELETE = "INVENTORY_DELETE";

    private final InventoryRepository inventoryRepository;
    private final CompanyRepository companyRepository;
    private final CommodityRepository commodityRepository;
    private final GradeRepository gradeRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            CompanyRepository companyRepository,
            CommodityRepository commodityRepository,
            GradeRepository gradeRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AuthorizationService authorizationService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.companyRepository = companyRepository;
        this.commodityRepository = commodityRepository;
        this.gradeRepository = gradeRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    public InventoryResponse create(
            CreateInventoryRequest request
    ) {

        authorizationService.requirePermission(
                INVENTORY_CREATE
        );

        UUID companyId =
                TenantContext.requireCompanyId();

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Current company not found"
                                )
                        );

        Commodity commodity =
                commodityRepository
                        .findByIdAndCompanyId(
                                request.commodityId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Commodity not found"
                                )
                        );

        Grade grade =
                gradeRepository
                        .findByIdAndCompanyId(
                                request.gradeId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Grade not found"
                                )
                        );

        validateCommodityGrade(
                commodity,
                grade
        );

        Client supplierClient =
                resolveSupplierClient(
                        request.supplierClientId(),
                        companyId
                );

        Inventory inventory =
                new Inventory(
                        company,
                        commodity,
                        grade,
                        supplierClient,
                        request.quantity(),
                        normalize(request.unit()),
                        normalize(request.location()),
                        normalizeNullable(request.washPlant()),
                        normalizeNullable(request.owner()),
                        normalizeNullable(request.notes())
                );

        return InventoryResponse.from(
                inventoryRepository.save(inventory)
        );
    }

    @Transactional
    public List<InventoryResponse> findAll() {

        authorizationService.requirePermission(
                INVENTORY_READ
        );

        UUID companyId =
                TenantContext.requireCompanyId();

        return inventoryRepository
                .findAllByCompanyIdOrderByCreatedAtDesc(
                        companyId
                )
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    public InventoryResponse findById(
            UUID inventoryId
    ) {

        authorizationService.requirePermission(
                INVENTORY_READ
        );

        UUID companyId =
                TenantContext.requireCompanyId();

        Inventory inventory =
                findInventory(
                        inventoryId,
                        companyId
                );

        return InventoryResponse.from(
                inventory
        );
    }

    public InventoryResponse update(
            UUID inventoryId,
            UpdateInventoryRequest request
    ) {

        authorizationService.requirePermission(
                INVENTORY_UPDATE
        );

        UUID companyId =
                TenantContext.requireCompanyId();

        Inventory inventory =
                findInventory(
                        inventoryId,
                        companyId
                );

        Commodity commodity =
                commodityRepository
                        .findByIdAndCompanyId(
                                request.commodityId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Commodity not found"
                                )
                        );

        Grade grade =
                gradeRepository
                        .findByIdAndCompanyId(
                                request.gradeId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Grade not found"
                                )
                        );

        validateCommodityGrade(
                commodity,
                grade
        );

        Client supplierClient =
                resolveSupplierClient(
                        request.supplierClientId(),
                        companyId
                );

        inventory.update(
                commodity,
                grade,
                supplierClient,
                request.quantity(),
                normalize(request.unit()),
                normalize(request.location()),
                normalizeNullable(request.washPlant()),
                normalizeNullable(request.owner()),
                normalizeNullable(request.notes())
        );

        return InventoryResponse.from(
                inventoryRepository.save(inventory)
        );
    }

    public void delete(
            UUID inventoryId
    ) {

        authorizationService.requirePermission(
                INVENTORY_DELETE
        );

        UUID companyId =
                TenantContext.requireCompanyId();

        Inventory inventory =
                findInventory(
                        inventoryId,
                        companyId
                );

        inventoryRepository.delete(
                inventory
        );
    }

    private Inventory findInventory(
            UUID inventoryId,
            UUID companyId
    ) {

        return inventoryRepository
                .findByIdAndCompanyId(
                        inventoryId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found"
                        )
                );
    }

    private Client resolveSupplierClient(
            UUID supplierClientId,
            UUID companyId
    ) {

        if (supplierClientId == null) {
            return null;
        }

        return clientRepository
                .findByIdAndCompanyId(
                        supplierClientId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier client not found"
                        )
                );
    }

    private void validateCommodityGrade(
            Commodity commodity,
            Grade grade
    ) {

        if (!grade.getCommodity()
                .getId()
                .equals(commodity.getId())) {

            throw new InvalidInventoryCommodityGradeException(
                    "Grade does not belong to the selected commodity"
            );
        }
    }

    private String normalize(
            String value
    ) {
        return value.trim();
    }

    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}