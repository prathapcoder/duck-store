import { formatPrice } from '../format.js';

const COLUMNS = [
  { field: 'id', label: 'Id' },
  { field: 'color', label: 'Color' },
  { field: 'size', label: 'Size' },
  { field: 'price', label: 'Price' },
  { field: 'quantity', label: 'Quantity' },
];

export default function DuckTable({ ducks, sort, onSort, onEdit, onDelete }) {
  const ariaSort = (field) => (sort.field === field ? (sort.dir === 'asc' ? 'ascending' : 'descending') : 'none');
  const arrow = (field) => (sort.field === field ? (sort.dir === 'asc' ? ' \u25B2' : ' \u25BC') : '');

  return (
    <table>
      <thead>
        <tr>
          {COLUMNS.map(({ field, label }) => (
            <th key={field} aria-sort={ariaSort(field)}>
              <button type="button" className="link" onClick={() => onSort(field)}>
                {label}{arrow(field)}
              </button>
            </th>
          ))}
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {ducks.length === 0 && (
          <tr><td colSpan={COLUMNS.length + 1} className="empty">No ducks in the warehouse yet.</td></tr>
        )}
        {ducks.map((duck) => (
          <tr key={duck.id}>
            <td>{duck.id}</td>
            <td>{duck.color}</td>
            <td>{duck.size}</td>
            <td>{formatPrice(duck.price)}</td>
            <td>{duck.quantity}</td>
            <td className="row-actions">
              <button type="button" className="link" onClick={() => onEdit(duck)}>edit</button>
              {' / '}
              <button type="button" className="link" onClick={() => onDelete(duck)}>delete</button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
