import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useUI } from "../context/UIContext.jsx";
import { configuracaoService } from "../api/configuracaoService.js";
import Icon from "../components/Icon.jsx";

// Tela de configuracoes de consumo: carrega a configuracao do usuario (um 404
// significa "ainda nao existe", nao erro) e permite criar ou atualizar.
export default function Configuracoes() {
  const { usuario } = useAuth();
  const { toast } = useUI();

  const [configuracaoAtual, setConfiguracaoAtual] = useState(null);
  const [meta, setMeta] = useState("");
  const [valorTarifa, setValorTarifa] = useState("");
  const [unidadeMedida, setUnidadeMedida] = useState("kWh");
  const [salvando, setSalvando] = useState(false);

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
        toast(error?.message || "Erro ao carregar configuração", "erro");
      }
    }

    carregarConfiguracao();

    return () => {
      ativo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  async function handleSubmit(event) {
    event.preventDefault();

    if (Number(meta) < 0 || Number(valorTarifa) < 0) {
      toast("Meta e tarifa não podem ser negativas.", "erro");
      return;
    }

    const payload = {
      valorTarifa: Number(valorTarifa),
      meta: Number(meta),
      unidadeMedida,
    };

    try {
      setSalvando(true);
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
      toast("Configuração salva com sucesso.", "sucesso");
    } catch (error) {
      toast(error?.message || "Erro ao salvar configuração", "erro");
    } finally {
      setSalvando(false);
    }
  }

  const temConfig = Boolean(configuracaoAtual);
  const metaAtual = temConfig ? `${configuracaoAtual.meta} kWh` : "Não definida";
  const tarifaAtual = temConfig
    ? "R$ " + Number(configuracaoAtual.valorTarifa).toFixed(2)
    : "Não definida";
  const unidadeAtual = temConfig ? configuracaoAtual.unidadeMedida : "kWh";

  // Preview: custo estimado de atingir exatamente a meta com a tarifa atual.
  const custoNaMeta =
    meta && valorTarifa ? Number(meta) * Number(valorTarifa) : null;

  return (
    <div>
      <div className="page-header">
        <h2>Configurações</h2>
        <p>Gerencie os parâmetros de consumo utilizados pelo sistema.</p>
      </div>

      <div className="config-layout">
        <div className="config-principal">
          <div className="config-card">
            <h5>Configuração de Consumo</h5>

            <form onSubmit={handleSubmit}>
              <div className="campo">
                <label className="form-label" htmlFor="meta">
                  Meta de Consumo
                </label>
                <div className="input-afixo">
                  <input
                    id="meta"
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-control"
                    placeholder="Ex.: 400"
                    value={meta}
                    onChange={(e) => setMeta(e.target.value)}
                    required
                  />
                  <span className="afixo">kWh</span>
                </div>
                <div className="helper-text">
                  Consumo mensal que você quer não ultrapassar.
                </div>
              </div>

              <div className="campo">
                <label className="form-label" htmlFor="valorTarifa">
                  Valor da Tarifa
                </label>
                <div className="input-afixo">
                  <span className="afixo prefixo">R$</span>
                  <input
                    id="valorTarifa"
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-control com-prefixo"
                    placeholder="Ex.: 0.98"
                    value={valorTarifa}
                    onChange={(e) => setValorTarifa(e.target.value)}
                    required
                  />
                </div>
                <div className="helper-text">Preço cobrado por kWh na sua conta.</div>
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

              {custoNaMeta !== null && (
                <div className="config-preview">
                  <Icon name="info" size={18} />
                  <span>
                    Atingindo a meta, o custo estimado seria de{" "}
                    <strong>R$ {custoNaMeta.toFixed(2)}</strong> no mês.
                  </span>
                </div>
              )}

              <button type="submit" className="btn-save" disabled={salvando}>
                {salvando ? "Salvando..." : "Salvar Configuração"}
              </button>
            </form>
          </div>
        </div>

        <div className="config-lateral">
          <div className="config-card config-resumo verde">
            <div className="config-resumo-ico">
              <Icon name="calendar" size={20} />
            </div>
            <div>
              <h6>Meta Atual</h6>
              <h2>{metaAtual}</h2>
            </div>
          </div>

          <div className="config-card config-resumo azul">
            <div className="config-resumo-ico">
              <Icon name="info" size={20} />
            </div>
            <div>
              <h6>Tarifa Atual</h6>
              <h2>{tarifaAtual}</h2>
            </div>
          </div>

          <div className="config-card config-resumo ambar">
            <div className="config-resumo-ico">
              <Icon name="battery" size={20} />
            </div>
            <div>
              <h6>Unidade</h6>
              <h2>{unidadeAtual}</h2>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
