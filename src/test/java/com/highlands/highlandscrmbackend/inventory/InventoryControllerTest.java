package com.highlands.highlandscrmbackend.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.highlandscrmbackend.common.exception.GlobalExceptionHandler;
import com.highlands.highlandscrmbackend.inventory.dto.CreateInventoryRequest;
import com.highlands.highlandscrmbackend.inventory.dto.InventoryResponse;
import com.highlands.highlandscrmbackend.inventory.dto.UpdateInventoryRequest;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID inventoryId;
    private UUID companyId;
    private UUID commodityId;
    private UUID gradeId;
    private UUID supplierClientId;

    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(inventoryController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();

        inventoryId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        commodityId = UUID.randomUUID();
        gradeId = UUID.randomUUID();
        supplierClientId = UUID.randomUUID();

        inventoryResponse =
                new InventoryResponse(
                        inventoryId,
                        companyId,
                        commodityId,
                        "Chrome",
                        "CHROME",
                        gradeId,
                        "42%",
                        "G42",
                        supplierClientId,
                        new BigDecimal("500.0000"),
                        "MT",
                        "Mooinooi",
                        "Lefa Wash Plant",
                        "ABC Minerals",
                        "AVAILABLE",
                        false,
                        "Chrome concentrate available",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    void create_shouldReturn201Created() throws Exception {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        commodityId,
                        gradeId,
                        supplierClientId,
                        new BigDecimal("500.0000"),
                        "MT",
                        "Mooinooi",
                        "Lefa Wash Plant",
                        "ABC Minerals",
                        "Chrome concentrate available"
                );

        when(inventoryService.create(any(CreateInventoryRequest.class)))
                .thenReturn(inventoryResponse);

        mockMvc.perform(
                        post("/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/inventory/" + inventoryId
                ))
                .andExpect(jsonPath("$.id").value(
                        inventoryId.toString()
                ))
                .andExpect(jsonPath("$.companyId").value(
                        companyId.toString()
                ))
                .andExpect(jsonPath("$.commodityId").value(
                        commodityId.toString()
                ))
                .andExpect(jsonPath("$.commodityName").value(
                        "Chrome"
                ))
                .andExpect(jsonPath("$.commodityCode").value(
                        "CHROME"
                ))
                .andExpect(jsonPath("$.gradeId").value(
                        gradeId.toString()
                ))
                .andExpect(jsonPath("$.gradeName").value(
                        "42%"
                ))
                .andExpect(jsonPath("$.gradeCode").value(
                        "G42"
                ))
                .andExpect(jsonPath("$.quantity").value(
                        500.0000
                ))
                .andExpect(jsonPath("$.unit").value(
                        "MT"
                ))
                .andExpect(jsonPath("$.location").value(
                        "Mooinooi"
                ))
                .andExpect(jsonPath("$.washPlant").value(
                        "Lefa Wash Plant"
                ))
                .andExpect(jsonPath("$.owner").value(
                        "ABC Minerals"
                ))
                .andExpect(jsonPath("$.status").value(
                        "AVAILABLE"
                ))
                .andExpect(jsonPath("$.portalSubmitted").value(
                        false
                ));

        verify(inventoryService)
                .create(any(CreateInventoryRequest.class));
    }

    @Test
    void create_shouldReturn400WhenRequestIsInvalid() throws Exception {

        CreateInventoryRequest request =
                new CreateInventoryRequest(
                        null,
                        gradeId,
                        null,
                        new BigDecimal("500.0000"),
                        "MT",
                        "Mooinooi",
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        post("/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                inventoryService,
                never()
        ).create(any());
    }

    // =========================================================================
    // FIND ALL
    // =========================================================================

    @Test
    void findAll_shouldReturn200Ok() throws Exception {

        when(inventoryService.findAll())
                .thenReturn(List.of(inventoryResponse));

        mockMvc.perform(
                        get("/inventory")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(
                        inventoryId.toString()
                ))
                .andExpect(jsonPath("$[0].commodityName").value(
                        "Chrome"
                ))
                .andExpect(jsonPath("$[0].gradeName").value(
                        "42%"
                ))
                .andExpect(jsonPath("$[0].quantity").value(
                        500.0000
                ));

        verify(inventoryService)
                .findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoInventoryExists()
            throws Exception {

        when(inventoryService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/inventory")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(inventoryService)
                .findAll();
    }

    // =========================================================================
    // FIND BY ID
    // =========================================================================

    @Test
    void findById_shouldReturn200Ok() throws Exception {

        when(inventoryService.findById(inventoryId))
                .thenReturn(inventoryResponse);

        mockMvc.perform(
                        get("/inventory/{inventoryId}", inventoryId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        inventoryId.toString()
                ))
                .andExpect(jsonPath("$.commodityId").value(
                        commodityId.toString()
                ))
                .andExpect(jsonPath("$.gradeId").value(
                        gradeId.toString()
                ))
                .andExpect(jsonPath("$.status").value(
                        "AVAILABLE"
                ));

        verify(inventoryService)
                .findById(inventoryId);
    }

    @Test
    void findById_shouldReturn404WhenInventoryDoesNotExist()
            throws Exception {

        when(inventoryService.findById(inventoryId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Inventory not found"
                        )
                );

        mockMvc.perform(
                        get("/inventory/{inventoryId}", inventoryId)
                )
                .andExpect(status().isNotFound());

        verify(inventoryService)
                .findById(inventoryId);
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    void update_shouldReturn200Ok() throws Exception {

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(
                        commodityId,
                        gradeId,
                        supplierClientId,
                        new BigDecimal("750.0000"),
                        "MT",
                        "Rustenburg",
                        "Updated Wash Plant",
                        "Updated Owner",
                        "Updated notes"
                );

        when(inventoryService.update(
                eq(inventoryId),
                any(UpdateInventoryRequest.class)
        )).thenReturn(inventoryResponse);

        mockMvc.perform(
                        put("/inventory/{inventoryId}", inventoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        inventoryId.toString()
                ))
                .andExpect(jsonPath("$.commodityId").value(
                        commodityId.toString()
                ))
                .andExpect(jsonPath("$.gradeId").value(
                        gradeId.toString()
                ));

        verify(inventoryService)
                .update(
                        eq(inventoryId),
                        any(UpdateInventoryRequest.class)
                );
    }

    @Test
    void update_shouldReturn400WhenRequestIsInvalid()
            throws Exception {

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(
                        commodityId,
                        null,
                        null,
                        new BigDecimal("750.0000"),
                        "MT",
                        "Rustenburg",
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        put("/inventory/{inventoryId}", inventoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                inventoryService,
                never()
        ).update(
                any(),
                any()
        );
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    void delete_shouldReturn204NoContent() throws Exception {

        doNothing()
                .when(inventoryService)
                .delete(inventoryId);

        mockMvc.perform(
                        delete(
                                "/inventory/{inventoryId}",
                                inventoryId
                        )
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(inventoryService)
                .delete(inventoryId);
    }

    @Test
    void delete_shouldReturn404WhenInventoryDoesNotExist()
            throws Exception {

        doThrow(
                new ResourceNotFoundException(
                        "Inventory not found"
                )
        )
                .when(inventoryService)
                .delete(inventoryId);

        mockMvc.perform(
                        delete(
                                "/inventory/{inventoryId}",
                                inventoryId
                        )
                )
                .andExpect(status().isNotFound());

        verify(inventoryService)
                .delete(inventoryId);
    }
}