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
  { id: 'synapseresponse', label: 'SynapseResponse' },
  { id: 'synapseexception', label: 'SynapseException' },
  { id: 'interceptors', label: 'Interceptors' },
  { id: 'synapseretrypolicy', label: 'SynapseRetryPolicy' },
  { id: 'synapsemetricslistener', label: 'SynapseMetricsListener' },
]

const configBuilderMethods = [
  { name: 'baseUrl', type: 'String', description: 'Base URL of the LLM API provider' },
  { name: 'endpoint', type: 'String', description: 'API endpoint path' },
  { name: 'apiKey', type: 'String', description: 'Authentication API key' },
  { name: 'modelName', type: 'String', description: 'Model identifier' },
  { name: 'temperature', type: 'double', description: 'Sampling temperature (0.0 - 2.0)' },
  { name: 'maxTokens', type: 'int', description: 'Maximum tokens in response' },
  { name: 'timeout', type: 'Duration', description: 'Request timeout duration' },
  { name: 'maxRetries', type: 'int', description: 'Maximum retry attempts' },
  { name: 'retryDelay', type: 'Duration', description: 'Delay between retries' },
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

// Build a conversation
List<ChatMessage> messages = List.of(
    system,
    user,
    assistant,
    ChatMessage.user("Tell me more about qubits.")
);`

const responseGettersCode = `SynapseResponse response = hub.sendPrompt("Hello");

// Get the response content
String content = response.getContent();

// Get model used
String model = response.getModel();

// Token usage
int promptTokens = response.getPromptTokens();
int completionTokens = response.getCompletionTokens();
int totalTokens = promptTokens + completionTokens;

// Finish reason
String finishReason = response.getFinishReason();`

const interceptorInterfaceCode = `// Request Interceptor
public interface SynapseRequestInterceptor {
    void beforeRequest(SynapseRequestContext ctx);
    void afterRequest(SynapseRequestContext ctx);
    void onError(SynapseRequestContext ctx, SynapseException error);
}

// Response Interceptor
public interface SynapseResponseInterceptor {
    void onResponse(SynapseResponseContext ctx);
    void onError(SynapseResponseContext ctx, SynapseException error);
}`

const retryPolicyInterfaceCode = `public interface SynapseRetryPolicy {
    boolean shouldRetry(int attempt, SynapseException error);
    long getDelay(int attempt);
    int getMaxRetries();
}`

const metricsListenerInterfaceCode = `public interface SynapseMetricsListener {
    void onRequestStarted(String requestId);
    void onRequestCompleted(String requestId, long latencyMs);
    void onRequestFailed(String requestId, SynapseException error);
    void onTokensUsed(String requestId, int promptTokens, int completionTokens);
}`

export default function ApiReferencePage() {
  const [expandedMethod, setExpandedMethod] = useState<string | null>(null)
  const [activeNav, setActiveNav] = useState('isynapsehub')

  const methodExamples: Record<string, string> = {
    sendPrompt: `SynapseResponse response = hub.sendPrompt(
    "What is the capital of France?"
);
System.out.println(response.getContent());`,
    sendChat: `List<ChatMessage> messages = List.of(
    ChatMessage.system("You are a helpful assistant."),
    ChatMessage.user("Explain quantum computing.")
);
SynapseResponse response = hub.sendChat(messages);
System.out.println(response.getContent());`,
    chatCompletion: `SynapseResponse response = hub.chatCompletion(
    "Write a haiku about programming"
);
System.out.println(response.getContent());`,
    streamPrompt: `hub.streamPrompt("Write a poem about coding", chunk -> {
    System.out.print(chunk);
});`,
    streamChat: `hub.streamChat(messages, chunk -> {
    System.out.print(chunk);
});`,
    streamCompletion: `hub.streamCompletion("Tell me a joke", chunk -> {
    System.out.print(chunk);
});`,
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {apiMethods.map((method) => (
                      <div key={method.method}>
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
                    ))}
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 p-6 mb-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 p-6 mb-6">
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
                  <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 p-6">
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
                  <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 p-6">
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Interface Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'shouldRetry(int attempt, SynapseException error)', type: 'boolean', description: 'Determine if retry should occur' },
                      { method: 'getDelay(int attempt)', type: 'long', description: 'Delay in ms before next retry' },
                      { method: 'getMaxRetries()', type: 'int', description: 'Maximum number of retry attempts' },
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

                <div className="bg-gray-900/60 rounded-xl border border-gray-800/50 overflow-hidden mb-6">
                  <div className="p-4 border-b border-gray-800/50">
                    <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                      <FileText className="w-4 h-4 text-gray-400" />
                      Interface Methods
                    </h3>
                  </div>
                  <div className="divide-y divide-gray-800/50">
                    {[
                      { method: 'onRequestStarted(String requestId)', description: 'Called when a request begins' },
                      { method: 'onRequestCompleted(String requestId, long latencyMs)', description: 'Called on successful completion' },
                      { method: 'onRequestFailed(String requestId, SynapseException error)', description: 'Called on request failure' },
                      { method: 'onTokensUsed(String requestId, int promptTokens, int completionTokens)', description: 'Called with token usage data' },
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
