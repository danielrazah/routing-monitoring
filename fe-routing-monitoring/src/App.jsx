import { useEffect, useState } from 'react'
import Login from './components/Login.jsx'
import Dashboard from './components/Dashboard.jsx'
import { getAuth, logout } from './lib/auth.js'
import { setUnauthorizedHandler } from './lib/api.js'

export default function App() {
  const [auth, setAuth] = useState(getAuth())

  useEffect(() => {
    // If any API call gets a 401/403, the token is gone: send the user back to login.
    setUnauthorizedHandler(() => setAuth(null))
  }, [])

  function handleLogout() {
    logout()
    setAuth(null)
  }

  if (!auth) {
    return <Login onLogin={setAuth} />
  }
  return <Dashboard auth={auth} onLogout={handleLogout} />
}
