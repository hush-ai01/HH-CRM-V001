package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.RoleAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();

        company = new Company(
                "Highlands Holdings SA (Pty) Ltd",
                "HIGHLANDS"
        );
    }

    @Test
    void shouldCreateRoleWhenCompanyExists() {

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(java.util.Optional.of(company));

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

        RoleResponse response = roleService.createRole(request);

        assertNotNull(response);
        assertEquals("ADMIN", response.name());
        assertEquals("Company administrator", response.description());
        assertTrue(response.active());

        verify(companyRepository).findById(companyId);

        verify(roleRepository)
                .existsByCompanyIdAndName(companyId, "ADMIN");

        verify(roleRepository)
                .save(any(Role.class));
    }

    @Test
    void shouldThrowExceptionWhenCompanyDoesNotExist() {

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.createRole(request)
        );

        verify(companyRepository).findById(companyId);

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleAlreadyExists() {

        RoleCreateRequest request = new RoleCreateRequest(
                companyId,
                "ADMIN",
                "Company administrator"
        );

        when(companyRepository.findById(companyId))
                .thenReturn(java.util.Optional.of(company));

        when(roleRepository.existsByCompanyIdAndName(
                companyId,
                "ADMIN"
        )).thenReturn(true);

        assertThrows(
                RoleAlreadyExistsException.class,
                () -> roleService.createRole(request)
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldReturnRolesForCompany() {

        when(companyRepository.existsById(companyId))
                .thenReturn(true);

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
                .thenReturn(List.of(adminRole, salesRole));

        List<RoleResponse> response =
                roleService.getRolesByCompany(companyId);

        assertEquals(2, response.size());

        assertEquals("ADMIN", response.get(0).name());
        assertEquals("SALES", response.get(1).name());

        verify(roleRepository)
                .findAllByCompanyId(companyId);
    }

    @Test
    void shouldThrowExceptionWhenGettingRolesForUnknownCompany() {

        when(companyRepository.existsById(companyId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRolesByCompany(companyId)
        );

        verify(roleRepository, never())
                .findAllByCompanyId(any(UUID.class));
    }

    @Test
    void shouldReturnRoleWhenRoleExists() {

        UUID roleId = UUID.randomUUID();

        Role role = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        when(roleRepository.findById(roleId))
                .thenReturn(java.util.Optional.of(role));

        RoleResponse response =
                roleService.getRoleById(roleId);

        assertNotNull(response);
        assertEquals("ADMIN", response.name());
        assertEquals("Company administrator", response.description());

        verify(roleRepository).findById(roleId);
    }

    @Test
    void shouldThrowExceptionWhenRoleDoesNotExist() {

        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRoleById(roleId)
        );

        verify(roleRepository).findById(roleId);
    }
}