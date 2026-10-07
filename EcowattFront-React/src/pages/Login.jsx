import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import AuthLayout from "../components/AuthLayout.jsx";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [mensagem, setMensagem] = useState(null); // { texto, tipo }
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setMensagem(null);

    if (!email.trim() || !senha) {
      setMensagem({ texto: "Preencha todos os campos.", tipo: "erro" });
      return;
    }

    try {
      setCarregando(true);
      await login(email.trim(), senha);
      setMensagem({ texto: "Login realizado com sucesso!", tipo: "sucesso" });
      setTimeout(() => navigate("/"), 800);
    } catch (error) {
      const texto =
        error?.status === 401
          ? "Email ou senha inválidos."
          : "Não foi possível conectar ao servidor.";
      setMensagem({ texto, tipo: "erro" });
    } finally {
      setCarregando(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Bem-vindo de volta</h1>
      <p className="auth-sub">Entre com sua conta para continuar.</p>

      {mensagem && <div className={`auth-msg ${mensagem.tipo}`}>{mensagem.texto}</div>}

      <form onSubmit={handleSubmit}>
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
            placeholder="••••••••"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="auth-btn" disabled={carregando}>
          {carregando ? "Entrando..." : "Fazer Login"}
        </button>
      </form>

      <p className="auth-rodape">
        Não tem conta? <Link to="/cadastro">Criar conta</Link>
      </p>
    </AuthLayout>
  );
}
