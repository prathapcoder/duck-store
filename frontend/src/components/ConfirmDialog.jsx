import { useAsyncAction } from '../hooks/useAsyncAction.js';
import Modal from './Modal.jsx';

export default function ConfirmDialog({ title, message, confirmLabel, onConfirm, onCancel }) {
  const { run, busy, error } = useAsyncAction(onConfirm);

  return (
    <Modal title={title} onClose={onCancel}>
      <p>{message}</p>
      {error && <p className="error" role="alert">{error.message}</p>}
      <div className="actions">
        <button type="button" onClick={onCancel} disabled={busy}>Cancel</button>
        <button type="button" className="danger" onClick={() => run()} disabled={busy}>{confirmLabel}</button>
      </div>
    </Modal>
  );
}
