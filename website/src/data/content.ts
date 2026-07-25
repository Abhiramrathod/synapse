import {
  Zap, Shield, Gauge, Puzzle, RefreshCw, BarChart3,
  Blocks, Code2, Workflow, Terminal, Globe, Layers
} from 'lucide-react'

export const features = [
  {
    icon: Globe,
    title: 'Provider Agnostic',
    description: 'Works with any LLM API that follows the OpenAI-compatible format. OpenAI, Anthropic, Cohere, and more.',
    color: 'text-synapse-400',
    bgColor: 'bg-synapse-500/10',
    borderColor: 'border-synapse-500/20',
  },
  {
    icon: Zap,
    title: 'Explicit Streaming',
    description: 'Decide per-request whether to use synchronous or streaming mode with intuitive APIs.',
    color: 'text-yellow-400',
    bgColor: 'bg-yellow-500/10',
    borderColor: 'border-yellow-500/20',
  },
  {
    icon: Layers,
    title: 'Modular Architecture',
    description: 'Pick only what you need via focused Maven modules. No unnecessary dependencies.',
    color: 'text-neon-green',
    bgColor: 'bg-green-500/10',
    borderColor: 'border-green-500/20',
  },
  {
    icon: Workflow,
    title: 'Interceptor Pattern',
    description: 'Customize request/response handling with pluggable interceptors for logging, metrics, and more.',
    color: 'text-neon-purple',
    bgColor: 'bg-purple-500/10',
    borderColor: 'border-purple-500/20',
  },
  {
    icon: RefreshCw,
    title: 'Automatic Retry',
    description: 'Built-in exponential backoff with configurable retry policies for transient failures.',
    color: 'text-neon-blue',
    bgColor: 'bg-cyan-500/10',
    borderColor: 'border-cyan-500/20',
  },
  {
    icon: BarChart3,
    title: 'Metrics Tracking',
    description: 'Monitor latency, token usage, and request success rates out of the box.',
    color: 'text-orange-400',
    bgColor: 'bg-orange-500/10',
    borderColor: 'border-orange-500/20',
  },
  {
    icon: Shield,
    title: 'Spring Boot Integration',
    description: 'Auto-configuration for Spring Boot with YAML properties and dependency injection.',
    color: 'text-green-400',
    bgColor: 'bg-green-500/10',
    borderColor: 'border-green-500/20',
  },
  {
    icon: Gauge,
    title: 'Production Ready',
    description: 'Structured exception hierarchy, connection pooling, and comprehensive error handling.',
    color: 'text-red-400',
    bgColor: 'bg-red-500/10',
    borderColor: 'border-red-500/20',
  },
]

export const modules = [
  {
    name: 'synapse-core',
    description: 'Core interfaces, models, and exceptions',
    icon: Code2,
    color: 'from-synapse-500 to-synapse-700',
    items: ['ISynapseHub', 'ChatMessage', 'SynapseResponse', 'SynapseException'],
  },
  {
    name: 'synapse-interceptors',
    description: 'Request/response interceptor contracts',
    icon: Workflow,
    color: 'from-purple-500 to-purple-700',
    items: ['SynapseRequestInterceptor', 'SynapseResponseInterceptor', 'SynapseRetryPolicy', 'SynapseMetricsListener'],
  },
  {
    name: 'synapse-config',
    description: 'Configuration management with builder',
    icon: Terminal,
    color: 'from-cyan-500 to-cyan-700',
    items: ['SynapseConfig', 'Builder Pattern', 'Validation'],
  },
  {
    name: 'synapse-http',
    description: 'HTTP transport and orchestration',
    icon: Blocks,
    color: 'from-neon-green/80 to-green-700',
    items: ['SynapseHub', 'HttpClient', 'StreamHandler', 'RetryHandler'],
  },
  {
    name: 'synapse-metrics',
    description: 'Metrics collection and tracking',
    icon: BarChart3,
    color: 'from-orange-500 to-orange-700',
    items: ['SynapseMetrics', 'MetricsCollector'],
  },
  {
    name: 'synapse-spring-boot-starter',
    description: 'Spring Boot auto-configuration',
    icon: Puzzle,
    color: 'from-pink-500 to-pink-700',
    items: ['AutoConfiguration', 'Properties Binding', 'Conditional Beans'],
  },
]

export const exceptionTypes = [
  { type: 'CONFIG_ERROR', description: 'Configuration validation failed', retryable: false },
  { type: 'NETWORK_ERROR', description: 'Network connectivity issues', retryable: true },
  { type: 'TIMEOUT_ERROR', description: 'Request timeout', retryable: true },
  { type: 'RATE_LIMIT_ERROR', description: 'API rate limit exceeded', retryable: true },
  { type: 'SERVER_ERROR', description: 'LLM API server error', retryable: true },
  { type: 'PARSE_ERROR', description: 'Response parsing failed', retryable: false },
  { type: 'STREAMING_ERROR', description: 'Streaming connection error', retryable: false },
  { type: 'RETRY_EXHAUSTED', description: 'Max retries exceeded', retryable: false },
]

export const apiMethods = [
  {
    method: 'sendPrompt',
    signature: 'sendPrompt(String prompt) -> SynapseResponse',
    description: 'Send a simple text prompt and receive a synchronous response.',
    module: 'synapse-core',
  },
  {
    method: 'sendChat',
    signature: 'sendChat(List<ChatMessage> messages) -> SynapseResponse',
    description: 'Send a multi-turn conversation and receive a synchronous response.',
    module: 'synapse-core',
  },
  {
    method: 'chatCompletion',
    signature: 'chatCompletion(String prompt) -> SynapseResponse',
    description: 'Convenience method for chat completion with a single prompt.',
    module: 'synapse-core',
  },
  {
    method: 'streamPrompt',
    signature: 'streamPrompt(String prompt, Consumer<String> onChunk)',
    description: 'Stream a text prompt, receiving tokens via callback as they arrive.',
    module: 'synapse-core',
  },
  {
    method: 'streamChat',
    signature: 'streamChat(List<ChatMessage> messages, Consumer<String> onChunk)',
    description: 'Stream a multi-turn conversation with real-time token delivery.',
    module: 'synapse-core',
  },
  {
    method: 'streamCompletion',
    signature: 'streamCompletion(String prompt, Consumer<String> onChunk)',
    description: 'Stream completion with real-time token delivery.',
    module: 'synapse-core',
  },
]
