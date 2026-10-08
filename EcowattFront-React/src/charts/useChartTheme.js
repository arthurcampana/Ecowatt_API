import { useEffect } from "react";
import { Chart } from "chart.js";
import { useTheme } from "../context/ThemeContext.jsx";

// Ajusta os defaults globais do Chart.js (cor dos textos dos eixos/legendas e
// cor das linhas de grade) conforme o tema claro/escuro. Retorna o tema atual
// para ser usado como `key` nos componentes de grafico, forcando a remontagem
// quando o tema muda (os defaults so sao lidos na criacao do grafico).
export function useChartTheme() {
  const { tema } = useTheme();

  useEffect(() => {
    const escuro = tema === "dark";
    Chart.defaults.color = escuro ? "#94a3b8" : "#64748b";
    Chart.defaults.borderColor = escuro
      ? "rgba(148, 163, 184, 0.15)"
      : "rgba(100, 116, 139, 0.15)";
  }, [tema]);

  return tema;
}
