import { motion } from 'motion/react'
import { Code2, Workflow, Shield, RefreshCw, Gauge, Blocks, Globe } from 'lucide-react'

const layers = [
  {
    label: 'Your Application',
    sublabel: 'Java 17+ / Spring Boot 3.x',
    color: 'from-synapse-600/20 to-synapse-500/10',
    border: 'border-synapse-500/30',
    text: 'text-synapse-400',
    icon: Code2,
    iconColor: 'text-synapse-400',
  },
  {
    label: 'ISynapseHub',
    sublabel: '12-method interface, zero provider coupling',
    color: 'from-purple-600/20 to-purple-500/10',
    border: 'border-purple-500/30',
    text: 'text-purple-400',
    icon: Workflow,
    iconColor: 'text-purple-400',
    wide: true,
  },
]

const middleComponents = [
  { label: 'Retry Handler', sublabel: 'Jittered backoff + Retry-After', icon: RefreshCw, color: 'text-yellow-400', bg: 'bg-yellow-500/10', border: 'border-yellow-500/20' },
  { label: 'Circuit Breaker', sublabel: 'CLOSED → OPEN → HALF_OPEN', icon: Shield, color: 'text-cyan-400', bg: 'bg-cyan-500/10', border: 'border-cyan-500/20' },
  { label: 'Concurrency Limiter', sublabel: 'Semaphore-based bounded permits', icon: Gauge, color: 'text-green-400', bg: 'bg-green-500/10', border: 'border-green-500/20' },
]

const bottomLayers = [
  {
    label: 'synapse-http',
    sublabel: 'Shared HttpClient · Streaming · SSE Parser',
    color: 'from-green-600/20 to-green-500/10',
    border: 'border-green-500/30',
    text: 'text-green-400',
    icon: Blocks,
    iconColor: 'text-green-400',
  },
  {
    label: 'LLM Provider API',
    sublabel: 'OpenAI · Anthropic · Cohere · Any OpenAI-compatible',
    color: 'from-gray-700/30 to-gray-600/10',
    border: 'border-gray-600/30',
    text: 'text-gray-300',
    icon: Globe,
    iconColor: 'text-gray-400',
  },
]

function Arrow() {
  return (
    <div className="flex justify-center py-1">
      <svg width="24" height="20" viewBox="0 0 24 20" fill="none">
        <path d="M12 0 L12 14 M6 10 L12 16 L18 10" stroke="url(#arrowGrad)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        <defs>
          <linearGradient id="arrowGrad" x1="12" y1="0" x2="12" y2="16">
            <stop offset="0%" stopColor="#a8a29e" stopOpacity="0.6" />
            <stop offset="100%" stopColor="#e7e5e4" stopOpacity="0.8" />
          </linearGradient>
        </defs>
      </svg>
    </div>
  )
}

function LayerBlock({ layer, delay }: { layer: typeof layers[0]; delay: number }) {
  const Icon = layer.icon
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay, duration: 0.4 }}
      className={`w-full max-w-lg mx-auto px-6 py-4 rounded-xl bg-gradient-to-r ${layer.color} border ${layer.border} backdrop-blur-sm`}
    >
      <div className="flex items-center gap-3">
        <div className={`p-2 rounded-lg bg-gray-900/40 ${layer.iconColor}`}>
          <Icon className="w-4 h-4" />
        </div>
        <div>
          <div className={`font-semibold text-sm ${layer.text}`}>{layer.label}</div>
          <div className="text-xs text-gray-500">{layer.sublabel}</div>
        </div>
      </div>
    </motion.div>
  )
}

export default function ArchitectureDiagram() {
  return (
    <div className="py-4">
      {/* Layer 1: Your Application */}
      <LayerBlock layer={layers[0]} delay={0} />
      <Arrow />

      {/* Layer 2: ISynapseHub */}
      <LayerBlock layer={layers[1]} delay={0.08} />
      <Arrow />

      {/* Middle: Retry / Circuit Breaker / Concurrency */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ delay: 0.16, duration: 0.4 }}
        className="grid grid-cols-3 gap-2 max-w-lg mx-auto"
      >
        {middleComponents.map((comp) => {
          const Icon = comp.icon
          return (
            <div key={comp.label} className={`px-3 py-3 rounded-xl ${comp.bg} border ${comp.border} text-center`}>
              <Icon className={`w-4 h-4 ${comp.color} mx-auto mb-1.5`} />
              <div className={`text-[11px] font-semibold ${comp.color} leading-tight`}>{comp.label}</div>
              <div className="text-[9px] text-gray-500 mt-0.5 leading-tight">{comp.sublabel}</div>
            </div>
          )
        })}
      </motion.div>
      <Arrow />

      {/* Layer 4: synapse-http */}
      <LayerBlock layer={bottomLayers[0]} delay={0.24} />
      <Arrow />

      {/* Layer 5: LLM Provider */}
      <LayerBlock layer={bottomLayers[1]} delay={0.32} />
    </div>
  )
}
