import { useEffect, useMemo, useState } from "react";
import { Bar } from "react-chartjs-2";
import "../charts/registerCharts.js";
import { metricasService } from "../api/metricasService.js";
import Icon from "../components/Icon.jsx";

// Tela de analytics de uso (rota /analytics): mostra onde os usuarios mais
// usam o sistema — mapa de calor de acessos (dia x hora) e rankings de
// endpoints e de cliques. Os dados vem da API (/analytics/*).

const DIAS = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];

// O backend usa EXTRACT(DOW) do PostgreSQL: 0=domingo ... 6=sabado.
function nomeDia(diaSemana) {
  return DIAS[diaSemana] ?? String(diaSemana);
}

export default function Analytics() {
  const [mapa, setMapa] = useState([]);
  const [endpoints, setEndpoints] = useState([]);
  const [cliques, setCliques] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      try {
        const [m, e, c] = await Promise.all([
          metricasService.mapaCalor(),
          metricasService.endpointsMaisUsados(),
          metricasService.cliquesMaisUsados(),
        ]);
        if (!ativo) return;
        setMapa(m || []);
        setEndpoints(e || []);
        setCliques(c || []);
      } catch (err) {
        if (ativo) setErro(err?.message || "Erro ao carregar analytics");
      } finally {
        if (ativo) setCarregando(false);
      }
    }
    carregar();
    return () => {
      ativo = false;
    };
  }, []);

  // Indice [dia-hora] -> total e o maximo, para pintar o mapa de calor.
  const { grade, maxTotal, totalAcessos, pico } = useMemo(() => {
    const g = {};
    let max = 0;
    let soma = 0;
    let p = { label: "-", v: -1 };
    for (const c of mapa) {
      g[`${c.diaSemana}-${c.hora}`] = c.total;
      soma += c.total;
      if (c.total > max) max = c.total;
      if (c.total > p.v) p = { label: `${nomeDia(c.diaSemana)} ${c.hora}h`, v: c.total };
    }
    return { grade: g, maxTotal: max, totalAcessos: soma, pico: p };
  }, [mapa]);

  function corCelula(total) {
    const t = maxTotal ? total / maxTotal : 0;
    const alpha = total ? 0.15 + t * 0.85 : 0.05;
    return `rgba(25, 135, 84, ${alpha})`; // verde EcoWatt
  }

  // --- Exportação CSV ---------------------------------------------------

  // Escapa um valor para CSV (aspas quando há vírgula/aspas/quebra de linha).
  function campoCsv(valor) {
    const s = String(valor ?? "");
    if (/[",\n;]/.test(s)) {
      return `"${s.replace(/"/g, '""')}"`;
    }
    return s;
  }

  // Monta o conteúdo CSV a partir de cabeçalhos + linhas e dispara o download.
  function baixarCsv(nomeArquivo, cabecalhos, linhas) {
    const sep = ";"; // ponto-e-vírgula abre melhor no Excel pt-BR
    const conteudo = [
      cabecalhos.map(campoCsv).join(sep),
      ...linhas.map((l) => l.map(campoCsv).join(sep)),
    ].join("\r\n");

    // BOM para o Excel reconhecer acentos (UTF-8).
    const blob = new Blob(["\uFEFF" + conteudo], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = nomeArquivo;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  function exportarMapaCalor() {
    baixarCsv(
      "mapa-calor-acessos.csv",
      ["dia_semana_num", "dia_semana", "hora", "total"],
      mapa.map((c) => [c.diaSemana, nomeDia(c.diaSemana), c.hora, c.total])
    );
  }

  function exportarEndpoints() {
    baixarCsv(
      "endpoints-mais-usados.csv",
      ["metodo", "endpoint", "total"],
      endpoints.map((e) => [e.metodo, e.endpoint, e.total])
    );
  }

  function exportarCliques() {
    baixarCsv(
      "cliques-mais-usados.csv",
      ["rotulo", "total"],
      cliques.map((c) => [c.rotulo, c.total])
    );
  }

  // Grafico de endpoints (todos os monitorados).
  const dadosEndpoints = useMemo(() => {
    return {
      labels: endpoints.map((e) => `${e.metodo} ${e.endpoint}`),
      datasets: [
        {
          label: "Acessos",
          data: endpoints.map((e) => e.total),
          backgroundColor: "#198754",
          borderRadius: 6,
        },
      ],
    };
  }, [endpoints]);

  // Grafico de cliques (todos os rotulos monitorados).
  const dadosCliques = useMemo(() => {
    return {
      labels: cliques.map((c) => c.rotulo),
      datasets: [
        {
          label: "Cliques",
          data: cliques.map((c) => c.total),
          backgroundColor: "#0d6efd",
          borderRadius: 6,
        },
      ],
    };
  }, [cliques]);

  // Altura dinamica: ~34px por barra, para todas caberem sem espremer.
  function alturaGrafico(n) {
    return Math.max(220, n * 34 + 60);
  }

  const opcoesBarraH = {
    indexAxis: "y",
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { x: { beginAtZero: true } },
  };

  return (
    <div>
      <div className="page-header">
        <h2>Analytics de Uso</h2>
        <p>Onde e quando os usuários mais usam o sistema.</p>
      </div>

      {erro && <div className="erro">{erro}</div>}

      {carregando ? (
        <div className="relatorio-placeholder">
          <div className="relatorio-placeholder-ico">
            <Icon name="chart" size={32} />
          </div>
          <h4>Carregando analytics…</h4>
        </div>
      ) : (
        <>
          {/* Cards de resumo */}
          <div className="rel-cards">
            <div className="rel-card verde">
              <div className="rel-card-ico">
                <Icon name="trending" />
              </div>
              <h6>Acessos registrados</h6>
              <h3>{totalAcessos}</h3>
            </div>
            <div className="rel-card azul">
              <div className="rel-card-ico">
                <Icon name="calendar" />
              </div>
              <h6>Horário de pico</h6>
              <h3>{pico.v > 0 ? pico.label : "-"}</h3>
            </div>
            <div className="rel-card verde">
              <div className="rel-card-ico">
                <Icon name="bolt" />
              </div>
              <h6>Endpoints monitorados</h6>
              <h3>{endpoints.length}</h3>
            </div>
            <div className="rel-card azul">
              <div className="rel-card-ico">
                <Icon name="plug" />
              </div>
              <h6>Tipos de clique</h6>
              <h3>{cliques.length}</h3>
            </div>
          </div>

          {/* Mapa de calor */}
          <div className="chart-card">
            <div className="chart-card-header">
              <h5>Mapa de calor de acessos</h5>
              <button
                type="button"
                className="btn-save"
                onClick={exportarMapaCalor}
                disabled={mapa.length === 0}
              >
                Exportar CSV
              </button>
            </div>
            {mapa.length === 0 ? (
              <p className="aviso">
                Sem dados ainda. Use o sistema (ou rode /demo/popular) para gerar eventos.
              </p>
            ) : (
              <div style={{ overflowX: "auto" }}>
                <table
                  style={{ borderCollapse: "collapse", fontSize: 11, margin: "0 auto" }}
                >
                  <thead>
                    <tr>
                      <th />
                      {Array.from({ length: 24 }, (_, h) => (
                        <th key={h} style={{ padding: 2, opacity: 0.6, fontWeight: 500 }}>
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {DIAS.map((dia, d) => (
                      <tr key={d}>
                        <td style={{ paddingRight: 8, fontWeight: 600, whiteSpace: "nowrap" }}>
                          {dia}
                        </td>
                        {Array.from({ length: 24 }, (_, h) => {
                          const total = grade[`${d}-${h}`] || 0;
                          return (
                            <td key={h} style={{ padding: 2 }}>
                              <div
                                title={`${dia} ${h}h — ${total} acessos`}
                                style={{
                                  width: 22,
                                  height: 22,
                                  borderRadius: 4,
                                  background: corCelula(total),
                                }}
                              />
                            </td>
                          );
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Ranking de endpoints */}
          <div className="chart-card">
            <div className="chart-card-header">
              <h5>Endpoints mais usados ({endpoints.length})</h5>
              <button
                type="button"
                className="btn-save"
                onClick={exportarEndpoints}
                disabled={endpoints.length === 0}
              >
                Exportar CSV
              </button>
            </div>
            {endpoints.length === 0 ? (
              <p className="aviso">Sem acessos registrados.</p>
            ) : (
              <div style={{ height: alturaGrafico(endpoints.length) }}>
                <Bar data={dadosEndpoints} options={opcoesBarraH} />
              </div>
            )}
          </div>

          {/* Ranking de cliques */}
          <div className="chart-card">
            <div className="chart-card-header">
              <h5>Cliques mais usados ({cliques.length})</h5>
              <button
                type="button"
                className="btn-save"
                onClick={exportarCliques}
                disabled={cliques.length === 0}
              >
                Exportar CSV
              </button>
            </div>
            {cliques.length === 0 ? (
              <p className="aviso">Sem cliques registrados.</p>
            ) : (
              <div style={{ height: alturaGrafico(cliques.length) }}>
                <Bar data={dadosCliques} options={opcoesBarraH} />
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
