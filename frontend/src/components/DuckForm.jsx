import { useState } from 'react';
import { COLORS, SIZES } from '../constants.js';
import { useAsyncAction } from '../hooks/useAsyncAction.js';
import Field from './Field.jsx';
import Modal from './Modal.jsx';

const PRICE = /^\d{1,10}(\.\d{1,2})?$/;
const WHOLE = /^\d+$/;
const MAX_QUANTITY = 1000000000;

function validate(values, editing) {
  const errors = {};
  if (!PRICE.test(values.price) || Number(values.price) < 0.01) {
    errors.price = 'Enter a price of at least 0.01 with at most 2 decimals.';
  }
  const minimum = editing ? 0 : 1;
  const quantity = Number(values.quantity);
  if (!WHOLE.test(values.quantity) || quantity < minimum || quantity > MAX_QUANTITY) {
    errors.quantity = `Enter a whole number from ${minimum} to ${MAX_QUANTITY}.`;
  }
  return errors;
}

const toPayload = (values) => ({
  color: values.color,
  size: values.size,
  price: Number(values.price),
  quantity: Number(values.quantity),
});

/** One form for both adding and editing. When editing, color and size are shown but read-only. */
export default function DuckForm({ duck, onSubmit, onCancel }) {
  const editing = Boolean(duck);
  const [values, setValues] = useState({
    color: duck?.color ?? COLORS[0],
    size: duck?.size ?? SIZES[0],
    price: duck ? String(duck.price) : '',
    quantity: duck ? String(duck.quantity) : '',
  });
  const [clientErrors, setClientErrors] = useState({});
  const { run, busy, error } = useAsyncAction(onSubmit);

  const change = (field) => (event) => setValues({ ...values, [field]: event.target.value });
  const errors = { ...clientErrors, ...(error?.fieldErrors ?? {}) };

  const submit = async (event) => {
    event.preventDefault();
    const found = validate(values, editing);
    setClientErrors(found);
    if (Object.keys(found).length === 0) {
      await run(toPayload(values));
    }
  };

  return (
    <Modal title={editing ? `Edit duck ${duck.id}` : 'Add duck'} onClose={onCancel}>
      <form onSubmit={submit} noValidate>
        <Field label="Color">
          <select value={values.color} onChange={change('color')} disabled={editing}>
            {COLORS.map((c) => <option key={c}>{c}</option>)}
          </select>
        </Field>
        <Field label="Size">
          <select value={values.size} onChange={change('size')} disabled={editing}>
            {SIZES.map((s) => <option key={s}>{s}</option>)}
          </select>
        </Field>
        <Field label="Price (USD)" error={errors.price}>
          <input type="text" inputMode="decimal" value={values.price} onChange={change('price')} />
        </Field>
        <Field label="Quantity" error={errors.quantity}>
          <input type="text" inputMode="numeric" value={values.quantity} onChange={change('quantity')} />
        </Field>
        {!editing && (
          <p className="hint">If a duck with the same color, size and price already exists, the quantity is added to it.</p>
        )}
        {error && <p className="error" role="alert">{error.message}</p>}
        <div className="actions">
          <button type="button" onClick={onCancel} disabled={busy}>Cancel</button>
          <button type="submit" className="primary" disabled={busy}>{editing ? 'Save' : 'Add'}</button>
        </div>
      </form>
    </Modal>
  );
}
