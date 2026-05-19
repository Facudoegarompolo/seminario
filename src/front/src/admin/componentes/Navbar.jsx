import '../estilos/AdminLayout.css'

function Navbar() {
  return (
    <header className="admin-navbar">
      <div>
        <h1>Panel administrativo</h1>
        <p>Monitoreo en tiempo real del local</p>
      </div>
      <div className="navbar-actions">
        <button className="notification-button" type="button">
          <span className="dot" />
          Notificaciones
        </button>
        <div className="user-chip">Admin</div>
      </div>
    </header>
  )
}

export default Navbar
