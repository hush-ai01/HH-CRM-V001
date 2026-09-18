package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.RoleAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private RoleService roleService;

    private UUID companyId;
    private UUID anotherCompanyId;
    private UUID roleId;

    private Company company;
    private Company anotherCompany;

    @BeforeEach
    void setUp() {

        TenantContext.clear();

        companyId = UUID.randomUUID();
        anotherCompanyId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        company = new Company(
                "Highlands Holdings SA (Pty) Ltd",
                "HIGHLANDS"
        );

        anotherCompany = new Company(
                "Another Company",
                "ANOTHER"
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // CREATE ROLE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateRoleWhenCompanyExists() {

        TenantContext.setCompanyId(companyId);

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndName(
                companyId,
                "ADMIN"
        )).thenReturn(false);

        Role savedRole = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponse response =
                roleService.createRole(request);

        assertNotNull(response);

        assertEquals(
                "ADMIN",
                response.name()
        );

        assertEquals(
                "Company administrator",
                response.description()
        );

        assertTrue(response.active());

        verify(companyRepository)
                .findById(companyId);

        verify(roleRepository)
                .existsByCompanyIdAndName(
                        companyId,
                        "ADMIN"
                );

        verify(roleRepository)
                .save(any(Role.class));
    }

    @Test
    void shouldUseTenantContextInsteadOfRequestCompanyId() {

        TenantContext.setCompanyId(companyId);

        RoleCreateRequest request = new RoleCreateRequest(
                anotherCompanyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndName(
                companyId,
                "ADMIN"
        )).thenReturn(false);

        Role savedRole = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponse response =
                roleService.createRole(request);

        assertNotNull(response);

        assertEquals(
                "ADMIN",
                response.name()
        );

        verify(companyRepository)
                .findById(companyId);

        verify(companyRepository, never())
                .findById(anotherCompanyId);

        verify(roleRepository)
                .existsByCompanyIdAndName(
                        companyId,
                        "ADMIN"
                );

        verify(roleRepository, never())
                .existsByCompanyIdAndName(
                        anotherCompanyId,
                        "ADMIN"
                );

        verify(roleRepository)
                .save(any(Role.class));
    }

    @Test
    void shouldRejectCreateRoleWhenTenantContextIsMissing() {

        TenantContext.clear();

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        assertThrows(
                IllegalStateException.class,
                () -> roleService.createRole(request)
        );

        verifyNoInteractions(
                companyRepository,
                roleRepository
        );
    }

    @Test
    void shouldThrowExceptionWhenTenantCompanyDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> roleService.createRole(request)
                );

        assertEquals(
                "Company with id '" + companyId + "' not found",
                exception.getMessage()
        );

        verify(companyRepository)
                .findById(companyId);

        verify(roleRepository, never())
                .existsByCompanyIdAndName(
                        any(UUID.class),
                        anyString()
                );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleAlreadyExists() {

        TenantContext.setCompanyId(companyId);

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndName(
                companyId,
                "ADMIN"
        )).thenReturn(true);

        RoleAlreadyExistsException exception =
                assertThrows(
                        RoleAlreadyExistsException.class,
                        () -> roleService.createRole(request)
                );

        assertEquals(
                "Role with name 'ADMIN' already exists for this company",
                exception.getMessage()
        );

        verify(roleRepository)
                .existsByCompanyIdAndName(
                        companyId,
                        "ADMIN"
                );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    // -------------------------------------------------------------------------
    // GET ROLES BY TENANT
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnRolesForCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Role adminRole = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        Role salesRole = new Role(
                company,
                "SALES",
                "Sales user"
        );

        when(roleRepository.findAllByCompanyId(companyId))
                .thenReturn(
                        List.of(
                                adminRole,
                                salesRole
                        )
                );

        List<RoleResponse> response =
                roleService.getRolesByCompany();

        assertNotNull(response);

        assertEquals(
                2,
                response.size()
        );

        assertEquals(
                "ADMIN",
                response.get(0).name()
        );

        assertEquals(
                "SALES",
                response.get(1).name()
        );

        verify(roleRepository)
                .findAllByCompanyId(companyId);

        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void shouldOnlyQueryRolesUsingCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Role adminRole = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.findAllByCompanyId(companyId))
                .thenReturn(List.of(adminRole));

        roleService.getRolesByCompany();

        verify(roleRepository)
                .findAllByCompanyId(companyId);

        verify(roleRepository, never())
                .findAllByCompanyId(anotherCompanyId);
    }

    @Test
    void shouldRejectGetRolesWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> roleService.getRolesByCompany()
        );

        verifyNoInteractions(
                roleRepository,
                companyRepository
        );
    }

    // -------------------------------------------------------------------------
    // GET ROLE BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnRoleWhenRoleExistsForCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Role role = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        RoleResponse response =
                roleService.getRoleById(roleId);

        assertNotNull(response);

        assertEquals(
                "ADMIN",
                response.name()
        );

        assertEquals(
                "Company administrator",
                response.description()
        );

        assertTrue(response.active());

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );
    }

    @Test
    void shouldRejectRoleFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> roleService.getRoleById(roleId)
                );

        assertEquals(
                "Role with id '" + roleId + "' not found",
                exception.getMessage()
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(roleId);
    }

    @Test
    void shouldRejectGetRoleWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> roleService.getRoleById(roleId)
        );

        verifyNoInteractions(
                roleRepository,
                companyRepository
        );
    }

    // -------------------------------------------------------------------------
    // TENANT ISOLATION
    // -------------------------------------------------------------------------

    @Test
    void shouldNotUseRequestCompanyWhenGettingRoles() {

        TenantContext.setCompanyId(companyId);

        Role role = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.findAllByCompanyId(companyId))
                .thenReturn(List.of(role));

        List<RoleResponse> response =
                roleService.getRolesByCompany();

        assertEquals(
                1,
                response.size()
        );

        verify(roleRepository)
                .findAllByCompanyId(companyId);

        verify(roleRepository, never())
                .findAllByCompanyId(anotherCompanyId);
    }

    @Test
    void shouldNotFallBackToUnscopedRoleLookup() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRoleById(roleId)
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(any(UUID.class));
    }
}