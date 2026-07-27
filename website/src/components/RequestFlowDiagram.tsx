import { motion } from 'framer-motion'
import { AlertTriangle, CheckCircle2, XCircle, Clock, ArrowDown, ArrowRight } from 'lucide-react'

interface FlowStep {
  id: string
  label: string
  detail: string
  color: string
  borderColor: string
  iconColor: string
  branch?: { label: string; path: 'left' | 'right' }
}

const steps: FlowStep[] = [
  { id: 'call', label: 'hub.sendChat()', detail: 'messages + options', color: 'bg-synapse-600/15', borderColor: 'border-synapse-500/30', iconColor: 'text-synapse-400' },
  { id: 'cb', label: 'Circuit Breaker', detail: 'allowRequest()', color: 'bg-cyan-500/10', borderColor: 'border-cyan-500/20', iconColor: 'text-cyan-400' },
  { id: 'cl', label: 'Concurrency Limiter', detail: 'acquire() — block until slot', color: 'bg-green-500/10', borderColor: 'border-green-500/20', iconColor: 'text-green-400' },
  { id: 'intercept', label: 'beforeRequest()', detail: 'logging, tracing, headers', color: 'bg-purple-500/10', borderColor: 'border-purple-500/20', iconColor: 'text-purple-400' },
  { id: 'http', label: 'HttpClient.send()', detail: 'POST /chat/completions', color: 'bg-gray-700/30', borderColor: 'border-gray-600/30', iconColor: 'text-gray-300' },
]

const successPath: FlowStep[] = [
  { id: 'parse', label: 'Parse Response', detail: 'SynapseResponse', color: 'bg-green-500/10', borderColor: 'border-green-500/20', iconColor: 'text-green-400' },
  { id: 'record-ok', label: 'Record Success', detail: 'CB + metrics', color: 'bg-green-500/10', borderColor: 'border-green-500/20', iconColor: 'text-green-400' },
  { id: 'release', label: 'Release + Return', detail: 'SynapseResponse', color: 'bg-synapse-600/15', borderColor: 'border-synapse-500/30', iconColor: 'text-synapse-400' },
]

const errorPath: FlowStep[] = [
  { id: 'retry-check', label: 'Retry?', detail: 'attempt < maxRetries', color: 'bg-yellow-500/10', borderColor: 'border-yellow-500/20', iconColor: 'text-yellow-400' },
  { id: 'retry-sleep', label: 'Sleep + Jitter', detail: 'Retry-After or exponential', color: 'bg-yellow-500/10', borderColor: 'border-yellow-500/20', iconColor: 'text-yellow-400' },
  { id: 'record-fail', label: 'Record Failure', detail: 'CB + metrics', color: 'bg-red-500/10', borderColor: 'border-red-500/20', iconColor: 'text-red-400' },
  { id: 'throw', label: 'Throw SynapseException', detail: 'RETRY_EXHAUSTED / type', color: 'bg-red-500/10', borderColor: 'border-red-500/20', iconColor: 'text-red-400' },
]

function FlowNode({ step, delay, small }: { step: FlowStep; delay: number; small?: boolean }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: -8 }}
      whileInView={{ opacity: 1, x: 0 }}
      viewport={{ once: true }}
      transition={{ delay, duration: 0.3 }}
      className={`${small ? 'px-3 py-2' : 'px-4 py-3'} rounded-lg ${step.color} border ${step.border} backdrop-blur-sm`}
    >
      <div className="flex items-center gap-2">
        <div className={`w-1.5 h-1.5 rounded-full ${step.iconColor.replace('text-', 'bg-')}`} />
        <div>
          <div className={`font-semibold ${small ? 'text-[11px]' : 'text-xs'} ${step.iconColor}`}>{step.label}</div>
          <div className={`${small ? 'text-[9px]' : 'text-[10px]'} text-gray-500`}>{step.detail}</div>
        </div>
      </div>
    </motion.div>
  )
}

function VerticalArrow({ delay }: { delay: number }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      whileInView={{ opacity: 1 }}
      viewport={{ once: true }}
      transition={{ delay, duration: 0.2 }}
      className="flex justify-center py-0.5"
    >
      <ArrowDown className="w-3.5 h-3.5 text-gray-600" />
    </motion.div>
  )
}

function BranchArrow({ label, delay }: { label: string; delay: number }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      whileInView={{ opacity: 1 }}
      viewport={{ once: true }}
      transition={{ delay, duration: 0.2 }}
      className="flex items-center justify-center gap-1.5 py-0.5"
    >
      <ArrowRight className="w-3 h-3 text-gray-600" />
      <span className="text-[9px] text-gray-500 font-medium">{label}</span>
    </motion.div>
  )
}

export default function RequestFlowDiagram() {
  let delay = 0
  const d = () => { delay += 0.06; return delay }

  return (
    <div className="py-4">
      {/* Main flow */}
      <div className="max-w-md mx-auto space-y-0">
        {steps.map((step, i) => (
          <div key={step.id}>
            <FlowNode step={step} delay={d()} />
            {i < steps.length - 1 && <VerticalArrow delay={d()} />}
          </div>
        ))}
      </div>

      {/* Branch label */}
      <BranchArrow label="2xx Success" delay={d()} />

      {/* Success / Error split */}
      <div className="max-w-lg mx-auto grid grid-cols-2 gap-3 mt-1">
        {/* Success path */}
        <div className="space-y-0">
          <div className="flex items-center gap-1.5 mb-2 px-1">
            <CheckCircle2 className="w-3 h-3 text-green-400" />
            <span className="text-[10px] text-green-400 font-semibold uppercase tracking-wider">Success</span>
          </div>
          {successPath.map((step, i) => (
            <div key={step.id}>
              <FlowNode step={step} delay={d()} small />
              {i < successPath.length - 1 && <VerticalArrow delay={d()} />}
            </div>
          ))}
        </div>

        {/* Error path */}
        <div className="space-y-0">
          <div className="flex items-center gap-1.5 mb-2 px-1">
            <XCircle className="w-3 h-3 text-red-400" />
            <span className="text-[10px] text-red-400 font-semibold uppercase tracking-wider">4xx / 5xx</span>
          </div>
          {errorPath.map((step, i) => (
            <div key={step.id}>
              <FlowNode step={step} delay={d()} small />
              {i < errorPath.length - 1 && <VerticalArrow delay={d()} />}
            </div>
          ))}
          {/* Retry loop-back indicator */}
          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ delay: d(), duration: 0.3 }}
            className="mt-2 flex items-center gap-1.5 px-2 py-1.5 rounded-md bg-yellow-500/5 border border-yellow-500/10"
          >
            <Clock className="w-3 h-3 text-yellow-400 flex-shrink-0" />
            <span className="text-[9px] text-yellow-400/80">loops back to HttpClient.send()</span>
          </motion.div>
        </div>
      </div>
    </div>
  )
}
