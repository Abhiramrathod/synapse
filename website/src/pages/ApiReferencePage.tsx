import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, Code2, FileText, ChevronRight, ArrowRight, Info } from 'lucide-react'
import FadeIn from '../components/FadeIn'
import CodeBlock from '../components/CodeBlock'
import Badge from '../components/Badge'
import { apiMethods, exceptionTypes } from '../data/content'
import { quickStartCode, errorHandlingCode, fullConfigCode, interceptorCode, retryPolicyCode } from '../data/codeExamples'

interface NavItem {
  id: string
  label: string
}

const navItems: NavItem[] = [
  { id: 'isynapsehub', label: 'ISynapseHub' },
  { id: 'synapsehub', label: 'SynapseHub' },
  { id: 'synapseconfig', label: 'SynapseConfig' },
  { id: 'chatmessage', label: 'ChatMessage' },
  { id: 'model', label: 'Model' },
  { id: 'synapseresponse', label: 'SynapseResponse' },
  { id: 'synapseexception', label: 'SynapseException' },
  { id: 'interceptors', label: 'Interceptors' },
  { id: 'synapseretrypolicy', label: 'SynapseRetryPolicy' },
  { id: 'synapsemetricslistener', label: 'SynapseMetricsListener' },
]

const configBuilderMethods = [
  { name: 'baseUrl', type: 'String', description: 'Base URL of the LLM API provider' },
  { name: 'endpoint', type: 'String', description: 'API endpoint path' },
  { name: 'apiKey', type: 'String', description: 'Authentication API key (redacted in toString)' },
  { name: 'modelName', type: 'String', description: 'Default model identifier' },
  { name: 'temperature', type: 'double', description: 'Sampling temperature (0.0 - 2.0)' },
  { name: 'maxTokens', type: 'int', description: 'Maximum tokens in response' },
  { name: 'connectTimeout', type: 'Duration', description: 'TCP connect timeout' },
  { name: 'readTimeout', type: 'Duration', description: 'Read/response timeout' },
  { name: 'requestTimeout', type: 'Duration', description: 'Overall request deadline' },
  { name: 'streamIdleTimeout', type: 'Duration', description: 'Idle timeout for streaming' },
  { name: 'maxRetries', type: 'int', description: 'Maximum retry attempts' },
  { name: 'retryDelay', type: 'Duration', description: 'Base delay between retries' },
  { name: 'maxRetryElapsedTime', type: 'Duration', description: 'Max total time for retries' },
  { name: 'maxConcurrentRequests', type: 'int', description: 'Semaphore permits for concurrency' },
  { name: 'circuitBreakerFailureThreshold', type: 'int', description: 'Failures before circuit opens' },
  { name: 'circuitBreakerOpenDuration', type: 'Duration', description: 'How long circuit stays open' },
  { name: 'enableLogging', type: 'boolean', description: 'Enable request/response logging' },
  { name: 'requestInterceptor', type: 'SynapseRequestInterceptor', description: 'Request interceptor' },
  { name: 'responseInterceptor', type: 'SynapseResponseInterceptor', description: 'Response interceptor' },
  { name: 'retryPolicy', type: 'SynapseRetryPolicy', description: 'Custom retry policy' },
  { name: 'metricsListener', type: 'SynapseMetricsListener', description: 'Metrics listener' },
]

const chatMessageCode = `// System message
ChatMessage system = ChatMessage.system("You are a helpful assistant.");

// User message
ChatMessage user = ChatMessage.user("Explain quantum computing.");

// Assistant message (for context)
ChatMessage assistant = ChatMessage.assistant("Quantum computing uses...");

// Tool result message (for tool calling)
ChatMessage toolResult = ChatMessage.tool("call_123", "get_weather", "{\\"temp\\": 72}");

// Build a conversation
List<ChatMessage> messages = List.of(
    system,
    user,
    assistant,
    ChatMessage.user("Tell me more about qubits.")
);`

const responseGettersCode = `SynapseResponse response = hub.sendPrompt("Hello", null);

// Core fields
String content = response.getContent();
String model = response.getModel();
int promptTokens = response.getPromptTokens();
int completionTokens = response.getCompletionTokens();
int totalTokens = promptTokens + completionTokens;
String finishReason = response.getFinishReason();

// Metadata
String correlationId = response.getCorrelationId();
String provider = response.getProvider();
List<ToolCall> toolCalls = response.getToolCalls();
String responseFormat = response.getResponseFormat();`

const interceptorInterfaceCode = `// Request Interceptor
public interface SynapseRequestInterceptor {
    default void beforeRequest(SynapseRequestContext ctx) {}
    default void afterRequest(SynapseRequestContext ctx) {}
    default void onError(SynapseRequestContext ctx, SynapseException error) {}
}

// Response Interceptor
public interface SynapseResponseInterceptor {
    default void beforeResponse(SynapseResponseContext ctx) {}
    default void afterResponse(SynapseResponseContext ctx) {}
    default void onError(SynapseResponseContext ctx, SynapseException error) {}
}`

const retryPolicyInterfaceCode = `public interface SynapseRetryPolicy {
    default boolean shouldRetry(int attempt, SynapseException error);
    default long getDelay(int attempt, SynapseException exception,
                          Map<String, List<String>> responseHeaders);
    default int getMaxRetries();
    default Duration getRetryDelay();
    default Duration getMaxRetryElapsedTime();
}`

const metricsListenerInterfaceCode = `public interface SynapseMetricsListener {
    default void onRequestStarted(String model) {}
    default void onRequestCompleted(SynapseMetricsSummary summary) {}
    default void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {}
}`

export default function ApiReferencePage() {
  const [expandedMethod, setExpandedMethod] = useState<string | null>(null)
  const [activeNav, setActiveNav] = useState('isynapsehub')

  const methodExamples: Record<string, string> = {
    'sendPrompt': `SynapseResponse response = hub.sendPrompt(
    "What is the capital of France?", null
);
System.out.println(response.getContent());
System.out.println("Correlation: " + response.getCorrelationId());`,
    'sendChat': `List<ChatMessage> messages = List.of(
    ChatMessage.system("You are a helpful assistant."),
    ChatMessage.user("Explain quantum computing.")
);
SynapseResponse response = hub.sendChat(messages, null);
System.out.println(response.getContent());`,
    'sendPromptAsync': `// Non-blocking async call
CompletableFuture<SynapseResponse> future =
    hub.sendPromptAsync("What is Java?", null);
future.thenAccept(response -> {
    System.out.println(response.getContent());
});`,
    'sendChatAsync': `List<ChatMessage> messages = List.of(
    ChatMessage.system("You are helpful."),
    ChatMessage.user("What is Java?")
);
CompletableFuture<SynapseResponse> future =
    hub.sendChatAsync(messages, null);
future.thenAccept(r -> System.out.println(r.getContent()));`,
    'chatCompletion': `// Raw JSON escape hatch
SynapseResponse response = hub.chatCompletion(
    "{\\"model\\": \\"gpt-4\\", \\"messages\\": [{\\"role\\": \\"user\\", \\"content\\": \\"Hello\\"}]}",
    null
);
System.out.println(response.getContent());`,
    'streamPrompt': `// Stream with StreamListener callbacks
StreamHandle handle = hub.streamPrompt(
    "Write a poem about coding",
    StreamListener.of(chunk -> System.out.print(chunk))
);
// Cancel mid-stream if needed:
// handle.cancel();
// Or await full response:
// SynapseResponse full = handle.getFuture().join();`,
    'streamChat': `List<ChatMessage> messages = List.of(
    ChatMessage.user("Write a haiku about Java")
);
StreamHandle handle = hub.streamChat(messages,
    StreamListener.of(chunk -> System.out.print(chunk))
);`,
    'streamCompletion': `// Raw JSON streaming escape hatch
StreamHandle handle = hub.streamCompletion(
    "{\\"model\\": \\"gpt-4\\", \\"messages\\": [{\\"role\\": \\"user\\", \\"content\\": \\"Tell me a joke\\"}], \\"stream\\": true}",
    StreamListener.of(chunk -> System.out.print(chunk))
);`,
    'streamChatAsFlow': `// Reactive Flow.Publisher for integration with
// Reactor, RxJava, or any Flow.Subscriber
Flow.Publisher<String> publisher =
    hub.streamChatAsFlow(messages);
publisher.subscribe(new Flow.Subscriber<>() {
    public void onSubscribe(Flow.Subscription s) {
        s.request(Long.MAX_VALUE);
    }
    public void onNext(String chunk) {
        System.out.print(chunk);
    }
    public void onError(Throwable t) {}
    public void onComplete() {}
});`,
    'streamPromptAsFlow': `Flow.Publisher<String> publisher =
    hub.streamPromptAsFlow("Write a joke");
publisher.subscribe(new Flow.Subscriber<>() {
    public void onSubscribe(Flow.Subscription s) {
        s.request(Long.MAX_VALUE);
    }
    public void onNext(String chunk) {
        System.out.print(chunk);
    }
    public void onError(Throwable t) {}
    public void onComplete() {}
});`,
    'getModelsList': `List<Model> models = hub.getModelsList();
for (Model model : models) {
    System.out.printf("Model: %s (owned by: %s)%n",
        model.getId(), model.getOwnedBy());
}`,
  }

  return (
    <div className="min-h-screen bg-gray-950 py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <FadeIn>
          <div className="mb-12">
            <h1 className="text-4xl font-bold text-white mb-4">API Reference</h1>
            <p className="text-lg text-gray-400 max-w-2xl">
              Comprehensive documentation for all classes, interfaces, and methods in the Synapse library.
            </p>
          </div>
        </FadeIn>

        <div className="flex gap-8">
          {/* Sidebar Navigation */}
          <aside className="hidden lg:block w-64 flex-shrink-0">
            <nav className="sticky top-24 space-y-1">
              {navItems.map((item) => (
                <a
                  key={item.id}
                  href={`#${item.id}`}
                  onClick={() => setActiveNav(item.id)}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-all ${
                    activeNav === item.id
                      ? 'bg-synapse-600/20 text-synapse-400'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800/50'
                  }`}
                >
                  <ChevronRight className={`w-3 h-3 transition-transform ${activeNav === item.id ? 'rotate-90' : ''}`} />
                  {item.label}
                </a>
              ))}
            </nav>
          </aside>

          {/* Main Content */}
          <div className="flex-1 min-w-0 space-y-16">
            {/* ISynapseHub Interface */}
            <FadeIn>
              <section id="isynapsehub">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-synapse-500/10 border border-synapse-500/20">
                    <Code2 className="w-5 h-5 text-synapse-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">ISynapseHub</h2>
                  <Badge variant="blue">interface</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  The main entry point for interacting with LLM APIs. Provides synchronous and streaming methods for sending prompts and receiving responses.
                </p>

                <div className="glass-card rounded-xl overflow-hidden">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {apiMethods.map((method, idx) => {
                      const badgeLabel = method.signature.includes('-> StreamHandle') ? 'StreamHandle' :
                        method.signature.includes('-> CompletableFuture') ? 'CompletableFuture' :
                        method.signature.includes('-> Flow.Publisher') ? 'Flow.Publisher' :
                        method.signature.includes('-> SynapseResponse') ? 'SynapseResponse' :
                        method.signature.includes('-> List<Model>') ? 'List<Model>' :
                        method.signature.includes('void') ? 'void' : '';
                      return (
                        <div key={`${method.method}-${idx}`}>
                          <button
                            onClick={() => setExpandedMethod(expandedMethod === method.method ? null : method.method)}
                            className="w-full px-4 py-3 flex items-center gap-4 hover:bg-gray-800/30 transition-colors text-left"
                          >
                            <span className="font-mono text-synapse-400 text-sm min-w-[140px]">{method.method}</span>
                            <span className="font-mono text-gray-300 text-xs flex-1 truncate">{method.signature}</span>
                            <ChevronRight
                              className={`w-4 h-4 text-gray-500 transition-transform ${
                                expandedMethod === method.method ? 'rotate-90' : ''
                              }`}
                            />
                          </button>
                          <AnimatePresence>
                            {expandedMethod === method.method && (
                              <motion.div
                                initial={{ height: 0, opacity: 0 }}
                                animate={{ height: 'auto', opacity: 1 }}
                                exit={{ height: 0, opacity: 0 }}
                                transition={{ duration: 0.2 }}
                                className="overflow-hidden"
                              >
                                <div className="px-4 pb-4">
                                  <p className="text-sm text-gray-400 mb-3">{method.description}</p>
                                  <CodeBlock code={methodExamples[method.method] || ''} title={`${method.method} example`} />
                                </div>
                              </motion.div>
                            )}
                          </AnimatePresence>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </section>
            </FadeIn>

            {/* SynapseHub Implementation */}
            <FadeIn>
              <section id="synapsehub">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-green-500/10 border border-green-500/20">
                    <Code2 className="w-5 h-5 text-green-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseHub</h2>
                  <Badge variant="green">class</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  The concrete implementation of <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">ISynapseHub</code>. Handles HTTP communication, retry logic, and interceptor orchestration.
                </p>

                <div className="glass-card p-6 mb-6">
                  <div className="space-y-4">
                    <div className="flex items-start gap-3">
                      <Info className="w-5 h-5 text-synapse-400 mt-0.5 flex-shrink-0" />
                      <div>
                        <h4 className="text-sm font-semibold text-white mb-1">Constructor</h4>
                        <code className="text-sm font-mono text-gray-300">public SynapseHub(SynapseConfig config)</code>
                        <p className="text-sm text-gray-400 mt-1">Requires a valid SynapseConfig instance.</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3">
                      <Info className="w-5 h-5 text-green-400 mt-0.5 flex-shrink-0" />
                      <div>
                        <h4 className="text-sm font-semibold text-white mb-1">Implements AutoCloseable</h4>
                        <p className="text-sm text-gray-400">Use try-with-resources to ensure proper cleanup of HTTP connections.</p>
                      </div>
                    </div>
                  </div>
                </div>

                <CodeBlock code={quickStartCode} title="Quick Start" />
              </section>
            </FadeIn>

            {/* SynapseConfig */}
            <FadeIn>
              <section id="synapseconfig">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-cyan-500/10 border border-cyan-500/20">
                    <Code2 className="w-5 h-5 text-cyan-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseConfig</h2>
                  <Badge variant="blue">class</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Configuration holder using the Builder pattern. All required fields must be set before calling <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">.build()</code>.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Builder Methods
                    </h3>
                  </div>
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-gray-800/50">
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Method</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Type</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Description</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-800/50">
                      {configBuilderMethods.map((method) => (
                        <tr key={method.name} className="hover:bg-gray-800/30">
                          <td className="px-4 py-3 font-mono text-synapse-400">{method.name}</td>
                          <td className="px-4 py-3 font-mono text-gray-300 text-xs">{method.type}</td>
                          <td className="px-4 py-3 text-gray-400">{method.description}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <CodeBlock code={fullConfigCode} title="Full Configuration" />
              </section>
            </FadeIn>

            {/* ChatMessage */}
            <FadeIn>
              <section id="chatmessage">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-purple-500/10 border border-purple-500/20">
                    <Code2 className="w-5 h-5 text-purple-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">ChatMessage</h2>
                  <Badge variant="purple">class</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Immutable message model for multi-turn conversations. Use factory methods to create messages with different roles.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Factory Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'system(String content)', description: 'Creates a system message for setting assistant behavior', badge: 'blue' as const },
                      { method: 'user(String content)', description: 'Creates a user message from human input', badge: 'green' as const },
                      { method: 'assistant(String content)', description: 'Creates an assistant message for context', badge: 'purple' as const },
                      { method: 'tool(String toolCallId, String name, String content)', description: 'Creates a tool result message for tool calling', badge: 'blue' as const },
                    ].map((item) => (
                      <div key={item.method} className="px-4 py-3 flex items-center gap-4">
                        <span className="font-mono text-synapse-400 text-sm min-w-[220px]">{item.method}</span>
                        <Badge variant={item.badge}>{item.badge === 'blue' ? 'static' : item.badge === 'green' ? 'static' : 'static'}</Badge>
                        <span className="text-gray-400 text-sm flex-1">{item.description}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <CodeBlock code={chatMessageCode} title="ChatMessage Factory Methods" />
              </section>
            </FadeIn>

            {/* Model */}
            <FadeIn>
              <section id="model">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-green-500/10 border border-green-500/20">
                    <Code2 className="w-5 h-5 text-green-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">Model</h2>
                  <Badge variant="green">class</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Represents a model available from an LLM API endpoint. Returned by <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">getModelsList()</code> following the OpenAI-compatible model list format.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Getters
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'getId()', type: 'String', description: 'Unique model identifier (e.g. "gpt-4")' },
                      { method: 'getObject()', type: 'String', description: 'Object type, typically "model"' },
                      { method: 'getCreated()', type: 'long', description: 'Unix timestamp when the model was created' },
                      { method: 'getOwnedBy()', type: 'String', description: 'Organization that owns the model' },
                    ].map((item) => (
                      <div key={item.method} className="px-4 py-3 flex items-center gap-4">
                        <span className="font-mono text-synapse-400 text-sm min-w-[150px]">{item.method}</span>
                        <span className="font-mono text-gray-500 text-xs min-w-[60px]">{item.type}</span>
                        <span className="text-gray-400 text-sm flex-1">{item.description}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <CodeBlock code={`List<Model> models = hub.getModelsList();
for (Model model : models) {
    System.out.printf("Model: %s (owned by: %s)%n",
        model.getId(), model.getOwnedBy());
}`} title="Listing Available Models" />
              </section>
            </FadeIn>

            {/* SynapseResponse */}
            <FadeIn>
              <section id="synapseresponse">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-synapse-500/10 border border-synapse-500/20">
                    <Code2 className="w-5 h-5 text-synapse-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseResponse</h2>
                  <Badge variant="blue">class</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Immutable response model containing the LLM output, token usage, and metadata.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Getters
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'getContent()', type: 'String', description: 'The generated text content' },
                      { method: 'getModel()', type: 'String', description: 'Model that generated the response' },
                      { method: 'getPromptTokens()', type: 'int', description: 'Number of tokens in the prompt' },
                      { method: 'getCompletionTokens()', type: 'int', description: 'Number of tokens in the completion' },
                      { method: 'getFinishReason()', type: 'String', description: 'Why generation stopped (stop, length, etc.)' },
                      { method: 'getCorrelationId()', type: 'String', description: 'Unique ID for tracing this request' },
                      { method: 'getProvider()', type: 'String', description: 'Provider that handled the request' },
                      { method: 'getToolCalls()', type: 'List<ToolCall>', description: 'Tool calls requested by the model' },
                      { method: 'getResponseFormat()', type: 'String', description: 'Response format (e.g. JSON schema)' },
                    ].map((item) => (
                      <div key={item.method} className="px-4 py-3 flex items-center gap-4">
                        <span className="font-mono text-synapse-400 text-sm min-w-[200px]">{item.method}</span>
                        <span className="font-mono text-gray-500 text-xs min-w-[60px]">{item.type}</span>
                        <span className="text-gray-400 text-sm flex-1">{item.description}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <CodeBlock code={responseGettersCode} title="Response Getters" />
              </section>
            </FadeIn>

            {/* SynapseException */}
            <FadeIn>
              <section id="synapseexception">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-red-500/10 border border-red-500/20">
                    <Code2 className="w-5 h-5 text-red-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseException</h2>
                  <Badge variant="purple">enum</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Structured exception hierarchy with typed error categories. Each exception carries a type and retryability flag.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Exception Types
                    </h3>
                  </div>
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-gray-800/50">
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Type</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Description</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Retryable</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-800/50">
                      {exceptionTypes.map((ex) => (
                        <tr key={ex.type} className="hover:bg-gray-800/30">
                          <td className="px-4 py-3 font-mono text-synapse-400">{ex.type}</td>
                          <td className="px-4 py-3 text-gray-400">{ex.description}</td>
                          <td className="px-4 py-3">
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${ex.retryable ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'}`}>
                              {ex.retryable ? 'Yes' : 'No'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="glass-card p-6 mb-6">
                  <h4 className="text-sm font-semibold text-white mb-2 flex items-center gap-2">
                    <Info className="w-4 h-4 text-synapse-400" />
                    isRetryable() Method
                  </h4>
                  <p className="text-sm text-gray-400 mb-3">
                    Returns <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">true</code> if the exception type supports automatic retry based on the configured retry policy.
                  </p>
                  <code className="text-sm font-mono text-gray-300">if (exception.isRetryable()) {'{'}
  <span className="text-gray-500">  // Will be retried automatically</span>
{'}'}</code>
                </div>

                <CodeBlock code={errorHandlingCode} title="Error Handling" />
              </section>
            </FadeIn>

            {/* Interceptors */}
            <FadeIn>
              <section id="interceptors">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-purple-500/10 border border-purple-500/20">
                    <Code2 className="w-5 h-5 text-purple-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">Interceptors</h2>
                  <Badge variant="purple">interface</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Pluggable interceptor contracts for customizing request and response handling. Implement these interfaces to add logging, metrics, or transformation logic.
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                  <div className="glass-card p-6">
                    <h4 className="text-sm font-semibold text-white mb-3">SynapseRequestInterceptor</h4>
                    <ul className="space-y-2 text-sm text-gray-400">
                      <li className="flex items-start gap-2">
                        <ArrowRight className="w-3 h-3 text-synapse-400 mt-1.5 flex-shrink-0" />
                        <span className="font-mono text-synapse-400">beforeRequest(ctx)</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ArrowRight className="w-3 h-3 text-synapse-400 mt-1.5 flex-shrink-0" />
                        <span className="font-mono text-synapse-400">afterRequest(ctx)</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ArrowRight className="w-3 h-3 text-synapse-400 mt-1.5 flex-shrink-0" />
                        <span className="font-mono text-synapse-400">onError(ctx, error)</span>
                      </li>
                    </ul>
                  </div>
                  <div className="glass-card p-6">
                    <h4 className="text-sm font-semibold text-white mb-3">SynapseResponseInterceptor</h4>
                    <ul className="space-y-2 text-sm text-gray-400">
                      <li className="flex items-start gap-2">
                        <ArrowRight className="w-3 h-3 text-purple-400 mt-1.5 flex-shrink-0" />
                        <span className="font-mono text-purple-400">onResponse(ctx)</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ArrowRight className="w-3 h-3 text-purple-400 mt-1.5 flex-shrink-0" />
                        <span className="font-mono text-purple-400">onError(ctx, error)</span>
                      </li>
                    </ul>
                  </div>
                </div>

                <CodeBlock code={interceptorInterfaceCode} title="Interceptor Interfaces" />
              </section>
            </FadeIn>

            {/* SynapseRetryPolicy */}
            <FadeIn>
              <section id="synapseretrypolicy">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-yellow-500/10 border border-yellow-500/20">
                    <Code2 className="w-5 h-5 text-yellow-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseRetryPolicy</h2>
                  <Badge variant="blue">interface</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Customizable retry logic interface. Implement to control when and how failed requests are retried.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Interface Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'shouldRetry(int attempt, SynapseException error)', type: 'boolean', description: 'Determine if retry should occur' },
                      { method: 'getDelay(int attempt, SynapseException exc, Map<String, List<String>> headers)', type: 'long', description: 'Delay in ms, parses Retry-After header' },
                      { method: 'getMaxRetries()', type: 'int', description: 'Maximum number of retry attempts' },
                      { method: 'getRetryDelay()', type: 'Duration', description: 'Base delay between retries' },
                      { method: 'getMaxRetryElapsedTime()', type: 'Duration', description: 'Max total time for retry loop' },
                    ].map((item) => (
                      <div key={item.method} className="px-4 py-3 flex items-center gap-4">
                        <span className="font-mono text-synapse-400 text-sm min-w-[320px]">{item.method}</span>
                        <span className="font-mono text-gray-500 text-xs min-w-[50px]">{item.type}</span>
                        <span className="text-gray-400 text-sm flex-1">{item.description}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <CodeBlock code={retryPolicyCode} title="Custom Retry Policy" />
              </section>
            </FadeIn>

            {/* SynapseMetricsListener */}
            <FadeIn>
              <section id="synapsemetricslistener">
                <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 rounded-lg bg-orange-500/10 border border-orange-500/20">
                    <Code2 className="w-5 h-5 text-orange-400" />
                  </div>
                  <h2 className="text-2xl font-bold text-white">SynapseMetricsListener</h2>
                  <Badge variant="blue">interface</Badge>
                </div>
                <p className="text-gray-400 mb-6">
                  Listener interface for tracking request metrics, latency, and token usage. Implement to integrate with your monitoring system.
                </p>

                <div className="glass-card overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Interface Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'onRequestStarted(String model)', description: 'Called when a request begins, with target model' },
                      { method: 'onRequestCompleted(SynapseMetricsSummary summary)', description: 'Called on successful completion with metrics summary' },
                      { method: 'onRequestFailed(SynapseMetricsSummary summary, SynapseException error)', description: 'Called on request failure with partial metrics' },
                    ].map((item) => (
                      <div key={item.method} className="px-4 py-3 flex items-center gap-4">
                        <span className="font-mono text-synapse-400 text-sm min-w-[380px]">{item.method}</span>
                        <span className="text-gray-400 text-sm flex-1">{item.description}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <CodeBlock code={metricsListenerInterfaceCode} title="Metrics Listener Interface" />
              </section>
            </FadeIn>
          </div>
        </div>
      </div>
    </div>
  )
}
