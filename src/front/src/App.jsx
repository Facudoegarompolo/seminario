import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import AdminLayout from './admin/componentes/AdminLayout'
import Login from './admin/paginas/Login'
import Dashboard from './admin/paginas/Dashboard'
import Fila from './admin/paginas/Fila'
import LlamarCliente from './admin/paginas/LlamarCliente'
import Estadisticas from './admin/paginas/Estadisticas'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/admin/login" element={<Login />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Dashboard />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="fila" element={<Fila />} />
          <Route path="llamar" element={<LlamarCliente />} />
          <Route path="estadisticas" element={<Estadisticas />} />
        </Route>
        <Route path="*" element={<Navigate to="/admin/login" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
