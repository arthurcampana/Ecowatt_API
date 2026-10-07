import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

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
    <div className="login-page">
      <div className="login-card">
        <h4>Login</h4>

        {mensagem && (
          <div className={mensagem.tipo}>{mensagem.texto}</div>
        )}

        <form onSubmit={handleSubmit}>
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

          <button
            type="submit"
            className="btn btn-primary"
            disabled={carregando}
          >
            {carregando ? "Entrando..." : "Fazer Login"}
          </button>
        </form>

        <p className="rodape">
          Não tem conta? <Link to="/cadastro">Criar conta</Link>
        </p>
      </div>
    </div>
  );
}
