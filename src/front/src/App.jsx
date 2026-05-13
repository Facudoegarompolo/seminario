import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Inicio from './paginas/Inicio'
import Estado from './paginas/Estado'

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