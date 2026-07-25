import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import GettingStartedPage from './pages/GettingStartedPage'
import ApiReferencePage from './pages/ApiReferencePage'
import WebAssemblyPage from './pages/WebAssemblyPage'

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="getting-started" element={<GettingStartedPage />} />
          <Route path="api" element={<ApiReferencePage />} />
          <Route path="webassembly" element={<WebAssemblyPage />} />
        </Route>
      </Routes>
    </Router>
  )
}
