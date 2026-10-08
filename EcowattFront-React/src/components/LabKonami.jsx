import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { equipamentoService } from "../api/equipamentoService.js";
import { equipamentoUsuarioService } from "../api/equipamentoUsuarioService.js";
import { configuracaoService } from "../api/configuracaoService.js";

// Segundo atalho secreto (cenario "laboratorio/sala de aula"), acionado pela
// combinacao Ctrl+Shift+F. Cria 4 equipamentos agregados no catalogo, os
// vincula ao usuario logado e configura meta/tarifa com base em Florianopolis
// (Saco Grande), concessionaria Celesc. Usa os services do front.
const HORAS_POR_DIA = 4;

// Tarifa residencial B1 da Celesc (~R$ 0,76/kWh, vigente em 2026) e meta mensal
// com folga sobre o consumo esperado do laboratorio (~654 kWh/mes em ~22 dias).
const TARIFA_CELESC = 0.76;
const META_MENSAL = 700;

// Equipamentos agregados. consumoPorHora em kWh/h = (potencia_W * qtd) / 1000.
const LAB = [
  {
    nome: "36 Computadores",
    modelo: "Desktop 90.3 W",
    consumoPorHora: (90.3 * 36) / 1000, // 3.2508
  },
  {
    nome: "36 Monitores",
    modelo: "Monitor 100 W",
    consumoPorHora: (100 * 36) / 1000, // 3.6
  },
  {
    nome: "1 Projetor",
    modelo: "Projetor 290 W (média 280–300)",
    consumoPorHora: (290 * 1) / 1000, // 0.29
  },
  {
    nome: "16 Lâmpadas",
    modelo: "Lâmpada 18 W",
    consumoPorHora: (18 * 16) / 1000, // 0.288
  },
];

export default function LabKonami() {
  const { usuario } = useAuth();
  const [toast, setToast] = useState(null); // { texto, tipo }

  useEffect(() => {
    let ocupado = false;

    async function popularLab() {
      if (ocupado) {
        return;
      }
      ocupado = true;
      setToast({ texto: "Gerando dados do laboratório...", tipo: "info" });

      try {
        // Catalogo atual, para reaproveitar equipamentos base ja existentes
        // (evita duplicar caso o atalho seja usado mais de uma vez).
        const catalogo = (await equipamentoService.listar()) || [];

        for (const item of LAB) {
          let base = catalogo.find((e) => e.nome === item.nome);
          if (!base) {
            base = await equipamentoService.adicionar({
              nome: item.nome,
              modelo: item.modelo,
              consumoPorHora: item.consumoPorHora,
            });
          }

          await equipamentoUsuarioService.adicionar({
            nomeIdentificacao: item.nome,
            horasPorDia: HORAS_POR_DIA,
            consumoEsperado: Number(
              (item.consumoPorHora * HORAS_POR_DIA).toFixed(4)
            ),
            usuarioId: usuario.id,
            equipamentoId: base.id,
          });
        }

        // Configura meta e tarifa (Celesc / Florianopolis - Saco Grande).
        // Cria se ainda nao existe (404), ou atualiza a existente.
        const payloadConfig = {
          valorTarifa: TARIFA_CELESC,
          meta: META_MENSAL,
          unidadeMedida: "kWh",
        };
        try {
          const atual = await configuracaoService.buscarPorUsuario(usuario.id);
          await configuracaoService.alterar(atual.id, payloadConfig);
        } catch (error) {
          if (error?.status === 404) {
            await configuracaoService.adicionar({
              ...payloadConfig,
              usuarioId: usuario.id,
            });
          } else {
            throw error;
          }
        }

        setToast({
          texto: "Laboratório e configuração criados! Atualizando...",
          tipo: "sucesso",
        });
        setTimeout(() => window.location.reload(), 1200);
      } catch (error) {
        setToast({
          texto: error?.message || "Erro ao gerar dados do laboratório.",
          tipo: "erro",
        });
        ocupado = false;
        setTimeout(() => setToast(null), 4000);
      }
    }

    function handleKeyDown(event) {
      // Ctrl + Shift + F
      if (
        event.ctrlKey &&
        event.shiftKey &&
        (event.key === "F" || event.key === "f")
      ) {
        event.preventDefault();
        popularLab();
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
