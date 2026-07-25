export const quickStartCode = `import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.http.SynapseHub;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.ChatMessage;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) {
        // Build configuration
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4")
                .temperature(0.7)
                .maxTokens(1024)
                .build();

        // Create hub and send prompts
        try (SynapseHub hub = new SynapseHub(config)) {
            // Simple prompt
            SynapseResponse response = hub.sendPrompt(
                "What is the capital of France?"
            );
            System.out.println(response.getContent());

            // Multi-turn conversation
            List<ChatMessage> messages = List.of(
                ChatMessage.system("You are a helpful assistant."),
                ChatMessage.user("Explain quantum computing.")
            );
            SynapseResponse chat = hub.sendChat(messages);
            System.out.println(chat.getContent());
        }
    }
}`

export const streamingCode = `try (SynapseHub hub = new SynapseHub(config)) {
    // Stream a prompt - tokens arrive in real-time
    hub.streamPrompt("Write a poem about programming", chunk -> {
        System.out.print(chunk);  // Prints each token as it arrives
    });

    // Stream a multi-turn conversation
    hub.streamChat(messages, chunk -> {
        System.out.print(chunk);
    });
}`

export const springBootCode = `# application.yml
synapse:
  base-url: https://api.openai.com
  endpoint: /v1/chat/completions
  api-key: \${OPENAI_API_KEY}
  model-name: gpt-4
  temperature: 0.7
  max-tokens: 1024
  timeout: 30s
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
        SynapseResponse response = synapseHub.sendPrompt(question);
        return response.getContent();
    }

    public Stream<String> streamQuestion(String question) {
        List<String> chunks = new ArrayList<>();
        synapseHub.streamPrompt(question, chunks::add);
        return chunks.stream();
    }
}`

export const interceptorCode = `public class LoggingInterceptor implements SynapseRequestInterceptor {

    @Override
    public void beforeRequest(SynapseRequestContext ctx) {
        System.out.println("Sending request to: " + ctx.getUrl());
        System.out.println("Headers: " + ctx.getHeaders());
    }

    @Override
    public void afterRequest(SynapseRequestContext ctx) {
        System.out.println("Request completed for: " + ctx.getUrl());
    }

    @Override
    public void onError(SynapseRequestContext ctx, SynapseException error) {
        System.err.println("Request failed: " + error.getMessage());
    }
}`

export const retryPolicyCode = `public class CustomRetryPolicy implements SynapseRetryPolicy {

    @Override
    public boolean shouldRetry(int attempt, SynapseException error) {
        return error.getType() == ExceptionType.RATE_LIMIT_ERROR
                || error.getType() == ExceptionType.SERVER_ERROR;
    }

    @Override
    public long getDelay(int attempt) {
        // Exponential backoff: 1s, 2s, 4s
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
        .timeout(Duration.ofSeconds(30))
        .maxRetries(3)
        .retryDelay(Duration.ofMillis(500))
        .enableLogging(true)
        .requestInterceptor(new LoggingInterceptor())
        .responseInterceptor(new ResponseLogger())
        .retryPolicy(new CustomRetryPolicy())
        .metricsListener(new MetricsListener())
        .build();`

export const errorHandlingCode = `try {
    SynapseResponse response = hub.sendPrompt("Hello");
} catch (SynapseException e) {
    switch (e.getType()) {
        case RATE_LIMIT_ERROR:
            // Handle rate limiting
            break;
        case SERVER_ERROR:
            // Handle server errors
            break;
        case NETWORK_ERROR:
            // Handle network issues
            break;
        case TIMEOUT_ERROR:
            // Handle timeout
            break;
        default:
            // Handle other errors
    }

    if (e.isRetryable()) {
        // Automatically retried based on retry policy
    }
}`

export const mavenXml = `<!-- JitPack Repository -->
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<!-- Pure Java -->
<dependency>
    <groupId>com.github.Abhiramrathod</groupId>
    <artifactId>synapse-all</artifactId>
    <version>v1.0.4</version>
</dependency>

<!-- Spring Boot Starter -->
<dependency>
    <groupId>com.github.Abhiramrathod</groupId>
    <artifactId>synapse-spring-boot-starter</artifactId>
    <version>v1.0.4</version>
</dependency>`
