import { Link, useLocation, Outlet } from 'react-router'
import { useState, useEffect } from 'react'
import { Menu, X, ExternalLink } from 'lucide-react'
import { motion, AnimatePresence } from 'motion/react'
import NeuralNetworkBg from './NeuralNetworkBg'
import GithubIcon from './GithubIcon'

const navItems = [
  { path: '/', label: 'Home' },
  { path: '/getting-started', label: 'Getting Started' },
  { path: '/api', label: 'API Reference' },
]

export default function Layout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  const location = useLocation()

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20)
    window.addEventListener('scroll', onScroll)
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  useEffect(() => {
    setMobileOpen(false)
  }, [location])

  return (
    <div className="min-h-screen bg-gray-950 relative">
      <NeuralNetworkBg />
      {/* Navbar */}
      <nav className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 ${scrolled ? 'glass shadow-lg shadow-black/20' : 'bg-transparent'}`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link to="/" className="flex items-center gap-3 group">
              <img src="/synapse-icon.svg" alt="Synapse" className="w-8 h-8 transition-transform group-hover:scale-110" />
              <span className="text-lg font-bold gradient-text">SYNAPSE</span>
            </Link>

            {/* Desktop nav */}
            <div className="hidden md:flex items-center gap-1">
              {navItems.map((item) => (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                    location.pathname === item.path
                      ? 'bg-synapse-600/20 text-synapse-400'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800/50'
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </div>

            <div className="hidden md:flex items-center gap-3">
              <a
                href="https://github.com/Abhiramrathod/synapse"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-gray-400 hover:text-white hover:bg-gray-800/50 transition-all"
              >
                <GithubIcon className="w-4 h-4" />
                GitHub
              </a>
              <a
                href="https://central.sonatype.com/artifact/io.github.abhiramrathod/synapse-all"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium bg-synapse-600 text-white hover:bg-synapse-500 transition-all"
              >
                <ExternalLink className="w-4 h-4" />
                Install
              </a>
            </div>

            {/* Mobile toggle */}
            <button
              className="md:hidden p-2 rounded-lg text-gray-400 hover:text-white hover:bg-gray-800/50"
              onClick={() => setMobileOpen(!mobileOpen)}
            >
              {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        <AnimatePresence>
          {mobileOpen && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="md:hidden glass border-t border-gray-800/50"
            >
              <div className="px-4 py-3 space-y-1">
                {navItems.map((item) => (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`block px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                      location.pathname === item.path
                        ? 'bg-synapse-600/20 text-synapse-400'
                        : 'text-gray-400 hover:text-white hover:bg-gray-800/50'
                    }`}
                  >
                    {item.label}
                  </Link>
                ))}
                <a
                  href="https://github.com/Abhiramrathod/synapse"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium text-gray-400 hover:text-white"
                >
                  <GithubIcon className="w-4 h-4" /> GitHub
                </a>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </nav>

      {/* Main content */}
      <main className="pt-16 relative" style={{ zIndex: 1 }}>
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.2 }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-800/50 mt-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="md:col-span-2">
              <div className="flex items-center gap-3 mb-4">
                <img src="/synapse-icon.svg" alt="Synapse" className="w-8 h-8" />
                <span className="text-lg font-bold gradient-text">SYNAPSE</span>
              </div>
              <p className="text-gray-400 text-sm leading-relaxed max-w-md">
                A production-ready, multi-module Java library for seamless integration with any LLM API provider.
                Built with performance, extensibility, and developer experience in mind.
              </p>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white mb-3">Documentation</h4>
              <ul className="space-y-2">
                <li><Link to="/getting-started" className="text-sm text-gray-400 hover:text-synapse-400 transition-colors">Getting Started</Link></li>
                <li><Link to="/api" className="text-sm text-gray-400 hover:text-synapse-400 transition-colors">API Reference</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white mb-3">Resources</h4>
              <ul className="space-y-2">
                <li>
                  <a href="https://github.com/Abhiramrathod/synapse" target="_blank" rel="noopener noreferrer" className="text-sm text-gray-400 hover:text-synapse-400 transition-colors">
                    GitHub Repository
                  </a>
                </li>
                <li>
                  <a href="https://central.sonatype.com/artifact/io.github.abhiramrathod/synapse-all" target="_blank" rel="noopener noreferrer" className="text-sm text-gray-400 hover:text-synapse-400 transition-colors">
                    Maven Central
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Abhiramrathod/synapse/releases" target="_blank" rel="noopener noreferrer" className="text-sm text-gray-400 hover:text-synapse-400 transition-colors">
                    Releases
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div className="mt-8 pt-8 border-t border-gray-800/50 flex flex-col sm:flex-row items-center justify-between gap-4">
            <p className="text-sm text-gray-500">
              &copy; {new Date().getFullYear()} Synapse. Open source under Apache 2.0 License.
            </p>
            <div className="flex items-center gap-4">
              <span className="text-xs text-gray-600 font-mono">Java 17+</span>
              <span className="text-xs text-gray-700">|</span>
              <span className="text-xs text-gray-600 font-mono">Maven 3.8+</span>
              <span className="text-xs text-gray-700">|</span>
              <span className="text-xs text-gray-600 font-mono">Spring Boot 3.x</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
