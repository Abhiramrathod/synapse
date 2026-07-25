import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { useMemo } from 'react'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import GettingStartedPage from './pages/GettingStartedPage'
import ApiReferencePage from './pages/ApiReferencePage'
import WebAssemblyPage from './pages/WebAssemblyPage'

function useBasename(): string {
  return useMemo(() => {
    const scriptEl = document.querySelector('script[src*="/assets/"]')
    if (scriptEl) {
      const src = scriptEl.getAttribute('src') || ''
      const match = src.match(/^(.*?)assets\//)
      if (match) return match[1] || '/'
    }
    return '/'
  }, [])
}

export default function App() {
  const basename = useBasename()

  return (
    <Router basename={basename}>
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
