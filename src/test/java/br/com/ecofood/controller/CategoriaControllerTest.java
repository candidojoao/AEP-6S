package br.com.ecofood.controller;

import br.com.ecofood.exception.CategoriaNaoEncontradaException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nome": "Laticínios",
              "diasAlertaValidade": 3,
              "dicasConservacao": ["Manter refrigerado", "Consumir em até 3 dias após aberto"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    private Categoria categoria() {
        return new Categoria("c1", "Laticínios", 3, List.of("Manter refrigerado"));
    }

    private ResultActions enviarPost(String json) throws Exception {
        return mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void deveCadastrarCategoria() throws Exception {
        when(service.cadastrar(any())).thenReturn(categoria());

        enviarPost(JSON_VALIDO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("c1"))
                .andExpect(jsonPath("$.diasAlertaValidade").value(3))
                .andExpect(jsonPath("$.dicasConservacao[0]").value("Manter refrigerado"));
    }

    @Test
    void deveRejeitarCategoriaInvalida() throws Exception {
        enviarPost("""
                { "nome": "", "dicasConservacao": [" "] }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.diasAlertaValidade").exists())
                .andExpect(jsonPath("$.campos['dicasConservacao[0]']").exists());
    }

    @Test
    void deveRejeitarDiasDeAlertaNegativos() throws Exception {
        enviarPost(JSON_VALIDO.replace("\"diasAlertaValidade\": 3", "\"diasAlertaValidade\": -1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.diasAlertaValidade")
                        .value("Os dias de alerta não podem ser negativos"));
    }

    @Test
    void deveRetornar422ParaNomeRepetido() throws Exception {
        when(service.cadastrar(any()))
                .thenThrow(new RegraDeNegocioException("Já existe uma categoria com o nome: Laticínios"));

        enviarPost(JSON_VALIDO)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Já existe uma categoria com o nome: Laticínios"));
    }

    @Test
    void deveRetornar409QuandoOIndiceUnicoBarrarODuplicado() throws Exception {
        when(service.cadastrar(any())).thenThrow(new DuplicateKeyException("E11000 duplicate key"));

        enviarPost(JSON_VALIDO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveListarCategorias() throws Exception {
        when(service.listar()).thenReturn(List.of(categoria()));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Laticínios"));
    }

    @Test
    void deveBuscarCategoriaPorId() throws Exception {
        when(service.buscarPorId("c1")).thenReturn(categoria());

        mockMvc.perform(get("/categorias/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Laticínios"));
    }

    @Test
    void deveRetornar404ParaCategoriaInexistente() throws Exception {
        when(service.buscarPorId("999")).thenThrow(new CategoriaNaoEncontradaException("999"));

        mockMvc.perform(get("/categorias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Categoria não encontrada: 999"));
    }

    @Test
    void deveAtualizarCategoria() throws Exception {
        when(service.atualizar(eq("c1"), any())).thenReturn(categoria());

        mockMvc.perform(put("/categorias/c1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"));
    }

    @Test
    void deveExcluirCategoria() throws Exception {
        mockMvc.perform(delete("/categorias/c1"))
                .andExpect(status().isNoContent());

        verify(service).excluir("c1");
    }
}
