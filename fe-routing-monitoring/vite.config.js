import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // 5173 (Vite's default) often clashes with other local projects; use 8090.
    port: 8090,
    // In dev, forward API and WebSocket traffic to the Spring backend.
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': { target: 'http://localhost:8080', ws: true },
    },
  },
})
