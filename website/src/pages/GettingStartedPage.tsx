import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Package, Play, Code2, Wrench, Settings, ArrowRight, CheckCircle2, Copy, Terminal, Check, X } from 'lucide-react'
import FadeIn from '../components/FadeIn'
import CodeBlock from '../components/CodeBlock'
import Badge from '../components/Badge'
import {
  mavenXml,
  quickStartCode,
  streamingCode,
  springBootCode,
  springBootServiceCode,
  interceptorCode,
  retryPolicyCode,
  fullConfigCode,
  errorHandlingCode,
} from '../data/codeExamples'
import { exceptionTypes } from '../data/content'

const prerequisites = [
  'Java 17 or higher',
  'Maven 3.8 or higher',
  'An LLM API key (OpenAI, Anthropic, etc.)',
]

export default function GettingStartedPage() {
  const [installTab, setInstallTab] = useState<'pure' | 'spring'>('pure')

  return (
    <div className="min-h-screen">
      {/* Page Header */}
      <section className="relative py-20 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-b from-synapse-600/5 via-transparent to-transparent" />
        <div className="max-w-5xl mx-auto px-6 relative">
          <FadeIn>
            <div className="flex items-center gap-3 mb-6">
              <div className="bg-synapse-600/20 p-3 rounded-xl border border-synapse-500/20">
                <Package className="w-6 h-6 text-synapse-400" />
              </div>
              <Badge>5 min read</Badge>
            </div>
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-4">
              Getting Started
            </h1>
            <p className="text-xl text-gray-400 max-w-2xl">
              Get up and running with Synapse in minutes. This guide walks you through installation,
              configuration, and your first API calls.
            </p>
          </FadeIn>
        </div>
      </section>

      {/* Prerequisites */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-8 flex items-center gap-3">
              <Settings className="w-6 h-6 text-synapse-400" />
              Prerequisites
            </h2>
            <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl p-6">
              <div className="space-y-4">
                {prerequisites.map((item) => (
                  <div key={item} className="flex items-center gap-3">
                    <CheckCircle2 className="w-5 h-5 text-green-400 shrink-0" />
                    <span className="text-gray-300">{item}</span>
                  </div>
                ))}
              </div>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Installation */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-8 flex items-center gap-3">
              <Package className="w-6 h-6 text-synapse-400" />
              Installation
            </h2>

            <div className="flex gap-1 p-1 bg-gray-900/60 rounded-xl border border-gray-800/50 w-fit mb-6">
              <button
                onClick={() => setInstallTab('pure')}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  installTab === 'pure'
                    ? 'bg-synapse-600/20 text-synapse-400 border-b-2 border-synapse-400'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Pure Java
              </button>
              <button
                onClick={() => setInstallTab('spring')}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  installTab === 'spring'
                    ? 'bg-synapse-600/20 text-synapse-400 border-b-2 border-synapse-400'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Spring Boot
              </button>
            </div>

            <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
              <div className="p-4 border-b border-gray-800/50">
                <div className="flex items-center gap-2 text-sm text-gray-400">
                  <span>Hosted on</span>
                  <span className="text-white font-semibold">JitPack</span>
                  <a
                    href="https://jitpack.io/#Abhiramrathod/synapse"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-synapse-400 hover:text-synapse-300 underline underline-offset-2"
                  >
                    View on JitPack
                  </a>
                </div>
              </div>
              <CodeBlock
                code={mavenXml}
                language="xml"
                title="pom.xml"
              />
            </div>

            {installTab === 'pure' ? (
              <div className="mt-4 p-4 bg-gray-900/40 rounded-xl border border-gray-800/30">
                <p className="text-sm text-gray-400">
                  <span className="text-white font-medium">Pure Java:</span> Use <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">synapse-all</code> for a standalone Java application with all modules bundled.
                </p>
              </div>
            ) : (
              <div className="mt-4 p-4 bg-gray-900/40 rounded-xl border border-gray-800/30">
                <p className="text-sm text-gray-400">
                  <span className="text-white font-medium">Spring Boot:</span> Use <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">synapse-spring-boot-starter</code> for auto-configured Spring Boot integration with YAML properties.
                </p>
              </div>
            )}
          </FadeIn>
        </div>
      </section>

      {/* Quick Start Steps */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-8 flex items-center gap-3">
              <Play className="w-6 h-6 text-synapse-400" />
              Quick Start
            </h2>
          </FadeIn>

          <div className="space-y-12">
            {/* Step 1 */}
            <FadeIn delay={0.1}>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <div>
                  <div className="flex items-center gap-3 mb-3">
                    <div className="bg-synapse-600 text-white rounded-full w-10 h-10 flex items-center justify-center font-bold">
                      1
                    </div>
                    <h3 className="text-xl font-semibold text-white">Create Configuration</h3>
                  </div>
                  <p className="text-gray-400 mb-4">
                    Build your SynapseConfig using the builder pattern. Set your API endpoint, key, model, and tuning parameters.
                  </p>
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <Code2 className="w-4 h-4" />
                    <span>synapse-config module</span>
                  </div>
                </div>
                <CodeBlock
                  code={`SynapseConfig config = SynapseConfig.builder()
        .baseUrl("https://api.openai.com")
        .endpoint("/v1/chat/completions")
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4")
        .temperature(0.7)
        .maxTokens(1024)
        .build();`}
                  language="java"
                  title="SynapseConfig.java"
                />
              </div>
            </FadeIn>

            {/* Step 2 */}
            <FadeIn delay={0.2}>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <div>
                  <div className="flex items-center gap-3 mb-3">
                    <div className="bg-synapse-600 text-white rounded-full w-10 h-10 flex items-center justify-center font-bold">
                      2
                    </div>
                    <h3 className="text-xl font-semibold text-white">Create Hub & Send Prompts</h3>
                  </div>
                  <p className="text-gray-400 mb-4">
                    Create a <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">SynapseHub</code> instance with your config. Send simple prompts or multi-turn conversations.
                  </p>
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <Terminal className="w-4 h-4" />
                    <span>Use try-with-resources for auto-close</span>
                  </div>
                </div>
                <CodeBlock
                  code={quickStartCode}
                  language="java"
                  title="QuickStart.java"
                />
              </div>
            </FadeIn>

            {/* Step 3 */}
            <FadeIn delay={0.3}>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <div>
                  <div className="flex items-center gap-3 mb-3">
                    <div className="bg-synapse-600 text-white rounded-full w-10 h-10 flex items-center justify-center font-bold">
                      3
                    </div>
                    <h3 className="text-xl font-semibold text-white">Streaming</h3>
                  </div>
                  <p className="text-gray-400 mb-4">
                    Enable real-time token streaming with <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">streamPrompt</code> and <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">streamChat</code>. Tokens arrive via a callback as they're generated.
                  </p>
                  <div className="flex items-center gap-2 text-sm text-gray-500">
                    <Wrench className="w-4 h-4" />
                    <span>Reactive streaming pattern</span>
                  </div>
                </div>
                <CodeBlock
                  code={streamingCode}
                  language="java"
                  title="Streaming.java"
                />
              </div>
            </FadeIn>
          </div>
        </div>
      </section>

      {/* Spring Boot Integration */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <Settings className="w-6 h-6 text-synapse-400" />
              Spring Boot Integration
            </h2>
            <p className="text-gray-400 mb-8 max-w-2xl">
              Spring Boot auto-configuration handles everything. Add the starter dependency, configure via YAML, and inject <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">ISynapseHub</code> where you need it.
            </p>
          </FadeIn>

          <div className="space-y-8">
            <FadeIn delay={0.1}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-green-400" />
                  <span className="text-sm font-medium text-white">application.yml</span>
                  <span className="text-xs text-gray-500 ml-auto">Auto-configured properties</span>
                </div>
                <CodeBlock code={springBootCode} language="yaml" title="application.yml" showLineNumbers={false} />
              </div>
            </FadeIn>

            <FadeIn delay={0.2}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-synapse-400" />
                  <span className="text-sm font-medium text-white">LlmService.java</span>
                  <span className="text-xs text-gray-500 ml-auto">Dependency injection</span>
                </div>
                <CodeBlock code={springBootServiceCode} language="java" title="LlmService.java" />
              </div>
            </FadeIn>
          </div>
        </div>
      </section>

      {/* Advanced Usage */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <Wrench className="w-6 h-6 text-synapse-400" />
              Advanced Usage
            </h2>
            <p className="text-gray-400 mb-8 max-w-2xl">
              Fine-tune Synapse with custom interceptors, retry policies, and comprehensive error handling.
            </p>
          </FadeIn>

          <div className="space-y-8">
            {/* Custom Request Interceptor */}
            <FadeIn delay={0.1}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50">
                  <h3 className="text-lg font-semibold text-white">Custom Request Interceptor</h3>
                  <p className="text-sm text-gray-400 mt-1">Implement <code className="text-synapse-400">SynapseRequestInterceptor</code> to hook into the request lifecycle.</p>
                </div>
                <CodeBlock code={interceptorCode} language="java" title="LoggingInterceptor.java" />
              </div>
            </FadeIn>

            {/* Custom Retry Policy */}
            <FadeIn delay={0.2}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50">
                  <h3 className="text-lg font-semibold text-white">Custom Retry Policy</h3>
                  <p className="text-sm text-gray-400 mt-1">Implement <code className="text-synapse-400">SynapseRetryPolicy</code> to control retry behavior with exponential backoff.</p>
                </div>
                <CodeBlock code={retryPolicyCode} language="java" title="CustomRetryPolicy.java" />
              </div>
            </FadeIn>

            {/* Full Configuration */}
            <FadeIn delay={0.3}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50">
                  <h3 className="text-lg font-semibold text-white">Full Configuration Example</h3>
                  <p className="text-sm text-gray-400 mt-1">Complete configuration with all available options.</p>
                </div>
                <CodeBlock code={fullConfigCode} language="java" title="FullConfig.java" />
              </div>
            </FadeIn>

            {/* Error Handling */}
            <FadeIn delay={0.4}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50">
                  <h3 className="text-lg font-semibold text-white">Error Handling</h3>
                  <p className="text-sm text-gray-400 mt-1">Structured exception types with retryable flags for robust error handling.</p>
                </div>
                <CodeBlock code={errorHandlingCode} language="java" title="ErrorHandling.java" />
              </div>
            </FadeIn>
          </div>
        </div>
      </section>

      {/* Exception Types Table */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <Terminal className="w-6 h-6 text-synapse-400" />
              Exception Types
            </h2>
            <p className="text-gray-400 mb-8 max-w-2xl">
              Synapse provides a structured exception hierarchy for precise error handling and retry logic.
            </p>
          </FadeIn>

          <FadeIn delay={0.1}>
            <div className="bg-gray-900/60 rounded-xl overflow-hidden border border-gray-800/50">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-800">
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">Exception Type</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-300">Description</th>
                    <th className="px-6 py-4 text-center text-sm font-semibold text-gray-300">Retryable</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800">
                  {exceptionTypes.map((ex) => (
                    <tr key={ex.type} className="hover:bg-gray-800/30 transition-colors">
                      <td className="px-6 py-4">
                        <code className="text-synapse-400 bg-synapse-500/10 px-2 py-1 rounded text-sm font-mono">
                          {ex.type}
                        </code>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-400">{ex.description}</td>
                      <td className="px-6 py-4 text-center">
                        {ex.retryable ? (
                          <Check className="w-5 h-5 text-green-400 mx-auto" />
                        ) : (
                          <X className="w-5 h-5 text-red-400 mx-auto" />
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Next Steps CTA */}
      <section className="py-20">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <div className="bg-gradient-to-br from-synapse-600/10 via-gray-900/60 to-purple-600/10 backdrop-blur-xl border border-gray-800/50 rounded-2xl p-8 md:p-12 text-center">
              <h2 className="text-2xl md:text-3xl font-bold text-white mb-4">
                Ready to dive deeper?
              </h2>
              <p className="text-gray-400 mb-8 max-w-xl mx-auto">
                Explore the full API reference for detailed class documentation.
              </p>
              <div className="flex items-center justify-center">
                <Link
                  to="/synapse/api"
                  className="inline-flex items-center gap-2 px-6 py-3 bg-synapse-600 hover:bg-synapse-500 text-white rounded-xl font-medium transition-all group"
                >
                  <Code2 className="w-4 h-4" />
                  API Reference
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                </Link>
              </div>
            </div>
          </FadeIn>
        </div>
      </section>
    </div>
  )
}
