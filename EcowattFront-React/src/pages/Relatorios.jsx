import { useEffect, useMemo, useRef, useState } from "react";
import { Bar, Doughnut, Line } from "react-chartjs-2";
import { jsPDF } from "jspdf";
import autoTable from "jspdf-autotable";
import "../charts/registerCharts.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useUI } from "../context/UIContext.jsx";
import { consumoService } from "../api/consumoService.js";
import { configuracaoService } from "../api/configuracaoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";
import { formatarData } from "../utils/formato.js";
import { CORES } from "../utils/chartPalette.js";
import { useChartTheme } from "../charts/useChartTheme.js";
import LegendaEquipamentos from "../components/LegendaEquipamentos.jsx";
import Icon from "../components/Icon.jsx";

// Formata uma data para o input date (YYYY-MM-DD) no fuso local.
function paraInput(d) {
  const ano = d.getFullYear();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

// Tela de relatorios (rota /relatorios): filtra os consumos por intervalo de
// datas e mostra cards, tabela, um grafico de barras/linha alternavel, os dois
// graficos de equipamentos (doughnut + custo estimado) e exportacao em PDF via
// html2canvas + jsPDF. Portado do legado relatorios.js, usando sempre a lista
// unica de consumos carregada (o legado tinha uma mistura consumos/todosConsumos).
export default function Relatorios() {
  const { usuario } = useAuth();
  const { toast } = useUI();
  const temaChart = useChartTheme();

  const [consumos, setConsumos] = useState([]);
  const [config, setConfig] = useState({ meta: 0, valorTarifa: 0 });
  const [equipamentos, setEquipamentos] = useState([]);

  const [dataInicial, setDataInicial] = useState("");
  const [dataFinal, setDataFinal] = useState("");
  const [tipoGrafico, setTipoGrafico] = useState("bar"); // 'bar' | 'line'
  const [painelVisivel, setPainelVisivel] = useState(false);
  const [consumosFiltrados, setConsumosFiltrados] = useState([]);
  const [exportando, setExportando] = useState(false);

  // Quais graficos o usuario quer ver na tela e no PDF.
  const [graficosAtivos, setGraficosAtivos] = useState({
    evolucao: true,
    equipamentos: true,
    custo: true,
  });

  function alternarGrafico(chave) {
    setGraficosAtivos((atual) => ({ ...atual, [chave]: !atual[chave] }));
  }

  const [mensagem, setMensagem] = useState(null); // { texto, tipo }

  const painelRef = useRef(null);

  // Refs para os componentes de grafico (react-chartjs-2 expoe o canvas em
  // ref.current.canvas), usados para embutir as imagens no PDF nativo.
  const refGraficoPrincipal = useRef(null);
  const refDoughnut = useRef(null);
  const refCusto = useRef(null);

  useEffect(() => {
    async function carregar() {
      try {
        const cfg = await configuracaoService.buscarPorUsuario(usuario.id);
        setConfig(cfg);
      } catch (error) {
        if (error?.status === 404) {
          setConfig({ meta: 0, valorTarifa: 0 });
        } else {
          setMensagem({
            texto: error?.message || "Erro ao carregar configuração",
            tipo: "erro",
          });
        }
      }

      try {
        const dados = await consumoService.listarPorUsuario(usuario.id);
        setConsumos(dados || []);
      } catch (error) {
        setMensagem({
          texto: error?.message || "Erro ao carregar consumos",
          tipo: "erro",
        });
      }

      try {
        const equips = await equipamentoUsuarioService.listarPorUsuario(
          usuario.id
        );
        setEquipamentos(equips || []);
      } catch (error) {
        setMensagem({
          texto: error?.message || "Erro ao carregar equipamentos",
          tipo: "erro",
        });
      }
    }

    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [usuario.id]);

  // Preenche o intervalo de datas a partir de um atalho rapido.
  function aplicarAtalho(tipo) {
    const hoje = new Date();
    let inicio = new Date();
    let fim = new Date();

    if (tipo === "7dias") {
      inicio.setDate(hoje.getDate() - 6);
    } else if (tipo === "mes") {
      inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
      fim = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);
    } else if (tipo === "mesPassado") {
      inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
      fim = new Date(hoje.getFullYear(), hoje.getMonth(), 0);
    } else if (tipo === "ano") {
      inicio = new Date(hoje.getFullYear(), 0, 1);
      fim = new Date(hoje.getFullYear(), 11, 31);
    }

    setDataInicial(paraInput(inicio));
    setDataFinal(paraInput(fim));
  }

  function gerarRelatorio() {
    // Validacao: data final nao pode ser anterior a inicial.
    if (dataInicial && dataFinal && new Date(dataFinal) < new Date(dataInicial)) {
      toast("A data final não pode ser anterior à inicial.", "erro");
      return;
    }

    const filtrados = consumos.filter((item) => {
      const data = new Date(item.dataRegistro);

      if (dataInicial) {
        if (data < new Date(dataInicial)) {
          return false;
        }
      }

      if (dataFinal) {
        const fim = new Date(dataFinal);
        fim.setHours(23, 59, 59, 999);
        if (data > fim) {
          return false;
        }
      }

      return true;
    });

    setConsumosFiltrados(filtrados);
    setPainelVisivel(true);

    if (filtrados.length === 0) {
      toast("Nenhum consumo encontrado no período selecionado.", "info");
    }
  }

  function trocarGrafico() {
    setTipoGrafico((prev) => (prev === "bar" ? "line" : "bar"));
  }

  // Cards do painel.
  const cards = useMemo(() => {
    const total = consumosFiltrados.reduce(
      (soma, item) => soma + Number(item.consumoKwh),
      0
    );
    const len = consumosFiltrados.length;
    const media = len ? total / len : 0;
    const maior = len
      ? Math.max(...consumosFiltrados.map((c) => Number(c.consumoKwh)))
      : 0;
    const menor = len
      ? Math.min(...consumosFiltrados.map((c) => Number(c.consumoKwh)))
      : 0;
    const custo = total * Number(config.valorTarifa);
    const statusMeta =
      media <= config.meta
        ? { texto: "Dentro da Meta", cls: "status-ok" }
        : { texto: "Meta Ultrapassada", cls: "status-alerta" };

    return {
      consumoTotal: total.toFixed(2) + " kWh",
      consumoMedio: media.toFixed(2) + " kWh",
      maiorConsumo: maior.toFixed(2) + " kWh",
      menorConsumo: menor.toFixed(2) + " kWh",
      custoTotal: "R$ " + custo.toFixed(2),
      metaAtual: config.meta + " kWh",
      statusMeta,
    };
  }, [consumosFiltrados, config]);

  // Tabela: ordenada de forma crescente por data.
  const tabela = useMemo(
    () =>
      [...consumosFiltrados].sort(
        (a, b) => new Date(a.dataRegistro) - new Date(b.dataRegistro)
      ),
    [consumosFiltrados]
  );

  // Grafico principal: agrupa por dia ou por mes quando o intervalo passa de
  // 31 dias.
  const graficoPrincipal = useMemo(() => {
    const agruparPorMes =
      dataInicial &&
      dataFinal &&
      (new Date(dataFinal) - new Date(dataInicial)) / 86400000 > 31;

    const mapa = {};
    consumosFiltrados.forEach((c) => {
      const chave = agruparPorMes
        ? new Date(c.dataRegistro).toLocaleDateString("pt-BR", {
            month: "short",
            year: "numeric",
          })
        : formatarData(c.dataRegistro);
      mapa[chave] = (mapa[chave] || 0) + Number(c.consumoKwh);
    });

    return {
      labels: Object.keys(mapa),
      datasets: [
        {
          label: "Consumo (kWh)",
          data: Object.values(mapa),
          backgroundColor: "#198754",
          borderColor: "#198754",
          borderWidth: 2,
          borderRadius: 8,
          fill: false,
        },
      ],
    };
  }, [consumosFiltrados, dataInicial, dataFinal]);

  const opcoesGraficoPrincipal = {
    responsive: true,
    maintainAspectRatio: false,
  };

  // Equipamentos: doughnut do consumo esperado.
  const labelsEquip = equipamentos.map((e) => e.nomeIdentificacao);
  const valoresEquip = equipamentos.map((e) => Number(e.consumoEsperado));

  const dadosDoughnut = {
    labels: labelsEquip,
    datasets: [
      {
        data: valoresEquip,
        backgroundColor: CORES,
        borderColor: "#fff",
        borderWidth: 3,
      },
    ],
  };

  const opcoesDoughnut = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
  };

  // Equipamentos: custo estimado por equipamento.
  const graficoCusto = useMemo(() => {
    const somaEsperado = equipamentos.reduce(
      (soma, e) => soma + Number(e.consumoEsperado),
      0
    );
    const consumoTotalRef =
      consumosFiltrados.length > 0
        ? Number(consumosFiltrados[consumosFiltrados.length - 1].consumoKwh)
        : 0;
    const tarifa = Number(config.valorTarifa);

    const custos = equipamentos.map((e) => {
      const percentual = somaEsperado
        ? Number(e.consumoEsperado) / somaEsperado
        : 0;
      const consumoEstimado = consumoTotalRef * percentual;
      return consumoEstimado * tarifa;
    });

    return {
      labels: equipamentos.map((e) => e.nomeIdentificacao),
      datasets: [
        {
          label: "Custo estimado (R$)",
          data: custos,
          backgroundColor: "#0d6efd",
          borderWidth: 2,
          borderRadius: 8,
        },
      ],
    };
  }, [equipamentos, consumosFiltrados, config]);

  const opcoesGraficoCusto = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      tooltip: {
        callbacks: {
          label: (ctx) => "R$ " + ctx.raw.toFixed(2),
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (v) => "R$ " + v,
        },
      },
    },
  };

  // Gera um PDF nativo (texto selecionavel + graficos em alta resolucao),
  // com cabecalho da marca, cards de indicadores, graficos, tabela e rodape
  // paginado. Substitui a antiga "foto" via html2canvas.
  // Forca o texto/grade de um grafico para cores claras (para o PDF, que tem
  // fundo branco), captura a imagem e restaura o estado anterior.
  function capturarGraficoClaro(chart) {
    if (!chart) {
      return null;
    }
    const corTextoOriginal = chart.options.color;
    const corGradeOriginal = chart.options.scales?.x?.grid?.color;

    chart.options.color = "#334155";
    ["x", "y"].forEach((eixo) => {
      if (chart.options.scales?.[eixo]) {
        chart.options.scales[eixo].ticks = {
          ...chart.options.scales[eixo].ticks,
          color: "#334155",
        };
        chart.options.scales[eixo].grid = {
          ...chart.options.scales[eixo].grid,
          color: "rgba(100,116,139,0.15)",
        };
      }
    });
    chart.update("none");

    const img = chart.canvas.toDataURL("image/png", 1.0);

    chart.options.color = corTextoOriginal;
    if (corGradeOriginal !== undefined && chart.options.scales?.x?.grid) {
      chart.options.scales.x.grid.color = corGradeOriginal;
    }
    chart.update("none");

    return { img, w: chart.canvas.width, h: chart.canvas.height };
  }

  function exportarPDF() {
    if (consumosFiltrados.length === 0) {
      toast("Gere um relatório antes de exportar.", "info");
      return;
    }

    setExportando(true);

    const VERDE = [21, 128, 61];
    const CINZA = [100, 116, 139];
    const ESCURO = [15, 23, 42];

    const doc = new jsPDF("p", "mm", "a4");
    const larguraPagina = doc.internal.pageSize.getWidth();
    const alturaPagina = doc.internal.pageSize.getHeight();
    const margem = 14;
    const larguraUtil = larguraPagina - margem * 2;

    // ---------- Cabecalho ----------
    doc.setFillColor(...VERDE);
    doc.rect(0, 0, larguraPagina, 30, "F");
    doc.setTextColor(255, 255, 255);
    doc.setFont("helvetica", "bold");
    doc.setFontSize(18);
    doc.text("EcoWatt", margem, 13);
    doc.setFont("helvetica", "normal");
    doc.setFontSize(12);
    doc.text("Relatório de Consumo", margem, 21);

    const periodo =
      dataInicial || dataFinal
        ? `${dataInicial ? formatarData(dataInicial) : "início"} até ${
            dataFinal ? formatarData(dataFinal) : "hoje"
          }`
        : "Todo o período";
    doc.setFontSize(9);
    doc.text(`Período: ${periodo}`, larguraPagina - margem, 13, {
      align: "right",
    });
    doc.text(
      `Gerado em: ${new Date().toLocaleDateString("pt-BR")}`,
      larguraPagina - margem,
      20,
      { align: "right" }
    );
    if (usuario?.nome) {
      doc.text(`Usuário: ${usuario.nome}`, larguraPagina - margem, 27, {
        align: "right",
      });
    }

    let y = 42;

    // ---------- Cards de indicadores ----------
    const indicadores = [
      { label: "Consumo Total", valor: cards.consumoTotal },
      { label: "Consumo Médio", valor: cards.consumoMedio },
      { label: "Maior Consumo", valor: cards.maiorConsumo },
      { label: "Menor Consumo", valor: cards.menorConsumo },
      { label: "Custo Estimado", valor: cards.custoTotal },
      { label: "Meta", valor: cards.metaAtual },
      { label: "Status", valor: cards.statusMeta.texto },
    ];

    const colunas = 4;
    const espaco = 4;
    const larguraCard = (larguraUtil - espaco * (colunas - 1)) / colunas;
    const alturaCard = 20;

    indicadores.forEach((ind, i) => {
      const col = i % colunas;
      const linha = Math.floor(i / colunas);
      const x = margem + col * (larguraCard + espaco);
      const cardY = y + linha * (alturaCard + espaco);

      doc.setDrawColor(226, 232, 240);
      doc.setFillColor(248, 250, 252);
      doc.roundedRect(x, cardY, larguraCard, alturaCard, 2, 2, "FD");

      doc.setTextColor(...CINZA);
      doc.setFont("helvetica", "normal");
      doc.setFontSize(7.5);
      doc.text(ind.label, x + 4, cardY + 7);

      const ehStatus = ind.label === "Status";
      if (ehStatus) {
        const ok = cards.statusMeta.cls === "status-ok";
        doc.setTextColor(...(ok ? VERDE : [220, 38, 38]));
      } else {
        doc.setTextColor(...ESCURO);
      }
      doc.setFont("helvetica", "bold");
      doc.setFontSize(ehStatus ? 9 : 12);
      doc.text(String(ind.valor), x + 4, cardY + 15);
    });

    const linhasCards = Math.ceil(indicadores.length / colunas);
    y += linhasCards * (alturaCard + espaco) + 6;

    // ---------- Helper para embutir um grafico ----------
    function addGrafico(ref, titulo) {
      const captura = capturarGraficoClaro(ref.current);
      if (!captura) {
        return;
      }
      const { img, w: propW, h: propH } = captura;
      const imgW = larguraUtil;
      const imgH = (propH * imgW) / propW;

      // Nova pagina se nao couber (titulo + grafico).
      if (y + 10 + imgH > alturaPagina - 18) {
        doc.addPage();
        y = 20;
      }

      doc.setTextColor(...ESCURO);
      doc.setFont("helvetica", "bold");
      doc.setFontSize(12);
      doc.text(titulo, margem, y);
      y += 5;

      doc.addImage(img, "PNG", margem, y, imgW, imgH);
      y += imgH + 8;
    }

    if (graficosAtivos.evolucao) {
      addGrafico(refGraficoPrincipal, "Evolução do Consumo");
    }
    if (equipamentos.length > 0) {
      if (graficosAtivos.equipamentos) {
        addGrafico(refDoughnut, "Consumo por Equipamento");
      }
      if (graficosAtivos.custo) {
        addGrafico(refCusto, "Estimativa de Custo por Equipamento");
      }
    }

    // ---------- Tabela (histórico) ----------
    autoTable(doc, {
      startY: y,
      head: [["Data", "Consumo (kWh)"]],
      body: tabela.map((item) => [
        formatarData(item.dataRegistro),
        Number(item.consumoKwh).toFixed(2) + " kWh",
      ]),
      margin: { left: margem, right: margem },
      styles: { fontSize: 9, cellPadding: 2.5 },
      headStyles: { fillColor: VERDE, textColor: 255, fontStyle: "bold" },
      alternateRowStyles: { fillColor: [248, 250, 252] },
      didDrawPage: () => {
        // Rodape com numero de pagina em todas as paginas.
        const pagina = doc.internal.getNumberOfPages();
        doc.setFontSize(8);
        doc.setTextColor(...CINZA);
        doc.text(
          `EcoWatt • Relatório de Consumo`,
          margem,
          alturaPagina - 8
        );
        doc.text(
          `Página ${pagina}`,
          larguraPagina - margem,
          alturaPagina - 8,
          { align: "right" }
        );
      },
    });

    doc.save("Relatorio_EcoWatt.pdf");
    toast("Relatório exportado em PDF.", "sucesso");
    setExportando(false);
  }

  return (
    <div>
      <div className="page-header">
        <h2>Relatórios</h2>
        <p>Gere relatórios de consumo por período e exporte em PDF.</p>
      </div>

      {mensagem && <div className={mensagem.tipo}>{mensagem.texto}</div>}

      <div className="config-card filtro-card">
        <h5>Relatório de Consumo</h5>

        <div className="filtro-grid">
          <div className="campo">
            <label className="form-label" htmlFor="dataInicial">
              Data Inicial
            </label>
            <input
              id="dataInicial"
              type="date"
              className="form-control"
              value={dataInicial}
              onChange={(e) => setDataInicial(e.target.value)}
            />
          </div>

          <div className="campo">
            <label className="form-label" htmlFor="dataFinal">
              Data Final
            </label>
            <input
              id="dataFinal"
              type="date"
              className="form-control"
              value={dataFinal}
              onChange={(e) => setDataFinal(e.target.value)}
            />
          </div>
        </div>

        <div className="atalhos-periodo">
          <span className="atalhos-label">Atalhos:</span>
          <button type="button" onClick={() => aplicarAtalho("7dias")}>
            Últimos 7 dias
          </button>
          <button type="button" onClick={() => aplicarAtalho("mes")}>
            Este mês
          </button>
          <button type="button" onClick={() => aplicarAtalho("mesPassado")}>
            Mês passado
          </button>
          <button type="button" onClick={() => aplicarAtalho("ano")}>
            Este ano
          </button>
        </div>

        <div className="relatorio-acoes">
          <button type="button" className="btn-add" onClick={gerarRelatorio}>
            Gerar Relatório
          </button>
          <button
            type="button"
            className="btn-save"
            onClick={exportarPDF}
            disabled={exportando || !painelVisivel || consumosFiltrados.length === 0}
          >
            {exportando ? "Exportando..." : "Exportar PDF"}
          </button>
        </div>
      </div>

      {/* Seletor: quais graficos incluir (tela + PDF) */}
      {painelVisivel && consumosFiltrados.length > 0 && (
        <div className="config-card seletor-graficos">
          <h5>Personalizar relatório</h5>
          <p className="helper-text">Escolha quais gráficos incluir na tela e no PDF.</p>
          <div className="seletor-opcoes">
            <label className="check-grafico">
              <input
                type="checkbox"
                checked={graficosAtivos.evolucao}
                onChange={() => alternarGrafico("evolucao")}
              />
              <span>Evolução do consumo</span>
            </label>
            <label className="check-grafico">
              <input
                type="checkbox"
                checked={graficosAtivos.equipamentos}
                onChange={() => alternarGrafico("equipamentos")}
              />
              <span>Consumo por equipamento</span>
            </label>
            <label className="check-grafico">
              <input
                type="checkbox"
                checked={graficosAtivos.custo}
                onChange={() => alternarGrafico("custo")}
              />
              <span>Custo por equipamento</span>
            </label>
          </div>
        </div>
      )}

      {/* Estado inicial: antes de gerar */}
      {!painelVisivel && (
        <div className="relatorio-placeholder">
          <div className="relatorio-placeholder-ico">
            <Icon name="chart" size={32} />
          </div>
          <h4>Nenhum relatório gerado</h4>
          <p>
            Selecione um período (ou use um atalho) e clique em “Gerar
            Relatório” para visualizar seus indicadores e gráficos.
          </p>
        </div>
      )}

      {/* Painel gerado, mas sem dados no periodo */}
      {painelVisivel && consumosFiltrados.length === 0 && (
        <div className="relatorio-placeholder">
          <div className="relatorio-placeholder-ico">
            <Icon name="inbox" size={32} />
          </div>
          <h4>Nenhum consumo no período</h4>
          <p>Não há registros de consumo no intervalo selecionado. Tente outro período.</p>
        </div>
      )}

      {painelVisivel && consumosFiltrados.length > 0 && (
        <div ref={painelRef} className="painel-relatorio">
          <div className="rel-cards">
            <div className="rel-card verde">
              <div className="rel-card-ico">
                <Icon name="bolt" />
              </div>
              <h6>Consumo Total</h6>
              <h3>{cards.consumoTotal}</h3>
            </div>
            <div className="rel-card azul">
              <div className="rel-card-ico">
                <Icon name="trending" />
              </div>
              <h6>Consumo Médio</h6>
              <h3>{cards.consumoMedio}</h3>
            </div>
            <div className="rel-card verde">
              <div className="rel-card-ico">
                <Icon name="battery" />
              </div>
              <h6>Maior Consumo</h6>
              <h3>{cards.maiorConsumo}</h3>
            </div>
            <div className="rel-card azul">
              <div className="rel-card-ico">
                <Icon name="battery" />
              </div>
              <h6>Menor Consumo</h6>
              <h3>{cards.menorConsumo}</h3>
            </div>
            <div className="rel-card ambar">
              <div className="rel-card-ico">
                <Icon name="info" />
              </div>
              <h6>Custo Estimado</h6>
              <h3>{cards.custoTotal}</h3>
            </div>
            <div className="rel-card ambar">
              <div className="rel-card-ico">
                <Icon name="calendar" />
              </div>
              <h6>Meta</h6>
              <h3>{cards.metaAtual}</h3>
            </div>
            <div className="rel-card status">
              <div className="rel-card-ico">
                <Icon name={cards.statusMeta.cls === "status-ok" ? "check-circle" : "alert-circle"} />
              </div>
              <h6>Status</h6>
              <h3 className={cards.statusMeta.cls}>{cards.statusMeta.texto}</h3>
            </div>
          </div>

          {graficosAtivos.evolucao && (
            <div className="chart-card">
              <div className="chart-card-header">
                <h5>Evolução do Consumo</h5>
                <div className="tipo-grafico-toggle">
                  <button
                    type="button"
                    className={tipoGrafico === "bar" ? "ativo" : ""}
                    onClick={() => setTipoGrafico("bar")}
                  >
                    Barras
                  </button>
                  <button
                    type="button"
                    className={tipoGrafico === "line" ? "ativo" : ""}
                    onClick={() => setTipoGrafico("line")}
                  >
                    Linha
                  </button>
                </div>
              </div>
              <div className="chart-container">
                {tipoGrafico === "bar" ? (
                  <Bar
                    key={`bar-${temaChart}`}
                    ref={refGraficoPrincipal}
                    data={graficoPrincipal}
                    options={opcoesGraficoPrincipal}
                  />
                ) : (
                  <Line
                    key={`line-${temaChart}`}
                    ref={refGraficoPrincipal}
                    data={graficoPrincipal}
                    options={opcoesGraficoPrincipal}
                  />
                )}
              </div>
            </div>
          )}

          <div className="chart-card">
            <h5>Histórico</h5>
            <div className="tabela-relatorio">
              <table className="table">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Consumo (kWh)</th>
                  </tr>
                </thead>
                <tbody>
                  {tabela.map((item) => (
                    <tr key={item.id}>
                      <td>{formatarData(item.dataRegistro)}</td>
                      <td>{Number(item.consumoKwh).toFixed(2)} kWh</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {graficosAtivos.equipamentos && (
            <div className="chart-card">
              <h5>Consumo por Equipamentos</h5>
              {equipamentos.length === 0 ? (
                <p className="aviso">Nenhum equipamento cadastrado.</p>
              ) : (
                <>
                  <div className="chart-container">
                    <Doughnut
                      key={`dough-${temaChart}`}
                      ref={refDoughnut}
                      data={dadosDoughnut}
                      options={opcoesDoughnut}
                    />
                  </div>
                  <LegendaEquipamentos
                    labels={labelsEquip}
                    valores={valoresEquip}
                    cores={CORES}
                  />
                </>
              )}
            </div>
          )}

          {graficosAtivos.custo && (
            <div className="chart-card">
              <h5>Estimativa de custo por equipamento</h5>
              {equipamentos.length === 0 ? (
                <p className="aviso">Nenhum equipamento cadastrado.</p>
              ) : (
                <div className="chart-container">
                  <Bar
                    key={`custo-${temaChart}`}
                    ref={refCusto}
                    data={graficoCusto}
                    options={opcoesGraficoCusto}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
