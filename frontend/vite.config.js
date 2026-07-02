import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// sockjs-client expects a `global`; map it to the browser window.
export default defineConfig({
  plugins: [react()],
  define: { global: 'globalThis' },
  server: {
    // In dev, forward API and WebSocket traffic to the Spring backend.
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': { target: 'http://localhost:8080', ws: true },
    },
  },
})
