export const quickStartCode = `import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.http.SynapseHub;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.ChatMessage;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4")
                .temperature(0.7)
                .maxTokens(1024)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
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
        }
    }
}`

export const streamingCode = `try (SynapseHub hub = new SynapseHub(config)) {
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
}`

export const springBootCode = `# application.yml
synapse:
  base-url: https://api.openai.com
  endpoint: /v1/chat/completions
  api-key: \${OPENAI_API_KEY}
  model-name: gpt-4
  temperature: 0.7
  max-tokens: 1024
  connect-timeout: 5s
  read-timeout: 30s
  request-timeout: 60s
  max-retries: 3
  retry-delay: 500ms
  enable-logging: true`

export const springBootServiceCode = `@Service
public class LlmService {

    private final ISynapseHub synapseHub;

    public LlmService(ISynapseHub synapseHub) {
        this.synapseHub = synapseHub;
    }

    public String askQuestion(String question) {
        SynapseResponse response = synapseHub.sendPrompt(question, null);
        return response.getContent();
    }
}`

export const interceptorCode = `public class TracingInterceptor implements SynapseRequestInterceptor {

    @Override
    public void beforeRequest(SynapseRequestContext ctx) {
        ctx.getHeaders().put("X-Request-Id",
            UUID.randomUUID().toString());
    }

    @Override
    public void afterRequest(SynapseRequestContext ctx) { }

    @Override
    public void onError(SynapseRequestContext ctx,
                        SynapseException error) { }
}`

export const retryPolicyCode = `public class CustomRetryPolicy implements SynapseRetryPolicy {

    @Override
    public boolean shouldRetry(int attempt, SynapseException error) {
        return error.getType() == ExceptionType.RATE_LIMIT_ERROR
                || error.getType() == ExceptionType.SERVER_ERROR;
    }

    @Override
    public long getDelay(int attempt, SynapseException error,
                          Map<String, List<String>> headers) {
        // Uses Retry-After header if present, otherwise
        // exponential backoff: 1s, 2s, 4s + jitter
        return 1000L * (long) Math.pow(2, attempt);
    }

    @Override
    public int getMaxRetries() {
        return 3;
    }
}`

export const fullConfigCode = `SynapseConfig config = SynapseConfig.builder()
        .baseUrl("https://api.openai.com")
        .endpoint("/v1/chat/completions")
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4")
        .provider("openai")         // matches registered ProviderAdapter
        .temperature(0.7)
        .maxTokens(2048)
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(30))
        .requestTimeout(Duration.ofSeconds(60))
        .streamIdleTimeout(Duration.ofSeconds(30))
        .maxRetries(3)
        .retryDelay(Duration.ofMillis(500))
        .maxRetryElapsedTime(Duration.ofSeconds(120))
        .maxConcurrentRequests(64)
        .circuitBreakerFailureThreshold(5)
        .circuitBreakerOpenDuration(Duration.ofSeconds(30))
        .enableLogging(true)
        .requestInterceptor(new TracingInterceptor())
        .responseInterceptor(new MetricsInterceptor())
        .retryPolicy(new CustomRetryPolicy())
        .metricsListener(new MicrometerListener())
        // synapse-cache module
        .cache(CaffeineResponseCache.builder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build())
        .build();`

export const errorHandlingCode = `try {
    SynapseResponse response = hub.sendPrompt("Hello", null);
} catch (SynapseException e) {
    switch (e.getType()) {
        case RATE_LIMIT_ERROR:
            break;
        case SERVER_ERROR:
            break;
        case NETWORK_ERROR:
            break;
        case TIMEOUT_ERROR:
            break;
        case CIRCUIT_BREAKER_OPEN:
            break;
        case RETRY_EXHAUSTED:
            break;
        case STREAMING_ERROR:
            break;
        default:
            break;
    }
    if (e.isRetryable()) {
        // Automatically retried by framework
    }
}`

export const mavenXml = `<!-- JitPack Repository -->
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<!-- Pure Java (all modules) -->
<dependency>
    <groupId>com.github.Abhiramrathod</groupId>
    <artifactId>synapse-all</artifactId>
    <version>TAG</version>
</dependency>

<!-- Spring Boot Starter -->
<dependency>
    <groupId>com.github.Abhiramrathod</groupId>
    <artifactId>synapse-spring-boot-starter</artifactId>
    <version>TAG</version>
</dependency>`

export const providerAdapterContractCode = `public interface ProviderAdapter {
    // unique id, matched against config.provider
    String providerName();

    // request URL construction
    String buildUrl(String baseUrl, String endpoint);

    // auth headers (Bearer, x-api-key, ...)
    Map<String, String> buildAuthHeaders(String apiKey);

    // request body for chat completions
    Map<String, Object> buildChatBody(List<ChatMessage> messages,
        double temperature, int maxTokens, String modelName,
        boolean streaming, List<ToolDefinition> tools, String responseFormat);

    // non-streaming response -> SynapseResponse
    SynapseResponse parseResponse(String responseBody);

    // /models response -> List<Model>
    List<Model> parseModels(String responseBody);

    // one SSE chunk -> text delta
    String extractContentFromStreamChunk(String jsonData);

    // is the stream finished?
    boolean isStreamDone(String line);

    // SSE framing (default handles "data: " lines)
    default String extractStreamData(String line) { ... }
}`

export const anthropicAdapterCode = `package com.myapp.provider;

import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class AnthropicProviderAdapter implements ProviderAdapter {

    private final ObjectMapper objectMapper;

    public AnthropicProviderAdapter() { this(new ObjectMapper()); }
    public AnthropicProviderAdapter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    @Override
    public String providerName() { return "anthropic"; }

    @Override
    public String buildUrl(String baseUrl, String endpoint) {
        return baseUrl.replaceAll("/+$", "") + endpoint;
    }

    @Override
    public Map<String, String> buildAuthHeaders(String apiKey) {
        Map<String, String> h = new HashMap<>();
        h.put("x-api-key", apiKey);                 // not Bearer!
        h.put("anthropic-version", "2023-06-01");
        return h;
    }

    @Override
    public Map<String, Object> buildChatBody(List<ChatMessage> messages,
            double temperature, int maxTokens, String modelName, boolean streaming,
            List<ToolDefinition> tools, String responseFormat) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        if (streaming) body.put("stream", true);

        StringBuilder system = new StringBuilder();
        List<Map<String, Object>> msgs = new ArrayList<>();
        for (ChatMessage m : messages) {
            if ("system".equals(m.getRole())) {
                system.append(m.getContent()).append("\\n");   // top-level system field
            } else {
                Map<String, Object> msg = new HashMap<>();
                msg.put("role", m.getRole());
                msg.put("content", m.getContent());
                msgs.add(msg);
            }
        }
        if (system.length() > 0) body.put("system", system.toString().trim());
        body.put("messages", msgs);
        return body;
    }

    @Override
    public SynapseResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            SynapseResponse response = new SynapseResponse();
            response.setModel(root.path("model").asText(null));
            response.setContent(root.path("content").path(0).path("text").asText(""));
            response.setFinishReason(root.path("stop_reason").asText(null));
            JsonNode usage = root.path("usage");
            response.setPromptTokens(usage.path("input_tokens").asInt(0));
            response.setCompletionTokens(usage.path("output_tokens").asInt(0));
            return response;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse Anthropic response", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    @Override
    public List<Model> parseModels(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<Model> models = new ArrayList<>();
            for (JsonNode n : root.path("data")) {
                models.add(Model.builder()
                        .id(n.path("id").asText(null))
                        .ownedBy(n.path("display_name").asText(null))
                        .build());
            }
            return models;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse models response", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    @Override
    public String extractStreamData(String line) {
        if (line == null) return null;
        String t = line.trim();
        return t.startsWith("data:") && t.substring(5).trim().startsWith("{")
                ? t.substring(5).trim() : null;
    }

    @Override
    public String extractContentFromStreamChunk(String jsonData) {
        try {
            return objectMapper.readTree(jsonData)
                    .path("delta").path("text").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean isStreamDone(String line) {
        return line == null || line.contains("message_stop");
    }
}`

export const providerServiceFileCode = `# src/main/resources/META-INF/services/org.abhi.synapse.core.ProviderAdapter
com.myapp.provider.AnthropicProviderAdapter`

export const providerSpiConfigCode = `SynapseConfig config = SynapseConfig.builder()
        .baseUrl("https://api.anthropic.com")
        .endpoint("/v1/messages")
        .apiKey(System.getenv("ANTHROPIC_API_KEY"))
        .modelName("claude-3-5-sonnet-20240620")
        .provider("anthropic")      // matches providerName()
        .build();

try (SynapseHub hub = new SynapseHub(config)) {
    // Same API as OpenAI — no code changes required
    SynapseResponse response = hub.sendPrompt("Explain the contract.", null);
    System.out.println(response.getContent());
}`

export const providerSpiYamlCode = `# application.yml
synapse:
  base-url: https://api.anthropic.com
  endpoint: /v1/messages
  api-key: \${ANTHROPIC_API_KEY}
  model-name: claude-3-5-sonnet-20240620
  provider: anthropic`

export const responseCacheCode = `// synapse-cache module
ResponseCache cache = CaffeineResponseCache.builder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build();

SynapseConfig config = SynapseConfig.builder()
        .baseUrl("https://api.openai.com")
        .endpoint("/v1/chat/completions")
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4")
        .cache(cache)                // alias: .responseCache(...)
        .build();

try (SynapseHub hub = new SynapseHub(config)) {
    // First call hits the provider and stores the result...
    SynapseResponse first = hub.sendPrompt("Summarize the SLA", null);

    // ...identical prompts are served straight from cache
    SynapseResponse second = hub.sendPrompt("Summarize the SLA", null);
}

// Distributed alternative — driver supplied via RedisClientProvider SPI
ResponseCache redis = RedisResponseCache.viaServiceLoader();`

export const fallbackHubCode = `import org.abhi.synapse.core.FallbackSynapseHub;
import org.abhi.synapse.core.ISynapseHub;

ISynapseHub primary = new SynapseHub(openAiConfig);
ISynapseHub backup  = new SynapseHub(anthropicConfig);

// Try hubs in order; route around any hub that throws a SynapseException
ISynapseHub hub = new FallbackSynapseHub(primary, backup);

// The full ISynapseHub surface — calling code is unchanged
SynapseResponse response = hub.sendPrompt(
    "Explain the fallback semantics.", null);
System.out.println(response.getContent());`

export const loadBalancingHubCode = `import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.LoadBalancingSynapseHub;

// Same provider, different accounts / regions / API keys
ISynapseHub hubA = new SynapseHub(configA);
ISynapseHub hubB = new SynapseHub(configB);

// Thread-safe round-robin: each call goes to the next hub
ISynapseHub hub = new LoadBalancingSynapseHub(hubA, hubB);

// Combine both patterns for distribution AND resilience
ISynapseHub resilient =
        new FallbackSynapseHub(
                new LoadBalancingSynapseHub(hubA, hubB),
                new SynapseHub(configC));`

export const streamFlowCode = `import org.abhi.synapse.core.StreamFlow;

// Stream a prompt, filter blanks, trim, and print each chunk
StreamFlow.ofPrompt(hub, "Tell me a story")
        .filter(chunk -> !chunk.isBlank())
        .map(String::trim)
        .forEach(chunk -> System.out.print(chunk)); // CompletableFuture<Void>

// Collect the whole stream into one string
String fullText = StreamFlow.ofPrompt(hub, "Explain qubits")
        .join().join();

// Block and unwrap SynapseExceptions for direct handling
String lastChunk = StreamFlow.ofChat(hub, messages).blockLast();

// Suppress upstream errors with a fallback element
List<String> chunks = StreamFlow.of(publisher)
        .onErrorReturn("(error)")
        .toList().join();

// Count elements
long count = StreamFlow.of(publisher).count().join();`

export const dynamicReconfigCode = `SynapseHub hub = new SynapseHub(config);

// Individual updates — chainable, returns the same hub
hub.updateApiKey("sk-rotated")
   .updateDefaultModel("gpt-4o")
   .updateBaseUrl("https://backup.example.com")
   .updateEndpoint("/v2/chat/completions")
   .updateRequestTimeout(Duration.ofSeconds(90))
   .updateTemperature(0.2)
   .updateMaxTokens(2048);

// Or apply a whole new config in one shot (re-validates first)
hub.reconfigure(newConfig);

// The HttpClient pool, async executor, circuit breaker,
// rate limiter, interceptors, and metrics are all preserved.
// Only subsequent requests see the new values.`

export const tokenProviderCode = `import org.abhi.synapse.core.TokenProvider;

// Rotating token — the supplier is invoked on every request
AtomicReference<String> token = new AtomicReference<>("current");

SynapseConfig config = SynapseConfig.builder()
        .baseUrl("https://api.openai.com")
        .endpoint("/v1/chat/completions")
        .tokenProvider(TokenProvider.fromSupplier(token::get))  // no apiKey needed
        .modelName("gpt-4")
        .build();

try (SynapseHub hub = new SynapseHub(config)) {
    hub.sendPrompt("Hi", null);   // Authorization: Bearer current
    token.set("rotated");
    hub.sendPrompt("Hi", null);   // Authorization: Bearer rotated
}

// Static bearer token alternative
TokenProvider.bearer("static-token");

// Custom schemes (AWS SigV4, etc.) override buildAuthorizationHeader()
public class SigV4Provider implements TokenProvider {
    @Override public String getToken() {
        return signCanonicalRequest();
    }
    @Override public String buildAuthorizationHeader() {
        return "AWS4-HMAC-SHA256 " + getToken();
    }
}`
