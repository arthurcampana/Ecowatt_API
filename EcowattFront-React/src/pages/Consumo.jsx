import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useUI } from "../context/UIContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { formatarDataHora } from "../utils/formato.js";
import EmptyState from "../components/EmptyState.jsx";
import Icon from "../components/Icon.jsx";

const MESES_COMPLETOS = [
  "Janeiro",
  "Fevereiro",
  "Março",
  "Abril",
  "Maio",
  "Junho",
  "Julho",
  "Agosto",
  "Setembro",
  "Outubro",
  "Novembro",
  "Dezembro",
];

// Tela de registro de consumo: lista os registros (mais recentes primeiro),
// permite criar/editar/excluir e filtrar por mes/ano sobre a lista carregada.
export default function Consumo() {
  const { usuario } = useAuth();
  const { toast, confirmar } = useUI();

  const [todosConsumos, setTodosConsumos] = useState([]);
  const [erroLista, setErroLista] = useState(null);
  const [carregando, setCarregando] = useState(true);

  const [formAberto, setFormAberto] = useState(false);
  const [consumoId, setConsumoId] = useState("");
  const [consumoKwh, setConsumoKwh] = useState("");
  const [dataRegistro, setDataRegistro] = useState("");

  const [filtroMes, setFiltroMes] = useState("");
  const [filtroAno, setFiltroAno] = useState("");

  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  async function carregarConsumos() {
    setCarregando(true);
    try {
      const dados = await consumoService.listarPorUsuario(usuario.id);
      setTodosConsumos(dados || []);
      setErroLista(null);
    } catch (error) {
      setErroLista(error?.message || "Erro ao carregar consumos");
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarConsumos();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  function abrirFormulario() {
    setFormAberto(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function fecharFormulario() {
    setFormAberto(false);
    setConsumoId("");
    setConsumoKwh("");
    setDataRegistro("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    const payload = {
      usuarioId: usuario.id,
      consumoKwh: Number(consumoKwh),
      dataRegistro: dataRegistro || null,
    };

    try {
      if (consumoId) {
        await consumoService.alterar(consumoId, payload);
        toast("Consumo atualizado com sucesso.", "sucesso");
      } else {
        await consumoService.adicionar(payload);
        toast("Consumo registrado com sucesso.", "sucesso");
      }
      fecharFormulario();
      carregarConsumos();
    } catch (error) {
      toast(error?.message || "Erro ao salvar consumo", "erro");
    }
  }

  function editarConsumo(item) {
    abrirFormulario();
    setConsumoId(item.id);
    setConsumoKwh(item.consumoKwh);
    setDataRegistro(
      item.dataRegistro ? item.dataRegistro.substring(0, 16) : ""
    );
  }

  async function excluirConsumo(id) {
    const ok = await confirmar({
      titulo: "Excluir consumo",
      mensagem: "Tem certeza que deseja excluir este registro de consumo?",
      confirmLabel: "Excluir",
    });
    if (!ok) {
      return;
    }

    try {
      await consumoService.remover(id);
      toast("Consumo excluído.", "sucesso");
      carregarConsumos();
    } catch (error) {
      toast(error?.message || "Erro ao excluir consumo", "erro");
    }
  }

  // Anos distintos presentes nos dados, para o dropdown de filtro.
  const anosDisponiveis = useMemo(() => {
    const set = new Set();
    todosConsumos.forEach((c) => {
      if (c.dataRegistro) {
        set.add(new Date(c.dataRegistro).getFullYear());
      }
    });
    return [...set].sort((a, b) => b - a);
  }, [todosConsumos]);

  // Filtra a lista carregada por mes/ano e exibe sempre do mais recente para
  // o mais antigo.
  const filtrados = todosConsumos
    .filter((item) => {
      if (filtroMes === "") {
        return true;
      }
      return new Date(item.dataRegistro).getMonth() === Number(filtroMes);
    })
    .filter((item) => {
      if (!filtroAno) {
        return true;
      }
      return new Date(item.dataRegistro).getFullYear() === Number(filtroAno);
    });

  const ordenados = [...filtrados].reverse();

  const temFiltro = filtroMes !== "" || filtroAno !== "";

  // Resumo da lista filtrada: quantidade, total e media.
  const resumo = useMemo(() => {
    const qtd = filtrados.length;
    const total = filtrados.reduce((s, c) => s + Number(c.consumoKwh), 0);
    return {
      qtd,
      total,
      media: qtd ? total / qtd : 0,
    };
  }, [filtrados]);

  // Agrupa os registros (ja ordenados do mais recente) por "Mes Ano".
  const grupos = useMemo(() => {
    const mapa = new Map();
    ordenados.forEach((item) => {
      const d = new Date(item.dataRegistro);
      const chave = `${d.getFullYear()}-${d.getMonth()}`;
      if (!mapa.has(chave)) {
        mapa.set(chave, {
          titulo: `${MESES_COMPLETOS[d.getMonth()]} de ${d.getFullYear()}`,
          itens: [],
          total: 0,
        });
      }
      const g = mapa.get(chave);
      g.itens.push(item);
      g.total += Number(item.consumoKwh);
    });
    return [...mapa.values()];
  }, [ordenados]);

  function limparFiltros() {
    setFiltroMes("");
    setFiltroAno("");
  }

  return (
    <div>
      <div className="page-header consumo-header">
        <div>
          <h2>Registrar Consumo</h2>
          <p>Gerencie e acompanhe os registros de consumo energético.</p>
        </div>

        <button type="button" className="btn-add" onClick={abrirFormulario}>
          Adicionar Consumo
        </button>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className="config-card filtro-card">
        <h5>Filtrar Consumos</h5>

        <div className="filtro-grid">
          <div className="campo">
            <label className="form-label" htmlFor="filtroMes">
              Mês
            </label>
            <select
              id="filtroMes"
              className="form-control"
              value={filtroMes}
              onChange={(e) => setFiltroMes(e.target.value)}
            >
              <option value="">Todos</option>
              <option value="0">Janeiro</option>
              <option value="1">Fevereiro</option>
              <option value="2">Março</option>
              <option value="3">Abril</option>
              <option value="4">Maio</option>
              <option value="5">Junho</option>
              <option value="6">Julho</option>
              <option value="7">Agosto</option>
              <option value="8">Setembro</option>
              <option value="9">Outubro</option>
              <option value="10">Novembro</option>
              <option value="11">Dezembro</option>
            </select>
          </div>

          <div className="campo">
            <label className="form-label" htmlFor="filtroAno">
              Ano
            </label>
            <select
              id="filtroAno"
              className="form-control"
              value={filtroAno}
              onChange={(e) => setFiltroAno(e.target.value)}
            >
              <option value="">Todos</option>
              {anosDisponiveis.map((ano) => (
                <option key={ano} value={ano}>
                  {ano}
                </option>
              ))}
            </select>
          </div>
        </div>

        {temFiltro && (
          <button type="button" className="btn-limpar-filtro" onClick={limparFiltros}>
            <Icon name="x" size={14} /> Limpar filtros
          </button>
        )}
      </div>

      <div className={`form-card${formAberto ? " active" : ""}`}>
        <div className="form-card-header">
          <h5>{consumoId ? "Editar Consumo" : "Novo Consumo"}</h5>
          <button
            type="button"
            className="btn-close-custom"
            onClick={fecharFormulario}
          >
            ×
          </button>
        </div>

        <div className="form-card-body">
          <form onSubmit={handleSubmit}>
            <div className="campo">
              <label className="form-label" htmlFor="consumoKwh">
                Consumo (kWh)
              </label>
              <input
                id="consumoKwh"
                type="number"
                step="0.01"
                className="form-control"
                placeholder="Ex.: 42.50"
                value={consumoKwh}
                onChange={(e) => setConsumoKwh(e.target.value)}
                required
              />
              <div className="helper-text">Informe o valor consumido.</div>
            </div>

            <div className="campo">
              <label className="form-label" htmlFor="dataRegistro">
                Data
              </label>
              <input
                id="dataRegistro"
                type="datetime-local"
                className="form-control"
                value={dataRegistro}
                onChange={(e) => setDataRegistro(e.target.value)}
              />
            </div>

            <button type="submit" className="btn-save">
              Salvar Consumo
            </button>
          </form>
        </div>
      </div>

      <div className="historico-title">
        <h4>Histórico de Consumos</h4>
      </div>

      {/* Resumo da lista filtrada */}
      {!carregando && !erroLista && ordenados.length > 0 && (
        <div className="resumo-barra">
          <div className="resumo-item">
            <span>Registros</span>
            <strong>{resumo.qtd}</strong>
          </div>
          <div className="resumo-item">
            <span>Total {temFiltro ? "filtrado" : ""}</span>
            <strong>{resumo.total.toFixed(2)} kWh</strong>
          </div>
          <div className="resumo-item">
            <span>Média</span>
            <strong>{resumo.media.toFixed(2)} kWh</strong>
          </div>
        </div>
      )}

      <div className="lista-consumos">
        {carregando ? (
          <>
            <div className="lista-skel" />
            <div className="lista-skel" />
            <div className="lista-skel" />
          </>
        ) : erroLista ? (
          <div className="consumo-card">{erroLista}</div>
        ) : ordenados.length === 0 && temFiltro ? (
          <EmptyState
            icon="inbox"
            titulo="Nenhum resultado para esse filtro"
            descricao="Não há consumos no período selecionado. Ajuste ou limpe os filtros."
            acaoLabel="Limpar filtros"
            onAcao={limparFiltros}
          />
        ) : ordenados.length === 0 ? (
          <EmptyState
            icon="bolt"
            titulo="Nenhum consumo registrado"
            descricao="Comece registrando seu primeiro consumo de energia para acompanhar sua evolução."
            acaoLabel="Adicionar consumo"
            onAcao={abrirFormulario}
          />
        ) : (
          grupos.map((grupo) => (
            <div className="consumo-grupo" key={grupo.titulo}>
              <div className="consumo-grupo-head">
                <h5>{grupo.titulo}</h5>
                <span>{grupo.total.toFixed(2)} kWh</span>
              </div>

              {grupo.itens.map((item) => (
                <div className="consumo-card" key={item.id}>
                  <div className="consumo-info">
                    <h5>{Number(item.consumoKwh).toFixed(2)} kWh</h5>
                    <p>{formatarDataHora(item.dataRegistro)}</p>
                  </div>

                  <div className="consumo-actions">
                    <button
                      type="button"
                      className="btn-action btn-edit"
                      onClick={() => editarConsumo(item)}
                    >
                      Editar
                    </button>
                    <button
                      type="button"
                      className="btn-action btn-delete"
                      onClick={() => excluirConsumo(item.id)}
                    >
                      Excluir
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
