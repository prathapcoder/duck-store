import { useState } from 'react';

/** Wraps an async action with busy and error state, so forms and dialogs do not each re-implement it. */
export function useAsyncAction(action) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const run = async (...args) => {
    setBusy(true);
    setError(null);
    try {
      await action(...args);
      return true;
    } catch (e) {
      setError(e);
      return false;
    } finally {
      setBusy(false);
    }
  };

  return { run, busy, error };
}
