import { Outlet, Navigate } from 'react-router-dom'
import authService from '../../shared/services/authService'
import '../estilos/AdminLayout.css'

function AdminLayout() {
  const token = authService.getToken()

  if (!token) {
    return <Navigate to="/admin/login" replace />
  }

  return (
    <div className="admin-shell">
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  )
}

export default AdminLayout
