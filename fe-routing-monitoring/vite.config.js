import { defineConfig } from 'vitest/config'
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
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: 'coverage',
      include: ['src/**/*.{js,jsx}'],
      exclude: ['src/main.jsx', 'src/test/**', 'src/**/*.test.{js,jsx}'],
    },
  },
})
