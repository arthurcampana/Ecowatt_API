package com.ecowatt.demo.config;

import com.ecowatt.demo.model.Usuario;
import com.ecowatt.demo.repository.ConfiguracaoRepository;
import com.ecowatt.demo.repository.ConsumoRepository;
import com.ecowatt.demo.repository.EquipamentoUsuarioRepository;
import com.ecowatt.demo.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Controle de acesso por dono do dado (OWASP API1:2023, Broken Object Level
 * Authorization). Usado nos controllers via @PreAuthorize, por exemplo:
 *
 *   @PreAuthorize("@acesso.podeAcessarUsuario(#usuarioId)")
 *
 * Regra: um USER so acessa os proprios dados; um ADMIN acessa todos.
 * Sem essa verificacao, qualquer usuario logado lia, alterava ou apagava os
 * dados de outro apenas trocando o ID na URL.
 *
 * Quando o registro pedido nao existe, a verificacao libera e o controller
 * responde 404 normalmente.
 */
@Component("acesso")
public class ControleAcesso {

    private final UsuarioRepository usuarioRepository;
    private final ConsumoRepository consumoRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final EquipamentoUsuarioRepository equipamentoUsuarioRepository;

    public ControleAcesso(
            UsuarioRepository usuarioRepository,
            ConsumoRepository consumoRepository,
            ConfiguracaoRepository configuracaoRepository,
            EquipamentoUsuarioRepository equipamentoUsuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.consumoRepository = consumoRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.equipamentoUsuarioRepository = equipamentoUsuarioRepository;
    }

    /** O usuario logado e ADMIN? */
    public boolean ehAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /** O usuario logado pode acessar os dados do usuario {@code usuarioId}? */
    public boolean podeAcessarUsuario(Long usuarioId) {
        if (ehAdmin()) {
            return true;
        }
        Long logado = idDoUsuarioLogado();
        return logado != null && logado.equals(usuarioId);
    }

    /** O usuario logado pode acessar o consumo {@code consumoId}? */
    public boolean donoDoConsumo(Long consumoId) {
        return consumoRepository.findById(consumoId)
                .map(consumo -> podeAcessarDonoDe(consumo.getUsuario()))
                .orElse(true);
    }

    /** O usuario logado pode acessar a configuracao {@code configuracaoId}? */
    public boolean donoDaConfiguracao(Long configuracaoId) {
        return configuracaoRepository.findById(configuracaoId)
                .map(config -> podeAcessarDonoDe(config.getCliente()))
                .orElse(true);
    }

    /** O usuario logado pode acessar o equipamento do usuario {@code equipamentoUsuarioId}? */
    public boolean donoDoEquipamentoUsuario(Long equipamentoUsuarioId) {
        return equipamentoUsuarioRepository.findById(equipamentoUsuarioId)
                .map(equipamento -> podeAcessarDonoDe(equipamento.getUsuario()))
                .orElse(true);
    }

    // Registro sem dono (usuario_id nulo no banco) so e acessivel ao ADMIN.
    private boolean podeAcessarDonoDe(Usuario dono) {
        if (dono == null) {
            return ehAdmin();
        }
        return podeAcessarUsuario(dono.getId());
    }

    // O JwtFilter coloca o e-mail do token como principal da autenticacao.
    private Long idDoUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByEmail(auth.getName())
                .map(Usuario::getId)
                .orElse(null);
    }
}
