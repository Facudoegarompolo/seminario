import { NavLink } from 'react-router-dom'
import '../estilos/AdminLayout.css'

function Sidebar() {
  return (
    <aside className="admin-sidebar">
      <div className="sidebar-brand">
        <span className="brand-mark">DQ</span>
        <div>
          <strong>DigitalQueue</strong>
          <p>Panel admin</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/admin/dashboard" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Dashboard
        </NavLink>
        <NavLink to="/admin/fila" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Gestión de fila
        </NavLink>
        <NavLink to="/admin/llamar" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Llamar cliente
        </NavLink>
        <NavLink to="/admin/estadisticas" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Estadísticas
        </NavLink>
      </nav>
    </aside>
  )
}

export default Sidebar
