package com.ecowatt.demo.controller;

import com.ecowatt.demo.model.*;
import com.ecowatt.demo.repository.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final UsuarioRepository usuarioRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final EquipamentoUsuarioRepository equipamentoUsuarioRepository;
    private final ConsumoRepository consumoRepository;
    private final ConfiguracaoRepository configuracaoRepository;

    public DemoController(
            UsuarioRepository usuarioRepository,
            EquipamentoRepository equipamentoRepository,
            EquipamentoUsuarioRepository equipamentoUsuarioRepository,
            ConsumoRepository consumoRepository,
            ConfiguracaoRepository configuracaoRepository
    ) {

        this.usuarioRepository = usuarioRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.equipamentoUsuarioRepository = equipamentoUsuarioRepository;
        this.consumoRepository = consumoRepository;
        this.configuracaoRepository = configuracaoRepository;

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
}