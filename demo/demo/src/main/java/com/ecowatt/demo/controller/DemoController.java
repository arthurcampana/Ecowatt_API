package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.*;
import com.ecowatt.demo.repository.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final UsuarioRepository usuarioRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final EquipamentoUsuarioRepository equipamentoUsuarioRepository;
    private final ConsumoRepository consumoRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final EventoUsoRepository eventoUsoRepository;

    public DemoController(
            UsuarioRepository usuarioRepository,
            EquipamentoRepository equipamentoRepository,
            EquipamentoUsuarioRepository equipamentoUsuarioRepository,
            ConsumoRepository consumoRepository,
            ConfiguracaoRepository configuracaoRepository,
            EventoUsoRepository eventoUsoRepository
    ) {

        this.usuarioRepository = usuarioRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.equipamentoUsuarioRepository = equipamentoUsuarioRepository;
        this.consumoRepository = consumoRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.eventoUsoRepository = eventoUsoRepository;

    }

    @PostMapping("/popular/{usuarioId}")
    public ResponseEntity<String> popularBanco(
            @PathVariable Long usuarioId
    ){

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        criarEquipamentosPadrao();

        criarEquipamentosDoUsuario(usuario);

        criarConfiguracao(usuario);

        gerarHistoricoConsumo(usuario);
        gerarHistoricoConsumo(usuario);

        gerarEventosDeUso(usuario);

        gerarEventosDeClique(usuario);

        return ResponseEntity.ok(
                "Dados de demonstração criados com sucesso!"
        );

    }

    private Equipamento buscarOuCriarEquipamento(
            String nome,
            String modelo,
            double consumoPorHora
    ) {

        return equipamentoRepository
                .findByNome(nome)
                .orElseGet(() -> {

                    Equipamento equipamento = new Equipamento();

                    equipamento.setNome(nome);
                    equipamento.setModelo(modelo);
                    equipamento.setConsumoPorHora(consumoPorHora);

                    return equipamentoRepository.save(equipamento);

                });

    }
    private void criarEquipamentosPadrao() {

        buscarOuCriarEquipamento(
                "Geladeira",
                "Brastemp Frost Free",
                0.15
        );

        buscarOuCriarEquipamento(
                "Televisão",
                "Samsung 50\"",
                0.12
        );

        buscarOuCriarEquipamento(
                "Notebook",
                "Dell Inspiron",
                0.06
        );

        buscarOuCriarEquipamento(
                "Ar-condicionado",
                "LG Dual Inverter 12000 BTU",
                1.20
        );

        buscarOuCriarEquipamento(
                "Chuveiro Elétrico",
                "Lorenzetti Advanced",
                5.50
        );

        buscarOuCriarEquipamento(
                "Micro-ondas",
                "Electrolux 31L",
                1.30
        );

        buscarOuCriarEquipamento(
                "Máquina de Lavar",
                "Brastemp 12kg",
                0.50
        );

        buscarOuCriarEquipamento(
                "Iluminação",
                "LED Residencial",
                0.08
        );

    }

    private void adicionarEquipamentoUsuario(
            Usuario usuario,
            String nomeEquipamento,
            String nomeIdentificacao,
            double horasPorDia
    ) {

        Equipamento equipamento = equipamentoRepository
                .findByNome(nomeEquipamento)
                .orElseThrow();

        boolean existe = equipamentoUsuarioRepository
                .findByUsuarioIdAndEquipamentoId(
                        usuario.getId(),
                        equipamento.getId()
                )
                .isPresent();

        if(existe){
            return;
        }

        EquipamentoUsuario eu = new EquipamentoUsuario();

        eu.setUsuario(usuario);
        eu.setEquipamento(equipamento);

        eu.setNomeIdentificacao(nomeIdentificacao);

        eu.setHorasPorDia(horasPorDia);

        eu.setConsumoEsperado(
                horasPorDia *
                        equipamento.getConsumoPorHora()
        );

        equipamentoUsuarioRepository.save(eu);

    }
    private void criarEquipamentosDoUsuario(
            Usuario usuario
    ){

        adicionarEquipamentoUsuario(
                usuario,
                "Geladeira",
                "Geladeira da cozinha",
                24
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Televisão",
                "TV Sala",
                5
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Notebook",
                "Notebook Dell",
                7
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Ar-condicionado",
                "Ar-condicionado",
                3
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Chuveiro Elétrico",
                "Chuveiro Elétrico",
                0.5
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Micro-ondas",
                "Micro-ondas",
                0.3
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Máquina de Lavar",
                "Máquina de Lavar",
                0.6
        );

        adicionarEquipamentoUsuario(
                usuario,
                "Iluminação",
                "Iluminação",
                6
        );

    }

    private void criarConfiguracao(Usuario usuario){

        if(configuracaoRepository.findByUsuarioId(usuario.getId()).isPresent()){
            return;
        }

        Configuracao configuracao = new Configuracao();

        configuracao.setCliente(usuario);

        configuracao.setMeta(400.0);

        configuracao.setValorTarifa(
                BigDecimal.valueOf(0.98)
        );

        configuracao.setUnidadeMedida("kWh");


        configuracaoRepository.save(configuracao);

    }
    private void gerarHistoricoConsumo(Usuario usuario){

        if(!consumoRepository
                .findByUsuarioId(usuario.getId())
                .isEmpty()){

            return;

        }

        double[] consumos = {

                352,
                339,
                305,
                288,
                279,
                291,
                307,
                301,
                276,
                269,
                281,
                319

        };

        for(int i=11;i>=0;i--){

            Consumo consumo = new Consumo();

            consumo.setUsuario(usuario);

            consumo.setDataRegistro(

                    LocalDateTime.now()

                            .minusMonths(i)

                            .withDayOfMonth(1)

                            .withHour(12)

            );

            consumo.setConsumoKwh(

                    consumos[11-i]

            );

            consumoRepository.save(consumo);

        }

    }

    /**
     * Gera eventos de uso fictícios (tabela evento_uso) espalhados pelos últimos
     * 30 dias, em horários variados, para que o mapa de calor e o ranking de
     * analytics fiquem preenchidos com dados que saem de verdade das queries.
     *
     * O padrão simula comportamento real: mais acessos de manhã (7-9h) e à noite
     * (18-22h), pouquíssimos de madrugada, e um pouco mais nos fins de semana.
     */
    private void gerarEventosDeUso(Usuario usuario) {

        // Evita duplicar os eventos se o /demo/popular for chamado mais de uma vez.
        if (eventoUsoRepository.countByTipoEvento("API") > 0) {
            return;
        }

        // Endpoints típicos e seus pesos (quanto maior, mais acessado).
        String[][] endpoints = {
                {"GET",    "/consumo/listar",      "30"},
                {"GET",    "/equipamentos/listar", "22"},
                {"GET",    "/consumo/usuario/1",   "18"},
                {"GET",    "/configuracao/1",      "10"},
                {"POST",   "/consumo/add",          "8"},
                {"PUT",    "/equipamentos/3",       "5"},
                {"POST",   "/equipamento-usuario/add", "4"},
                {"DELETE", "/consumo/8",            "2"}
        };

        // "Sorteio ponderado": repete cada endpoint conforme seu peso.
        java.util.List<String[]> sorteio = new java.util.ArrayList<>();
        for (String[] e : endpoints) {
            int peso = Integer.parseInt(e[2]);
            for (int k = 0; k < peso; k++) {
                sorteio.add(e);
            }
        }

        Random random = new Random();
        java.util.List<EventoUso> eventos = new java.util.ArrayList<>();

        // Varre os últimos 30 dias.
        for (int diasAtras = 29; diasAtras >= 0; diasAtras--) {

            LocalDateTime base = LocalDateTime.now().minusDays(diasAtras);

            boolean fimDeSemana =
                    base.getDayOfWeek().getValue() >= 6; // 6=sábado, 7=domingo

            // Quantidade de acessos no dia (fins de semana um pouco mais movimentados).
            int acessosNoDia = (fimDeSemana ? 12 : 8) + random.nextInt(8);

            for (int a = 0; a < acessosNoDia; a++) {

                int hora = sortearHoraRealista(random);
                int minuto = random.nextInt(60);

                LocalDateTime dataHora = base
                        .withHour(hora)
                        .withMinute(minuto)
                        .withSecond(random.nextInt(60));

                String[] ep = sorteio.get(random.nextInt(sorteio.size()));

                EventoUso evento = new EventoUso(
                        usuario,
                        ep[1],                 // endpoint
                        ep[0],                 // método
                        200,                   // status
                        dataHora
                );

                eventos.add(evento);
            }
        }

        eventoUsoRepository.saveAll(eventos);
    }

    /**
     * Sorteia uma hora do dia favorecendo os picos de manhã (7-9h) e noite (18-22h),
     * com pouquíssima atividade de madrugada.
     */
    private int sortearHoraRealista(Random random) {

        int faixa = random.nextInt(100);

        if (faixa < 35) {
            // 35% -> pico da noite (18h às 22h)
            return 18 + random.nextInt(5);
        } else if (faixa < 60) {
            // 25% -> manhã (7h às 9h)
            return 7 + random.nextInt(3);
        } else if (faixa < 75) {
            // 15% -> horário de almoço (12h às 13h)
            return 12 + random.nextInt(2);
        } else if (faixa < 95) {
            // 20% -> resto do dia comercial (10h às 17h)
            return 10 + random.nextInt(8);
        } else {
            // 5% -> madrugada (0h às 5h)
            return random.nextInt(6);
        }
    }

    /**
     * Gera cliques de interface fictícios (tipo_evento = 'CLIQUE') espalhados
     * pelos últimos 30 dias, para o ranking de cliques e o dashboard ficarem
     * preenchidos. É a camada bronze: alguns rótulos vêm "sujos" de propósito
     * (maiúsculas, espaços, variações) para o tratamento PySpark depois ter o
     * que padronizar na camada prata.
     */
    private void gerarEventosDeClique(Usuario usuario) {

        // Regera os cliques do zero a cada chamada: apaga os antigos para que
        // os rótulos fiquem sempre alinhados com os data-evento atuais do front.
        eventoUsoRepository.deleteByTipoEvento("CLIQUE");

        // Rótulos e páginas típicos, com peso. Os rótulos "menu_*" batem com os
        // data-evento dos itens da Sidebar, para o mapa de calor pintar manchas
        // sobre os botões reais. Alguns rótulos vêm "sujos" de propósito, para
        // o tratamento PySpark ter o que padronizar na camada prata.
        String[][] cliquesPossiveis = {
                // Navegação (menu lateral)
                {"menu_perfil",        "/dashboard",    "22"},
                {"menu_consumo",       "/dashboard",    "20"},
                {"menu_relatorios",    "/dashboard",    "16"},
                {"menu_equipamentos",  "/dashboard",    "12"},
                {"menu_configuracoes", "/dashboard",    "8"},
                {"menu_inicio",        "/dashboard",    "14"},
                {"menu_analytics",     "/dashboard",    "7"},
                {"logout",             "/dashboard",    "10"},
                // Ações dentro das páginas
                {"consumo_adicionar",       "/consumo",       "18"},
                {"consumo_salvar",          "/consumo",       "15"},
                {"consumo_editar",          "/consumo",       "9"},
                {"consumo_excluir",         "/consumo",       "4"},
                {"relatorio_gerar",         "/relatorios",    "13"},
                {"relatorio_exportar_pdf",  "/relatorios",    "6"},
                {"equipamento_adicionar",   "/equipamentos",  "11"},
                {"equipamento_salvar",      "/equipamentos",  "8"},
                {"configuracao_salvar",     "/configuracoes", "5"},
                {"perfil_salvar_dados",     "/perfil",        "6"},
                {"perfil_alterar_senha",    "/perfil",        "3"},
                // Variações "sujas" propositais (para o PySpark padronizar na prata)
                {"Sair",               "/dashboard", "5"},
                {"Configurações ",     "/perfil",    "4"}
        };

        java.util.List<String[]> sorteio = new java.util.ArrayList<>();
        for (String[] c : cliquesPossiveis) {
            int peso = Integer.parseInt(c[2]);
            for (int k = 0; k < peso; k++) {
                sorteio.add(c);
            }
        }

        Random random = new Random();
        java.util.List<EventoUso> eventos = new java.util.ArrayList<>();

        for (int diasAtras = 29; diasAtras >= 0; diasAtras--) {

            LocalDateTime base = LocalDateTime.now().minusDays(diasAtras);

            boolean fimDeSemana = base.getDayOfWeek().getValue() >= 6;

            int cliquesNoDia = (fimDeSemana ? 10 : 6) + random.nextInt(8);

            for (int a = 0; a < cliquesNoDia; a++) {

                int hora = sortearHoraRealista(random);

                LocalDateTime dataHora = base
                        .withHour(hora)
                        .withMinute(random.nextInt(60))
                        .withSecond(random.nextInt(60));

                String[] c = sorteio.get(random.nextInt(sorteio.size()));

                eventos.add(
                        EventoUso.clique(usuario, c[0], c[1], dataHora)
                );
            }
        }

        eventoUsoRepository.saveAll(eventos);
    }
}