import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { useUI } from "../context/UIContext.jsx";
import { usuarioService } from "../api/usuarioService.js";
import Icon from "../components/Icon.jsx";
import "../styles/perfil.css"; // estilos da pagina de perfil

// Calcula uma pontuacao simples de forca de senha (0-4) com base em tamanho e
// variedade de caracteres. Usado apenas para feedback visual.
function forcaSenha(senha) {
  if (!senha) {
    return 0;
  }
  let score = 0;
  if (senha.length >= 6) score++;
  if (senha.length >= 10) score++;
  if (/[A-Z]/.test(senha) && /[a-z]/.test(senha)) score++;
  if (/\d/.test(senha) && /[^A-Za-z0-9]/.test(senha)) score++;
  return Math.min(score, 4);
}

const ROTULO_FORCA = ["", "Fraca", "Razoável", "Boa", "Forte"];

// Tela de perfil: header com avatar + identidade e dois blocos independentes
// (informacoes pessoais / seguranca). Toda a comunicacao com a API foi mantida
// do fluxo anterior; apenas separada em duas submissoes.
export default function Perfil() {
  const { usuario, atualizarUsuario } = useAuth();
  const { toast } = useUI();

  const [nome, setNome] = useState("");
  const [nomeOriginal, setNomeOriginal] = useState("");
  const [email, setEmail] = useState("");

  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [verSenha, setVerSenha] = useState(false);
  const [verConfirmar, setVerConfirmar] = useState(false);

  const [salvandoDados, setSalvandoDados] = useState(false);
  const [salvandoSenha, setSalvandoSenha] = useState(false);

  useEffect(() => {
    let ativo = true;

    async function carregarPerfil() {
      try {
        const dados = await usuarioService.buscar(usuario.id);
        if (!ativo) {
          return;
        }
        setNome(dados.nome ?? "");
        setNomeOriginal(dados.nome ?? "");
        setEmail(dados.email ?? "");
      } catch (error) {
        if (ativo) {
          toast(error?.message || "Erro ao carregar perfil.", "erro");
        }
      }
    }

    carregarPerfil();

    return () => {
      ativo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  const nomeMudou = nome.trim() !== nomeOriginal && nome.trim().length > 0;
  const senhasBatem = senha === confirmarSenha;
  const score = useMemo(() => forcaSenha(senha), [senha]);

  async function salvarDados(event) {
    event.preventDefault();
    const nomeNormalizado = nome.trim();
    if (!nomeNormalizado) {
      toast("O nome não pode ficar vazio.", "erro");
      return;
    }

    try {
      setSalvandoDados(true);
      await usuarioService.alterar(usuario.id, {
        nome: nomeNormalizado,
        senha: null,
      });
      setNomeOriginal(nomeNormalizado);
      if (nomeNormalizado !== usuario.nome) {
        atualizarUsuario({ nome: nomeNormalizado });
      }
      toast("Informações atualizadas com sucesso.", "sucesso");
    } catch (error) {
      toast(error?.message || "Erro ao atualizar perfil.", "erro");
    } finally {
      setSalvandoDados(false);
    }
  }

  async function salvarSenha(event) {
    event.preventDefault();
    if (!senha) {
      toast("Informe a nova senha.", "erro");
      return;
    }
    if (senha.length < 6) {
      toast("A senha deve ter pelo menos 6 caracteres.", "erro");
      return;
    }
    if (!senhasBatem) {
      toast("As senhas não conferem.", "erro");
      return;
    }

    try {
      setSalvandoSenha(true);
      await usuarioService.alterar(usuario.id, {
        nome: nomeOriginal,
        senha,
      });
      setSenha("");
      setConfirmarSenha("");
      toast("Senha alterada com sucesso.", "sucesso");
    } catch (error) {
      toast(error?.message || "Erro ao alterar a senha.", "erro");
    } finally {
      setSalvandoSenha(false);
    }
  }

  const iniciais = (nome || usuario?.nome || "?")
    .split(" ")
    .map((p) => p[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <div className="perfil">
      <div className="page-header">
        <h2>Meu Perfil</h2>
        <p>Gerencie suas informações pessoais e de segurança.</p>
      </div>

      {/* Header de identidade */}
      <div className="perfil-header">
        <div className="perfil-avatar">{iniciais}</div>
        <div className="perfil-identidade">
          <h3>{nome || usuario?.nome}</h3>
          <span>{email}</span>
        </div>
      </div>

      <div className="perfil-grid">
        {/* Informacoes pessoais */}
        <section className="perfil-secao">
          <div className="perfil-secao-head">
            <Icon name="user" size={18} />
            <h4>Informações pessoais</h4>
          </div>

          <form onSubmit={salvarDados}>
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
              <div className="helper-text">O email não pode ser alterado.</div>
            </div>

            <button
              type="submit"
              className="btn-save"
              disabled={!nomeMudou || salvandoDados}
            >
              {salvandoDados ? "Salvando..." : "Salvar informações"}
            </button>
          </form>
        </section>

        {/* Seguranca */}
        <section className="perfil-secao">
          <div className="perfil-secao-head">
            <Icon name="settings" size={18} />
            <h4>Segurança</h4>
          </div>

          <form onSubmit={salvarSenha}>
            <div className="campo">
              <label className="form-label" htmlFor="senha">
                Nova senha
              </label>
              <div className="input-senha">
                <input
                  id="senha"
                  type={verSenha ? "text" : "password"}
                  className="form-control"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  placeholder="Mínimo 6 caracteres"
                />
                <button
                  type="button"
                  className="btn-olho"
                  onClick={() => setVerSenha((v) => !v)}
                  aria-label={verSenha ? "Ocultar senha" : "Mostrar senha"}
                >
                  <Icon name={verSenha ? "eye-off" : "eye"} size={18} />
                </button>
              </div>

              {senha && (
                <div className="forca-senha">
                  <div className="forca-barras">
                    {[1, 2, 3, 4].map((n) => (
                      <span
                        key={n}
                        className={`forca-barra n${score} ${
                          n <= score ? "ativa" : ""
                        }`}
                      />
                    ))}
                  </div>
                  <span className={`forca-rotulo n${score}`}>
                    {ROTULO_FORCA[score]}
                  </span>
                </div>
              )}
            </div>

            <div className="campo">
              <label className="form-label" htmlFor="confirmarSenha">
                Confirmar senha
              </label>
              <div className="input-senha">
                <input
                  id="confirmarSenha"
                  type={verConfirmar ? "text" : "password"}
                  className={`form-control ${
                    confirmarSenha && !senhasBatem ? "invalido" : ""
                  }`}
                  value={confirmarSenha}
                  onChange={(e) => setConfirmarSenha(e.target.value)}
                  placeholder="Repita a nova senha"
                />
                <button
                  type="button"
                  className="btn-olho"
                  onClick={() => setVerConfirmar((v) => !v)}
                  aria-label={verConfirmar ? "Ocultar senha" : "Mostrar senha"}
                >
                  <Icon name={verConfirmar ? "eye-off" : "eye"} size={18} />
                </button>
              </div>
              {confirmarSenha && !senhasBatem && (
                <div className="helper-text erro-texto">
                  As senhas não conferem.
                </div>
              )}
            </div>

            <button
              type="submit"
              className="btn-save"
              disabled={
                !senha || !senhasBatem || senha.length < 6 || salvandoSenha
              }
            >
              {salvandoSenha ? "Alterando..." : "Alterar senha"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
