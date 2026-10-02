package com.highlands.highlandscrmbackend.grade;

import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.commodity.CommodityRepository;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.GradeAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GradeService {

    private static final String GRADE_CREATE = "GRADE_CREATE";
    private static final String GRADE_READ = "GRADE_READ";
    private static final String GRADE_UPDATE = "GRADE_UPDATE";
    private static final String GRADE_DELETE = "GRADE_DELETE";

    private final GradeRepository gradeRepository;
    private final CommodityRepository commodityRepository;
    private final CompanyRepository companyRepository;
    private final AuthorizationService authorizationService;

    public GradeService(
            GradeRepository gradeRepository,
            CommodityRepository commodityRepository,
            CompanyRepository companyRepository,
            AuthorizationService authorizationService
    ) {
        this.gradeRepository = gradeRepository;
        this.commodityRepository = commodityRepository;
        this.companyRepository = companyRepository;
        this.authorizationService = authorizationService;
    }

    public Grade create(
            UUID commodityId,
            String name,
            String code,
            String description
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_CREATE);

        String normalizedName = normalizeName(name);
        String normalizedCode = normalizeCode(code);
        String normalizedDescription = normalizeDescription(description);

        Commodity commodity = commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        if (gradeRepository.existsByCompanyIdAndCommodityIdAndCode(
                companyId,
                commodityId,
                normalizedCode
        )) {
            throw new GradeAlreadyExistsException(
                    "Grade code already exists for commodity: " + normalizedCode
            );
        }

        Company company = companyRepository
                .findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found: " + companyId
                        )
                );

        Grade grade = new Grade(
                company,
                commodity,
                normalizedName,
                normalizedCode,
                normalizedDescription
        );

        return gradeRepository.save(grade);
    }

    @Transactional(readOnly = true)
    public List<Grade> findAll() {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_READ);

        return gradeRepository.findAllByCompanyIdOrderByNameAsc(companyId);
    }

    @Transactional(readOnly = true)
    public List<Grade> findByCommodity(UUID commodityId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_READ);

        commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        return gradeRepository
                .findAllByCompanyIdAndCommodityIdOrderByNameAsc(
                        companyId,
                        commodityId
                );
    }

    @Transactional(readOnly = true)
    public Grade findById(UUID gradeId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_READ);

        return gradeRepository
                .findByIdAndCompanyId(gradeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Grade not found: " + gradeId
                        )
                );
    }

    public Grade update(
            UUID gradeId,
            UUID commodityId,
            String name,
            String code,
            String description
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_UPDATE);

        Grade grade = gradeRepository
                .findByIdAndCompanyId(gradeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Grade not found: " + gradeId
                        )
                );

        Commodity commodity = commodityRepository
                .findByIdAndCompanyId(commodityId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Commodity not found: " + commodityId
                        )
                );

        String normalizedName = normalizeName(name);
        String normalizedCode = normalizeCode(code);
        String normalizedDescription = normalizeDescription(description);

        if (gradeRepository.existsByCompanyIdAndCommodityIdAndCodeAndIdNot(
                companyId,
                commodityId,
                normalizedCode,
                gradeId
        )) {
            throw new GradeAlreadyExistsException(
                    "Grade code already exists for commodity: " + normalizedCode
            );
        }

        grade.update(
                commodity,
                normalizedName,
                normalizedCode,
                normalizedDescription
        );

        return gradeRepository.save(grade);
    }

    public Grade changeStatus(UUID gradeId, boolean active) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_UPDATE);

        Grade grade = gradeRepository
                .findByIdAndCompanyId(gradeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Grade not found: " + gradeId
                        )
                );

        grade.changeStatus(active);

        return gradeRepository.save(grade);
    }

    public void delete(UUID gradeId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission(GRADE_DELETE);

        Grade grade = gradeRepository
                .findByIdAndCompanyId(gradeId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Grade not found: " + gradeId
                        )
                );

        gradeRepository.delete(grade);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}