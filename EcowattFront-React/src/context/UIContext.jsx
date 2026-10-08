import {
  createContext,
  useContext,
  useState,
  useCallback,
  useRef,
} from "react";
import Icon from "../components/Icon.jsx";
import "../styles/ui.css";

const UIContext = createContext(null);

let idSeq = 0;

// Provedor de UI global: toasts (feedback de acoes) e um modal de confirmacao
// que substitui o window.confirm. Expoe:
//   toast(texto, tipo)  -> tipo: "sucesso" | "erro" | "info"
//   confirmar({ titulo, mensagem, confirmLabel, perigo }) -> Promise<boolean>
export function UIProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const [dialogo, setDialogo] = useState(null);
  const resolverRef = useRef(null);

  const removerToast = useCallback((id) => {
    setToasts((lista) => lista.filter((t) => t.id !== id));
  }, []);

  const toast = useCallback(
    (texto, tipo = "info", duracao = 3500) => {
      const id = ++idSeq;
      setToasts((lista) => [...lista, { id, texto, tipo }]);
      if (duracao > 0) {
        setTimeout(() => removerToast(id), duracao);
      }
      return id;
    },
    [removerToast]
  );

  const confirmar = useCallback((opcoes = {}) => {
    return new Promise((resolve) => {
      resolverRef.current = resolve;
      setDialogo({
        titulo: opcoes.titulo || "Tem certeza?",
        mensagem: opcoes.mensagem || "Esta ação não pode ser desfeita.",
        confirmLabel: opcoes.confirmLabel || "Confirmar",
        cancelLabel: opcoes.cancelLabel || "Cancelar",
        perigo: opcoes.perigo ?? true,
      });
    });
  }, []);

  function fechar(resultado) {
    if (resolverRef.current) {
      resolverRef.current(resultado);
      resolverRef.current = null;
    }
    setDialogo(null);
  }

  const iconePorTipo = {
    sucesso: "check-circle",
    erro: "alert-circle",
    info: "info",
  };

  return (
    <UIContext.Provider value={{ toast, confirmar }}>
      {children}

      {/* Pilha de toasts */}
      <div className="ui-toasts">
        {toasts.map((t) => (
          <div key={t.id} className={`ui-toast ${t.tipo}`} role="status">
            <Icon name={iconePorTipo[t.tipo] || "info"} size={18} />
            <span>{t.texto}</span>
            <button
              type="button"
              className="ui-toast-x"
              onClick={() => removerToast(t.id)}
              aria-label="Fechar"
            >
              <Icon name="x" size={14} />
            </button>
          </div>
        ))}
      </div>

      {/* Modal de confirmacao */}
      {dialogo && (
        <div className="ui-overlay" onClick={() => fechar(false)}>
          <div
            className="ui-dialog"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <div className={`ui-dialog-ico ${dialogo.perigo ? "perigo" : ""}`}>
              <Icon name={dialogo.perigo ? "alert-circle" : "info"} size={26} />
            </div>
            <h3 className="ui-dialog-titulo">{dialogo.titulo}</h3>
            <p className="ui-dialog-msg">{dialogo.mensagem}</p>
            <div className="ui-dialog-acoes">
              <button
                type="button"
                className="ui-btn-cancelar"
                onClick={() => fechar(false)}
              >
                {dialogo.cancelLabel}
              </button>
              <button
                type="button"
                className={dialogo.perigo ? "ui-btn-perigo" : "ui-btn-ok"}
                onClick={() => fechar(true)}
              >
                {dialogo.confirmLabel}
              </button>
            </div>
          </div>
        </div>
      )}
    </UIContext.Provider>
  );
}

export function useUI() {
  const ctx = useContext(UIContext);
  if (!ctx) {
    throw new Error("useUI deve ser usado dentro de <UIProvider>");
  }
  return ctx;
}
