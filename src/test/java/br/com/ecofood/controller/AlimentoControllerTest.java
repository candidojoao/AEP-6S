package br.com.ecofood.controller;

import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.service.AlimentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlimentoController.class)
class AlimentoControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nome": "Leite",
              "residenciaId": "r1",
              "categoriaId": "c1",
              "local": "Geladeira",
              "quantidade": 2,
              "unidade": "L",
              "dataValidade": "2026-11-20"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlimentoService service;

    private Alimento alimento() {
        Alimento alimento = new Alimento(
                "Leite",
                "r1",
                "c1",
                "Geladeira",
                new BigDecimal("2"),
                "L",
                LocalDate.of(2026, 11, 20)
        );
        ReflectionTestUtils.setField(alimento, "id", "001");
        return alimento;
    }

    @Test
    void deveCadastrarAlimento() throws Exception {
        when(service.cadastrar(any())).thenReturn(alimento());

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(content().string(startsWith("{\"id\":\"001\"")))
                .andExpect(jsonPath("$.residenciaId").value("r1"))
                .andExpect(jsonPath("$.categoriaId").value("c1"))
                .andExpect(jsonPath("$.local").value("Geladeira"));
    }

    @Test
    void deveRejeitarCadastroComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                  "nome": " ",
                  "residenciaId": "",
                  "local": "",
                  "quantidade": 0,
                  "unidade": "",
                  "dataValidade": null
                }
                """;

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.residenciaId").value("Informe a residência"))
                .andExpect(jsonPath("$.campos.categoriaId").value("Informe a categoria"))
                .andExpect(jsonPath("$.campos.local").exists())
                .andExpect(jsonPath("$.campos.quantidade").exists())
                .andExpect(jsonPath("$.campos.dataValidade").exists());

        verify(service, never()).cadastrar(any());
    }

    @Test
    void deveRejeitarDataEmFormatoInvalido() throws Exception {
        String jsonComDataInvalida = JSON_VALIDO.replace("2026-11-20", "20/11/2026");

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComDataInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido"));
    }

    @Test
    void deveRetornar422QuandoOLocalNaoExisteNaResidencia() throws Exception {
        when(service.cadastrar(any())).thenThrow(
                new RegraDeNegocioException("A residência Casa da família Silva não possui o local: Varanda"));

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO.replace("Geladeira", "Varanda")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem")
                        .value("A residência Casa da família Silva não possui o local: Varanda"));
    }

    @Test
    void deveListarAlimentos() throws Exception {
        when(service.listar()).thenReturn(List.of(alimento()));

        mockMvc.perform(get("/alimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Leite"));
    }

    @Test
    void deveFiltrarAlimentosPorResidencia() throws Exception {
        when(service.listarPorResidencia("r1")).thenReturn(List.of(alimento()));

        mockMvc.perform(get("/alimentos").param("residenciaId", "r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].residenciaId").value("r1"));

        verify(service, never()).listar();
    }

    @Test
    void deveBuscarAlimentoPorId() throws Exception {
        when(service.buscarPorId("001")).thenReturn(alimento());

        mockMvc.perform(get("/alimentos/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("001"));
    }

    @Test
    void deveRetornar404AoBuscarAlimentoInexistente() throws Exception {
        when(service.buscarPorId("999")).thenThrow(new AlimentoNaoEncontradoException("999"));

        mockMvc.perform(get("/alimentos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Alimento não encontrado: 999"));
    }

    @Test
    void deveAtualizarAlimento() throws Exception {
        when(service.atualizar(eq("001"), any())).thenReturn(alimento());

        mockMvc.perform(put("/alimentos/001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("001"));
    }

    @Test
    void deveExcluirAlimento() throws Exception {
        mockMvc.perform(delete("/alimentos/001"))
                .andExpect(status().isNoContent());

        verify(service).excluir("001");
    }

    @Test
    void deveRetornar404AoExcluirAlimentoInexistente() throws Exception {
        doThrow(new AlimentoNaoEncontradoException("999"))
                .when(service).excluir("999");

        mockMvc.perform(delete("/alimentos/999"))
                .andExpect(status().isNotFound());
    }
}
