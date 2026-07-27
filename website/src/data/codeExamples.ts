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
