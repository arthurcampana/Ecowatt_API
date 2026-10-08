import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { demoService } from "../api/demoService.js";

// Atalho secreto (Ctrl+Shift+D), recriando o atalho oculto do front legado.
// Ao ser acionado, chama o endpoint /demo/popular/{usuarioId} do backend, que
// popula o banco com dados de demonstracao residencial.
export default function DemoKonami() {
  const { usuario } = useAuth();
  const [toast, setToast] = useState(null); // { texto, tipo }

  useEffect(() => {
    let ocupado = false;

    async function popular() {
      if (ocupado) {
        return;
      }
      ocupado = true;
      setToast({ texto: "Gerando dados de demonstração...", tipo: "info" });

      try {
        await demoService.popular(usuario.id);
        setToast({
          texto: "Dados de demonstração criados! Atualizando...",
          tipo: "sucesso",
        });
        setTimeout(() => window.location.reload(), 1200);
      } catch (error) {
        setToast({
          texto: error?.message || "Erro ao gerar dados de demonstração.",
          tipo: "erro",
        });
        ocupado = false;
        setTimeout(() => setToast(null), 4000);
      }
    }

    function handleKeyDown(event) {
      // Ctrl + Shift + D
      if (
        event.ctrlKey &&
        event.shiftKey &&
        (event.key === "D" || event.key === "d")
      ) {
        event.preventDefault();
        popular();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [usuario.id]);

  if (!toast) {
    return null;
  }

  return <div className={`demo-toast ${toast.tipo}`}>{toast.texto}</div>;
}
