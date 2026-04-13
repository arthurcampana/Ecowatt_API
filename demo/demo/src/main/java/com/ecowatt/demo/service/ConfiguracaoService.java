package com.ecowatt.demo.service;

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

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }


    public Configuracao cadastrarConfig(Configuracao config){
        return configuracaoRepository.save(config);
    }

    public List<Configuracao> listarConfig(){
        return configuracaoRepository.findAll();
    }

    public Optional<Configuracao> buscarConfig(Long id){
        return configuracaoRepository.findByUsuarioId(id);
    }

    public Optional<Configuracao> alterarConfig(Long id, Configuracao configAtualizado){
        Optional<Configuracao> configOpt = configuracaoRepository.findById(id);

        if(configOpt.isPresent()){
            Configuracao configuracao = configOpt.get();
            configuracao.setValorTarifa(configAtualizado.getValorTarifa());
            configuracao.setMeta(configAtualizado.getMeta());
            configuracao.setUnidadeMedida(configAtualizado.getUnidadeMedida());
            configuracao.setDataFechamento(configAtualizado.getDataFechamento());

            return Optional.of(configuracaoRepository.save(configAtualizado));
        }

        return Optional.empty();
    }

    public boolean excluirConfig(Long id){
        if(configuracaoRepository.existsById(id)){
            configuracaoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

