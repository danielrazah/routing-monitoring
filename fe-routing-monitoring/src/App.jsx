import { useAuthStore } from './features/auth/authStore.js'
import LoginPage from './features/auth/LoginPage.jsx'
import DashboardPage from './features/dashboard/DashboardPage.jsx'
import CustomerPage from './features/customer/CustomerPage.jsx'
import LandingPage from './features/landing/LandingPage.jsx'

// Plain pathname routing (no router dependency). nginx and the Vite dev server both fall
// back to index.html for any path, so the SPA can pick the screen from the pathname:
//   /              -> landing (choose how to continue)
//   /atendimento   -> public customer queue screen (no login)
//   /painel        -> dashboard if logged in, otherwise the login screen
export const CUSTOMER_PATH = '/atendimento'
export const PANEL_PATH = '/painel'

export default function App() {
  // Read the token unconditionally to keep hook order stable across renders.
  const authenticated = useAuthStore((s) => !!s.token)
  const path = typeof window !== 'undefined' ? window.location.pathname : '/'

  if (path.startsWith(CUSTOMER_PATH)) return <CustomerPage />
  if (path.startsWith(PANEL_PATH)) return authenticated ? <DashboardPage /> : <LoginPage />
  return <LandingPage />
}
