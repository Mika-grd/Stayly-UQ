import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// En desarrollo, /api va al backend (cd backend && ./gradlew bootRun): sin CORS, mismo origen.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
