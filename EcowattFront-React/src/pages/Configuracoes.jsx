import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { configuracaoService } from "../api/configuracaoService.js";

// Tela de configuracoes de consumo: carrega a configuracao do usuario (um 404
// significa "ainda nao existe", nao erro) e permite criar ou atualizar.
export default function Configuracoes() {
  const { usuario } = useAuth();

  const [configuracaoAtual, setConfiguracaoAtual] = useState(null);
  const [meta, setMeta] = useState("");
  const [valorTarifa, setValorTarifa] = useState("");
  const [unidadeMedida, setUnidadeMedida] = useState("kWh");
  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  function preencherTela(config) {
    setConfiguracaoAtual(config);
    setMeta(config.meta ?? "");
    setValorTarifa(config.valorTarifa ?? "");
    setUnidadeMedida(config.unidadeMedida ?? "kWh");
  }

  useEffect(() => {
    let ativo = true;

    async function carregarConfiguracao() {
      try {
        const config = await configuracaoService.buscarPorUsuario(usuario.id);
        if (ativo) {
          preencherTela(config);
        }
      } catch (error) {
        if (!ativo) {
          return;
        }
        // 404 = usuario ainda nao tem configuracao; nao e um erro.
        if (error?.status === 404) {
          setConfiguracaoAtual(null);
          return;
        }
        setMensagem({
          texto: error?.message || "Erro ao carregar configuração",
          tipo: "erro",
        });
      }
    }

    carregarConfiguracao();

    return () => {
      ativo = false;
    };
  }, [usuario.id]);

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    const payload = {
      valorTarifa: Number(valorTarifa),
      meta: Number(meta),
      unidadeMedida,
    };

    try {
      let resposta;
      if (configuracaoAtual) {
        resposta = await configuracaoService.alterar(
          configuracaoAtual.id,
          payload
        );
      } else {
        resposta = await configuracaoService.adicionar({
          ...payload,
          usuarioId: usuario.id,
        });
      }

      preencherTela(resposta);
      setMensagem({
        texto: "Configuração salva com sucesso",
        tipo: "sucesso",
      });
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao salvar configuração",
        tipo: "erro",
      });
    }
  }

  const metaAtual = configuracaoAtual ? configuracaoAtual.meta : "--";
  const tarifaAtual = configuracaoAtual
    ? "R$ " + Number(configuracaoAtual.valorTarifa).toFixed(2)
    : "--";
  const unidadeAtual = configuracaoAtual
    ? configuracaoAtual.unidadeMedida
    : "--";

  return (
    <div>
      <div className="page-header">
        <h2>Configurações</h2>
        <p>Gerencie os parâmetros de consumo utilizados pelo sistema.</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className="config-layout">
        <div className="config-principal">
          <div className="config-card">
            <h5>Configuração de Consumo</h5>

            <form onSubmit={handleSubmit}>
              <div className="campo">
                <label className="form-label" htmlFor="meta">
                  Meta de Consumo
                </label>
                <input
                  id="meta"
                  type="number"
                  step="0.01"
                  className="form-control"
                  value={meta}
                  onChange={(e) => setMeta(e.target.value)}
                  required
                />
              </div>

              <div className="campo">
                <label className="form-label" htmlFor="valorTarifa">
                  Valor da Tarifa
                </label>
                <input
                  id="valorTarifa"
                  type="number"
                  step="0.01"
                  className="form-control"
                  value={valorTarifa}
                  onChange={(e) => setValorTarifa(e.target.value)}
                  required
                />
              </div>

              <div className="campo">
                <label className="form-label" htmlFor="unidadeMedida">
                  Unidade de Medida
                </label>
                <select
                  id="unidadeMedida"
                  className="form-control"
                  value={unidadeMedida}
                  onChange={(e) => setUnidadeMedida(e.target.value)}
                >
                  <option value="kWh">kWh</option>
                  <option value="MWh">MWh</option>
                </select>
              </div>

              <button type="submit" className="btn-save">
                Salvar Configuração
              </button>
            </form>
          </div>
        </div>

        <div className="config-lateral">
          <div className="config-card">
            <h6>Meta Atual</h6>
            <h2>{metaAtual}</h2>
          </div>

          <div className="config-card">
            <h6>Tarifa Atual</h6>
            <h2>{tarifaAtual}</h2>
          </div>

          <div className="config-card">
            <h6>Unidade</h6>
            <h2>{unidadeAtual}</h2>
          </div>
        </div>
      </div>
    </div>
  );
}
