package com.highlands.highlandscrmbackend.commodity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.highlandscrmbackend.commodity.dto.CommodityStatusUpdateRequest;
import com.highlands.highlandscrmbackend.commodity.dto.CommodityResponse;
import com.highlands.highlandscrmbackend.commodity.dto.CreateCommodityRequest;
import com.highlands.highlandscrmbackend.commodity.dto.UpdateCommodityRequest;
import com.highlands.highlandscrmbackend.company.Company;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommodityControllerTest {

    @Mock
    private CommodityService commodityService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID commodityId;
    private UUID companyId;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        CommodityController controller =
                new CommodityController(commodityService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        commodityId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateCommodity() throws Exception {

        CreateCommodityRequest request =
                new CreateCommodityRequest(
                        "Gold",
                        "GOLD",
                        "Gold commodity"
                );

        CommodityResponse response =
                new CommodityResponse(
                        commodityId,
                        companyId,
                        "Gold",
                        "GOLD",
                        "Gold commodity",
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Commodity commodity = toCommodity(response);

        when(commodityService.create(
                eq("Gold"),
                eq("GOLD"),
                eq("Gold commodity")
        )).thenReturn(commodity);

        mockMvc.perform(
                        post("/commodities")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/commodities/" + commodityId
                ))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(commodityId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Gold"))
                .andExpect(jsonPath("$.code")
                        .value("GOLD"))
                .andExpect(jsonPath("$.description")
                        .value("Gold commodity"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(commodityService).create(
                eq("Gold"),
                eq("GOLD"),
                eq("Gold commodity")
        );
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    @Test
    void shouldGetCommodities() throws Exception {

        CommodityResponse response =
                new CommodityResponse(
                        commodityId,
                        companyId,
                        "Gold",
                        "GOLD",
                        "Gold commodity",
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Commodity commodity = toCommodity(response);

        when(commodityService.findAll())
                .thenReturn(List.of(commodity));

        mockMvc.perform(
                        get("/commodities")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(commodityId.toString()))
                .andExpect(jsonPath("$[0].name")
                        .value("Gold"))
                .andExpect(jsonPath("$[0].code")
                        .value("GOLD"));

        verify(commodityService).findAll();
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetCommodityById() throws Exception {

        CommodityResponse response =
                new CommodityResponse(
                        commodityId,
                        companyId,
                        "Gold",
                        "GOLD",
                        "Gold commodity",
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Commodity commodity = toCommodity(response);

        when(commodityService.findById(commodityId))
                .thenReturn(commodity);

        mockMvc.perform(
                        get("/commodities/{commodityId}", commodityId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(commodityId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Gold"))
                .andExpect(jsonPath("$.code")
                        .value("GOLD"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(commodityService).findById(commodityId);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateCommodity() throws Exception {

        UpdateCommodityRequest request =
                new UpdateCommodityRequest(
                        "Chrome",
                        "CHROME",
                        "Chrome commodity"
                );

        CommodityResponse response =
                new CommodityResponse(
                        commodityId,
                        companyId,
                        "Chrome",
                        "CHROME",
                        "Chrome commodity",
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Commodity commodity = toCommodity(response);

        when(commodityService.update(
                eq(commodityId),
                eq("Chrome"),
                eq("CHROME"),
                eq("Chrome commodity")
        )).thenReturn(commodity);

        mockMvc.perform(
                        put("/commodities/{commodityId}", commodityId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(commodityId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Chrome"))
                .andExpect(jsonPath("$.code")
                        .value("CHROME"))
                .andExpect(jsonPath("$.description")
                        .value("Chrome commodity"));

        verify(commodityService).update(
                eq(commodityId),
                eq("Chrome"),
                eq("CHROME"),
                eq("Chrome commodity")
        );
    }

    // -------------------------------------------------------------------------
    // CHANGE STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldChangeCommodityStatus() throws Exception {

        CommodityStatusUpdateRequest request =
                new CommodityStatusUpdateRequest(false);

        CommodityResponse response =
                new CommodityResponse(
                        commodityId,
                        companyId,
                        "Gold",
                        "GOLD",
                        "Gold commodity",
                        false,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Commodity commodity = toCommodity(response);

        when(commodityService.changeStatus(
                commodityId,
                false
        )).thenReturn(commodity);

        mockMvc.perform(
                        patch("/commodities/{commodityId}/status", commodityId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(commodityId.toString()))
                .andExpect(jsonPath("$.active")
                        .value(false));

        verify(commodityService)
                .changeStatus(commodityId, false);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteCommodity() throws Exception {

        doNothing()
                .when(commodityService)
                .delete(commodityId);

        mockMvc.perform(
                        delete("/commodities/{commodityId}", commodityId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(commodityService)
                .delete(commodityId);
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectCreateWhenNameIsMissing() throws Exception {

        String request = """
                {
                    "code": "GOLD",
                    "description": "Gold commodity"
                }
                """;

        mockMvc.perform(
                        post("/commodities")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commodityService);
    }

    @Test
    void shouldRejectCreateWhenCodeIsMissing() throws Exception {

        String request = """
                {
                    "name": "Gold",
                    "description": "Gold commodity"
                }
                """;

        mockMvc.perform(
                        post("/commodities")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commodityService);
    }

    @Test
    void shouldRejectUpdateWhenNameIsMissing() throws Exception {

        String request = """
                {
                    "code": "GOLD",
                    "description": "Gold commodity"
                }
                """;

        mockMvc.perform(
                        put("/commodities/{commodityId}", commodityId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commodityService);
    }

    @Test
    void shouldRejectStatusWhenActiveIsMissing() throws Exception {

        String request = """
                {
                }
                """;

        mockMvc.perform(
                        patch(
                                "/commodities/{commodityId}/status",
                                commodityId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commodityService);
    }

    // -------------------------------------------------------------------------
    // INVALID UUID
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectInvalidCommodityId() throws Exception {

        mockMvc.perform(
                        get("/commodities/not-a-uuid")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commodityService);
    }

    // -------------------------------------------------------------------------
    // SERVICE EXCEPTION
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // TEST HELPER
    // -------------------------------------------------------------------------

    private Commodity toCommodity(CommodityResponse response) {

        Commodity commodity = mock(Commodity.class);

        Company company = mock(Company.class);

        when(company.getId())
                .thenReturn(response.companyId());

        when(commodity.getId())
                .thenReturn(response.id());

        when(commodity.getCompany())
                .thenReturn(company);

        when(commodity.getName())
                .thenReturn(response.name());

        when(commodity.getCode())
                .thenReturn(response.code());

        when(commodity.getDescription())
                .thenReturn(response.description());

        when(commodity.isActive())
                .thenReturn(response.active());

        when(commodity.getCreatedAt())
                .thenReturn(response.createdAt());

        when(commodity.getUpdatedAt())
                .thenReturn(response.updatedAt());

        return commodity;
    }
}