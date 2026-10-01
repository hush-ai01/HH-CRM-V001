package com.highlands.highlandscrmbackend.deal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.highlands.highlandscrmbackend.deal.dto.ChangeDealStatusRequest;
import com.highlands.highlandscrmbackend.deal.dto.CreateDealRequest;
import com.highlands.highlandscrmbackend.deal.dto.DealResponse;
import com.highlands.highlandscrmbackend.deal.dto.UpdateDealRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    @Mock
    private DealService dealService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID dealId;
    private UUID companyId;
    private UUID clientId;
    private UUID ownerUserId;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        DealController controller =
                new DealController(dealService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        dealId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateDeal() throws Exception {

        CreateDealRequest request =
                new CreateDealRequest(
                        clientId,
                        DealType.SELL,
                        "Chrome",
                        "42% Grade",
                        new BigDecimal("100.0000"),
                        "TON",
                        new BigDecimal("1500.0000"),
                        "ZAR",
                        LocalDate.of(2026, 10, 31),
                        "Chrome shipment"
                );

        DealResponse response =
                new DealResponse(
                        dealId,
                        companyId,
                        clientId,
                        ownerUserId,
                        "DL-2026-ABC12345",
                        DealType.SELL,
                        DealStatus.DRAFT,
                        "Chrome",
                        "42% Grade",
                        new BigDecimal("100.0000"),
                        "TON",
                        new BigDecimal("1500.0000"),
                        "ZAR",
                        new BigDecimal("150000.0000"),
                        LocalDate.of(2026, 10, 31),
                        "Chrome shipment",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        /*
         * Important:
         * Create the Deal before calling when(...).thenReturn(...).
         * This avoids Mockito unfinished-stubbing issues caused by nested
         * Mockito calls inside thenReturn().
         */
        Deal deal = toDeal(response);

        when(dealService.create(
                eq(clientId),
                eq(DealType.SELL),
                eq("Chrome"),
                eq("42% Grade"),
                eq(new BigDecimal("100.0000")),
                eq("TON"),
                eq(new BigDecimal("1500.0000")),
                eq("ZAR"),
                eq(LocalDate.of(2026, 10, 31)),
                eq("Chrome shipment")
        )).thenReturn(deal);

        mockMvc.perform(
                        post("/deals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(dealId.toString()))
                .andExpect(jsonPath("$.companyId")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.clientId")
                        .value(clientId.toString()))
                .andExpect(jsonPath("$.ownerUserId")
                        .value(ownerUserId.toString()))
                .andExpect(jsonPath("$.dealNumber")
                        .value("DL-2026-ABC12345"))
                .andExpect(jsonPath("$.type")
                        .value("SELL"))
                .andExpect(jsonPath("$.status")
                        .value("DRAFT"))
                .andExpect(jsonPath("$.commodity")
                        .value("Chrome"))
                .andExpect(jsonPath("$.grade")
                        .value("42% Grade"))
                .andExpect(jsonPath("$.quantity")
                        .value(100.0))
                .andExpect(jsonPath("$.unit")
                        .value("TON"))
                .andExpect(jsonPath("$.unitPrice")
                        .value(1500.0))
                .andExpect(jsonPath("$.currency")
                        .value("ZAR"))
                .andExpect(jsonPath("$.totalValue")
                        .value(150000.0));

        verify(dealService).create(
                eq(clientId),
                eq(DealType.SELL),
                eq("Chrome"),
                eq("42% Grade"),
                eq(new BigDecimal("100.0000")),
                eq("TON"),
                eq(new BigDecimal("1500.0000")),
                eq("ZAR"),
                eq(LocalDate.of(2026, 10, 31)),
                eq("Chrome shipment")
        );
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    @Test
    void shouldGetDeals() throws Exception {

        DealResponse response =
                new DealResponse(
                        dealId,
                        companyId,
                        clientId,
                        ownerUserId,
                        "DL-2026-ABC12345",
                        DealType.SELL,
                        DealStatus.DRAFT,
                        "Chrome",
                        "42% Grade",
                        new BigDecimal("100.0000"),
                        "TON",
                        new BigDecimal("1500.0000"),
                        "ZAR",
                        new BigDecimal("150000.0000"),
                        LocalDate.of(2026, 10, 31),
                        "Chrome shipment",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Deal deal = toDeal(response);

        when(dealService.findAll())
                .thenReturn(List.of(deal));

        mockMvc.perform(
                        get("/deals")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(dealId.toString()))
                .andExpect(jsonPath("$[0].dealNumber")
                        .value("DL-2026-ABC12345"))
                .andExpect(jsonPath("$[0].commodity")
                        .value("Chrome"));

        verify(dealService).findAll();
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    @Test
    void shouldGetDealById() throws Exception {

        DealResponse response =
                new DealResponse(
                        dealId,
                        companyId,
                        clientId,
                        ownerUserId,
                        "DL-2026-ABC12345",
                        DealType.SELL,
                        DealStatus.DRAFT,
                        "Chrome",
                        "42% Grade",
                        new BigDecimal("100.0000"),
                        "TON",
                        new BigDecimal("1500.0000"),
                        "ZAR",
                        new BigDecimal("150000.0000"),
                        LocalDate.of(2026, 10, 31),
                        "Chrome shipment",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Deal deal = toDeal(response);

        when(dealService.findById(dealId))
                .thenReturn(deal);

        mockMvc.perform(
                        get("/deals/{dealId}", dealId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(dealId.toString()))
                .andExpect(jsonPath("$.dealNumber")
                        .value("DL-2026-ABC12345"))
                .andExpect(jsonPath("$.status")
                        .value("DRAFT"));

        verify(dealService).findById(dealId);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateDeal() throws Exception {

        UpdateDealRequest request =
                new UpdateDealRequest(
                        "Chrome",
                        "44% Grade",
                        new BigDecimal("120.0000"),
                        "TON",
                        new BigDecimal("1600.0000"),
                        "ZAR",
                        LocalDate.of(2026, 11, 15),
                        "Updated Chrome shipment"
                );

        DealResponse response =
                new DealResponse(
                        dealId,
                        companyId,
                        clientId,
                        ownerUserId,
                        "DL-2026-ABC12345",
                        DealType.SELL,
                        DealStatus.DRAFT,
                        "Chrome",
                        "44% Grade",
                        new BigDecimal("120.0000"),
                        "TON",
                        new BigDecimal("1600.0000"),
                        "ZAR",
                        new BigDecimal("192000.0000"),
                        LocalDate.of(2026, 11, 15),
                        "Updated Chrome shipment",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Deal deal = toDeal(response);

        when(dealService.update(
                eq(dealId),
                eq("Chrome"),
                eq("44% Grade"),
                eq(new BigDecimal("120.0000")),
                eq("TON"),
                eq(new BigDecimal("1600.0000")),
                eq("ZAR"),
                eq(LocalDate.of(2026, 11, 15)),
                eq("Updated Chrome shipment")
        )).thenReturn(deal);

        mockMvc.perform(
                        put("/deals/{dealId}", dealId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(dealId.toString()))
                .andExpect(jsonPath("$.grade")
                        .value("44% Grade"))
                .andExpect(jsonPath("$.quantity")
                        .value(120.0))
                .andExpect(jsonPath("$.unitPrice")
                        .value(1600.0))
                .andExpect(jsonPath("$.totalValue")
                        .value(192000.0));

        verify(dealService).update(
                eq(dealId),
                eq("Chrome"),
                eq("44% Grade"),
                eq(new BigDecimal("120.0000")),
                eq("TON"),
                eq(new BigDecimal("1600.0000")),
                eq("ZAR"),
                eq(LocalDate.of(2026, 11, 15)),
                eq("Updated Chrome shipment")
        );
    }

    // -------------------------------------------------------------------------
    // CHANGE STATUS
    // -------------------------------------------------------------------------

    @Test
    void shouldChangeDealStatus() throws Exception {

        ChangeDealStatusRequest request =
                new ChangeDealStatusRequest(DealStatus.WON);

        DealResponse response =
                new DealResponse(
                        dealId,
                        companyId,
                        clientId,
                        ownerUserId,
                        "DL-2026-ABC12345",
                        DealType.SELL,
                        DealStatus.WON,
                        "Chrome",
                        "42% Grade",
                        new BigDecimal("100.0000"),
                        "TON",
                        new BigDecimal("1500.0000"),
                        "ZAR",
                        new BigDecimal("150000.0000"),
                        LocalDate.of(2026, 10, 31),
                        "Chrome shipment",
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                );

        Deal deal = toDeal(response);

        when(dealService.changeStatus(
                dealId,
                DealStatus.WON
        )).thenReturn(deal);

        mockMvc.perform(
                        patch("/deals/{dealId}/status", dealId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(dealId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("WON"));

        verify(dealService)
                .changeStatus(dealId, DealStatus.WON);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteDeal() throws Exception {

        doNothing()
                .when(dealService)
                .delete(dealId);

        mockMvc.perform(
                        delete("/deals/{dealId}", dealId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(dealService)
                .delete(dealId);
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectCreateWhenClientIdIsMissing() throws Exception {

        String request = """
                {
                    "type": "SELL",
                    "commodity": "Chrome",
                    "quantity": 100,
                    "unit": "TON",
                    "unitPrice": 1500,
                    "currency": "ZAR"
                }
                """;

        mockMvc.perform(
                        post("/deals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    @Test
    void shouldRejectCreateWhenCommodityIsMissing() throws Exception {

        String request = """
                {
                    "clientId": "%s",
                    "type": "SELL",
                    "quantity": 100,
                    "unit": "TON",
                    "unitPrice": 1500,
                    "currency": "ZAR"
                }
                """.formatted(clientId);

        mockMvc.perform(
                        post("/deals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    @Test
    void shouldRejectCreateWhenQuantityIsInvalid() throws Exception {

        String request = """
                {
                    "clientId": "%s",
                    "type": "SELL",
                    "commodity": "Chrome",
                    "quantity": 0,
                    "unit": "TON",
                    "unitPrice": 1500,
                    "currency": "ZAR"
                }
                """.formatted(clientId);

        mockMvc.perform(
                        post("/deals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    @Test
    void shouldRejectCreateWhenUnitPriceIsInvalid() throws Exception {

        String request = """
                {
                    "clientId": "%s",
                    "type": "SELL",
                    "commodity": "Chrome",
                    "quantity": 100,
                    "unit": "TON",
                    "unitPrice": 0,
                    "currency": "ZAR"
                }
                """.formatted(clientId);

        mockMvc.perform(
                        post("/deals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    @Test
    void shouldRejectChangeStatusWhenStatusIsMissing() throws Exception {

        String request = """
                {
                }
                """;

        mockMvc.perform(
                        patch("/deals/{dealId}/status", dealId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    // -------------------------------------------------------------------------
    // INVALID UUID
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectInvalidDealId() throws Exception {

        mockMvc.perform(
                        get("/deals/not-a-uuid")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dealService);
    }

    // -------------------------------------------------------------------------
    // TEST HELPER
    // -------------------------------------------------------------------------

    private Deal toDeal(DealResponse response) {

        Deal deal = mock(Deal.class);

        com.highlands.highlandscrmbackend.company.Company company =
                mock(com.highlands.highlandscrmbackend.company.Company.class);

        com.highlands.highlandscrmbackend.client.Client client =
                mock(com.highlands.highlandscrmbackend.client.Client.class);

        com.highlands.highlandscrmbackend.user.User user =
                mock(com.highlands.highlandscrmbackend.user.User.class);

        when(company.getId())
                .thenReturn(response.companyId());

        when(client.getId())
                .thenReturn(response.clientId());

        when(user.getId())
                .thenReturn(response.ownerUserId());

        when(deal.getId())
                .thenReturn(response.id());

        when(deal.getCompany())
                .thenReturn(company);

        when(deal.getClient())
                .thenReturn(client);

        when(deal.getOwner())
                .thenReturn(user);

        when(deal.getDealNumber())
                .thenReturn(response.dealNumber());

        when(deal.getType())
                .thenReturn(response.type());

        when(deal.getStatus())
                .thenReturn(response.status());

        when(deal.getCommodity())
                .thenReturn(response.commodity());

        when(deal.getGrade())
                .thenReturn(response.grade());

        when(deal.getQuantity())
                .thenReturn(response.quantity());

        when(deal.getUnit())
                .thenReturn(response.unit());

        when(deal.getUnitPrice())
                .thenReturn(response.unitPrice());

        when(deal.getCurrency())
                .thenReturn(response.currency());

        when(deal.getTotalValue())
                .thenReturn(response.totalValue());

        when(deal.getExpectedCloseDate())
                .thenReturn(response.expectedCloseDate());

        when(deal.getNotes())
                .thenReturn(response.notes());

        when(deal.getCreatedAt())
                .thenReturn(response.createdAt());

        when(deal.getUpdatedAt())
                .thenReturn(response.updatedAt());

        return deal;
    }
}