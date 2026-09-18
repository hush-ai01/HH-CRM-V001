package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        RoleController roleController =
                new RoleController(roleService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // CREATE ROLE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateRole() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        RoleResponse response = new RoleResponse(
                roleId,
                companyId,
                "ADMIN",
                "Full system administrator",
                true,
                null,
                null
        );

        when(roleService.createRole(any(RoleCreateRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "companyId": "%s",
                    "name": "ADMIN",
                    "description": "Full system administrator"
                }
                """.formatted(companyId);

        mockMvc.perform(
                        post("/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("ADMIN"))
                .andExpect(jsonPath("$.description")
                        .value("Full system administrator"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(roleService)
                .createRole(any(RoleCreateRequest.class));
    }

    // -------------------------------------------------------------------------
    // GET ROLES
    // -------------------------------------------------------------------------

    @Test
    void shouldGetRolesForCurrentTenant() throws Exception {

        UUID companyId = UUID.randomUUID();

        RoleResponse admin = new RoleResponse(
                UUID.randomUUID(),
                companyId,
                "ADMIN",
                "Full system administrator",
                true,
                null,
                null
        );

        RoleResponse manager = new RoleResponse(
                UUID.randomUUID(),
                companyId,
                "MANAGER",
                "Company manager",
                true,
                null,
                null
        );

        when(roleService.getRolesByCompany())
                .thenReturn(List.of(admin, manager));

        mockMvc.perform(
                        get("/roles")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].name")
                        .value("ADMIN"))
                .andExpect(jsonPath("$[1].name")
                        .value("MANAGER"));

        verify(roleService)
                .getRolesByCompany();
    }

    // -------------------------------------------------------------------------
    // GET ROLE BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetRoleById() throws Exception {

        UUID companyId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        RoleResponse response = new RoleResponse(
                roleId,
                companyId,
                "ADMIN",
                "Full system administrator",
                true,
                null,
                null
        );

        when(roleService.getRoleById(roleId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/roles/{id}", roleId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(roleId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("ADMIN"));

        verify(roleService)
                .getRoleById(roleId);
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnBadRequestWhenCreatingRoleWithInvalidBody()
            throws Exception {

        String requestBody = """
                {
                    "companyId": null,
                    "name": "",
                    "description": ""
                }
                """;

        mockMvc.perform(
                        post("/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roleService);
    }

    // -------------------------------------------------------------------------
    // NOT FOUND
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist()
            throws Exception {

        UUID roleId = UUID.randomUUID();

        when(roleService.getRoleById(roleId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Role with id '" +
                                        roleId +
                                        "' not found"
                        )
                );

        mockMvc.perform(
                        get("/roles/{id}", roleId)
                )
                .andExpect(status().isNotFound());

        verify(roleService)
                .getRoleById(roleId);
    }

    // -------------------------------------------------------------------------
    // INVALID UUID
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnBadRequestForMalformedUuid()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/roles/{id}",
                                "not-a-valid-uuid"
                        )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roleService);
    }

    // -------------------------------------------------------------------------
    // OLD COMPANY-SCOPED ENDPOINT
    // -------------------------------------------------------------------------

    @Test
    void shouldNotExposeCompanyIdBasedRoleEndpoint()
            throws Exception {

        UUID companyId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/roles/company/{companyId}",
                                companyId
                        )
                )
                .andExpect(status().isNotFound());

        verifyNoInteractions(roleService);
    }
}