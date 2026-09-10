package com.highlands.highlandscrmbackend.user;

import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        UserController userController =
                new UserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserResponse response = new UserResponse(
                userId,
                companyId,
                "john.doe@highlands.co.za",
                "John",
                "Doe",
                true,
                Set.of(roleId),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "companyId": "%s",
                    "email": "john.doe@highlands.co.za",
                    "password": "Password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "roleIds": ["%s"]
                }
                """.formatted(companyId, roleId);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("john.doe@highlands.co.za"))
                .andExpect(jsonPath("$.firstName")
                        .value("John"))
                .andExpect(jsonPath("$.lastName")
                        .value("Doe"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(userService)
                .createUser(any(UserCreateRequest.class));
    }

    @Test
    void shouldRejectInvalidUserRequest() throws Exception {

        String requestBody = """
                {
                    "companyId": null,
                    "email": "invalid-email",
                    "password": "123",
                    "firstName": "",
                    "lastName": ""
                }
                """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any(UserCreateRequest.class));
    }

    @Test
    void shouldRejectDuplicateUser() throws Exception {

        UUID companyId = UUID.randomUUID();

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(
                        new UserAlreadyExistsException(
                                "User with email 'john.doe@highlands.co.za' " +
                                        "already exists for this company"
                        )
                );

        String requestBody = """
                {
                    "companyId": "%s",
                    "email": "john.doe@highlands.co.za",
                    "password": "Password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "roleIds": []
                }
                """.formatted(companyId);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetUsersByCompanySuccessfully() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserResponse response = new UserResponse(
                userId,
                companyId,
                "john.doe@highlands.co.za",
                "John",
                "Doe",
                true,
                Set.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userService.getUsersByCompany(companyId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/users/company/{companyId}", companyId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$[0].email")
                        .value("john.doe@highlands.co.za"));

        verify(userService)
                .getUsersByCompany(companyId);
    }

    @Test
    void shouldRejectMissingCompanyWhenGettingUsers() throws Exception {

        UUID companyId = UUID.randomUUID();

        when(userService.getUsersByCompany(companyId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Company with id '" +
                                        companyId +
                                        "' not found"
                        )
                );

        mockMvc.perform(
                        get("/users/company/{companyId}", companyId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserResponse response = new UserResponse(
                userId,
                companyId,
                "john.doe@highlands.co.za",
                "John",
                "Doe",
                true,
                Set.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userService.getUserById(userId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/users/{id}", userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("john.doe@highlands.co.za"));

        verify(userService)
                .getUserById(userId);
    }

    @Test
    void shouldRejectMissingUser() throws Exception {

        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "User with id '" +
                                        userId +
                                        "' not found"
                        )
                );

        mockMvc.perform(
                        get("/users/{id}", userId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectMalformedUserId() throws Exception {

        mockMvc.perform(
                        get("/users/{id}", "not-a-uuid")
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .getUserById(any(UUID.class));
    }
}