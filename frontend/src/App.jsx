import { useState } from 'react';
import ConfirmDialog from './components/ConfirmDialog.jsx';
import DuckForm from './components/DuckForm.jsx';
import DuckTable from './components/DuckTable.jsx';
import { useDucks } from './hooks/useDucks.js';

export default function App() {
  const { ducks, sort, loading, error, toggleSort, add, update, remove } = useDucks();
  const [form, setForm] = useState(null);      // null | { duck?: Duck }
  const [toDelete, setToDelete] = useState(null);

  const closeForm = () => setForm(null);

  const save = async (values) => {
    if (form.duck) {
      await update(form.duck.id, values);
    } else {
      await add(values);
    }
    closeForm();
  };

  return (
    <main>
      <h1>Duck Warehouse</h1>
      <button type="button" className="primary" onClick={() => setForm({})}>Add duck</button>

      {error && <p className="error" role="alert">{error}</p>}
      {loading ? (
        <p>Loading...</p>
      ) : (
        <DuckTable ducks={ducks} sort={sort} onSort={toggleSort} onEdit={(duck) => setForm({ duck })} onDelete={setToDelete} />
      )}

      {form && <DuckForm duck={form.duck} onSubmit={save} onCancel={closeForm} />}
      {toDelete && (
        <ConfirmDialog
          title="Delete duck"
          message={`Delete duck ${toDelete.id} (${toDelete.color}, ${toDelete.size}, ${toDelete.quantity} in stock)?`}
          confirmLabel="Delete"
          onCancel={() => setToDelete(null)}
          onConfirm={async () => {
            await remove(toDelete.id);
            setToDelete(null);
          }}
        />
      )}
    </main>
  );
}
