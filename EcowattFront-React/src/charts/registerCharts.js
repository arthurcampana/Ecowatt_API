// Modulo de efeito colateral: registra os componentes do Chart.js usados pelas
// telas de graficos (barras, linha e doughnut). Deve ser importado uma unica
// vez pelas paginas que renderizam graficos.
import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  ArcElement,
  Tooltip,
  Legend,
} from "chart.js";

Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  ArcElement,
  Tooltip,
  Legend
);
