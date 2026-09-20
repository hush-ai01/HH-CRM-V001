package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.permission.Permission;
import com.highlands.highlandscrmbackend.permission.PermissionRepository;
import com.highlands.highlandscrmbackend.permission.PermissionResponse;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.company.Company;
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
class RolePermissionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    private UUID companyId;
    private UUID anotherCompanyId;
    private UUID roleId;
    private UUID permissionId;

    private Company company;
    private Permission permission;

    @BeforeEach
    void setUp() {

        TenantContext.clear();

        companyId = UUID.randomUUID();
        anotherCompanyId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();

        company = mock(Company.class);

        permission = new Permission(
                "CLIENT_READ",
                "Read client records"
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldGetPermissionsForRoleInCurrentTenant() {

        TenantContext.setCompanyId(companyId);

        Role role = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        role.addPermission(permission);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        List<PermissionResponse> response =
                rolePermissionService.getPermissions(roleId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "CLIENT_READ",
                response.get(0).code()
        );
        assertEquals(
                "Read client records",
                response.get(0).description()
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldRejectGettingPermissionsWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> rolePermissionService.getPermissions(roleId)
        );

        verifyNoInteractions(
                roleRepository,
                permissionRepository
        );
    }

    @Test
    void shouldRejectRoleFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.getPermissions(roleId)
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(any(UUID.class));

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldAddPermissionToRole() {

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

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(roleRepository.save(role))
                .thenReturn(role);

        List<PermissionResponse> response =
                rolePermissionService.addPermission(
                        roleId,
                        permissionId
                );

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "CLIENT_READ",
                response.get(0).code()
        );

        assertTrue(
                role.getPermissions().contains(permission)
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(permissionRepository)
                .findById(permissionId);

        verify(roleRepository)
                .save(role);
    }

    @Test
    void shouldRejectAddingPermissionWhenRoleDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.addPermission(
                        roleId,
                        permissionId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verifyNoInteractions(permissionRepository);

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldRejectAddingPermissionWhenPermissionDoesNotExist() {

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

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.addPermission(
                        roleId,
                        permissionId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(permissionRepository)
                .findById(permissionId);

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldRejectAddingPermissionWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> rolePermissionService.addPermission(
                        roleId,
                        permissionId
                )
        );

        verifyNoInteractions(
                roleRepository,
                permissionRepository
        );
    }

    @Test
    void shouldRemovePermissionFromRole() {

        TenantContext.setCompanyId(companyId);

        Role role = new Role(
                company,
                "ADMIN",
                "Company administrator"
        );

        role.addPermission(permission);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(roleRepository.save(role))
                .thenReturn(role);

        List<PermissionResponse> response =
                rolePermissionService.removePermission(
                        roleId,
                        permissionId
                );

        assertNotNull(response);
        assertTrue(response.isEmpty());

        assertFalse(
                role.getPermissions().contains(permission)
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(permissionRepository)
                .findById(permissionId);

        verify(roleRepository)
                .save(role);
    }

    @Test
    void shouldRejectRemovingPermissionWhenRoleDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.removePermission(
                        roleId,
                        permissionId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verifyNoInteractions(permissionRepository);

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldRejectRemovingPermissionWhenPermissionDoesNotExist() {

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

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.removePermission(
                        roleId,
                        permissionId
                )
        );

        verify(permissionRepository)
                .findById(permissionId);

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void shouldRejectRemovingPermissionWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> rolePermissionService.removePermission(
                        roleId,
                        permissionId
                )
        );

        verifyNoInteractions(
                roleRepository,
                permissionRepository
        );
    }

    @Test
    void shouldNotUseUnscopedRoleLookup() {

        TenantContext.setCompanyId(companyId);

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> rolePermissionService.getPermissions(roleId)
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findById(roleId);
    }
}