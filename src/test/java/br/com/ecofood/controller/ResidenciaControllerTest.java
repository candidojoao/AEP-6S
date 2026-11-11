package br.com.ecofood.controller;

import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.model.TipoLocal;
import br.com.ecofood.service.ResidenciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResidenciaController.class)
class ResidenciaControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nome": "Casa da família Silva",
              "responsavel": "Ana",
              "endereco": {
                "rua": "Rua A",
                "numero": "100",
                "bairro": "Centro",
                "cidade": "Maringá",
                "estado": "PR",
                "cep": "87000-000"
              },
              "locais": [
                { "nome": "Geladeira", "tipo": "REFRIGERADO" },
                { "nome": "Despensa", "tipo": "AMBIENTE" }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResidenciaService service;

    private Residencia residencia() {
        return new Residencia(
                "r1",
                "Casa da família Silva",
                "Ana",
                new Endereco("Rua A", "100", "Centro", "Maringá", "PR", "87000-000"),
                List.of(
                        new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO),
                        new LocalArmazenamento("Despensa", TipoLocal.AMBIENTE)
                )
        );
    }

    @Test
    void deveCadastrarResidenciaComEnderecoELocais() throws Exception {
        when(service.cadastrar(any())).thenReturn(residencia());

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("r1"))
                .andExpect(jsonPath("$.endereco.cidade").value("Maringá"))
                .andExpect(jsonPath("$.locais[0].nome").value("Geladeira"))
                .andExpect(jsonPath("$.locais[1].tipo").value("AMBIENTE"));
    }

    @Test
    void deveRejeitarResidenciaComSubdocumentosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                  "nome": "Casa",
                  "responsavel": "",
                  "endereco": { "rua": "Rua A", "cidade": "", "estado": "Paraná" },
                  "locais": [ { "nome": "", "tipo": "REFRIGERADO" } ]
                }
                """;

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.responsavel").exists())
                .andExpect(jsonPath("$.campos['endereco.cidade']").value("A cidade é obrigatória"))
                .andExpect(jsonPath("$.campos['endereco.estado']").exists())
                .andExpect(jsonPath("$.campos['locais[0].nome']").exists());
    }

    @Test
    void deveExigirPeloMenosUmLocal() throws Exception {
        String semLocais = JSON_VALIDO.replaceAll("(?s)\"locais\": \\[.*?]", "\"locais\": []");

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semLocais))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.locais").value("Informe pelo menos um local de armazenamento"));
    }

    @Test
    void deveRejeitarTipoDeLocalDesconhecido() throws Exception {
        String tipoInvalido = JSON_VALIDO.replace("REFRIGERADO", "GELADO");

        mockMvc.perform(post("/residencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tipoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido"));
    }

    @Test
    void deveListarResidencias() throws Exception {
        when(service.listar()).thenReturn(List.of(residencia()));

        mockMvc.perform(get("/residencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa da família Silva"));
    }

    @Test
    void deveBuscarResidenciaPorId() throws Exception {
        when(service.buscarPorId("r1")).thenReturn(residencia());

        mockMvc.perform(get("/residencias/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavel").value("Ana"));
    }

    @Test
    void deveRetornar404ParaResidenciaInexistente() throws Exception {
        when(service.buscarPorId("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));

        mockMvc.perform(get("/residencias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Residência não encontrada: 999"));
    }

    @Test
    void deveAtualizarResidencia() throws Exception {
        when(service.atualizar(eq("r1"), any())).thenReturn(residencia());

        mockMvc.perform(put("/residencias/r1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"));
    }

    @Test
    void deveExcluirResidencia() throws Exception {
        mockMvc.perform(delete("/residencias/r1"))
                .andExpect(status().isNoContent());

        verify(service).excluir("r1");
    }

    @Test
    void deveRetornar404AoExcluirResidenciaInexistente() throws Exception {
        doThrow(new ResidenciaNaoEncontradaException("999")).when(service).excluir("999");

        mockMvc.perform(delete("/residencias/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAdicionarLocalNaResidencia() throws Exception {
        when(service.adicionarLocal(eq("r1"), any())).thenReturn(residencia());

        mockMvc.perform(post("/residencias/r1/locais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Freezer", "tipo": "CONGELADO" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("r1"));
    }

    @Test
    void deveRetornar422AoAdicionarLocalRepetido() throws Exception {
        when(service.adicionarLocal(eq("r1"), any()))
                .thenThrow(new RegraDeNegocioException("A residência já possui o local: Geladeira"));

        mockMvc.perform(post("/residencias/r1/locais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Geladeira", "tipo": "REFRIGERADO" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("A residência já possui o local: Geladeira"));
    }
}
