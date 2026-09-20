package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.permission.PermissionResponse;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RolePermissionControllerTest {

    @Mock
    private RolePermissionService rolePermissionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        RolePermissionController controller =
                new RolePermissionController(
                        rolePermissionService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldGetPermissionsForRole() throws Exception {

        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        PermissionResponse permission = new PermissionResponse(
                permissionId,
                "CLIENT_READ",
                "Read client records"
        );

        when(rolePermissionService.getPermissions(roleId))
                .thenReturn(List.of(permission));

        mockMvc.perform(
                        get(
                                "/roles/{roleId}/permissions",
                                roleId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(permissionId.toString()))
                .andExpect(jsonPath("$[0].code")
                        .value("CLIENT_READ"))
                .andExpect(jsonPath("$[0].description")
                        .value("Read client records"));

        verify(rolePermissionService)
                .getPermissions(roleId);
    }

    @Test
    void shouldAddPermissionToRole() throws Exception {

        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        PermissionResponse permission = new PermissionResponse(
                permissionId,
                "CLIENT_READ",
                "Read client records"
        );

        when(rolePermissionService.addPermission(
                roleId,
                permissionId
        )).thenReturn(List.of(permission));

        mockMvc.perform(
                        post(
                                "/roles/{roleId}/permissions/{permissionId}",
                                roleId,
                                permissionId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(permissionId.toString()))
                .andExpect(jsonPath("$[0].code")
                        .value("CLIENT_READ"));

        verify(rolePermissionService)
                .addPermission(
                        roleId,
                        permissionId
                );
    }

    @Test
    void shouldRemovePermissionFromRole() throws Exception {

        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        when(rolePermissionService.removePermission(
                roleId,
                permissionId
        )).thenReturn(List.of());

        mockMvc.perform(
                        delete(
                                "/roles/{roleId}/permissions/{permissionId}",
                                roleId,
                                permissionId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rolePermissionService)
                .removePermission(
                        roleId,
                        permissionId
                );
    }

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist()
            throws Exception {

        UUID roleId = UUID.randomUUID();

        when(rolePermissionService.getPermissions(roleId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Role with id '" +
                                        roleId +
                                        "' not found"
                        )
                );

        mockMvc.perform(
                        get(
                                "/roles/{roleId}/permissions",
                                roleId
                        )
                )
                .andExpect(status().isNotFound());

        verify(rolePermissionService)
                .getPermissions(roleId);
    }

    @Test
    void shouldReturnBadRequestForMalformedRoleUuid()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/roles/{roleId}/permissions",
                                "not-a-valid-uuid"
                        )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rolePermissionService);
    }

    @Test
    void shouldReturnBadRequestForMalformedPermissionUuid()
            throws Exception {

        UUID roleId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/roles/{roleId}/permissions/{permissionId}",
                                roleId,
                                "not-a-valid-uuid"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rolePermissionService);
    }
}