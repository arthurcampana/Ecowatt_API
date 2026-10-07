import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { formatarDataHora } from "../utils/formato.js";

// Tela de registro de consumo: lista os registros (mais recentes primeiro),
// permite criar/editar/excluir e filtrar por mes/ano sobre a lista carregada.
export default function Consumo() {
  const { usuario } = useAuth();

  const [todosConsumos, setTodosConsumos] = useState([]);
  const [erroLista, setErroLista] = useState(null);

  const [formAberto, setFormAberto] = useState(false);
  const [consumoId, setConsumoId] = useState("");
  const [consumoKwh, setConsumoKwh] = useState("");
  const [dataRegistro, setDataRegistro] = useState("");

  const [filtroMes, setFiltroMes] = useState("");
  const [filtroAno, setFiltroAno] = useState("");

  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  async function carregarConsumos() {
    try {
      const dados = await consumoService.listarPorUsuario(usuario.id);
      setTodosConsumos(dados || []);
      setErroLista(null);
    } catch (error) {
      setErroLista(error?.message || "Erro ao carregar consumos");
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
      } else {
        await consumoService.adicionar(payload);
      }
      fecharFormulario();
      carregarConsumos();
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao salvar consumo",
        tipo: "erro",
      });
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
    if (!window.confirm("Deseja excluir esse consumo?")) {
      return;
    }

    try {
      await consumoService.remover(id);
      carregarConsumos();
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao excluir consumo",
        tipo: "erro",
      });
    }
  }

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
            <input
              id="filtroAno"
              type="number"
              className="form-control"
              placeholder="Ex: 2026"
              value={filtroAno}
              onChange={(e) => setFiltroAno(e.target.value)}
            />
          </div>
        </div>
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

      <div className="lista-consumos">
        {erroLista ? (
          <div className="consumo-card">{erroLista}</div>
        ) : ordenados.length === 0 ? (
          <div className="consumo-card">Nenhum consumo encontrado.</div>
        ) : (
          ordenados.map((item) => (
            <div className="consumo-card" key={item.id}>
              <div className="consumo-info">
                <h5>{Number(item.consumoKwh).toFixed(2)} kWh</h5>
                <p>{formatarDataHora(item.dataRegistro)}</p>
                <div className="badge-consumo">Registro #{item.id}</div>
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
          ))
        )}
      </div>
    </div>
  );
}
