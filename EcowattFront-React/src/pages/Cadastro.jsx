import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { usuarioService } from "../api/usuarioService.js";

export default function Cadastro() {
  const navigate = useNavigate();

  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [mensagem, setMensagem] = useState(null); // { texto, tipo }
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    if (!nome.trim() || !email.trim() || !senha) {
      setMensagem({ texto: "Preencha todos os campos.", tipo: "erro" });
      return;
    }

    if (senha.length < 6) {
      setMensagem({
        texto: "A senha deve ter pelo menos 6 caracteres.",
        tipo: "erro",
      });
      return;
    }

    if (senha !== confirmarSenha) {
      setMensagem({ texto: "As senhas não coincidem.", tipo: "erro" });
      return;
    }

    try {
      setCarregando(true);
      await usuarioService.cadastrar({
        nome: nome.trim(),
        email: email.trim(),
        senha,
      });
      setMensagem({
        texto: "Usuário cadastrado com sucesso!",
        tipo: "sucesso",
      });
      setTimeout(() => navigate("/login"), 1500);
    } catch (error) {
      const texto =
        error?.status === 409
          ? "Email já cadastrado."
          : error?.message || "Erro ao cadastrar usuário.";
      setMensagem({ texto, tipo: "erro" });
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h4>Cadastro</h4>

        {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            className="form-control"
            placeholder="Nome completo"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />

          <input
            type="email"
            className="form-control"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            className="form-control"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />

          <input
            type="password"
            className="form-control"
            placeholder="Confirmar senha"
            value={confirmarSenha}
            onChange={(e) => setConfirmarSenha(e.target.value)}
            required
          />

          <button
            type="submit"
            className="btn btn-primary"
            disabled={carregando}
          >
            {carregando ? "Cadastrando..." : "Cadastrar"}
          </button>
        </form>

        <p className="rodape">
          Já tem conta? <Link to="/login">Entrar</Link>
        </p>
      </div>
    </div>
  );
}
