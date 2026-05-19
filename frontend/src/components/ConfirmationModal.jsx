export default function ConfirmationModal({ open, title, message, confirmLabel, cancelLabel = "Cancel", danger, onConfirm, onCancel }) {
  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onCancel}>
      <div className="confirmation-modal" role="dialog" aria-modal="true" aria-labelledby="confirmation-title" onMouseDown={(event) => event.stopPropagation()}>
        <h2 id="confirmation-title">{title}</h2>
        <p>{message}</p>
        <div className="modal-actions">
          <button className="ghost-button" type="button" onClick={onCancel}>
            {cancelLabel}
          </button>
          <button className={danger ? "danger-button" : "solid-button"} type="button" onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
