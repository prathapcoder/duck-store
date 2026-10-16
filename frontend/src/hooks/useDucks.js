import { useCallback, useEffect, useState } from 'react';
import { addDuck, deleteDuck, listDucks, updateDuck } from '../api/ducksApi.js';

/** Owns the duck list, the sort order and the mutations. Mutations reload the list and rethrow errors to the caller. */
export function useDucks() {
  const [ducks, setDucks] = useState([]);
  const [sort, setSort] = useState({ field: 'quantity', dir: 'desc' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      setError(null);
      setDucks(await listDucks(sort.field, sort.dir));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [sort]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const toggleSort = (field) =>
    setSort((previous) =>
      previous.field === field
        ? { field, dir: previous.dir === 'asc' ? 'desc' : 'asc' }
        : { field, dir: field === 'quantity' ? 'desc' : 'asc' }
    );

  const mutate = async (action) => {
    await action();
    await refresh();
  };

  return {
    ducks,
    sort,
    loading,
    error,
    toggleSort,
    add: (duck) => mutate(() => addDuck(duck)),
    update: (id, duck) => mutate(() => updateDuck(id, duck)),
    remove: (id) => mutate(() => deleteDuck(id)),
  };
}
