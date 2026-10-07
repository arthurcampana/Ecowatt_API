import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { demoService } from "../api/demoService.js";

// Sequencia secreta (Konami Code): cima cima baixo baixo esq dir esq dir B A.
// Ao completar, chama o endpoint /demo/popular/{usuarioId} do backend, que
// recria o atalho oculto do front legado (popular banco com dados de demo).
const SEQUENCIA = [
  "ArrowUp",
  "ArrowUp",
  "ArrowDown",
  "ArrowDown",
  "ArrowLeft",
  "ArrowRight",
  "ArrowLeft",
  "ArrowRight",
  "b",
  "a",
];

export default function DemoKonami() {
  const { usuario } = useAuth();
  const [toast, setToast] = useState(null); // { texto, tipo }

  useEffect(() => {
    let progresso = 0;
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
      // Ignora quando o foco esta em um campo de texto, para nao atrapalhar
      // a digitacao normal nos formularios.
      const alvo = event.target;
      const digitando =
        alvo &&
        (alvo.tagName === "INPUT" ||
          alvo.tagName === "TEXTAREA" ||
          alvo.isContentEditable);
      if (digitando) {
        progresso = 0;
        return;
      }

      const tecla = event.key.length === 1 ? event.key.toLowerCase() : event.key;
      const esperada = SEQUENCIA[progresso];

      if (tecla === esperada) {
        progresso += 1;
        if (progresso === SEQUENCIA.length) {
          progresso = 0;
          popular();
        }
      } else {
        // Recomeca; se a tecla errada for o inicio da sequencia, ja conta 1.
        progresso = tecla === SEQUENCIA[0] ? 1 : 0;
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
