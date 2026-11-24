import { useEffect } from "react";

// Termos de Uso e Política de Privacidade (LGPD) exibidos em um modal sobre a
// própria tela, sem trocar de página. Fecha no botão, no X, clicando fora ou
// com a tecla Esc.
export default function ModalTermos({ aberto, aoFechar }) {
  // Fecha com Esc e trava o scroll do fundo enquanto o modal está aberto.
  useEffect(() => {
    if (!aberto) return;
    function onKey(e) {
      if (e.key === "Escape") aoFechar();
    }
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [aberto, aoFechar]);

  if (!aberto) return null;

  return (
    <div
      className="modal-overlay"
      onClick={aoFechar}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-termos-titulo"
    >
      <div className="modal-caixa" onClick={(e) => e.stopPropagation()}>
        <div className="modal-cabecalho">
          <h2 id="modal-termos-titulo">Termos de Uso e Privacidade</h2>
          <button
            type="button"
            className="modal-fechar"
            onClick={aoFechar}
            aria-label="Fechar"
          >
            ✕
          </button>
        </div>

        <div className="modal-corpo termos-texto">
          <p className="termos-versao">Versão 1.0 — projeto acadêmico EcoWatt.</p>

          <h3>1. O que o EcoWatt faz</h3>
          <p>
            O EcoWatt é uma aplicação para registrar e acompanhar o consumo de
            energia elétrica. Os valores exibidos são estimativas de apoio à
            decisão e não substituem a medição oficial da sua distribuidora.
          </p>

          <h3>2. Dados que coletamos</h3>
          <p>
            Para funcionar, o sistema coleta e armazena: nome, e-mail e senha
            (guardada de forma criptografada); os consumos, equipamentos e metas
            que você cadastra; e registros de uso da aplicação (páginas
            acessadas e cliques), usados apenas para métricas internas.
          </p>

          <h3>3. Como usamos os dados</h3>
          <p>
            Seus dados são usados somente para operar o EcoWatt: mostrar seus
            indicadores, gerar relatórios e calcular a previsão de consumo. Não
            vendemos nem compartilhamos seus dados com terceiros.
          </p>

          <h3>4. Seus direitos (LGPD)</h3>
          <p>
            Conforme a Lei Geral de Proteção de Dados (Lei nº 13.709/2018), você
            pode acessar, corrigir e excluir seus dados a qualquer momento. A
            exclusão da conta remove seus dados pessoais do sistema.
          </p>

          <h3>5. Consentimento</h3>
          <p>
            Ao criar uma conta e marcar o aceite, você declara que leu e
            concorda com estes termos. O momento e a versão do seu aceite ficam
            registrados, conforme exige a LGPD.
          </p>
        </div>

        <div className="modal-rodape">
          <button type="button" className="auth-btn" onClick={aoFechar}>
            Entendi
          </button>
        </div>
      </div>
    </div>
  );
}
