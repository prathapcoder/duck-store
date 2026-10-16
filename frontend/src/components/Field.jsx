/** A labelled form control with an optional error message. */
export default function Field({ label, error, children }) {
  return (
    <label>
      {label}
      {children}
      {error && <span className="error">{error}</span>}
    </label>
  );
}
