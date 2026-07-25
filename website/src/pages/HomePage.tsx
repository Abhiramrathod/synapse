import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, Github, ChevronRight } from 'lucide-react'
import FadeIn from '../components/FadeIn'
import CodeBlock from '../components/CodeBlock'
import Badge from '../components/Badge'
import WasmDemo from '../components/WasmDemo'
import { features, modules } from '../data/content'
import { quickStartCode, streamingCode } from '../data/codeExamples'

export default function HomePage() {
  const [activeCodeTab, setActiveCodeTab] = useState<'sync' | 'streaming'>('sync')
  const [activeModule, setActiveModule] = useState<string | null>(null)

  return (
    <div className="bg-gray-950 min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-32 pb-20">
        {/* Background gradient mesh */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-40 -right-40 w-[600px] h-[600px] bg-synapse-600/20 rounded-full blur-[128px]" />
          <div className="absolute top-20 -left-20 w-[400px] h-[400px] bg-purple-600/15 rounded-full blur-[100px]" />
          <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-[800px] h-[300px] bg-synapse-500/10 rounded-full blur-[120px]" />
          {/* Grid pattern */}
          <div
            className="absolute inset-0 opacity-[0.03]"
            style={{
              backgroundImage:
                'linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)',
              backgroundSize: '64px 64px',
            }}
          />
          {/* Dot pattern */}
          <div
            className="absolute inset-0 opacity-[0.04]"
            style={{
              backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.5) 1px, transparent 1px)',
              backgroundSize: '32px 32px',
            }}
          />
        </div>

        <div className="max-w-7xl mx-auto px-6 relative">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              <FadeIn delay={0.1}>
                <Badge variant="blue">v1.0.4 — Now Available</Badge>
              </FadeIn>

              <FadeIn delay={0.2}>
                <h1 className="mt-6 text-5xl lg:text-7xl font-bold tracking-tight">
                  <span className="text-white">Universal</span>{' '}
                  <span className="gradient-text">LLM Client</span>{' '}
                  <span className="text-white">for Java</span>
                </h1>
              </FadeIn>

              <FadeIn delay={0.3}>
                <p className="mt-6 text-xl text-gray-400 max-w-lg leading-relaxed">
                  One provider-agnostic API for OpenAI, Anthropic, Cohere, and any
                  OpenAI-compatible LLM. Streaming, retries, metrics — built in.
                </p>
              </FadeIn>

              <FadeIn delay={0.4}>
                <div className="mt-10 flex flex-wrap gap-4">
                  <Link
                    to="/getting-started"
                    className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-synapse-600 hover:bg-synapse-500 text-white font-medium transition-all hover:shadow-lg hover:shadow-synapse-500/25"
                  >
                    Get Started
                    <ArrowRight className="w-4 h-4" />
                  </Link>
                  <a
                    href="https://github.com/Abhiramrathod/synapse"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-gray-800/60 hover:bg-gray-800 text-white font-medium border border-gray-700/50 transition-all"
                  >
                    <Github className="w-4 h-4" />
                    View on GitHub
                  </a>
                </div>
              </FadeIn>
            </div>

            <FadeIn delay={0.3} direction="left" className="hidden lg:block">
              <div className="relative">
                <div className="absolute -inset-4 bg-gradient-to-r from-synapse-600/20 to-purple-600/20 rounded-2xl blur-xl" />
                <div className="relative">
                  <CodeBlock code={quickStartCode} title="QuickStart.java" />
                </div>
              </div>
            </FadeIn>
          </div>
        </div>
      </section>

      {/* Badges Section */}
      <section className="py-8 border-y border-gray-800/40">
        <div className="max-w-7xl mx-auto px-6">
          <FadeIn>
            <div className="flex flex-wrap justify-center gap-4">
              <Badge>Java 17+</Badge>
              <Badge>Maven 3.8+</Badge>
              <Badge variant="green">Spring Boot 3.x</Badge>
              <Badge variant="blue">OpenAI Compatible</Badge>
              <Badge variant="purple">MIT License</Badge>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-6">
          <FadeIn>
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-white">Everything you need</h2>
              <p className="mt-4 text-lg text-gray-400 max-w-2xl mx-auto">
                A batteries-included HTTP client designed for LLM APIs. No boilerplate, no vendor lock-in.
              </p>
            </div>
          </FadeIn>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {features.map((feature, i) => (
              <FadeIn key={feature.title} delay={i * 0.07}>
                <div className="group p-6 rounded-2xl bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 hover:border-gray-700/60 transition-all hover:-translate-y-1 h-full">
                  <div className={`w-10 h-10 rounded-xl ${feature.bgColor} ${feature.borderColor} border flex items-center justify-center mb-4`}>
                    <feature.icon className={`w-5 h-5 ${feature.color}`} />
                  </div>
                  <h3 className="text-white font-semibold mb-2">{feature.title}</h3>
                  <p className="text-gray-400 text-sm leading-relaxed">{feature.description}</p>
                </div>
              </FadeIn>
            ))}
          </div>
        </div>
      </section>

      {/* Architecture Overview */}
      <section className="py-24 relative">
        <div className="absolute inset-0 bg-gradient-to-b from-gray-950 via-gray-900/30 to-gray-950 pointer-events-none" />
        <div className="max-w-7xl mx-auto px-6 relative">
          <FadeIn>
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-white">Modular Architecture</h2>
              <p className="mt-4 text-lg text-gray-400 max-w-2xl mx-auto">
                Pick only what you need. Each module is focused and lightweight.
              </p>
            </div>
          </FadeIn>

          <FadeIn delay={0.1}>
            <div className="relative">
              {/* Connection lines */}
              <div className="absolute top-1/2 left-0 right-0 h-px bg-gradient-to-r from-transparent via-gray-700/50 to-transparent hidden lg:block" />

              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {modules.map((mod, i) => (
                  <motion.div
                    key={mod.name}
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: i * 0.08 }}
                    className="relative p-5 rounded-2xl bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 hover:border-gray-700/60 transition-all"
                  >
                    <div className="flex items-start gap-3 mb-3">
                      <div className={`w-9 h-9 rounded-lg bg-gradient-to-br ${mod.color} flex items-center justify-center flex-shrink-0`}>
                        <mod.icon className="w-4 h-4 text-white" />
                      </div>
                      <div className="min-w-0">
                        <h3 className="text-white font-semibold text-sm truncate">{mod.name}</h3>
                        <p className="text-gray-500 text-xs mt-0.5">{mod.description}</p>
                      </div>
                    </div>
                    <div className="flex flex-wrap gap-1.5 ml-12">
                      {mod.items.map((item) => (
                        <span key={item} className="text-[11px] px-2 py-0.5 rounded-md bg-gray-800/60 text-gray-400 border border-gray-700/40">
                          {item}
                        </span>
                      ))}
                    </div>
                  </motion.div>
                ))}
              </div>

              {/* Simplified architecture diagram */}
              <FadeIn delay={0.2}>
                <div className="mt-12 p-8 rounded-2xl bg-gray-900/40 border border-gray-800/40">
                  <div className="flex flex-col lg:flex-row items-center justify-center gap-6 text-sm">
                    <div className="px-4 py-2.5 rounded-xl bg-synapse-600/10 border border-synapse-500/20 text-synapse-400 font-medium text-center">
                      Your Code
                    </div>
                    <ChevronRight className="w-5 h-5 text-gray-600 rotate-90 lg:rotate-0" />
                    <div className="px-4 py-2.5 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 font-medium text-center">
                      synapse-core
                    </div>
                    <ChevronRight className="w-5 h-5 text-gray-600 rotate-90 lg:rotate-0" />
                    <div className="px-4 py-2.5 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 font-medium text-center">
                      synapse-config
                    </div>
                    <ChevronRight className="w-5 h-5 text-gray-600 rotate-90 lg:rotate-0" />
                    <div className="px-4 py-2.5 rounded-xl bg-green-500/10 border border-green-500/20 text-green-400 font-medium text-center">
                      synapse-http
                    </div>
                    <ChevronRight className="w-5 h-5 text-gray-600 rotate-90 lg:rotate-0" />
                    <div className="px-4 py-2.5 rounded-xl bg-gray-800/60 border border-gray-700/40 text-gray-400 font-medium text-center">
                      LLM API
                    </div>
                  </div>
                </div>
              </FadeIn>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Quick Code Preview */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-6">
          <FadeIn>
            <div className="text-center mb-12">
              <h2 className="text-4xl font-bold text-white">Simple & Powerful APIs</h2>
              <p className="mt-4 text-lg text-gray-400">
                Synchronous or streaming — your choice, per request.
              </p>
            </div>
          </FadeIn>

          <FadeIn delay={0.1}>
            <div className="max-w-4xl mx-auto">
              {/* Tabs */}
              <div className="flex gap-1 p-1 bg-gray-900/60 rounded-xl border border-gray-800/50 mb-6 w-fit mx-auto">
                {(['sync', 'streaming'] as const).map((tab) => (
                  <button
                    key={tab}
                    onClick={() => setActiveCodeTab(tab)}
                    className={`px-5 py-2 rounded-lg text-sm font-medium transition-all ${
                      activeCodeTab === tab
                        ? 'bg-synapse-600 text-white shadow-lg shadow-synapse-500/20'
                        : 'text-gray-400 hover:text-white'
                    }`}
                  >
                    {tab === 'sync' ? 'Synchronous' : 'Streaming'}
                  </button>
                ))}
              </div>

              <CodeBlock
                code={activeCodeTab === 'sync' ? quickStartCode : streamingCode}
                title={activeCodeTab === 'sync' ? 'Synchronous Usage' : 'Streaming Usage'}
              />
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Module Explorer */}
      <section className="py-24 relative">
        <div className="absolute inset-0 bg-gradient-to-b from-gray-950 via-gray-900/30 to-gray-950 pointer-events-none" />
        <div className="max-w-7xl mx-auto px-6 relative">
          <FadeIn>
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-white">Module Explorer</h2>
              <p className="mt-4 text-lg text-gray-400 max-w-2xl mx-auto">
                Explore each module and see what classes they provide.
              </p>
            </div>
          </FadeIn>

          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {modules.map((mod, i) => (
              <FadeIn key={mod.name} delay={i * 0.06}>
                <motion.div
                  onClick={() => setActiveModule(activeModule === mod.name ? null : mod.name)}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className={`p-6 rounded-2xl border cursor-pointer transition-all ${
                    activeModule === mod.name
                      ? 'bg-gray-800/80 border-synapse-500/40 shadow-lg shadow-synapse-500/10'
                      : 'bg-gray-900/60 border-gray-800/50 hover:border-gray-700/60'
                  }`}
                >
                  <div className="flex items-center gap-3 mb-4">
                    <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${mod.color} flex items-center justify-center`}>
                      <mod.icon className="w-5 h-5 text-white" />
                    </div>
                    <div>
                      <h3 className="text-white font-semibold">{mod.name}</h3>
                      <p className="text-gray-500 text-xs">{mod.description}</p>
                    </div>
                  </div>

                  <motion.div
                    initial={false}
                    animate={{ height: activeModule === mod.name ? 'auto' : 0, opacity: activeModule === mod.name ? 1 : 0 }}
                    transition={{ duration: 0.2 }}
                    className="overflow-hidden"
                  >
                    <div className="pt-3 border-t border-gray-700/40 space-y-2">
                      {mod.items.map((item) => (
                        <div
                          key={item}
                          className="flex items-center gap-2 text-sm text-gray-300 px-2 py-1.5 rounded-lg bg-gray-800/40"
                        >
                          <div className={`w-1.5 h-1.5 rounded-full bg-gradient-to-r ${mod.color}`} />
                          <code className="font-mono text-xs">{item}</code>
                        </div>
                      ))}
                    </div>
                  </motion.div>

                  {activeModule !== mod.name && (
                    <div className="flex flex-wrap gap-1.5 mt-2">
                      {mod.items.slice(0, 3).map((item) => (
                        <span key={item} className="text-[11px] px-2 py-0.5 rounded-md bg-gray-800/60 text-gray-500 border border-gray-700/30">
                          {item}
                        </span>
                      ))}
                      {mod.items.length > 3 && (
                        <span className="text-[11px] px-2 py-0.5 rounded-md text-gray-600">
                          +{mod.items.length - 3}
                        </span>
                      )}
                    </div>
                  )}
                </motion.div>
              </FadeIn>
            ))}
          </div>
        </div>
      </section>

      {/* WebAssembly Demo */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-6">
          <FadeIn>
            <div className="text-center mb-12">
              <h2 className="text-4xl font-bold text-white">Run Anywhere — Even the Browser</h2>
              <p className="mt-4 text-lg text-gray-400 max-w-2xl mx-auto">
                Java compiled to WebAssembly runs natively in the browser. Try it below.
              </p>
            </div>
          </FadeIn>
          <FadeIn delay={0.1}>
            <WasmDemo />
          </FadeIn>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-6">
          <FadeIn>
            <div className="relative p-12 lg:p-16 rounded-3xl overflow-hidden">
              {/* Gradient background */}
              <div className="absolute inset-0 bg-gradient-to-br from-synapse-600/20 via-gray-900 to-purple-600/20" />
              <div className="absolute inset-0 bg-gray-900/60 backdrop-blur-sm" />
              <div className="absolute inset-0 border border-gray-700/30 rounded-3xl" />

              <div className="relative text-center">
                <h2 className="text-4xl lg:text-5xl font-bold text-white mb-4">
                  Ready to get started?
                </h2>
                <p className="text-lg text-gray-400 max-w-xl mx-auto mb-10">
                  Add Synapse to your project in minutes and start building with any LLM provider.
                </p>
                <div className="flex flex-wrap justify-center gap-4">
                  <Link
                    to="/getting-started"
                    className="inline-flex items-center gap-2 px-8 py-3.5 rounded-xl bg-synapse-600 hover:bg-synapse-500 text-white font-medium transition-all hover:shadow-lg hover:shadow-synapse-500/25 text-lg"
                  >
                    Get Started
                    <ArrowRight className="w-5 h-5" />
                  </Link>
                  <Link
                    to="/api-reference"
                    className="inline-flex items-center gap-2 px-8 py-3.5 rounded-xl bg-gray-800/60 hover:bg-gray-800 text-white font-medium border border-gray-700/50 transition-all text-lg"
                  >
                    API Reference
                  </Link>
                </div>
              </div>
            </div>
          </FadeIn>
        </div>
      </section>
    </div>
  )
}
