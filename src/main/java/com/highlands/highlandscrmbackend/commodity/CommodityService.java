package com.highlands.highlandscrmbackend.commodity;

import com.highlands.highlandscrmbackend.common.exception.CommodityAlreadyExistsException;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommodityService {

    private static final String COMMODITY_CREATE = "COMMODITY_CREATE";
    private static final String COMMODITY_READ = "COMMODITY_READ";
    private static final String COMMODITY_UPDATE = "COMMODITY_UPDATE";
    private static final String COMMODITY_DELETE = "COMMODITY_DELETE";

    private final CommodityRepository commodityRepository;
    private final CompanyRepository companyRepository;
    private final AuthorizationService authorizationService;

    public CommodityService(
            CommodityRepository commodityRepository,
            CompanyRepository companyRepository,
            AuthorizationService authorizationService
    ) {
        this.commodityRepository = commodityRepository;
        this.companyRepository = companyRepository;
        this.authorizationService = authorizationService;
    }

    public Commodity create(
            String name,
            String code,
            String description
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_CREATE);

        String normalizedName = normalizeName(name);
        String normalizedCode = normalizeCode(code);

        if (commodityRepository.existsByCompanyIdAndCode(
                companyId,
                normalizedCode
        )) {
            throw new CommodityAlreadyExistsException(
                    "Commodity code already exists: " + normalizedCode
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found: " + companyId
                        )
                );

        Commodity commodity = new Commodity(
                company,
                normalizedName,
                normalizedCode,
                normalizeDescription(description)
        );

        return commodityRepository.save(commodity);
    }

    @Transactional(readOnly = true)
    public List<Commodity> findAll() {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_READ);

        return commodityRepository
                .findAllByCompanyIdOrderByNameAsc(companyId);
    }

    @Transactional(readOnly = true)
    public Commodity findById(UUID commodityId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_READ);

        return commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );
    }

    public Commodity update(
            UUID commodityId,
            String name,
            String code,
            String description
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_UPDATE);

        Commodity commodity = commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        String normalizedName = normalizeName(name);
        String normalizedCode = normalizeCode(code);

        if (commodityRepository.existsByCompanyIdAndCodeAndIdNot(
                companyId,
                normalizedCode,
                commodityId
        )) {
            throw new CommodityAlreadyExistsException(
                    "Commodity code already exists: " + normalizedCode
            );
        }

        commodity.update(
                normalizedName,
                normalizedCode,
                normalizeDescription(description)
        );

        return commodityRepository.save(commodity);
    }

    public Commodity changeStatus(
            UUID commodityId,
            boolean active
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_UPDATE);

        Commodity commodity = commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        commodity.changeStatus(active);

        return commodityRepository.save(commodity);
    }

    public void delete(UUID commodityId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(COMMODITY_DELETE);

        Commodity commodity = commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        commodityRepository.delete(commodity);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeCode(String code) {
        return code == null
                ? null
                : code.trim().toUpperCase();
    }

    private String normalizeDescription(String description) {
        return description == null
                ? null
                : description.trim();
    }
}