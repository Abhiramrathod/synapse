import { useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  Play,
  Pause,
  RotateCcw,
  Terminal,
  Cpu,
  Globe,
  Zap,
  Code2,
  ArrowRight,
  ExternalLink,
  Download,
} from 'lucide-react'
import FadeIn from '../components/FadeIn'
import CodeBlock from '../components/CodeBlock'
import Badge from '../components/Badge'

const JAVA_SOURCE = `import java.util.stream.Collectors;
import java.util.Arrays;

public class StringProcessor {
    public static String processText(String input) {
        return Arrays.stream(input.split("\\\\s+"))
            .map(word -> word.substring(0, 1).toUpperCase()
                      + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    public static String reverseWords(String input) {
        return Arrays.stream(input.split("\\\\s+"))
            .map(StringBuffer::new)
            .map(StringBuffer::reverse)
            .map(StringBuffer::toString)
            .collect(Collectors.joining(" "));
    }

    public static int countWords(String input) {
        return (int) Arrays.stream(input.split("\\\\s+"))
            .filter(w -> !w.isEmpty())
            .count();
    }
}`

const LLM_SOURCE = `public class LlmClient {
    // Synapse adapted for WASM HTTP client
    private String apiEndpoint;
    private String apiKey;

    public LlmClient(String endpoint, String key) {
        this.apiEndpoint = endpoint;
        this.apiKey = key;
    }

    public native String sendPrompt(String prompt);
    // Compiled to WebAssembly import
    // Uses browser's fetch API under the hood
}`

const TEAVM_MAVEN = `<plugin>
    <groupId>org.teavm</groupId>
    <artifactId>teavm-maven-plugin</artifactId>
    <version>0.9.0</version>
    <configuration>
        <targetDirectory>\${project.build.directory}/wasm</targetDirectory>
        <targetType>WASM</targetType>
    </configuration>
</plugin>`

const COMPILATION_STEPS = [
  { label: 'Java Source', sub: '.java' },
  { label: 'javac', sub: 'compiler', icon: Code2 },
  { label: '.class Bytecode', sub: '.class' },
  { label: 'TeaVM / JWebAssembly', sub: 'transpiler', icon: Cpu },
  { label: '.wasm + JS Glue', sub: '.wasm' },
  { label: 'Browser Execution', sub: 'WebAssembly', icon: Globe },
]

const COMPARISON_ROWS = [
  { feature: 'Status', teavm: 'Stable', jweb: 'Experimental' },
  { feature: 'Output', teavm: 'Wasm + JS glue', jweb: 'Wasm + JS glue' },
  { feature: 'Java Support', teavm: 'Java 8+ subset', jweb: 'Java 8+ subset' },
  { feature: 'Performance', teavm: 'Good', jweb: 'Good' },
  { feature: 'Community', teavm: 'Active', jweb: 'Growing' },
  { feature: 'Integration', teavm: 'Maven plugin', jweb: 'Gradle plugin' },
]

const BROWSERS = [
  { name: 'Chrome', supported: true },
  { name: 'Firefox', supported: true },
  { name: 'Safari', supported: true },
  { name: 'Edge', supported: true },
]

const LIMITATIONS = [
  { title: 'No full JDK', desc: 'Only a subset of Java APIs is available in WASM compilation.' },
  { title: 'No threads', desc: 'Single-threaded execution only in the browser sandbox.' },
  { title: 'No reflection', desc: 'Limited or no reflection support depending on the compiler.' },
  { title: 'File I/O unavailable', desc: 'No access to the local filesystem from compiled code.' },
  { title: 'HTTP via fetch', desc: 'Network requests use the browser\'s fetch API under the hood.' },
]

function processText(input: string): string {
  return input
    .split(/\s+/)
    .filter((w) => w.length > 0)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

function reverseWords(input: string): string {
  return input
    .split(/\s+/)
    .filter((w) => w.length > 0)
    .map((w) => w.split('').reverse().join(''))
    .join(' ')
}

function countWords(input: string): number {
  return input.split(/\s+/).filter((w) => w.length > 0).length
}

function simulateWasmMetrics() {
  return {
    compileTime: Math.floor(Math.random() * 800 + 1200),
    executionTime: (Math.random() * 1.5 + 0.3).toFixed(2),
    binarySize: (Math.random() * 200 + 380).toFixed(0),
  }
}

function FlowDiagram() {
  return (
    <div className="flex flex-wrap items-center justify-center gap-3">
      {COMPILATION_STEPS.map((step, i) => (
        <div key={step.label} className="flex items-center gap-3">
          <div className="flex flex-col items-center gap-1.5">
            <div className="bg-gray-800/80 border border-gray-700/50 rounded-xl px-5 py-3 text-center min-w-[120px]">
              {step.icon && (
                <step.icon className="w-5 h-5 text-neon-green mx-auto mb-1" />
              )}
              <span className="text-sm font-semibold text-white block">{step.label}</span>
              <span className="text-xs text-gray-500 font-mono">{step.sub}</span>
            </div>
          </div>
          {i < COMPILATION_STEPS.length - 1 && (
            <ArrowRight className="w-5 h-5 text-gray-600 flex-shrink-0" />
          )}
        </div>
      ))}
    </div>
  )
}

function StringProcessorDemo() {
  const [input, setInput] = useState('hello world from java webassembly')
  const [running, setRunning] = useState(false)
  const [result, setResult] = useState<{
    processed: string
    reversed: string
    wordCount: number
    metrics: ReturnType<typeof simulateWasmMetrics>
  } | null>(null)
  const [activeTab, setActiveTab] = useState<'output' | 'code'>('code')

  const handleRun = useCallback(() => {
    setRunning(true)
    setResult(null)
    setTimeout(() => {
      setResult({
        processed: processText(input),
        reversed: reverseWords(input),
        wordCount: countWords(input),
        metrics: simulateWasmMetrics(),
      })
      setRunning(false)
    }, 600)
  }, [input])

  const handleReset = useCallback(() => {
    setInput('hello world from java webassembly')
    setResult(null)
    setRunning(false)
  }, [])

  return (
    <div className="bg-gray-900/80 border border-gray-700/50 rounded-2xl p-6 overflow-hidden">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-semibold text-gray-300 flex items-center gap-2">
              <Code2 className="w-4 h-4 text-neon-green" />
              Java Source
            </h4>
            <button
              onClick={() => setActiveTab(activeTab === 'code' ? 'output' : 'code')}
              className="text-xs text-gray-500 hover:text-gray-300 transition-colors lg:hidden"
            >
              {activeTab === 'code' ? 'Show Output' : 'Show Code'}
            </button>
          </div>
          <div className={`${activeTab === 'code' ? 'block' : 'hidden'} lg:block`}>
            <CodeBlock code={JAVA_SOURCE} language="java" title="StringProcessor.java" />
          </div>
        </div>

        <div className={`flex flex-col gap-4 ${activeTab === 'output' ? 'block' : 'hidden'} lg:block`}>
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-semibold text-gray-300 flex items-center gap-2">
              <Terminal className="w-4 h-4 text-neon-green" />
              Live Demo
            </h4>
          </div>

          <div>
            <label className="text-xs text-gray-500 mb-1.5 block">Input Text</label>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type text to process..."
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-3 text-white text-sm font-mono focus:outline-none focus:border-neon-green/50 focus:ring-1 focus:ring-neon-green/30 transition-all placeholder-gray-500"
            />
          </div>

          <div className="flex gap-3">
            <button
              onClick={handleRun}
              disabled={running || !input.trim()}
              className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-neon-green/80 to-neon-blue text-gray-900 font-bold px-4 py-3 rounded-lg transition-all hover:shadow-lg hover:shadow-neon-green/20 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              {running ? (
                <>
                  <Pause className="w-4 h-4" />
                  Compiling...
                </>
              ) : (
                <>
                  <Play className="w-4 h-4" />
                  Run in WASM
                </>
              )}
            </button>
            <button
              onClick={handleReset}
              className="flex items-center justify-center gap-2 bg-gray-800 border border-gray-700 text-gray-300 px-4 py-3 rounded-lg hover:bg-gray-700 transition-all text-sm"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>

          <AnimatePresence>
            {result && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.3 }}
                className="space-y-3"
              >
                <div className="bg-gray-800/50 border border-gray-700/30 rounded-xl p-4 space-y-3">
                  <div>
                    <span className="text-xs text-gray-500 font-mono block mb-1">processText()</span>
                    <p className="text-white text-sm font-mono">{result.processed}</p>
                  </div>
                  <div>
                    <span className="text-xs text-gray-500 font-mono block mb-1">reverseWords()</span>
                    <p className="text-white text-sm font-mono">{result.reversed}</p>
                  </div>
                  <div>
                    <span className="text-xs text-gray-500 font-mono block mb-1">countWords()</span>
                    <p className="text-white text-sm font-mono">{result.wordCount}</p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-3 text-xs font-mono text-neon-green">
                  <span>Compile: {result.metrics.compileTime}ms</span>
                  <span className="text-gray-600">|</span>
                  <span>Execute: {result.metrics.executionTime}ms</span>
                  <span className="text-gray-600">|</span>
                  <span>WASM: {result.metrics.binarySize}KB</span>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  )
}

function LlmDemo() {
  const [prompt, setPrompt] = useState('Explain WebAssembly in one sentence')
  const [running, setRunning] = useState(false)
  const [response, setResponse] = useState<string | null>(null)

  const mockResponses: Record<string, string> = {
    'Explain WebAssembly in one sentence':
      'WebAssembly is a binary instruction format for a stack-based virtual machine that enables near-native performance for web applications.',
  }

  const handleSend = useCallback(() => {
    setRunning(true)
    setResponse(null)
    setTimeout(() => {
      const key = Object.keys(mockResponses).find((k) => prompt.includes(k.substring(0, 20)))
      setResponse(
        key
          ? mockResponses[key]
          : `Received prompt: "${prompt}" — In a real WASM context, this would be sent via browser fetch to the Synapse LLM endpoint and the response would be returned as a Java String compiled to WebAssembly.`
      )
      setRunning(false)
    }, 1200)
  }, [prompt])

  return (
    <div className="bg-gray-900/80 border border-gray-700/50 rounded-2xl p-6">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <CodeBlock code={LLM_SOURCE} language="java" title="LlmClient.java" />
        </div>
        <div className="flex flex-col gap-4">
          <h4 className="text-sm font-semibold text-gray-300 flex items-center gap-2">
            <Zap className="w-4 h-4 text-neon-green" />
            Simulated LLM Call
          </h4>
          <div>
            <label className="text-xs text-gray-500 mb-1.5 block">Prompt</label>
            <input
              type="text"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Enter a prompt for the LLM..."
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-3 text-white text-sm font-mono focus:outline-none focus:border-neon-green/50 focus:ring-1 focus:ring-neon-green/30 transition-all placeholder-gray-500"
            />
          </div>
          <button
            onClick={handleSend}
            disabled={running || !prompt.trim()}
            className="flex items-center justify-center gap-2 bg-gradient-to-r from-neon-green/80 to-neon-blue text-gray-900 font-bold px-4 py-3 rounded-lg transition-all hover:shadow-lg hover:shadow-neon-green/20 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          >
            {running ? (
              <>
                <Pause className="w-4 h-4" />
                Sending...
              </>
            ) : (
              <>
                <Zap className="w-4 h-4" />
                Send to LLM
              </>
            )}
          </button>
          <AnimatePresence>
            {response && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.3 }}
                className="bg-gray-800/50 border border-gray-700/30 rounded-xl p-4"
              >
                <span className="text-xs text-gray-500 font-mono block mb-2">Response (via WASM fetch):</span>
                <p className="text-white text-sm font-mono leading-relaxed">{response}</p>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  )
}

export default function WebAssemblyPage() {
  return (
    <div className="min-h-screen bg-gray-950">
      {/* Hero */}
      <section className="py-20 border-b border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <Badge>Experimental</Badge>
            <h1 className="section-heading text-4xl md:text-5xl mt-4">
              <span className="gradient-text">Java WebAssembly</span>
            </h1>
            <p className="text-lg text-gray-400 mt-4 max-w-2xl">
              Run Java code directly in the browser with WebAssembly
            </p>
          </FadeIn>
        </div>
      </section>

      {/* Overview */}
      <section className="py-16">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">What is Java WebAssembly?</h2>
            <p className="section-subheading">
              WebAssembly enables running compiled Java bytecode directly in the browser without a
              full JVM. Tools like TeaVM and JWebAssembly transpile Java <code className="text-neon-green font-mono text-sm">.class</code> files
              into <code className="text-neon-green font-mono text-sm">.wasm</code> modules paired with lightweight JavaScript glue code, unlocking
              near-native performance for client-side Java logic.
            </p>
          </FadeIn>

          <FadeIn delay={0.1}>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-10">
              {[
                {
                  icon: Cpu,
                  title: 'TeaVM',
                  desc: 'Compiles Java bytecode to WebAssembly with an active community and stable releases.',
                },
                {
                  icon: Code2,
                  title: 'JWebAssembly',
                  desc: 'An alternative compiler focused on producing clean WASM output from Java class files.',
                },
                {
                  icon: Globe,
                  title: 'No JVM Needed',
                  desc: 'Java logic runs entirely client-side in the browser sandbox — no server-side execution required.',
                },
              ].map((item) => (
                <div
                  key={item.title}
                  className="bg-gray-900/60 border border-gray-800/50 rounded-xl p-6 hover:border-gray-700/80 transition-colors"
                >
                  <item.icon className="w-8 h-8 text-neon-green mb-3" />
                  <h3 className="text-white font-semibold mb-2">{item.title}</h3>
                  <p className="text-sm text-gray-400 leading-relaxed">{item.desc}</p>
                </div>
              ))}
            </div>
          </FadeIn>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">How It Works</h2>
            <p className="section-subheading">
              From Java source to browser execution — the compilation pipeline for Java WebAssembly.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10 bg-gray-900/60 border border-gray-800/50 rounded-2xl p-8 overflow-x-auto">
              <FlowDiagram />
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Live Demo: String Processing */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <div className="flex items-center gap-3 mb-2">
              <Badge variant="green">Interactive</Badge>
            </div>
            <h2 className="section-heading">Live Demo: Java String Processing in WASM</h2>
            <p className="section-subheading">
              Type any text and hit "Run" to see how Java string operations would execute in a
              WebAssembly runtime. The output below mirrors what the compiled Java code would produce.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10">
              <StringProcessorDemo />
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Demo: LLM Client */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <div className="flex items-center gap-3 mb-2">
              <Badge variant="blue">Synapse + WASM</Badge>
            </div>
            <h2 className="section-heading">HTTP Request Handler in WASM</h2>
            <p className="section-subheading">
              A simulated Synapse LLM client compiled to WebAssembly. The{' '}
              <code className="text-neon-green font-mono text-sm">native</code> method would be
              bridged to the browser's fetch API through WASM imports.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10">
              <LlmDemo />
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Comparison Table */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">TeaVM vs JWebAssembly</h2>
            <p className="section-subheading">
              A quick comparison of the two primary Java-to-WebAssembly compilers.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10 overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-800/50">
                    <th className="text-left py-3 px-4 text-gray-500 font-medium">Feature</th>
                    <th className="text-left py-3 px-4 text-neon-green font-medium">TeaVM</th>
                    <th className="text-left py-3 px-4 text-neon-blue font-medium">JWebAssembly</th>
                  </tr>
                </thead>
                <tbody>
                  {COMPARISON_ROWS.map((row) => (
                    <tr key={row.feature} className="border-b border-gray-800/30 hover:bg-gray-900/40 transition-colors">
                      <td className="py-3 px-4 text-gray-300 font-medium">{row.feature}</td>
                      <td className="py-3 px-4 text-gray-400">{row.teavm}</td>
                      <td className="py-3 px-4 text-gray-400">{row.jweb}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Getting Started with TeaVM */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">Getting Started with TeaVM</h2>
            <p className="section-subheading">
              Add the TeaVM Maven plugin to your project and configure it for WebAssembly output.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10 max-w-2xl">
              <CodeBlock code={TEAVM_MAVEN} language="xml" title="pom.xml" />
            </div>
          </FadeIn>
          <FadeIn delay={0.15}>
            <div className="mt-8 flex flex-wrap gap-4">
              <a
                href="https://teavm.org"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 text-sm text-neon-green hover:underline"
              >
                TeaVM Documentation
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
              <a
                href="https://github.com/nicholasgasior/jwebassembly"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 text-sm text-neon-blue hover:underline"
              >
                JWebAssembly on GitHub
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Browser Compatibility */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">Browser Compatibility</h2>
            <p className="section-subheading">
              WebAssembly is supported in all major modern browsers.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10 grid grid-cols-2 md:grid-cols-4 gap-4">
              {BROWSERS.map((b) => (
                <div
                  key={b.name}
                  className="bg-gray-900/60 border border-gray-800/50 rounded-xl p-5 text-center"
                >
                  <Globe className="w-6 h-6 text-neon-green mx-auto mb-2" />
                  <span className="text-white text-sm font-semibold block">{b.name}</span>
                  <span className="text-xs text-neon-green mt-1 block">Supported</span>
                </div>
              ))}
            </div>
          </FadeIn>
        </div>
      </section>

      {/* Limitations */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6">
          <FadeIn>
            <h2 className="section-heading">Limitations &amp; Caveats</h2>
            <p className="section-subheading">
              Java-to-WASM compilation comes with trade-offs you should be aware of.
            </p>
          </FadeIn>
          <FadeIn delay={0.1}>
            <div className="mt-10 grid grid-cols-1 md:grid-cols-2 gap-4">
              {LIMITATIONS.map((lim) => (
                <div
                  key={lim.title}
                  className="bg-gray-900/60 border border-gray-800/50 rounded-xl p-5 flex gap-4"
                >
                  <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-yellow-500/10 flex items-center justify-center mt-0.5">
                    <Pause className="w-4 h-4 text-yellow-400" />
                  </div>
                  <div>
                    <h4 className="text-white text-sm font-semibold mb-1">{lim.title}</h4>
                    <p className="text-xs text-gray-400 leading-relaxed">{lim.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </FadeIn>
        </div>
      </section>

      {/* CTA */}
      <section className="py-16 border-t border-gray-800/50">
        <div className="max-w-6xl mx-auto px-6 text-center">
          <FadeIn>
            <h2 className="section-heading">Ready to Try Synapse with WebAssembly?</h2>
            <p className="section-subheading mx-auto">
              Get started with Synapse and explore how Java WebAssembly can enhance your client-side
              applications.
            </p>
            <a
              href="/getting-started"
              className="inline-flex items-center gap-2 mt-8 bg-gradient-to-r from-neon-green/80 to-neon-blue text-gray-900 font-bold px-8 py-4 rounded-xl text-sm hover:shadow-lg hover:shadow-neon-green/20 transition-all"
            >
              <Download className="w-4 h-4" />
              Get Started
              <ArrowRight className="w-4 h-4" />
            </a>
          </FadeIn>
        </div>
      </section>
    </div>
  )
}
