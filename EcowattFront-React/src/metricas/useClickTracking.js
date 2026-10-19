import { useEffect } from "react";
import { rastrearClique } from "./tracker.js";

/**
 * Hook de rastreamento automatico de cliques (camada bronze: captura tudo).
 *
 * Registra um unico listener global que captura todo clique em elementos
 * interativos e envia o rotulo para a API. Deve ser usado UMA vez, no topo
 * da arvore (dentro do Layout das rotas protegidas, onde o usuario esta logado).
 *
 * Prioriza data-evento para rotulos estaveis:
 *   <button data-evento="logout">Sair</button>
 */
export function useClickTracking() {
  useEffect(() => {
    function onClick(e) {
      const el = e.target.closest(
        "a, button, [role='button'], input[type='submit'], input[type='button'], [data-evento]"
      );
      if (!el) return; // clique em area nao interativa: ignora

      const rotulo =
        el.getAttribute("data-evento") || // 1o: rotulo manual (estavel)
        el.getAttribute("aria-label") || // 2o: acessibilidade
        (el.innerText || el.textContent || "").trim().slice(0, 40) || // 3o: texto
        el.id || // 4o: id
        el.tagName.toLowerCase(); // 5o: tag

      rastrearClique(rotulo);
    }

    // capture = true garante o registro mesmo se algo chamar stopPropagation.
    document.addEventListener("click", onClick, true);
    return () => document.removeEventListener("click", onClick, true);
  }, []);
}
