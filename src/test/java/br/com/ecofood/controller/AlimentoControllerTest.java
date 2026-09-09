package br.com.ecofood.controller;

import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.service.AlimentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlimentoController.class)
class AlimentoControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nome": "Arroz",
              "categoria": "Grãos",
              "quantidade": 5,
              "unidade": "kg",
              "dataValidade": "2026-10-15"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlimentoService service;

    @Test
    void deveCadastrarAlimento() throws Exception {
        when(service.cadastrar(any())).thenReturn(alimento());

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("001"))
                .andExpect(jsonPath("$.nome").value("Arroz"));
    }

    @Test
    void deveRejeitarCadastroComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                  "nome": " ",
                  "categoria": "",
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
                .andExpect(jsonPath("$.campos.quantidade").exists())
                .andExpect(jsonPath("$.campos.dataValidade").exists());
    }

    @Test
    void deveRejeitarDataEmFormatoInvalido() throws Exception {
        String jsonComDataInvalida = JSON_VALIDO.replace("2026-10-15", "15/10/2026");

        mockMvc.perform(post("/alimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComDataInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido"));
    }

    @Test
    void deveListarAlimentos() throws Exception {
        when(service.listar()).thenReturn(List.of(alimento()));

        mockMvc.perform(get("/alimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Arroz"));
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
        when(service.atualizar(org.mockito.ArgumentMatchers.eq("001"), any()))
                .thenReturn(alimento());

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
        org.mockito.Mockito.doThrow(new AlimentoNaoEncontradoException("999"))
                .when(service).excluir("999");

        mockMvc.perform(delete("/alimentos/999"))
                .andExpect(status().isNotFound());
    }

    private Alimento alimento() {
        return new Alimento(
                "001",
                "Arroz",
                "Grãos",
                new BigDecimal("5"),
                "kg",
                LocalDate.of(2026, 10, 15)
        );
    }
}

