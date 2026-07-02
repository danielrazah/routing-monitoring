import { useAuthStore } from './features/auth/authStore.js'
import LoginPage from './features/auth/LoginPage.jsx'
import DashboardPage from './features/dashboard/DashboardPage.jsx'

export default function App() {
  // A token in the store means we're logged in. A 401/403 clears it (see shared/api/http),
  // which brings us straight back to the login screen.
  const authenticated = useAuthStore((s) => !!s.token)
  return authenticated ? <DashboardPage /> : <LoginPage />
}
