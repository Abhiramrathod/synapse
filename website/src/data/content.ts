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
    title: 'Streaming & Cancellation',
    description: 'StreamListener with chunk/complete/error callbacks. Cancel mid-stream with StreamHandle. Flow.Publisher for reactive.',
    color: 'text-yellow-400',
    bgColor: 'bg-yellow-500/10',
    borderColor: 'border-yellow-500/20',
  },
  {
    icon: Layers,
    title: 'Modular Architecture',
    description: '9 focused Maven modules. Pick only what you need. BOM for version alignment.',
    color: 'text-neon-green',
    bgColor: 'bg-green-500/10',
    borderColor: 'border-green-500/20',
  },
  {
    icon: Workflow,
    title: 'Interceptor Pattern',
    description: 'Pluggable request/response interceptors for logging, tracing, and custom behavior.',
    color: 'text-neon-purple',
    bgColor: 'bg-purple-500/10',
    borderColor: 'border-purple-500/20',
  },
  {
    icon: RefreshCw,
    title: 'Smart Retry + Circuit Breaker',
    description: 'Jittered exponential backoff, Retry-After header parsing, 3-state circuit breaker, and concurrency limiter.',
    color: 'text-neon-blue',
    bgColor: 'bg-cyan-500/10',
    borderColor: 'border-cyan-500/20',
  },
  {
    icon: BarChart3,
    title: 'Metrics & Observability',
    description: 'Thread-safe in-memory metrics. Optional Micrometer and OpenTelemetry adapters via reflection.',
    color: 'text-orange-400',
    bgColor: 'bg-orange-500/10',
    borderColor: 'border-orange-500/20',
  },
  {
    icon: Shield,
    title: 'Tool / Function Calling',
    description: 'First-class ToolCall, ToolDefinition, ResponseFormat models for multi-turn tool-calling workflows.',
    color: 'text-green-400',
    bgColor: 'bg-green-500/10',
    borderColor: 'border-green-500/20',
  },
  {
    icon: Gauge,
    title: 'Production Ready',
    description: 'Immutable config, thread-safe metrics, secrets redaction in logs, split timeouts, correlation IDs, JPMS support.',
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
    items: ['ISynapseHub', 'ProviderAdapter', 'ChatMessage', 'SynapseResponse', 'SynapseException', 'Model', 'ToolCall', 'ToolDefinition', 'RequestOptions', 'StreamListener', 'StreamHandle', 'CancellationToken'],
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
    description: 'Immutable config with split timeouts',
    icon: Terminal,
    color: 'from-cyan-500 to-cyan-700',
    items: ['SynapseConfig', 'Builder Pattern', 'Validation', 'Split Timeouts', 'Circuit Breaker Config'],
  },
  {
    name: 'synapse-http',
    description: 'HTTP transport and orchestration',
    icon: Blocks,
    color: 'from-neon-green/80 to-green-700',
    items: ['SynapseHub', 'OpenAiProviderAdapter', 'HttpClient', 'StreamHandler', 'RetryHandler', 'CircuitBreaker', 'ConcurrencyLimiter'],
  },
  {
    name: 'synapse-metrics',
    description: 'Thread-safe metrics + export adapters',
    icon: BarChart3,
    color: 'from-orange-500 to-orange-700',
    items: ['SynapseMetrics', 'MetricsCollector', 'MicrometerAdapter', 'OpenTelemetryExporter'],
  },
  {
    name: 'synapse-spring-boot-starter',
    description: 'Spring Boot auto-configuration',
    icon: Puzzle,
    color: 'from-pink-500 to-pink-700',
    items: ['AutoConfiguration', 'Properties Binding', 'Conditional Beans', 'All Config Fields'],
  },
]

export const exceptionTypes = [
  { type: 'CONFIG_ERROR', description: 'Configuration validation failed', retryable: false },
  { type: 'NETWORK_ERROR', description: 'Network connectivity issues', retryable: true },
  { type: 'TIMEOUT_ERROR', description: 'Request timeout', retryable: true },
  { type: 'RATE_LIMIT_ERROR', description: 'API rate limit exceeded (HTTP 429)', retryable: true },
  { type: 'SERVER_ERROR', description: 'LLM API server error (HTTP 5xx)', retryable: true },
  { type: 'PARSE_ERROR', description: 'Response parsing failed', retryable: false },
  { type: 'STREAMING_ERROR', description: 'Streaming connection error', retryable: false },
  { type: 'RETRY_EXHAUSTED', description: 'Max retries exceeded', retryable: false },
  { type: 'CIRCUIT_BREAKER_OPEN', description: 'Circuit breaker is open', retryable: false },
]

export const apiMethods = [
  {
    method: 'sendPrompt',
    signature: 'sendPrompt(String prompt, RequestOptions options) -> SynapseResponse',
    description: 'Send a single user prompt synchronously. Pass null for config defaults.',
    module: 'synapse-core',
  },
  {
    method: 'sendChat',
    signature: 'sendChat(List<ChatMessage> messages, RequestOptions options) -> SynapseResponse',
    description: 'Send a multi-turn chat conversation synchronously. Pass null for config defaults.',
    module: 'synapse-core',
  },
  {
    method: 'sendPromptAsync',
    signature: 'sendPromptAsync(String prompt, RequestOptions options) -> CompletableFuture<SynapseResponse>',
    description: 'Send a single user prompt asynchronously.',
    module: 'synapse-core',
  },
  {
    method: 'sendChatAsync',
    signature: 'sendChatAsync(List<ChatMessage> messages, RequestOptions options) -> CompletableFuture<SynapseResponse>',
    description: 'Send a multi-turn chat conversation asynchronously.',
    module: 'synapse-core',
  },
  {
    method: 'chatCompletion',
    signature: 'chatCompletion(String requestBody, RequestOptions options) -> SynapseResponse',
    description: 'Send a raw JSON request body (escape hatch for full control).',
    module: 'synapse-core',
  },
  {
    method: 'streamPrompt',
    signature: 'streamPrompt(String prompt, StreamListener listener) -> StreamHandle',
    description: 'Stream a single user prompt with chunk/complete/error callbacks. Returns cancellable handle.',
    module: 'synapse-core',
  },
  {
    method: 'streamChat',
    signature: 'streamChat(List<ChatMessage> messages, StreamListener listener) -> StreamHandle',
    description: 'Stream a multi-turn conversation with callbacks. Returns cancellable handle.',
    module: 'synapse-core',
  },
  {
    method: 'streamCompletion',
    signature: 'streamCompletion(String requestBody, StreamListener listener) -> StreamHandle',
    description: 'Stream a raw JSON request body (escape hatch). Returns cancellable handle.',
    module: 'synapse-core',
  },
  {
    method: 'streamChatAsFlow',
    signature: 'streamChatAsFlow(List<ChatMessage> messages) -> Flow.Publisher<String>',
    description: 'Stream a multi-turn conversation as a reactive Flow.Publisher.',
    module: 'synapse-core',
  },
  {
    method: 'streamPromptAsFlow',
    signature: 'streamPromptAsFlow(String prompt) -> Flow.Publisher<String>',
    description: 'Stream a single prompt as a reactive Flow.Publisher.',
    module: 'synapse-core',
  },
  {
    method: 'getModelsList',
    signature: 'getModelsList() -> List<Model>',
    description: 'Retrieve available models from the /v1/models endpoint.',
    module: 'synapse-core',
  },
  {
    method: 'close',
    signature: 'close()',
    description: 'Shut down the hub, releasing HttpClient and thread pool.',
    module: 'synapse-core',
  },
]

export const proofStats = [
  { value: '0', label: 'HTTP dependencies', detail: 'Uses the JDK java.net.http.HttpClient — no OkHttp, no Retrofit, no vendor HTTP stack.' },
  { value: '12', label: 'methods in ISynapseHub', detail: 'One interface covers sync, async, streaming, reactive flow, and models listing.' },
  { value: '3', label: 'circuit breaker states', detail: 'CLOSED → OPEN → HALF_OPEN with configurable failure threshold (default 5) and open duration (default 30s).' },
  { value: '4', label: 'split timeouts', detail: 'connect (10s), read (30s), request deadline (60s), and stream idle (120s) — tuned independently.' },
  { value: '64', label: 'default concurrent requests', detail: 'Semaphore-based concurrency limiter prevents thundering-herd 429s under load.' },
  { value: '9', label: 'typed exceptions', detail: 'CONFIG, NETWORK, TIMEOUT, RATE_LIMIT, SERVER, PARSE, STREAMING, RETRY_EXHAUSTED, CIRCUIT_BREAKER_OPEN — each with a retryable flag.' },
]

export const comparisonRows = [
  {
    feature: 'Multi-provider support',
    synapse: 'ProviderAdapter SPI via ServiceLoader. Add a provider with one class + one registration file; no core changes.',
    typical: 'Hardcoded to one vendor SDK; switching vendors means a new dependency, new API, and a rewrite.',
  },
  {
    feature: 'HTTP transport',
    synapse: 'Zero external HTTP deps — JDK HttpClient (HTTP/2, connection pooling, async).',
    typical: 'OkHttp / Retrofit / vendor SDK — extra transitive dependencies and version conflicts.',
  },
  {
    feature: 'Retry logic',
    synapse: 'Jittered exponential backoff + Retry-After header parsing + max-elapsed-time budget (default 120s).',
    typical: 'Fixed sleep loops or no retry — retry storms that make 429s worse.',
  },
  {
    feature: 'Failure isolation',
    synapse: '3-state circuit breaker + semaphore concurrency limiter (default 64) + per-minute rate cap.',
    typical: 'No protection — one failing call cascades into N concurrent failures.',
  },
  {
    feature: 'Streaming',
    synapse: 'SSE via StreamListener, cancellable StreamHandle, and reactive Flow.Publisher. Provider-specific SSE framing handled per adapter.',
    typical: 'Manual thread + InputStream parsing; no cancellation, no provider abstraction.',
  },
  {
    feature: 'Timeouts',
    synapse: '4 independent timeouts (connect, read, request, stream idle).',
    typical: 'A single socket read timeout — hangs or premature failures either way.',
  },
  {
    feature: 'Metrics',
    synapse: 'Thread-safe LongAdder counters + CopyOnWriteArrayList samples; token usage and latency; Micrometer/OTel adapters.',
    typical: 'No metrics, or HashMap counters that lose increments under contention.',
  },
  {
    feature: 'Error model',
    synapse: '9 typed exceptions with retryable flags and HTTP status/body context.',
    typical: 'Generic IOException / RuntimeException — no guidance on what to retry.',
  },
  {
    feature: 'Security',
    synapse: 'API keys masked in toString(), correlation IDs on every request, split timeout config.',
    typical: 'Keys and tokens visible in logs; no request correlation.',
  },
  {
    feature: 'Extensibility',
    synapse: 'Interceptors for request/response lifecycle, pluggable retry policy, custom metrics listeners.',
    typical: 'Fork the vendor SDK or write your own wrapper from scratch.',
  },
]
