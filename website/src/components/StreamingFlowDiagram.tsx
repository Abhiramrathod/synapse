import { motion } from 'motion/react'

interface SeqMessage {
  from: 'app' | 'hub' | 'api'
  to: 'app' | 'hub' | 'api'
  label: string
  detail?: string
  color: string
  dashed?: boolean
}

const messages: SeqMessage[] = [
  { from: 'app', to: 'hub', label: 'streamChat(msgs, listener)', color: 'text-synapse-400' },
  { from: 'hub', to: 'api', label: 'POST /chat/completions (stream: true)', color: 'text-purple-400' },
  { from: 'api', to: 'hub', label: 'data: {"delta": {"content": "Hello"}}', color: 'text-green-400', detail: 'SSE chunk 1' },
  { from: 'hub', to: 'app', label: 'listener.onChunk("Hello")', color: 'text-yellow-400' },
  { from: 'api', to: 'hub', label: 'data: {"delta": {"content": " world"}}', color: 'text-green-400', detail: 'SSE chunk 2' },
  { from: 'hub', to: 'app', label: 'listener.onChunk(" world")', color: 'text-yellow-400' },
  { from: 'api', to: 'hub', label: 'data: [DONE]', color: 'text-gray-400', detail: 'stream end' },
  { from: 'hub', to: 'app', label: 'listener.onComplete(response)', color: 'text-green-400' },
  { from: 'hub', to: 'app', label: 'handle.getFuture().join() completes', color: 'text-synapse-400', dashed: true },
]

const colLabels = [
  { key: 'app' as const, label: 'Application', color: 'text-synapse-400', bg: 'bg-synapse-500/10', border: 'border-synapse-500/20' },
  { key: 'hub' as const, label: 'SynapseHub', color: 'text-purple-400', bg: 'bg-purple-500/10', border: 'border-purple-500/20' },
  { key: 'api' as const, label: 'LLM API', color: 'text-green-400', bg: 'bg-green-500/10', border: 'border-green-500/20' },
]

function colX(key: string) {
  if (key === 'app') return '16.67%'
  if (key === 'hub') return '50%'
  return '83.33%'
}

export default function StreamingFlowDiagram() {
  let delay = 0
  const d = () => { delay += 0.05; return delay }

  return (
    <div className="py-4 overflow-x-auto">
      <div className="min-w-[520px] relative" style={{ height: `${messages.length * 56 + 52}px` }}>
        {/* Column headers */}
        {colLabels.map((col) => (
          <motion.div
            key={col.key}
            initial={{ opacity: 0, y: -8 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: d(), duration: 0.3 }}
            className={`absolute top-0 -translate-x-1/2 px-4 py-2 rounded-lg ${col.bg} border ${col.border} z-10`}
            style={{ left: colX(col.key) }}
          >
            <span className={`text-xs font-semibold ${col.color}`}>{col.label}</span>
          </motion.div>
        ))}

        {/* Lifelines */}
        {colLabels.map((col) => (
          <div
            key={`line-${col.key}`}
            className="absolute top-10 bottom-0 w-px bg-gradient-to-b from-gray-700/60 to-transparent"
            style={{ left: colX(col.key) }}
          />
        ))}

        {/* Messages */}
        {messages.map((msg, i) => {
          const y = 52 + i * 56
          const fromX = colX(msg.from)
          const toX = colX(msg.to)
          const isRight = colLabels.findIndex(c => c.key === msg.to) > colLabels.findIndex(c => c.key === msg.from)

          return (
            <motion.div
              key={i}
              initial={{ opacity: 0, x: isRight ? -10 : 10 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ delay: d(), duration: 0.3 }}
              className="absolute"
              style={{ top: `${y}px`, left: 0, right: 0 }}
            >
              {/* Arrow line */}
              <svg className="absolute inset-0 w-full h-full" style={{ top: 0 }}>
                <defs>
                  <marker id={`arrow-${i}`} markerWidth="6" markerHeight="4" refX="5" refY="2" orient="auto">
                    <path d="M0,0 L6,2 L0,4" fill="none" stroke="currentColor" strokeWidth="1" className={msg.color} />
                  </marker>
                </defs>
                <line
                  x1={fromX}
                  y1="12"
                  x2={toX}
                  y2="12"
                  stroke="currentColor"
                  strokeWidth="1"
                  className={msg.color}
                  strokeDasharray={msg.dashed ? '4,3' : 'none'}
                  markerEnd={`url(#arrow-${i})`}
                  opacity="0.6"
                />
              </svg>

              {/* Label */}
              <div
                className="absolute px-2 py-1 rounded bg-gray-900/80 backdrop-blur-sm border border-gray-800/50 whitespace-nowrap"
                style={{
                  left: `calc(${fromX} + (${isRight ? '' : '-'}8px))`,
                  transform: isRight ? 'translateX(0)' : 'translateX(-100%)',
                  top: '-4px',
                }}
              >
                <span className={`text-[10px] font-mono ${msg.color}`}>{msg.label}</span>
                {msg.detail && (
                  <span className="text-[9px] text-gray-500 ml-1.5">({msg.detail})</span>
                )}
              </div>
            </motion.div>
          )
        })}
      </div>
    </div>
  )
}
