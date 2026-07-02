import { useAuthStore } from './features/auth/authStore.js'
import LoginPage from './features/auth/LoginPage.jsx'
import DashboardPage from './features/dashboard/DashboardPage.jsx'
import CustomerPage from './features/customer/CustomerPage.jsx'

// Public, no-login customer screen. nginx (and the Vite dev server) fall back to index.html
// for any path, so a plain pathname check is enough routing for these two entry points.
export const CUSTOMER_PATH = '/atendimento'

export default function App() {
  // The customer queue screen is open to anyone and bypasses auth entirely.
  if (typeof window !== 'undefined' && window.location.pathname.startsWith(CUSTOMER_PATH)) {
    return <CustomerPage />
  }

  // A token in the store means we're logged in. A 401/403 clears it (see shared/api/http),
  // which brings us straight back to the login screen.
  const authenticated = useAuthStore((s) => !!s.token)
  return authenticated ? <DashboardPage /> : <LoginPage />
}
