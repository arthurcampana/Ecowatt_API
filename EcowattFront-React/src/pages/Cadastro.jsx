import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { usuarioService } from "../api/usuarioService.js";
import AuthLayout from "../components/AuthLayout.jsx";

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
    <AuthLayout>
      <h1>Criar conta</h1>
      <p className="auth-sub">Comece a monitorar seu consumo hoje.</p>

      {mensagem && <div className={`auth-msg ${mensagem.tipo}`}>{mensagem.texto}</div>}

      <form onSubmit={handleSubmit}>
        <div className="auth-field">
          <label htmlFor="nome">Nome completo</label>
          <input
            id="nome"
            type="text"
            className="auth-input"
            placeholder="Seu nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />
        </div>

        <div className="auth-field">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            className="auth-input"
            placeholder="seu@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="auth-field">
          <label htmlFor="senha">Senha</label>
          <input
            id="senha"
            type="password"
            className="auth-input"
            placeholder="Mínimo 6 caracteres"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </div>

        <div className="auth-field">
          <label htmlFor="confirmarSenha">Confirmar senha</label>
          <input
            id="confirmarSenha"
            type="password"
            className="auth-input"
            placeholder="Repita a senha"
            value={confirmarSenha}
            onChange={(e) => setConfirmarSenha(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="auth-btn" disabled={carregando}>
          {carregando ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>

      <p className="auth-rodape">
        Já tem conta? <Link to="/login">Entrar</Link>
      </p>
    </AuthLayout>
  );
}
