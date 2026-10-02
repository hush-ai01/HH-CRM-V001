package com.highlands.highlandscrmbackend.grade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.commodity.Commodity;
import com.highlands.highlandscrmbackend.grade.dto.GradeResponse;
import com.highlands.highlandscrmbackend.grade.dto.GradeStatusUpdateRequest;
import com.highlands.highlandscrmbackend.grade.dto.UpdateGradeRequest;
import com.highlands.highlandscrmbackend.grade.dto.CreateGradeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GradeControllerTest {

    @Mock
    private GradeService gradeService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID gradeId;
    private UUID commodityId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        GradeController controller = new GradeController(gradeService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        gradeId = UUID.randomUUID();
        commodityId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void shouldCreateGrade() throws Exception {

        CreateGradeRequest request = new CreateGradeRequest(
                commodityId,
                "24K",
                "24K",
                "24 karat gold"
        );

        GradeResponse response = gradeResponse();

        Grade grade = toGrade(response);

        when(gradeService.create(
                commodityId,
                "24K",
                "24K",
                "24 karat gold"
        )).thenReturn(grade);

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/grades/" + gradeId
                ))
                .andExpect(jsonPath("$.id").value(gradeId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.commodityId").value(commodityId.toString()))
                .andExpect(jsonPath("$.commodityName").value("Gold"))
                .andExpect(jsonPath("$.commodityCode").value("GOLD"))
                .andExpect(jsonPath("$.name").value("24K"))
                .andExpect(jsonPath("$.code").value("24K"))
                .andExpect(jsonPath("$.description").value("24 karat gold"))
                .andExpect(jsonPath("$.active").value(true));

        verify(gradeService).create(
                commodityId,
                "24K",
                "24K",
                "24 karat gold"
        );
    }

    @Test
    void shouldGetAllGrades() throws Exception {

        GradeResponse first = gradeResponse();
        GradeResponse second = new GradeResponse(
                UUID.randomUUID(),
                companyId,
                commodityId,
                "Gold",
                "GOLD",
                "22K",
                "22K",
                "22 karat gold",
                true,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(1)
        );

        Grade firstGrade = toGrade(first);
        Grade secondGrade = toGrade(second);

        when(gradeService.findAll())
                .thenReturn(List.of(firstGrade, secondGrade));

        mockMvc.perform(get("/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[0].name").value("24K"))
                .andExpect(jsonPath("$[1].id").value(second.id().toString()))
                .andExpect(jsonPath("$[1].name").value("22K"));

        verify(gradeService).findAll();
        verify(gradeService, never()).findByCommodity(any());
    }

    @Test
    void shouldGetGradesByCommodity() throws Exception {

        GradeResponse response = gradeResponse();
        Grade grade = toGrade(response);

        when(gradeService.findByCommodity(commodityId))
                .thenReturn(List.of(grade));

        mockMvc.perform(get("/grades")
                        .param("commodityId", commodityId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(gradeId.toString()))
                .andExpect(jsonPath("$[0].commodityId").value(commodityId.toString()))
                .andExpect(jsonPath("$[0].name").value("24K"));

        verify(gradeService).findByCommodity(commodityId);
        verify(gradeService, never()).findAll();
    }

    @Test
    void shouldGetGradeById() throws Exception {

        GradeResponse response = gradeResponse();
        Grade grade = toGrade(response);

        when(gradeService.findById(gradeId))
                .thenReturn(grade);

        mockMvc.perform(get("/grades/{gradeId}", gradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gradeId.toString()))
                .andExpect(jsonPath("$.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.commodityId").value(commodityId.toString()))
                .andExpect(jsonPath("$.commodityName").value("Gold"))
                .andExpect(jsonPath("$.commodityCode").value("GOLD"))
                .andExpect(jsonPath("$.name").value("24K"))
                .andExpect(jsonPath("$.code").value("24K"));

        verify(gradeService).findById(gradeId);
    }

    @Test
    void shouldUpdateGrade() throws Exception {

        UpdateGradeRequest request = new UpdateGradeRequest(
                commodityId,
                "22K",
                "22K",
                "22 karat gold"
        );

        GradeResponse response = new GradeResponse(
                gradeId,
                companyId,
                commodityId,
                "Gold",
                "GOLD",
                "22K",
                "22K",
                "22 karat gold",
                true,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now()
        );

        Grade grade = toGrade(response);

        when(gradeService.update(
                gradeId,
                commodityId,
                "22K",
                "22K",
                "22 karat gold"
        )).thenReturn(grade);

        mockMvc.perform(put("/grades/{gradeId}", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gradeId.toString()))
                .andExpect(jsonPath("$.name").value("22K"))
                .andExpect(jsonPath("$.code").value("22K"))
                .andExpect(jsonPath("$.description").value("22 karat gold"));

        verify(gradeService).update(
                gradeId,
                commodityId,
                "22K",
                "22K",
                "22 karat gold"
        );
    }

    @Test
    void shouldChangeGradeStatus() throws Exception {

        GradeStatusUpdateRequest request =
                new GradeStatusUpdateRequest(false);

        GradeResponse response = new GradeResponse(
                gradeId,
                companyId,
                commodityId,
                "Gold",
                "GOLD",
                "24K",
                "24K",
                "24 karat gold",
                false,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now()
        );

        Grade grade = toGrade(response);

        when(gradeService.changeStatus(gradeId, false))
                .thenReturn(grade);

        mockMvc.perform(patch("/grades/{gradeId}/status", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gradeId.toString()))
                .andExpect(jsonPath("$.active").value(false));

        verify(gradeService).changeStatus(gradeId, false);
    }

    @Test
    void shouldDeleteGrade() throws Exception {

        doNothing().when(gradeService).delete(gradeId);

        mockMvc.perform(delete("/grades/{gradeId}", gradeId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(gradeService).delete(gradeId);
    }

    @Test
    void shouldRejectCreateWhenCommodityIdIsMissing() throws Exception {

        String request = """
                {
                    "name": "24K",
                    "code": "24K",
                    "description": "24 karat gold"
                }
                """;

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectCreateWhenNameIsMissing() throws Exception {

        String request = """
                {
                    "commodityId": "%s",
                    "code": "24K",
                    "description": "24 karat gold"
                }
                """.formatted(commodityId);

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectCreateWhenCodeIsMissing() throws Exception {

        String request = """
                {
                    "commodityId": "%s",
                    "name": "24K",
                    "description": "24 karat gold"
                }
                """.formatted(commodityId);

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectUpdateWhenNameIsMissing() throws Exception {

        String request = """
                {
                    "commodityId": "%s",
                    "code": "24K",
                    "description": "24 karat gold"
                }
                """.formatted(commodityId);

        mockMvc.perform(put("/grades/{gradeId}", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectStatusChangeWhenActiveIsMissing() throws Exception {

        String request = """
                {
                }
                """;

        mockMvc.perform(patch("/grades/{gradeId}/status", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectInvalidGradeUuid() throws Exception {

        mockMvc.perform(get("/grades/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    @Test
    void shouldRejectInvalidCommodityUuid() throws Exception {

        mockMvc.perform(get("/grades")
                        .param("commodityId", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gradeService);
    }

    private GradeResponse gradeResponse() {
        return new GradeResponse(
                gradeId,
                companyId,
                commodityId,
                "Gold",
                "GOLD",
                "24K",
                "24K",
                "24 karat gold",
                true,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(1)
        );
    }

    private Grade toGrade(GradeResponse response) {

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(response.companyId());

        Commodity commodity = mock(Commodity.class);
        when(commodity.getId()).thenReturn(response.commodityId());
        when(commodity.getName()).thenReturn(response.commodityName());
        when(commodity.getCode()).thenReturn(response.commodityCode());

        Grade grade = mock(Grade.class);

        when(grade.getId()).thenReturn(response.id());
        when(grade.getCompany()).thenReturn(company);
        when(grade.getCommodity()).thenReturn(commodity);
        when(grade.getName()).thenReturn(response.name());
        when(grade.getCode()).thenReturn(response.code());
        when(grade.getDescription()).thenReturn(response.description());
        when(grade.isActive()).thenReturn(response.active());
        when(grade.getCreatedAt()).thenReturn(response.createdAt());
        when(grade.getUpdatedAt()).thenReturn(response.updatedAt());

        return grade;
    }
}