package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.role.Role;
import com.highlands.highlandscrmbackend.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Company company;

    @Mock
    private Role role;

    private UserService userService;

    private UUID companyId;
    private UUID roleId;

    @BeforeEach
    void setUp() {

        userService = new UserService(
                userRepository,
                companyRepository,
                roleRepository,
                passwordEncoder
        );

        companyId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void shouldCreateUserSuccessfully() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of(roleId)
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(company.getId())
                .thenReturn(companyId);

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(role.getId())
                .thenReturn(roleId);

        when(role.getCompany())
                .thenReturn(company);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertNotNull(response);

        assertEquals(companyId, response.companyId());

        assertEquals(
                "john.doe@highlands.co.za",
                response.email()
        );

        assertEquals("John", response.firstName());

        assertEquals("Doe", response.lastName());

        assertTrue(response.active());

        assertEquals(
                Set.of(roleId),
                response.roleIds()
        );

        verify(userRepository)
                .save(any(User.class));

        verify(passwordEncoder)
                .encode("Password123");
    }

    @Test
    void shouldRejectUserWhenCompanyDoesNotExist() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of()
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of()
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldRejectWhenRoleDoesNotExist() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of(roleId)
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldRejectRoleFromAnotherCompany() {

        UUID anotherCompanyId = UUID.randomUUID();

        Company anotherCompany = mock(Company.class);
        Role anotherCompanyRole = mock(Role.class);

        when(anotherCompany.getId())
                .thenReturn(anotherCompanyId);

        when(anotherCompanyRole.getCompany())
                .thenReturn(anotherCompany);

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of(roleId)
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(anotherCompanyRole));

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldCreateUserWithoutRoles() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of()
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertNotNull(response);

        assertTrue(response.roleIds().isEmpty());

        verify(userRepository)
                .save(any(User.class));

        verify(roleRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    void shouldEncodePasswordBeforeSavingUser() {

        UserCreateRequest request = new UserCreateRequest(
                companyId,
                "john.doe@highlands.co.za",
                "Password123",
                "John",
                "Doe",
                Set.of()
        );

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(userRepository.existsByCompanyIdAndEmail(
                companyId,
                request.email()
        )).thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(passwordEncoder)
                .encode("Password123");

        verify(userRepository)
                .save(argThat(user ->
                        user.getPasswordHash()
                                .equals("encoded-password")
                ));
    }
}