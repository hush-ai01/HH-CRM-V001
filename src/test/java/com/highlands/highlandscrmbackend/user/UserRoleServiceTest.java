package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private User user;

    @Mock
    private Role role;

    @Mock
    private Company company;

    private UserRoleService userRoleService;

    private UUID companyId;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {

        TenantContext.clear();

        userRoleService = new UserRoleService(
                userRepository,
                roleRepository
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // -------------------------------------------------------------------------
    // GET ROLES
    // -------------------------------------------------------------------------

    @Test
    void shouldGetRolesForCurrentTenantUser() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(user.getRoles())
                .thenReturn(Set.of(role));

        /*
         * UserRoleResponse.from(role) requires:
         *
         * role.getCompany().getId()
         */
        when(company.getId())
                .thenReturn(companyId);

        when(role.getCompany())
                .thenReturn(company);

        List<UserRoleResponse> response =
                userRoleService.getRoles(userId);

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldRejectGetRolesWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userRoleService.getRoles(userId)
        );

        verifyNoInteractions(
                userRepository,
                roleRepository
        );
    }

    @Test
    void shouldNotReturnUserFromAnotherTenant() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.getRoles(userId)
        );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verifyNoInteractions(roleRepository);
    }

    // -------------------------------------------------------------------------
    // ADD ROLE
    // -------------------------------------------------------------------------

    @Test
    void shouldAddRoleToUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        when(user.getRoles())
                .thenReturn(Set.of(role));

        /*
         * UserRoleResponse.from(role) requires:
         *
         * role.getCompany().getId()
         */
        when(company.getId())
                .thenReturn(companyId);

        when(role.getCompany())
                .thenReturn(company);

        when(userRepository.save(user))
                .thenReturn(user);

        List<UserRoleResponse> response =
                userRoleService.addRole(
                        userId,
                        roleId
                );

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(user)
                .addRole(role);

        verify(userRepository)
                .save(user);

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );
    }

    @Test
    void shouldRejectAddRoleWhenUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.addRole(
                        userId,
                        roleId
                )
        );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldRejectAddRoleWhenRoleDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.addRole(
                        userId,
                        roleId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(user, never())
                .addRole(any(Role.class));

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectAddRoleWhenRoleBelongsToAnotherTenant() {

        UUID anotherCompanyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        /*
         * Because the service uses tenant-scoped role lookup,
         * a role belonging to another company must not be returned.
         */
        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.addRole(
                        userId,
                        roleId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(roleRepository, never())
                .findByIdAndCompanyId(
                        roleId,
                        anotherCompanyId
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectAddRoleWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userRoleService.addRole(
                        userId,
                        roleId
                )
        );

        verifyNoInteractions(
                userRepository,
                roleRepository
        );
    }

    // -------------------------------------------------------------------------
    // REMOVE ROLE
    // -------------------------------------------------------------------------

    @Test
    void shouldRemoveRoleFromUserSuccessfully() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.of(role));

        when(user.getRoles())
                .thenReturn(Set.of());

        when(userRepository.save(user))
                .thenReturn(user);

        List<UserRoleResponse> response =
                userRoleService.removeRole(
                        userId,
                        roleId
                );

        assertNotNull(response);
        assertEquals(0, response.size());

        verify(user)
                .removeRole(role);

        verify(userRepository)
                .save(user);

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );
    }

    @Test
    void shouldRejectRemoveRoleWhenUserDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.removeRole(
                        userId,
                        roleId
                )
        );

        verify(userRepository)
                .findByIdAndCompanyId(
                        userId,
                        companyId
                );

        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldRejectRemoveRoleWhenRoleDoesNotExist() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.removeRole(
                        userId,
                        roleId
                )
        );

        verify(roleRepository)
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                );

        verify(user, never())
                .removeRole(any(Role.class));

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectRemoveRoleWhenTenantContextIsMissing() {

        TenantContext.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userRoleService.removeRole(
                        userId,
                        roleId
                )
        );

        verifyNoInteractions(
                userRepository,
                roleRepository
        );
    }

    // -------------------------------------------------------------------------
    // TENANT ISOLATION
    // -------------------------------------------------------------------------

    @Test
    void shouldNeverUseUnscopedUserLookup() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.getRoles(userId)
        );

        verify(userRepository, never())
                .findById(userId);
    }

    @Test
    void shouldNeverUseUnscopedRoleLookup() {

        TenantContext.setCompanyId(companyId);

        when(userRepository.findByIdAndCompanyId(
                userId,
                companyId
        )).thenReturn(Optional.of(user));

        when(roleRepository.findByIdAndCompanyId(
                roleId,
                companyId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userRoleService.addRole(
                        userId,
                        roleId
                )
        );

        verify(roleRepository, never())
                .findById(roleId);
    }
}