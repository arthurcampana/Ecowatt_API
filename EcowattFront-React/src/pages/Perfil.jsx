import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { usuarioService } from "../api/usuarioService.js";

// Tela de perfil: carrega os dados do usuario logado e permite alterar nome e
// senha. A data de criacao e exibida como '--' porque o endpoint de busca nao
// devolve dataRegistro (decisao registrada em context.json).
export default function Perfil() {
  const { usuario, atualizarUsuario } = useAuth();

  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [mensagem, setMensagem] = useState(null); // { texto, tipo }
  const [toastVisivel, setToastVisivel] = useState(false);
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    let ativo = true;

    async function carregarPerfil() {
      try {
        const dados = await usuarioService.buscar(usuario.id);
        if (!ativo) {
          return;
        }
        setNome(dados.nome ?? "");
        setEmail(dados.email ?? "");
      } catch (error) {
        if (!ativo) {
          return;
        }
        setMensagem({
          texto: error?.message || "Erro ao carregar perfil.",
          tipo: "erro",
        });
      }
    }

    carregarPerfil();

    return () => {
      ativo = false;
    };
  }, [usuario.id]);

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    if (senha && senha !== confirmarSenha) {
      setMensagem({ texto: "As senhas não conferem.", tipo: "erro" });
      return;
    }

    const nomeNormalizado = nome.trim();

    try {
      setCarregando(true);
      await usuarioService.alterar(usuario.id, {
        nome: nomeNormalizado,
        senha: senha || null,
      });

      setSenha("");
      setConfirmarSenha("");

      if (nomeNormalizado !== usuario.nome) {
        atualizarUsuario({ nome: nomeNormalizado });
      }

      setToastVisivel(true);
      setTimeout(() => setToastVisivel(false), 3000);
    } catch (error) {
      setMensagem({
        texto: error?.message || "Erro ao atualizar perfil.",
        tipo: "erro",
      });
    } finally {
      setCarregando(false);
    }
  }

  const inicial = nome ? nome.charAt(0).toUpperCase() : "";

  return (
    <div className="perfil-card">
      <div className="perfil-topo">
        <div className="avatar">
          <span>{inicial}</span>
        </div>
        <h3>Meu Perfil</h3>
        <p className="aviso">Atualize suas informações pessoais.</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <form onSubmit={handleSubmit}>
        <div className="campo">
          <label className="form-label" htmlFor="nome">
            Nome
          </label>
          <input
            id="nome"
            type="text"
            className="form-control"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
          />
        </div>

        <div className="campo">
          <label className="form-label" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            className="form-control"
            value={email}
            disabled
          />
        </div>

        <div className="campo">
          <label className="form-label" htmlFor="senha">
            Nova senha
          </label>
          <input
            id="senha"
            type="password"
            className="form-control"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
          />
        </div>

        <div className="campo">
          <label className="form-label" htmlFor="confirmarSenha">
            Confirmar senha
          </label>
          <input
            id="confirmarSenha"
            type="password"
            className="form-control"
            value={confirmarSenha}
            onChange={(e) => setConfirmarSenha(e.target.value)}
          />
        </div>

        <div className="info-box">
          <strong>Conta criada em</strong>
          <p>--</p>
        </div>

        <button type="submit" className="btn-save" disabled={carregando}>
          {carregando ? "Salvando..." : "Salvar Alterações"}
        </button>
      </form>

      <div className={`toast-custom${toastVisivel ? " show" : ""}`}>
        Perfil atualizado com sucesso.
      </div>
    </div>
  );
}
