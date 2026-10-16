import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development the UI calls /api/... and Vite forwards it to the Spring Boot backend,
// so no CORS setup is needed. Override the target with BACKEND_URL if the backend runs elsewhere.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': process.env.BACKEND_URL || 'http://localhost:8080' },
  },
});
