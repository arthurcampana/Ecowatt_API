import { metricasService } from "../api/metricasService.js";
import { getToken } from "../api/apiClient.js";

/**
 * Envia um clique para a API (camada bronze: rotulo cru, sem padronizar).
 * A limpeza/padronizacao acontece depois no pipeline PySpark (camada prata).
 *
 * So dispara se houver usuario logado; sem token, nem chama a API (o backend
 * ignoraria de qualquer forma, isso so poupa a requisicao e evita 401 em massa).
 *
 * Nunca lanca erro: metricas nao podem atrapalhar a navegacao do usuario.
 *
 * @param {string} rotulo identificador do elemento (ex.: "logout", "config").
 * @param {string} [pagina] rota atual; por padrao usa o pathname.
 */
export function rastrearClique(rotulo, pagina = window.location.pathname) {
  if (!getToken()) return;
  if (!rotulo) return;

  // Sem await de proposito: disparar e esquecer, para nao travar o clique.
  metricasService.registrarClique(rotulo, pagina).catch(() => {
    /* silencioso: metricas nunca quebram a UI */
  });
}
