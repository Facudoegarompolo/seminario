import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Inicio from './cliente/paginas/Inicio'
import Estado from './cliente/paginas/Estado'

function App() {
  return (
    <BrowserRouter>

      <Routes>

        <Route
          path="/"
          element={<Inicio />}
        />

        <Route
          path="/estado"
          element={<Estado />}
        />

      </Routes>

    </BrowserRouter>
  )
}

export default App