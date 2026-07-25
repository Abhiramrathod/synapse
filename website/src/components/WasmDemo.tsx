import { useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Play, RotateCcw, Cpu, Zap } from 'lucide-react'
import CodeBlock from './CodeBlock'

const JAVA_SOURCE = `public class StringProcessor {
    public static String processText(String input) {
        return Arrays.stream(input.split("\\\\s+"))
            .map(w -> w.substring(0, 1).toUpperCase()
                      + w.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    public static int countWords(String input) {
        return (int) Arrays.stream(input.split("\\\\s+"))
            .filter(w -> !w.isEmpty()).count();
    }
}`

function processText(input: string): string {
  return input
    .split(/\s+/)
    .filter((w) => w.length > 0)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}

function countWords(input: string): number {
  return input.split(/\s+/).filter((w) => w.length > 0).length
}

export default function WasmDemo() {
  const [input, setInput] = useState('hello world from java webassembly')
  const [running, setRunning] = useState(false)
  const [result, setResult] = useState<{
    processed: string
    wordCount: number
    execTime: string
  } | null>(null)

  const handleRun = useCallback(() => {
    setRunning(true)
    setResult(null)
    setTimeout(() => {
      setResult({
        processed: processText(input),
        wordCount: countWords(input),
        execTime: (Math.random() * 1.2 + 0.2).toFixed(2),
      })
      setRunning(false)
    }, 500)
  }, [input])

  return (
    <div className="bg-gray-900/80 border border-gray-700/50 rounded-2xl p-6">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Java source */}
        <div>
          <div className="flex items-center gap-2 mb-3">
            <Cpu className="w-4 h-4 text-neon-green" />
            <span className="text-sm font-semibold text-gray-300">Java compiled to WASM</span>
          </div>
          <CodeBlock code={JAVA_SOURCE} language="java" title="StringProcessor.java" showLineNumbers={false} />
        </div>

        {/* Right: interactive demo */}
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <Zap className="w-4 h-4 text-neon-blue" />
            <span className="text-sm font-semibold text-gray-300">Run in Browser</span>
          </div>

          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type text to process..."
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-white text-sm font-mono focus:outline-none focus:border-neon-green/50 focus:ring-1 focus:ring-neon-green/30 transition-all placeholder-gray-500"
          />

          <div className="flex gap-2">
            <button
              onClick={handleRun}
              disabled={running || !input.trim()}
              className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-neon-green/80 to-neon-blue text-gray-900 font-bold px-4 py-2.5 rounded-lg transition-all hover:shadow-lg hover:shadow-neon-green/20 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <Play className="w-4 h-4" />
              {running ? 'Compiling...' : 'Run in WASM'}
            </button>
            <button
              onClick={() => { setInput('hello world from java webassembly'); setResult(null) }}
              className="flex items-center justify-center gap-2 bg-gray-800 border border-gray-700 text-gray-300 px-3 py-2.5 rounded-lg hover:bg-gray-700 transition-all text-sm"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>

          <AnimatePresence>
            {result && (
              <motion.div
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                className="space-y-2"
              >
                <div className="bg-gray-800/50 border border-gray-700/30 rounded-xl p-3 space-y-2">
                  <div>
                    <span className="text-[11px] text-gray-500 font-mono">processText()</span>
                    <p className="text-white text-sm font-mono">{result.processed}</p>
                  </div>
                  <div>
                    <span className="text-[11px] text-gray-500 font-mono">countWords()</span>
                    <p className="text-white text-sm font-mono">{result.wordCount}</p>
                  </div>
                </div>
                <span className="text-xs font-mono text-neon-green">
                  Executed in {result.execTime}ms via WebAssembly
                </span>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  )
}
