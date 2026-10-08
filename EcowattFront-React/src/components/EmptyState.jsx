import Icon from "./Icon.jsx";

// Estado vazio reutilizavel: icone, titulo, descricao e um botao de acao
// opcional. Usado quando uma lista nao tem itens.
export default function EmptyState({
  icon = "inbox",
  titulo,
  descricao,
  acaoLabel,
  onAcao,
}) {
  return (
    <div className="empty-state">
      <div className="empty-state-ico">
        <Icon name={icon} size={30} />
      </div>
      <h4>{titulo}</h4>
      {descricao && <p>{descricao}</p>}
      {acaoLabel && onAcao && (
        <button type="button" className="empty-btn" onClick={onAcao}>
          <Icon name="plus" size={16} />
          {acaoLabel}
        </button>
      )}
    </div>
  );
}
