import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Inicio from './cliente/paginas/Inicio'
import Estado from './cliente/paginas/Estado'
import Confirmacion from './cliente/paginas/Confirmacion'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/confirmacion" element={<Confirmacion />} />
        <Route path="/estado" element={<Estado />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App