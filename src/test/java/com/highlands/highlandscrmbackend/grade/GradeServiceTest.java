package com.highlands.highlandscrmbackend.grade;

import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.commodity.CommodityRepository;
import com.highlands.highlandscrmbackend.common.exception.GradeAlreadyExistsException;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthorizationService authorizationService;

    private GradeService gradeService;

    private UUID companyId;
    private UUID commodityId;
    private UUID gradeId;

    private Company company;
    private Commodity commodity;
    private Grade grade;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService(
                gradeRepository,
                commodityRepository,
                companyRepository,
                authorizationService
        );

        companyId = UUID.randomUUID();
        commodityId = UUID.randomUUID();
        gradeId = UUID.randomUUID();

        company = mock(Company.class);
        commodity = mock(Commodity.class);
        grade = mock(Grade.class);

        TenantContext.setCompanyId(companyId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateGrade() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.of(commodity));

        when(gradeRepository.existsByCompanyIdAndCommodityIdAndCode(
                companyId,
                commodityId,
                "24K"
        )).thenReturn(false);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        Grade savedGrade = new Grade(
                company,
                commodity,
                "24K",
                "24K",
                "24 karat gold"
        );

        when(gradeRepository.save(any(Grade.class)))
                .thenReturn(savedGrade);

        Grade result = gradeService.create(
                commodityId,
                " 24K ",
                " 24k ",
                " 24 karat gold "
        );

        assertNotNull(result);
        assertEquals("24K", result.getName());
        assertEquals("24K", result.getCode());
        assertEquals("24 karat gold", result.getDescription());

        verify(authorizationService).requirePermission("GRADE_CREATE");
        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);
        verify(gradeRepository)
                .existsByCompanyIdAndCommodityIdAndCode(
                        companyId,
                        commodityId,
                        "24K"
                );
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void shouldRejectDuplicateGradeCode() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.of(commodity));

        when(gradeRepository.existsByCompanyIdAndCommodityIdAndCode(
                companyId,
                commodityId,
                "24K"
        )).thenReturn(true);

        assertThrows(
                GradeAlreadyExistsException.class,
                () -> gradeService.create(
                        commodityId,
                        "24K",
                        "24K",
                        "24 karat gold"
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenCommodityDoesNotExistForTenant() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.create(
                        commodityId,
                        "24K",
                        "24K",
                        null
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenCompanyDoesNotExist() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.of(commodity));

        when(gradeRepository.existsByCompanyIdAndCommodityIdAndCode(
                companyId,
                commodityId,
                "24K"
        )).thenReturn(false);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.create(
                        commodityId,
                        "24K",
                        "24K",
                        null
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldFindAllGradesForTenant() {
        when(gradeRepository.findAllByCompanyIdOrderByNameAsc(companyId))
                .thenReturn(List.of(grade));

        List<Grade> result = gradeService.findAll();

        assertEquals(1, result.size());
        assertSame(grade, result.get(0));

        verify(authorizationService).requirePermission("GRADE_READ");
        verify(gradeRepository)
                .findAllByCompanyIdOrderByNameAsc(companyId);
    }

    @Test
    void shouldFindGradesByCommodity() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.of(commodity));

        when(gradeRepository
                .findAllByCompanyIdAndCommodityIdOrderByNameAsc(
                        companyId,
                        commodityId
                ))
                .thenReturn(List.of(grade));

        List<Grade> result = gradeService.findByCommodity(commodityId);

        assertEquals(1, result.size());
        assertSame(grade, result.get(0));

        verify(authorizationService).requirePermission("GRADE_READ");
        verify(commodityRepository)
                .findByIdAndCompanyId(commodityId, companyId);
        verify(gradeRepository)
                .findAllByCompanyIdAndCommodityIdOrderByNameAsc(
                        companyId,
                        commodityId
                );
    }

    @Test
    void shouldRejectFindByCommodityWhenCommodityDoesNotExistForTenant() {
        when(commodityRepository.findByIdAndCompanyId(commodityId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.findByCommodity(commodityId)
        );

        verify(
                gradeRepository,
                never()
        ).findAllByCompanyIdAndCommodityIdOrderByNameAsc(
                any(),
                any()
        );
    }

    @Test
    void shouldFindGradeById() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        Grade result = gradeService.findById(gradeId);

        assertSame(grade, result);

        verify(authorizationService).requirePermission("GRADE_READ");
        verify(gradeRepository)
                .findByIdAndCompanyId(gradeId, companyId);
    }

    @Test
    void shouldRejectFindByIdWhenGradeDoesNotExistForTenant() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.findById(gradeId)
        );
    }

    @Test
    void shouldUpdateGrade() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository
                .existsByCompanyIdAndCommodityIdAndCodeAndIdNot(
                        companyId,
                        commodityId,
                        "22K",
                        gradeId
                )).thenReturn(false);

        when(gradeRepository.save(grade))
                .thenReturn(grade);

        Grade result = gradeService.update(
                gradeId,
                commodityId,
                "22K",
                "22k",
                "22 karat gold"
        );

        assertSame(grade, result);

        verify(grade).update(
                commodity,
                "22K",
                "22K",
                "22 karat gold"
        );

        verify(gradeRepository).save(grade);
    }

    @Test
    void shouldRejectUpdateWhenGradeDoesNotExistForTenant() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.update(
                        gradeId,
                        commodityId,
                        "22K",
                        "22K",
                        null
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenCommodityDoesNotExistForTenant() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.update(
                        gradeId,
                        commodityId,
                        "22K",
                        "22K",
                        null
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateGradeCodeDuringUpdate() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        when(commodityRepository.findByIdAndCompanyId(
                commodityId,
                companyId
        )).thenReturn(Optional.of(commodity));

        when(gradeRepository
                .existsByCompanyIdAndCommodityIdAndCodeAndIdNot(
                        companyId,
                        commodityId,
                        "24K",
                        gradeId
                )).thenReturn(true);

        assertThrows(
                GradeAlreadyExistsException.class,
                () -> gradeService.update(
                        gradeId,
                        commodityId,
                        "24K",
                        "24K",
                        null
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldActivateGrade() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        when(gradeRepository.save(grade))
                .thenReturn(grade);

        Grade result = gradeService.changeStatus(
                gradeId,
                true
        );

        assertSame(grade, result);

        verify(grade).changeStatus(true);
        verify(gradeRepository).save(grade);
        verify(authorizationService)
                .requirePermission("GRADE_UPDATE");
    }

    @Test
    void shouldDeactivateGrade() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        when(gradeRepository.save(grade))
                .thenReturn(grade);

        Grade result = gradeService.changeStatus(
                gradeId,
                false
        );

        assertSame(grade, result);

        verify(grade).changeStatus(false);
        verify(gradeRepository).save(grade);
    }

    @Test
    void shouldRejectStatusChangeWhenGradeDoesNotExistForTenant() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.changeStatus(
                        gradeId,
                        true
                )
        );

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void shouldDeleteGrade() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.of(grade));

        gradeService.delete(gradeId);

        verify(authorizationService)
                .requirePermission("GRADE_DELETE");

        verify(gradeRepository).delete(grade);
    }

    @Test
    void shouldRejectDeleteWhenGradeDoesNotExistForTenant() {
        when(gradeRepository.findByIdAndCompanyId(gradeId, companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.delete(gradeId)
        );

        verify(gradeRepository, never()).delete(any());
    }
}