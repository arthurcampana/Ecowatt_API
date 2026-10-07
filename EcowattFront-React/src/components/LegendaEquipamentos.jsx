// Legenda customizada dos graficos de equipamentos, portada do markup legado
// (index.js / relatorios.js). Lista cada equipamento com a cor usada no
// grafico, o nome e o consumo esperado em kWh/dia.
export default function LegendaEquipamentos({ labels, valores, cores }) {
  return (
    <div className="legenda-wrapper">
      {labels.map((label, index) => (
        <div className="legenda-item" key={index}>
          <div className="legenda-left">
            <div
              className="legenda-cor"
              style={{ background: cores[index] }}
            />
            <div>
              <div className="legenda-titulo">{label}</div>
              <div className="legenda-sub">Equipamento registrado</div>
            </div>
          </div>
          <div className="legenda-valor">
            {valores[index].toFixed(2)} kWh/dia
          </div>
        </div>
      ))}
    </div>
  );
}
