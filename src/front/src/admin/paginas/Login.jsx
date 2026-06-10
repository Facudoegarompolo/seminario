import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../../shared/services/authService'
import logo from '../../shared/assets/logo.jpeg'
import '../estilos/Login.css'

function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(true)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    setLoading(true)

    try {
      await authService.login({ email, password })
      if (remember) localStorage.setItem('dq_admin_remember', 'true')
      navigate('/admin/dashboard')
    } catch (err) {
      setError(err.message || 'Credenciales incorrectas o error del servidor.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-top">
        <img src={logo} alt="DQ" className="login-top-logo" />
        <div className="login-top-sub">Administrador</div>
      </div>

      <div className="login-panel">
        <form className="login-form" onSubmit={handleSubmit}>
          <h2>Iniciar sesión</h2>

          <label className="input-group">
            <span className="input-icon icon-mail" />
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Correo electrónico"
              required
            />
          </label>

          <label className="input-group">
            <span className="input-icon icon-lock" />
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Contraseña"
              required
            />
          </label>

          <div className="login-options">
            <label className="remember-checkbox">
              <input
                type="checkbox"
                checked={remember}
                onChange={(event) => setRemember(event.target.checked)}
              />
              Recordarme
            </label>
            <button type="button" className="forgot-link">
              ¿Olvidaste tu contraseña?
            </button>
          </div>

          {error && <div className="login-error">{error}</div>}

          <button type="submit" className="login-submit" disabled={loading}>
            {loading ? 'Validando...' : 'Iniciar sesión'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default Login
