package com.ecowatt.demo.service;

import com.ecowatt.demo.dto.*;
import com.ecowatt.demo.model.Configuracao;
import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConfiguracaoRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository, UsuarioRepository usuarioRepository) {
        this.configuracaoRepository = configuracaoRepository;
        this.usuarioRepository = usuarioRepository;
    }


    public ConfiguracaoResponseDTO salvar(ConfiguracaoRequestDTO dto){
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        Configuracao config = new Configuracao();

        config.setCliente(usuario);
        config.setMeta(dto.meta());
        config.setValorTarifa(dto.valorTarifa());
        config.setUnidadeMedida(dto.unidadeMedida());

        config = configuracaoRepository.save(config);

        return new ConfiguracaoResponseDTO(config);
    }

    public List<ConfiguracaoResponseDTO> listarConfig(){

        return configuracaoRepository.findAll()
                .stream()
                .map(ConfiguracaoResponseDTO::new)
                .toList();
    }

    public Optional<ConfiguracaoResponseDTO> buscarConfig(Long id){

        if (id == null) {
            throw new RuntimeException("ID inválido");
        }

        return configuracaoRepository.findById(id)
                .map(ConfiguracaoResponseDTO::new);
    }

    public Optional<ConfiguracaoResponseDTO> alterarConfig(
        Long id,
        ConfiguracaoUpdateDTO dto
    ) {

            return configuracaoRepository.findById(id)
                    .map(config -> {

                        if (dto.unidadeMedida() != null) {
                            config.setUnidadeMedida(dto.unidadeMedida());
                        }

                        if (dto.valorTarifa() != null) {
                            config.setValorTarifa(dto.valorTarifa());
                        }

                        if (dto.meta() != null) {
                            config.setMeta(dto.meta());
                        }



                        configuracaoRepository.save(config);

                        return new ConfiguracaoResponseDTO(config);
                    });
        }


    public boolean excluirConfig(Long id){

        if (!configuracaoRepository.existsById(id)) {
            return false;
        }

        configuracaoRepository.deleteById(id);

        return true;
    }

    public Optional<ConfiguracaoResponseDTO> buscarPorUsuario(Long id){

        return configuracaoRepository
                .findByUsuarioId(id)
                .map(ConfiguracaoResponseDTO::new);

    }
}

