package com.ecowatt.demo.integration.config;

import com.ecowatt.demo.model.Configuracao;
import com.ecowatt.demo.model.Consumo;
import com.ecowatt.demo.model.Equipamento;
import com.ecowatt.demo.model.EquipamentoUsuario;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConfiguracaoRepository;
import com.ecowatt.demo.repository.ConsumoRepository;
import com.ecowatt.demo.repository.EquipamentoRepository;
import com.ecowatt.demo.repository.EquipamentoUsuarioRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import com.ecowatt.demo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cada usuario so acessa os proprios dados; o ADMIN acessa todos
 * (OWASP API1:2023, Broken Object Level Authorization).
 *
 * Cenario: Ana e Bruno sao USER; Bruno tem consumo, configuracao e um
 * equipamento cadastrados. Ana tenta ler, alterar, apagar e criar dados no
 * nome do Bruno trocando o ID na URL ou no corpo da requisicao.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ControleAcessoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConsumoRepository consumoRepository;
    @Autowired private ConfiguracaoRepository configuracaoRepository;
    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private EquipamentoUsuarioRepository equipamentoUsuarioRepository;

    private Usuario ana;
    private Usuario bruno;
    private Usuario admin;
    private Consumo consumoDoBruno;
    private Configuracao configDoBruno;
    private EquipamentoUsuario equipamentoDoBruno;

    @BeforeEach
    void cenario() {
        ana = usuarioRepository.save(new Usuario(null, "Ana", "hash-ana", "ana@ecowatt.test", LocalDate.now(), "USER"));
        bruno = usuarioRepository.save(new Usuario(null, "Bruno", "hash-bruno", "bruno@ecowatt.test", LocalDate.now(), "USER"));
        admin = usuarioRepository.save(new Usuario(null, "Admin", "hash-admin", "admin-teste@ecowatt.test", LocalDate.now(), "ADMIN"));

        consumoDoBruno = consumoRepository.save(new Consumo(null, bruno, LocalDateTime.now(), 150.0));
        configDoBruno = configuracaoRepository.save(
                new Configuracao(null, bruno, new BigDecimal("0.76"), 300.0, null, "kWh"));

        Equipamento geladeira = equipamentoRepository.save(new Equipamento(null, "Geladeira", "Frost Free", 0.1));
        EquipamentoUsuario vinculo = new EquipamentoUsuario();
        vinculo.setUsuario(bruno);
        vinculo.setEquipamento(geladeira);
        vinculo.setNomeIdentificacao("Geladeira da cozinha");
        vinculo.setHorasPorDia(24.0);
        vinculo.setConsumoEsperado(2.4);
        equipamentoDoBruno = equipamentoUsuarioRepository.save(vinculo);
    }

    private ResultActions como(Usuario usuario, MockHttpServletRequestBuilder requisicao) throws Exception {
        return mockMvc.perform(requisicao
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(usuario))
                .contentType(MediaType.APPLICATION_JSON));
    }

    // ----- Leitura -----

    @Test
    @DisplayName("usuario le os proprios dados")
    void leProprosDados() throws Exception {
        como(bruno, get("/consumo/usuario/" + bruno.getId())).andExpect(status().isOk());
        como(bruno, get("/configuracao/usuario/" + bruno.getId())).andExpect(status().isOk());
        como(bruno, get("/equipamentousuario/listar/" + bruno.getId())).andExpect(status().isOk());
        como(bruno, get("/previsao/usuario/" + bruno.getId())).andExpect(status().isOk());
        como(bruno, get("/usuario/buscar/" + bruno.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("usuario nao le os dados de outro usuario trocando o ID na URL")
    void naoLeDadosDeOutro() throws Exception {
        como(ana, get("/consumo/usuario/" + bruno.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").exists());
        como(ana, get("/consumo/" + consumoDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/configuracao/usuario/" + bruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/configuracao/" + configDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/equipamentousuario/listar/" + bruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/equipamentousuario/" + equipamentoDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/previsao/usuario/" + bruno.getId())).andExpect(status().isForbidden());
        como(ana, get("/usuario/buscar/" + bruno.getId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("listas com dados de todos os usuarios sao exclusivas do ADMIN")
    void listasGeraisSoParaAdmin() throws Exception {
        como(ana, get("/usuario/buscar")).andExpect(status().isForbidden());
        como(ana, get("/consumo/listar")).andExpect(status().isForbidden());
        como(ana, get("/configuracao/listar")).andExpect(status().isForbidden());

        como(admin, get("/usuario/buscar")).andExpect(status().isOk());
        como(admin, get("/consumo/listar")).andExpect(status().isOk());
        como(admin, get("/configuracao/listar")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN le os dados de qualquer usuario")
    void adminLeDadosDeQualquerUm() throws Exception {
        como(admin, get("/consumo/usuario/" + bruno.getId())).andExpect(status().isOk());
        como(admin, get("/consumo/" + consumoDoBruno.getId())).andExpect(status().isOk());
        como(admin, get("/configuracao/usuario/" + bruno.getId())).andExpect(status().isOk());
        como(admin, get("/equipamentousuario/" + equipamentoDoBruno.getId())).andExpect(status().isOk());
    }

    // ----- Alteracao e exclusao -----

    @Test
    @DisplayName("usuario nao troca a senha de outro usuario")
    void naoTrocaSenhaDeOutro() throws Exception {
        como(ana, put("/usuario/alterar/" + bruno.getId())
                .content("{\"nome\":\"Invadido\",\"senha\":\"novasenha123\"}"))
                .andExpect(status().isForbidden());

        Usuario depois = usuarioRepository.findById(bruno.getId()).orElseThrow();
        assertThat(depois.getSenha()).isEqualTo("hash-bruno");
        assertThat(depois.getNome()).isEqualTo("Bruno");
    }

    @Test
    @DisplayName("usuario nao altera nem apaga dados de outro usuario")
    void naoAlteraNemApagaDadosDeOutro() throws Exception {
        como(ana, put("/consumo/" + consumoDoBruno.getId()).content("{\"consumoKwh\":1}"))
                .andExpect(status().isForbidden());
        como(ana, delete("/consumo/" + consumoDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, put("/configuracao/alterar/" + configDoBruno.getId())
                .content("{\"valorTarifa\":9.99,\"meta\":1,\"unidadeMedida\":\"kWh\"}"))
                .andExpect(status().isForbidden());
        como(ana, delete("/configuracao/" + configDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, put("/equipamentousuario/" + equipamentoDoBruno.getId()).content("{\"horasPorDia\":1}"))
                .andExpect(status().isForbidden());
        como(ana, delete("/equipamentousuario/" + equipamentoDoBruno.getId())).andExpect(status().isForbidden());
        como(ana, delete("/usuario/excluir/" + bruno.getId())).andExpect(status().isForbidden());

        assertThat(consumoRepository.findById(consumoDoBruno.getId()).orElseThrow().getConsumoKwh()).isEqualTo(150.0);
        assertThat(configuracaoRepository.findById(configDoBruno.getId()).orElseThrow().getValorTarifa())
                .isEqualByComparingTo("0.76");
        assertThat(equipamentoUsuarioRepository.existsById(equipamentoDoBruno.getId())).isTrue();
        assertThat(usuarioRepository.existsById(bruno.getId())).isTrue();
    }

    @Test
    @DisplayName("usuario altera e apaga os proprios dados")
    void alteraEApagaProprosDados() throws Exception {
        como(bruno, put("/consumo/" + consumoDoBruno.getId()).content("{\"consumoKwh\":180}"))
                .andExpect(status().isOk());
        assertThat(consumoRepository.findById(consumoDoBruno.getId()).orElseThrow().getConsumoKwh()).isEqualTo(180.0);

        como(bruno, delete("/consumo/" + consumoDoBruno.getId())).andExpect(status().isOk());
        assertThat(consumoRepository.existsById(consumoDoBruno.getId())).isFalse();
    }

    // ----- Criacao em nome de outro usuario -----

    @Test
    @DisplayName("usuario nao cria registros no nome de outro usuario")
    void naoCriaRegistrosNoNomeDeOutro() throws Exception {
        long consumosAntes = consumoRepository.count();

        como(ana, post("/consumo/add").content("{\"usuarioId\":" + bruno.getId() + ",\"consumoKwh\":999}"))
                .andExpect(status().isForbidden());
        como(ana, post("/configuracao/add").content(
                "{\"usuarioId\":" + bruno.getId() + ",\"valorTarifa\":1,\"meta\":1,\"unidadeMedida\":\"kWh\"}"))
                .andExpect(status().isForbidden());
        como(ana, post("/equipamentousuario/add").content(
                "{\"usuarioId\":" + bruno.getId() + ",\"equipamentoId\":" + equipamentoDoBruno.getEquipamento().getId()
                        + ",\"nomeIdentificacao\":\"x\",\"horasPorDia\":1}"))
                .andExpect(status().isForbidden());

        assertThat(consumoRepository.count()).isEqualTo(consumosAntes);
    }

    @Test
    @DisplayName("usuario cria registros no proprio nome")
    void criaRegistrosNoProprioNome() throws Exception {
        como(ana, post("/consumo/add").content("{\"usuarioId\":" + ana.getId() + ",\"consumoKwh\":120}"))
                .andExpect(status().isCreated());
    }

    // ----- Casos de borda -----

    @Test
    @DisplayName("registro inexistente continua respondendo 404")
    void registroInexistenteResponde404() throws Exception {
        como(ana, get("/consumo/999999")).andExpect(status().isNotFound());
        como(ana, get("/equipamentousuario/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("catalogo de equipamentos continua aberto a qualquer usuario logado")
    void catalogoContinuaAberto() throws Exception {
        como(ana, get("/equipamentos/listar")).andExpect(status().isOk());
        como(ana, post("/equipamentos/add").content("{\"nome\":\"Ventilador\",\"modelo\":\"Teto\",\"consumoPorHora\":0.08}"))
                .andExpect(status().isCreated());
    }
}
