import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { metricasService } from "../api/metricasService.js";

/**
 * Overlay de mapa de calor de cliques "estilo densidade" (manchas suaves
 * azul->verde->amarelo->vermelho), desenhado POR CIMA da tela atual.
 *
 * Não usa coordenadas salvas: na hora de renderizar, localiza cada elemento
 * interativo marcado com data-evento (getBoundingClientRect) e pinta uma
 * mancha sobre ele, com intensidade proporcional ao número de cliques daquele
 * rótulo (vindo de /metricas/cliques-mais-usados).
 *
 * Fluxo: desenha um "campo de calor" em escala de cinza (gradientes radiais
 * somados por alpha) e depois colore cada pixel pela paleta térmica — a mesma
 * técnica das libs de heatmap, feita em canvas puro.
 */
export default function HeatmapOverlay({ aberto, onFechar }) {
  const canvasRef = useRef(null);
  // Posicao de scroll no momento da ultima pintura real do canvas. Enquanto o
  // usuario rola, deslocamos o canvas por CSS (barato) em vez de repintar.
  const scrollPintadoRef = useRef({ x: 0, y: 0 });
  const [cliques, setCliques] = useState([]);
  const [carregado, setCarregado] = useState(false);
  // Rota atual: o heatmap e redesenhado quando ela muda (troca de pagina).
  const location = useLocation();

  // Busca o ranking de cliques quando o overlay abre.
  useEffect(() => {
    if (!aberto) return;
    let ativo = true;
    setCarregado(false);
    metricasService
      .cliquesMaisUsados()
      .then((dados) => {
        if (ativo) {
          setCliques(dados || []);
          setCarregado(true);
        }
      })
      .catch(() => {
        if (ativo) setCarregado(true);
      });
    return () => {
      ativo = false;
    };
  }, [aberto]);

  // Desenha (e redesenha em resize) enquanto estiver aberto.
  useEffect(() => {
    if (!aberto || !carregado) return;

    function desenhar() {
      const canvas = canvasRef.current;
      if (!canvas) return;

      const largura = window.innerWidth;
      const altura = window.innerHeight;
      canvas.width = largura;
      canvas.height = altura;

      const ctx = canvas.getContext("2d");
      ctx.clearRect(0, 0, largura, altura);

      // Mapa rotulo -> total de cliques.
      const porRotulo = {};
      let maxTotal = 0;
      for (const c of cliques) {
        porRotulo[c.rotulo] = c.total;
        if (c.total > maxTotal) maxTotal = c.total;
      }
      if (maxTotal === 0) return;

      // 1) Campo de calor em tons de cinza: para cada elemento com data-evento
      //    visível, soma um gradiente radial com intensidade pelos cliques.
      const elementos = document.querySelectorAll("[data-evento]");
      elementos.forEach((el) => {
        const rotulo = el.getAttribute("data-evento");
        const total = porRotulo[rotulo];
        if (!total) return;

        const r = el.getBoundingClientRect();
        // ignora o que está fora da viewport ou é o próprio overlay
        if (r.width === 0 || r.height === 0) return;

        const cx = r.left + r.width / 2;
        const cy = r.top + r.height / 2;

        const intensidade = total / maxTotal; // 0..1
        // Raio da mancha: cresce com os cliques, mas limitado ao tamanho do
        // proprio elemento para nao "vazar" sobre campos/vizinhos (antes um
        // botao muito clicado pintava ate os campos acima dele).
        const meia = Math.max(r.width, r.height) / 2;
        const raio = Math.min(meia + 24, 24 + intensidade * 60);

        const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, raio);
        grad.addColorStop(0, `rgba(0,0,0,${0.15 + intensidade * 0.85})`);
        grad.addColorStop(1, "rgba(0,0,0,0)");

        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(cx, cy, raio, 0, Math.PI * 2);
        ctx.fill();
      });

      // 2) Coloriza: lê a intensidade (alpha) de cada pixel e aplica a paleta
      //    térmica azul -> ciano -> verde -> amarelo -> vermelho.
      const img = ctx.getImageData(0, 0, largura, altura);
      const d = img.data;
      for (let i = 0; i < d.length; i += 4) {
        const alpha = d[i + 3] / 255; // intensidade acumulada nesse pixel
        if (alpha === 0) continue;
        const [red, green, blue] = corTermica(alpha);
        d[i] = red;
        d[i + 1] = green;
        d[i + 2] = blue;
        // transparência geral do overlay (deixa ver a tela por baixo)
        d[i + 3] = Math.min(255, alpha * 230);
      }
      ctx.putImageData(img, 0, 0);

      // A pintura foi feita para a posicao de scroll atual. Guarda a referencia
      // e zera qualquer deslocamento CSS acumulado durante o scroll anterior.
      scrollPintadoRef.current = {
        x: window.scrollX || window.pageXOffset || 0,
        y: window.scrollY || window.pageYOffset || 0,
      };
      canvas.style.transform = "";
    }

    // Agenda o desenho no proximo frame (deixa o layout estabilizar antes de
    // ler getBoundingClientRect) com um coalescing simples por requestAnimationFrame.
    let frame = null;
    function agendarRedesenho() {
      if (frame !== null) return;
      frame = window.requestAnimationFrame(() => {
        frame = null;
        desenhar();
      });
    }

    // Enquanto o usuario rola, NAO repintamos (caro). Apenas deslocamos o canvas
    // por CSS transform para as manchas acompanharem a pagina — a GPU faz isso
    // de graca, sem travar. Quando a rolagem para, um desenho real reposiciona.
    let scrollFrame = null;
    let pararScroll = null;
    function aoRolar() {
      const canvas = canvasRef.current;
      if (!canvas) return;
      if (scrollFrame === null) {
        scrollFrame = window.requestAnimationFrame(() => {
          scrollFrame = null;
          const dx = (window.scrollX || window.pageXOffset || 0) - scrollPintadoRef.current.x;
          const dy = (window.scrollY || window.pageYOffset || 0) - scrollPintadoRef.current.y;
          // Conteudo desce quando rolamos para baixo -> canvas sobe (-dy).
          canvas.style.transform = `translate(${-dx}px, ${-dy}px)`;
        });
      }
      // Debounce: ao parar de rolar, repinta de verdade na posicao final.
      if (pararScroll !== null) window.clearTimeout(pararScroll);
      pararScroll = window.setTimeout(agendarRedesenho, 120);
    }
    window.addEventListener("scroll", aoRolar, { passive: true, capture: true });

    // Desenha ao abrir e quando a pagina/dados mudam. NAO redesenhamos a cada
    // frame de scroll de proposito: o heatmap faz getImageData + loop em todos
    // os pixels da viewport, caro demais para rodar durante a rolagem (era isso
    // que travava). O mapa mostra a distribuicao de cliques da tela.
    desenhar();
    agendarRedesenho();

    // Ao trocar de pagina, os elementos com data-evento da nova tela podem
    // ainda nao existir no DOM (paginas que buscam dados antes de renderizar).
    // Em vez de chutar um unico timeout, observamos o DOM: cada mudanca agenda
    // um redesenho (com debounce), ate a pagina assentar. Assim o "atualizar
    // automatico ao entrar na pagina" acontece quando os botoes aparecem,
    // independentemente de quanto a pagina demorou para montar.
    let debounce = null;
    function aoMudarDom() {
      if (debounce !== null) window.clearTimeout(debounce);
      debounce = window.setTimeout(agendarRedesenho, 120);
    }
    const observer = new MutationObserver(aoMudarDom);
    observer.observe(document.body, { childList: true, subtree: true });
    // Rede-desenho de seguranca logo apos a montagem.
    const timer = window.setTimeout(agendarRedesenho, 150);
    // Para de observar depois que a pagina ja assentou (evita custo continuo).
    const parar = window.setTimeout(() => observer.disconnect(), 1500);

    window.addEventListener("resize", agendarRedesenho);
    return () => {
      window.clearTimeout(timer);
      window.clearTimeout(parar);
      if (debounce !== null) window.clearTimeout(debounce);
      if (pararScroll !== null) window.clearTimeout(pararScroll);
      if (scrollFrame !== null) window.cancelAnimationFrame(scrollFrame);
      observer.disconnect();
      if (frame !== null) window.cancelAnimationFrame(frame);
      window.removeEventListener("resize", agendarRedesenho);
      window.removeEventListener("scroll", aoRolar, { capture: true });
    };
  }, [aberto, carregado, cliques, location.pathname]);

  if (!aberto) return null;

  const temDados = cliques.length > 0;

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 9998,
        pointerEvents: "none",
      }}
    >
      <canvas ref={canvasRef} style={{ display: "block" }} />

      {/* Barra de controle do overlay (clicável) */}
      <div
        style={{
          position: "fixed",
          top: 16,
          right: 16,
          zIndex: 9999,
          pointerEvents: "auto",
          background: "rgba(15,20,25,0.9)",
          color: "#e6edf3",
          border: "1px solid #2a333d",
          borderRadius: 10,
          padding: "10px 14px",
          font: "13px system-ui, sans-serif",
          display: "flex",
          alignItems: "center",
          gap: 12,
          boxShadow: "0 6px 20px rgba(0,0,0,.35)",
        }}
      >
        <span style={{ fontWeight: 600 }}>🔥 Mapa de calor de cliques</span>
        {!temDados && (
          <span style={{ opacity: 0.7 }}>sem dados de clique ainda</span>
        )}
        <button
          type="button"
          onClick={onFechar}
          style={{
            background: "#198754",
            color: "#fff",
            border: "none",
            borderRadius: 6,
            padding: "4px 10px",
            cursor: "pointer",
          }}
        >
          Fechar
        </button>
      </div>

      {/* Legenda da escala */}
      <div
        style={{
          position: "fixed",
          bottom: 16,
          right: 16,
          zIndex: 9999,
          pointerEvents: "none",
          background: "rgba(15,20,25,0.9)",
          color: "#e6edf3",
          border: "1px solid #2a333d",
          borderRadius: 10,
          padding: "8px 12px",
          font: "12px system-ui, sans-serif",
        }}
      >
        <div style={{ marginBottom: 4 }}>Cliques</div>
        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <span>menos</span>
          <div
            style={{
              width: 120,
              height: 10,
              borderRadius: 5,
              background:
                "linear-gradient(90deg, rgb(0,0,255), rgb(0,255,255), rgb(0,255,0), rgb(255,255,0), rgb(255,0,0))",
            }}
          />
          <span>mais</span>
        </div>
      </div>
    </div>
  );
}

/**
 * Mapeia uma intensidade 0..1 para a paleta térmica (azul->vermelho),
 * igual às cores da referência de heatmap.
 */
function corTermica(t) {
  // pontos de cor da paleta
  const paradas = [
    { p: 0.0, c: [0, 0, 255] }, // azul
    { p: 0.4, c: [0, 255, 255] }, // ciano
    { p: 0.6, c: [0, 255, 0] }, // verde
    { p: 0.8, c: [255, 255, 0] }, // amarelo
    { p: 1.0, c: [255, 0, 0] }, // vermelho
  ];
  for (let i = 1; i < paradas.length; i++) {
    if (t <= paradas[i].p) {
      const a = paradas[i - 1];
      const b = paradas[i];
      const f = (t - a.p) / (b.p - a.p);
      return [
        Math.round(a.c[0] + (b.c[0] - a.c[0]) * f),
        Math.round(a.c[1] + (b.c[1] - a.c[1]) * f),
        Math.round(a.c[2] + (b.c[2] - a.c[2]) * f),
      ];
    }
  }
  return [255, 0, 0];
}
