import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { equipamentoService } from "../api/equipamentoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";

// Tela de equipamentos do usuario: lista os equipamentos vinculados (mais
// recentes primeiro), permite criar/editar/excluir com uma previa ao vivo do
// consumo esperado e um sub-formulario inline (modal) para cadastrar um
// equipamento base novo no catalogo.
export default function Equipamentos() {
  const { usuario } = useAuth();

  const [equipamentosUsuario, setEquipamentosUsuario] = useState([]);
  const [catalogo, setCatalogo] = useState([]);
  const [erroLista, setErroLista] = useState(null);

  const [formAberto, setFormAberto] = useState(false);
  const [equipUserId, setEquipUserId] = useState("");
  const [equipamentoSelecionado, setEquipamentoSelecionado] = useState("");
  const [nomeIdentificacao, setNomeIdentificacao] = useState("");
  const [horasPorDia, setHorasPorDia] = useState("");

  const [modalAberto, setModalAberto] = useState(false);
  const [novoNome, setNovoNome] = useState("");
  const [novoModelo, setNovoModelo] = useState("");
  const [novoConsumo, setNovoConsumo] = useState("");

  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  async function carregarEquipamentosUsuario() {
    try {
      const dados = await equipamentoUsuarioService.listarPorUsuario(
        usuario.id
      );
      setEquipamentosUsuario(dados || []);
      setErroLista(null);
    } catch (error) {
      setErroLista(error?.message || "Erro ao carregar equipamentos");
    }
  }

  async function carregarCatalogo() {
    try {
      const dados = await equipamentoService.listar();
      setCatalogo(dados || []);
      return dados || [];
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao carregar o catálogo de equipamentos",
        tipo: "erro",
      });
      return [];
    }
  }

  useEffect(() => {
    carregarCatalogo();
    carregarEquipamentosUsuario();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  // Previa ao vivo: consumoPorHora do equipamento base * horas por dia.
  const preview = useMemo(() => {
    const equipamentoId = Number(equipamentoSelecionado);
    const horas = Number(horasPorDia);

    if (!equipamentoId || !horas) {
      return "0.00";
    }

    const base = catalogo.find((e) => Number(e.id) === equipamentoId);
    if (!base) {
      return "0.00";
    }

    return (Number(base.consumoPorHora) * horas).toFixed(2);
  }, [equipamentoSelecionado, horasPorDia, catalogo]);

  function abrirFormulario() {
    setEquipUserId("");
    setEquipamentoSelecionado("");
    setNomeIdentificacao("");
    setHorasPorDia("");
    setFormAberto(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function fecharFormulario() {
    setFormAberto(false);
    setEquipUserId("");
    setEquipamentoSelecionado("");
    setNomeIdentificacao("");
    setHorasPorDia("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    const payload = {
      nomeIdentificacao,
      horasPorDia: Number(horasPorDia),
      consumoEsperado: Number(preview),
      usuarioId: usuario.id,
      equipamentoId: Number(equipamentoSelecionado),
    };

    try {
      if (equipUserId) {
        await equipamentoUsuarioService.alterar(equipUserId, payload);
      } else {
        await equipamentoUsuarioService.adicionar(payload);
      }
      fecharFormulario();
      carregarEquipamentosUsuario();
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao salvar equipamento",
        tipo: "erro",
      });
    }
  }

  async function editarEquipamento(id) {
    setMensagem(null);
    try {
      // Garante que o catalogo esteja carregado antes de setar o select,
      // para que a option correspondente ja exista.
      await carregarCatalogo();
      const item = await equipamentoUsuarioService.buscar(id);

      setEquipUserId(item.id);
      setNomeIdentificacao(item.nomeIdentificacao);
      setHorasPorDia(item.horasPorDia);
      setEquipamentoSelecionado(String(item.equipamentoId));
      setFormAberto(true);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao carregar o equipamento",
        tipo: "erro",
      });
    }
  }

  async function excluirEquipamento(id) {
    if (!window.confirm("Deseja excluir esse equipamento?")) {
      return;
    }

    try {
      await equipamentoUsuarioService.remover(id);
      carregarEquipamentosUsuario();
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao excluir equipamento",
        tipo: "erro",
      });
    }
  }

  function abrirModal() {
    setNovoNome("");
    setNovoModelo("");
    setNovoConsumo("");
    setModalAberto(true);
  }

  function fecharModal() {
    setModalAberto(false);
  }

  async function salvarEquipamentoBase() {
    setMensagem(null);
    try {
      const novoEquip = await equipamentoService.adicionar({
        nome: novoNome,
        modelo: novoModelo,
        consumoPorHora: Number(novoConsumo),
      });

      await carregarCatalogo();
      // Auto-seleciona o equipamento recem-criado (a previa recalcula sozinha).
      setEquipamentoSelecionado(String(novoEquip.id));
      fecharModal();
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao cadastrar equipamento",
        tipo: "erro",
      });
    }
  }

  // Mais recentes primeiro.
  const ordenados = [...equipamentosUsuario].reverse();

  return (
    <div>
      <div className="page-header consumo-header">
        <div>
          <h2>Equipamentos</h2>
          <p>Gerencie os equipamentos vinculados ao seu consumo.</p>
        </div>

        <button type="button" className="btn-add" onClick={abrirFormulario}>
          Adicionar Equipamento
        </button>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className={`form-card${formAberto ? " active" : ""}`}>
        <button
          type="button"
          className="btn-fechar-form"
          onClick={fecharFormulario}
        >
          ×
        </button>

        <div className="form-header">
          <h5>{equipUserId ? "Editar equipamento" : "Novo equipamento"}</h5>
        </div>

        <div className="form-body">
          <form onSubmit={handleSubmit}>
            <div className="campo">
              <label className="form-label" htmlFor="equipamento">
                Equipamento base
              </label>
              <div className="select-com-acao">
                <select
                  id="equipamento"
                  className="form-select"
                  value={equipamentoSelecionado}
                  onChange={(e) => setEquipamentoSelecionado(e.target.value)}
                  required
                >
                  <option value="">Selecione um equipamento</option>
                  {catalogo.map((eq) => (
                    <option key={eq.id} value={String(eq.id)}>
                      {eq.nome}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  className="btn-action btn-edit"
                  onClick={abrirModal}
                >
                  Novo
                </button>
              </div>
            </div>

            <div className="campo">
              <label className="form-label" htmlFor="nomeIdentificacao">
                Nome de identificação
              </label>
              <input
                id="nomeIdentificacao"
                type="text"
                className="form-control"
                placeholder="Ex.: Geladeira da cozinha"
                value={nomeIdentificacao}
                onChange={(e) => setNomeIdentificacao(e.target.value)}
                required
              />
            </div>

            <div className="campo">
              <label className="form-label" htmlFor="horasPorDia">
                Horas por dia
              </label>
              <input
                id="horasPorDia"
                type="number"
                step="0.01"
                className="form-control"
                placeholder="Ex.: 8"
                value={horasPorDia}
                onChange={(e) => setHorasPorDia(e.target.value)}
                required
              />
            </div>

            <div className="preview">
              <div className="preview-label">Consumo esperado</div>
              <div className="preview-value">
                <span>{preview}</span>
                kWh/dia
              </div>
            </div>

            <button type="submit" className="btn-save">
              Salvar Equipamento
            </button>
          </form>
        </div>
      </div>

      <div className="historico-title">
        <h4>Seus Equipamentos</h4>
      </div>

      <div className="lista-equip">
        {erroLista ? (
          <div className="equip-card">{erroLista}</div>
        ) : ordenados.length === 0 ? (
          <div className="equip-card">Nenhum equipamento encontrado.</div>
        ) : (
          ordenados.map((item) => (
            <div className="equip-card" key={item.id}>
              <div className="equip-info">
                <h5>{item.nomeIdentificacao}</h5>
                <p>{item.nomeEquipamento}</p>
                <a className="badge-consumo">
                  {Number(item.consumoEsperado).toFixed(2)} kWh/dia
                </a>
              </div>

              <div className="equip-actions">
                <button
                  type="button"
                  className="btn-action btn-edit"
                  onClick={() => editarEquipamento(item.id)}
                >
                  Editar
                </button>
                <button
                  type="button"
                  className="btn-action btn-delete"
                  onClick={() => excluirEquipamento(item.id)}
                >
                  Excluir
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {modalAberto && (
        <div className="modal" onClick={fecharModal}>
          <div
            className="modal-content"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h5>Novo equipamento base</h5>
              <button
                type="button"
                className="btn-close-custom"
                onClick={fecharModal}
              >
                ×
              </button>
            </div>

            <div className="modal-body">
              <div className="campo">
                <label className="form-label" htmlFor="novoNome">
                  Nome
                </label>
                <input
                  id="novoNome"
                  type="text"
                  className="form-control"
                  value={novoNome}
                  onChange={(e) => setNovoNome(e.target.value)}
                />
              </div>

              <div className="campo">
                <label className="form-label" htmlFor="novoModelo">
                  Modelo
                </label>
                <input
                  id="novoModelo"
                  type="text"
                  className="form-control"
                  value={novoModelo}
                  onChange={(e) => setNovoModelo(e.target.value)}
                />
              </div>

              <div className="campo">
                <label className="form-label" htmlFor="novoConsumo">
                  Consumo por hora (kWh)
                </label>
                <input
                  id="novoConsumo"
                  type="number"
                  step="0.01"
                  className="form-control"
                  value={novoConsumo}
                  onChange={(e) => setNovoConsumo(e.target.value)}
                />
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn-action btn-delete"
                onClick={fecharModal}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="btn-save"
                onClick={salvarEquipamentoBase}
              >
                Salvar Equipamento
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
