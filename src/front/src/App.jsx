import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import AdminLayout from './admin/componentes/AdminLayout'
import Login from './admin/paginas/Login'
import Dashboard from './admin/paginas/Dashboard'
import Fila from './admin/paginas/Fila'
import LlamarCliente from './admin/paginas/LlamarCliente'
import Estadisticas from './admin/paginas/Estadisticas'
import DemoNotificacion from './admin/paginas/DemoNotificacion'
import Inicio from './cliente/paginas/Inicio'
import Estado from './cliente/paginas/Estado'

import './cliente/estilos/global.css'

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
          <Route path="demo-notificacion" element={<DemoNotificacion />} />
        </Route>
        <Route path="/fila/:codigoPublico" element={<Inicio />} />
        <Route path="/turno/:tokenPublico" element={<Estado />} />
        <Route path="*" element={<Navigate to="/admin/login" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
