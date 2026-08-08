import { useState } from 'react'
import { Link } from 'react-router'
import { motion } from 'motion/react'
import { Package, Play, Code2, Wrench, Settings, ArrowRight, CheckCircle2, Copy, Terminal, Check, X, Plug, Database, Shuffle, Route, SlidersHorizontal, KeyRound } from 'lucide-react'
import FadeIn from '../components/FadeIn'
import PageMeta from '../components/PageMeta'
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
  providerAdapterContractCode,
  anthropicAdapterCode,
  providerServiceFileCode,
  providerSpiConfigCode,
  providerSpiYamlCode,
  responseCacheCode,
  fallbackHubCode,
  loadBalancingHubCode,
  streamFlowCode,
  dynamicReconfigCode,
  tokenProviderCode,
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
      <PageMeta
        title="Getting Started - Synapse"
        description="Install Synapse in minutes and make your first LLM API call. Covers Maven setup, Spring Boot starter, streaming, interceptors, retries, and custom provider integration."
      />
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
                  <span>Available on</span>
                  <span className="text-white font-semibold">Maven Central</span>
                  <a
                    href="https://central.sonatype.com/artifact/io.github.abhiramrathod/synapse-all"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-synapse-400 hover:text-synapse-300 underline underline-offset-2"
                  >
                    View on Maven Central
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
                  code={`try (SynapseHub hub = new SynapseHub(config)) {
    // One-shot prompt
    SynapseResponse response = hub.sendPrompt(
        "What is the capital of France?", null
    );
    System.out.println(response.getContent());

    // Multi-turn conversation
    List<ChatMessage> messages = List.of(
        ChatMessage.system("You are a helpful assistant."),
        ChatMessage.user("Explain quantum computing.")
    );
    SynapseResponse chat = hub.sendChat(messages, null);
    System.out.println(chat.getContent());
}`}
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
                  code={`try (SynapseHub hub = new SynapseHub(config)) {
    // Stream with StreamListener — chunk, complete, error callbacks
    StreamHandle handle = hub.streamPrompt(
        "Write a poem about programming",
        new StreamListener() {
            @Override
            public void onChunk(String text) {
                System.out.print(text);
            }

            @Override
            public void onComplete(SynapseResponse full) {
                System.out.println("\\n--- Done ---");
            }

            @Override
            public void onError(SynapseException e) {
                System.err.println("Failed: " + e.getMessage());
            }
        }
    );

    // Cancel mid-stream if needed
    // handle.cancel();

    // Or use the Consumer adapter
    hub.streamPrompt("Write a haiku", StreamListener.of(chunk -> {
        System.out.print(chunk);
    }));
}`}
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
                <CodeBlock code={`@Service
public class LlmService {

    private final ISynapseHub synapseHub;

    public LlmService(ISynapseHub synapseHub) {
        this.synapseHub = synapseHub;
    }

    public String askQuestion(String question) {
        SynapseResponse response = synapseHub.sendPrompt(question, null);
        return response.getContent();
    }
}`} language="java" title="LlmService.java" />
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

      {/* Production Patterns */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <Wrench className="w-6 h-6 text-synapse-400" />
              Production Patterns
            </h2>
            <p className="text-gray-400 mb-8 max-w-2xl">
              Caching, resilience, live reconfiguration, and rotating credentials — the patterns
              that take a demo client to production.
            </p>
          </FadeIn>

          <div className="space-y-8">
            {/* Response Caching */}
            <FadeIn delay={0.1}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center">
                    <Database className="w-5 h-5 text-cyan-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Response Caching</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Serve repeated prompts from a Caffeine or Redis cache instead of the provider.</p>
                  </div>
                </div>
                <CodeBlock code={responseCacheCode} language="java" title="ResponseCache.java" />
              </div>
            </FadeIn>

            {/* Fallback Hub */}
            <FadeIn delay={0.15}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
                    <Shuffle className="w-5 h-5 text-amber-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Fallback Hub</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Route around failed providers automatically.</p>
                  </div>
                </div>
                <CodeBlock code={fallbackHubCode} language="java" title="FallbackSynapseHub.java" />
              </div>
            </FadeIn>

            {/* Load Balancing Hub */}
            <FadeIn delay={0.2}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
                    <Shuffle className="w-5 h-5 text-indigo-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Load Balancing</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Round-robin across accounts, regions, or API keys — and combine with fallback.</p>
                  </div>
                </div>
                <CodeBlock code={loadBalancingHubCode} language="java" title="LoadBalancingSynapseHub.java" />
              </div>
            </FadeIn>

            {/* StreamFlow */}
            <FadeIn delay={0.25}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-teal-500/10 border border-teal-500/20 flex items-center justify-center">
                    <Route className="w-5 h-5 text-teal-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Fluent Stream Processing</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Compose reactive token streams without Flow.Subscriber boilerplate.</p>
                  </div>
                </div>
                <CodeBlock code={streamFlowCode} language="java" title="StreamFlow.java" />
              </div>
            </FadeIn>

            {/* Dynamic Reconfiguration */}
            <FadeIn delay={0.3}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
                    <SlidersHorizontal className="w-5 h-5 text-purple-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Dynamic Reconfiguration</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Rotate keys, models, endpoints, and timeouts without rebuilding the HTTP client pool.</p>
                  </div>
                </div>
                <CodeBlock code={dynamicReconfigCode} language="java" title="DynamicReconfiguration.java" />
              </div>
            </FadeIn>

            {/* Token Providers */}
            <FadeIn delay={0.35}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-lime-500/10 border border-lime-500/20 flex items-center justify-center">
                    <KeyRound className="w-5 h-5 text-lime-400" />
                  </div>
                  <div>
                    <h3 className="text-lg font-semibold text-white">Dynamic Token Providers</h3>
                    <p className="text-sm text-gray-400 mt-0.5">Per-request credentials for rotating tokens, AWS SigV4, and Azure Entra ID / Managed Identity.</p>
                  </div>
                </div>
                <CodeBlock code={tokenProviderCode} language="java" title="TokenProvider.java" />
              </div>
            </FadeIn>
          </div>
        </div>
      </section>

      {/* Provider SPI */}
      <section className="py-16">
        <div className="max-w-5xl mx-auto px-6">
          <FadeIn>
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <Plug className="w-6 h-6 text-synapse-400" />
              Provider SPI — Bring Your Own Provider
            </h2>
            <p className="text-gray-400 mb-8 max-w-2xl">
              Synapse is provider-agnostic by design. Every provider is a class implementing{' '}
              <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">ProviderAdapter</code>{' '}
              registered through Java's <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">ServiceLoader</code>.
              Adding a new provider requires one class and one registration file — no changes to Synapse itself.
            </p>
          </FadeIn>

          <div className="space-y-8">
            {/* The contract */}
            <FadeIn delay={0.1}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50">
                  <h3 className="text-lg font-semibold text-white">The contract</h3>
                  <p className="text-sm text-gray-400 mt-1">
                    Implement these methods to control URLs, auth headers, request bodies, response parsing, models listing, and SSE framing.
                  </p>
                </div>
                <CodeBlock code={providerAdapterContractCode} language="java" title="ProviderAdapter" />
              </div>
            </FadeIn>

            {/* Step 1: implement */}
            <FadeIn delay={0.15}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-synapse-600 text-white flex items-center justify-center text-xs font-bold">1</div>
                  <span className="text-sm font-medium text-white">Implement the adapter — Anthropic example</span>
                  <span className="text-xs text-gray-500 ml-auto">x-api-key auth, top-level system field, delta.text streaming</span>
                </div>
                <CodeBlock code={anthropicAdapterCode} language="java" title="AnthropicProviderAdapter.java" />
              </div>
            </FadeIn>

            {/* Step 2: register */}
            <FadeIn delay={0.2}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-synapse-600 text-white flex items-center justify-center text-xs font-bold">2</div>
                  <span className="text-sm font-medium text-white">Register for ServiceLoader discovery</span>
                </div>
                <CodeBlock code={providerServiceFileCode} language="text" title="META-INF/services/org.abhi.synapse.core.ProviderAdapter" showLineNumbers={false} />
              </div>
            </FadeIn>

            {/* Step 3: select */}
            <FadeIn delay={0.25}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-synapse-600 text-white flex items-center justify-center text-xs font-bold">3</div>
                  <span className="text-sm font-medium text-white">Select it in configuration</span>
                </div>
                <CodeBlock code={providerSpiConfigCode} language="java" title="ProviderConfig.java" />
              </div>
            </FadeIn>

            {/* Spring yaml */}
            <FadeIn delay={0.3}>
              <div className="bg-gray-900/60 backdrop-blur-xl border border-gray-800/50 rounded-2xl overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-800/50 flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-green-400" />
                  <span className="text-sm font-medium text-white">Spring Boot</span>
                  <span className="text-xs text-gray-500 ml-auto">synapse.provider selects the adapter</span>
                </div>
                <CodeBlock code={providerSpiYamlCode} language="yaml" title="application.yml" showLineNumbers={false} />
              </div>
            </FadeIn>
          </div>

          {/* Resolution note */}
          <FadeIn delay={0.35}>
            <div className="mt-8 p-6 rounded-2xl border border-synapse-500/20 bg-synapse-600/5">
              <h4 className="text-white font-semibold mb-2 flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 text-synapse-400" />
                How resolution works
              </h4>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>
                  At hub construction, Synapse scans{' '}
                  <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">ServiceLoader.load(ProviderAdapter.class)</code>{' '}
                  and selects the adapter whose <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">providerName()</code> matches{' '}
                  <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">config.provider</code> (default: <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">openai</code>).
                </li>
                <li>
                  No match → construction fails with an{' '}
                  <code className="text-synapse-400 bg-synapse-500/10 px-1.5 py-0.5 rounded">IllegalArgumentException</code>{' '}
                  listing every registered provider, e.g. <code className="text-gray-300">No ProviderAdapter registered for provider 'gemini'. Registered providers: openai</code>.
                </li>
                <li>
                  Because request bodies, auth headers, URLs, response parsing, and SSE framing are delegated to the adapter,
                  switching providers is a one-line config change — your ISynapseHub code, streaming, retries, metrics, and interceptors stay identical.
                </li>
              </ul>
            </div>
          </FadeIn>
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
            <div className="bg-gradient-to-br from-neon-green/5 via-gray-900/60 to-synapse-600/5 backdrop-blur-xl border border-gray-800/40 rounded-2xl p-8 md:p-12 text-center">
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
