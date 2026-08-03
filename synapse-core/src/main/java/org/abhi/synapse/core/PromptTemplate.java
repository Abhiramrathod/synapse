package org.abhi.synapse.core;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders {@code {placeholder}} templates for prompt messages.
 *
 * <p>Variable references use the {@code {name}} syntax where {@code name} must
 * start with a letter or underscore and may contain letters, digits and
 * underscores. This is a deliberate, safe subset of template syntax: there is
 * no expression evaluation, no method/field access and no EL injection.</p>
 *
 * <p>Unknown variables (keys absent from the supplied map) are left intact in
 * the output so a partially-supplied variable set never destroys a template.
 * Values are injected via {@link Matcher#quoteReplacement} so special
 * characters such as {@code $} and {@code \} are inserted literally.</p>
 *
 * <p>Typical usage via {@link org.abhi.synapse.core.model.ChatMessage}:</p>
 * <pre>{@code
 * ChatMessage msg = ChatMessage.user("Hello {name}, you asked about {topic}.")
 *         .withVariable("name", "Alice")
 *         .withVariable("topic", "Java");
 * }</pre>
 */
public final class PromptTemplate {

    private static final Pattern VARIABLE = Pattern.compile("\\{([A-Za-z_][A-Za-z0-9_]*)\\}");

    private PromptTemplate() {
    }

    /**
     * Renders every {@code {name}} reference found in {@code template} using the
     * supplied variables.
     *
     * @param template  the template text; may be {@code null}
     * @param variables the variable map; a {@code null} or empty map returns the
     *                  template unchanged
     * @return the rendered text, or {@code null} when {@code template} is {@code null}
     */
    public static String render(String template, Map<String, Object> variables) {
        if (template == null) return null;
        if (variables == null || variables.isEmpty()) return template;

        Matcher matcher = VARIABLE.matcher(template);
        if (!matcher.find()) return template;

        StringBuffer result = new StringBuffer(template.length());
        do {
            String key = matcher.group(1);
            Object value = variables.get(key);
            if (value != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
            } else {
                matcher.appendReplacement(result, matcher.group(0));
            }
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }
}
