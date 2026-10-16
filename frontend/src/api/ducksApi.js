const BASE = import.meta.env.VITE_API_URL ?? '';

export class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
  }
}

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${BASE}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new ApiError('Cannot reach the server. Is the backend running?', 0);
  }
  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiError(body?.detail ?? `Request failed (${response.status})`, response.status, body?.errors);
  }
  return body;
}

export const listDucks = (field, dir) => request(`/api/ducks?sort=${field}&dir=${dir}`);

export const addDuck = (duck) => request('/api/ducks', { method: 'POST', body: JSON.stringify(duck) });

// Only price and quantity are editable.
export const updateDuck = (id, { price, quantity }) =>
  request(`/api/ducks/${id}`, { method: 'PUT', body: JSON.stringify({ price, quantity }) });

export const deleteDuck = (id) => request(`/api/ducks/${id}`, { method: 'DELETE' });
