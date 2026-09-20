
package com.highlands.highlandscrmbackend.user;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRoleControllerTest {

    @Mock
    private UserRoleService userRoleService;

    private MockMvc mockMvc;

    private UUID userId;
    private UUID roleId;
    private UUID companyId;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new UserRoleController(userRoleService)
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();

        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // GET /users/{userId}/roles
    // -------------------------------------------------------------------------

    @Test
    void shouldGetUserRoles() throws Exception {

        UserRoleResponse roleResponse =
                new UserRoleResponse(
                        roleId,
                        companyId,
                        "management",
                        "Management role",
                        true,
                        null,
                        null
                );

        when(userRoleService.getRoles(userId))
                .thenReturn(List.of(roleResponse));

        mockMvc.perform(
                        get("/users/{userId}/roles", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$[0].companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$[0].name")
                        .value("management"))
                .andExpect(jsonPath("$[0].description")
                        .value("Management role"))
                .andExpect(jsonPath("$[0].active")
                        .value(true));

        verify(userRoleService)
                .getRoles(userId);
    }

    // -------------------------------------------------------------------------
    // POST /users/{userId}/roles/{roleId}
    // -------------------------------------------------------------------------

    @Test
    void shouldAddRoleToUser() throws Exception {

        UserRoleResponse roleResponse =
                new UserRoleResponse(
                        roleId,
                        companyId,
                        "management",
                        "Management role",
                        true,
                        null,
                        null
                );

        when(userRoleService.addRole(
                userId,
                roleId
        )).thenReturn(List.of(roleResponse));

        mockMvc.perform(
                        post(
                                "/users/{userId}/roles/{roleId}",
                                userId,
                                roleId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$[0].companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$[0].name")
                        .value("management"))
                .andExpect(jsonPath("$[0].active")
                        .value(true));

        verify(userRoleService)
                .addRole(
                        userId,
                        roleId
                );
    }

    // -------------------------------------------------------------------------
    // DELETE /users/{userId}/roles/{roleId}
    // -------------------------------------------------------------------------

    @Test
    void shouldRemoveRoleFromUser() throws Exception {

        when(userRoleService.removeRole(
                userId,
                roleId
        )).thenReturn(List.of());

        mockMvc.perform(
                        delete(
                                "/users/{userId}/roles/{roleId}",
                                userId,
                                roleId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userRoleService)
                .removeRole(
                        userId,
                        roleId
                );
    }

    // -------------------------------------------------------------------------
    // NOT FOUND
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        when(userRoleService.getRoles(userId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "User with id '" +
                                        userId +
                                        "' not found"
                        )
                );

        mockMvc.perform(
                        get(
                                "/users/{userId}/roles",
                                userId
                        )
                )
                .andExpect(status().isNotFound());

        verify(userRoleService)
                .getRoles(userId);
    }

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist()
            throws Exception {

        when(userRoleService.addRole(
                userId,
                roleId
        )).thenThrow(
                new ResourceNotFoundException(
                        "Role with id '" +
                                roleId +
                                "' not found"
                )
        );

        mockMvc.perform(
                        post(
                                "/users/{userId}/roles/{roleId}",
                                userId,
                                roleId
                        )
                )
                .andExpect(status().isNotFound());

        verify(userRoleService)
                .addRole(
                        userId,
                        roleId
                );
    }

    // -------------------------------------------------------------------------
    // MALFORMED UUID
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectMalformedUserId()
            throws Exception {

        mockMvc.perform(
                        get("/users/not-a-uuid/roles")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedRoleId()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/users/{userId}/roles/not-a-uuid",
                                userId
                        )
                )
                .andExpect(status().isBadRequest());
    }
}

