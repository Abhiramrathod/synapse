package org.abhi.synapse.core.tool;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type or a public method as an LLM-callable tool.
 *
 * <p>When applied to a class, every public method declared on that class
 * (except {@link Object} methods) is exposed as a separate tool named after
 * the method. When applied to a single method, only that method is exposed.</p>
 *
 * <p>Tool methods may declare parameters; each parameter is described with
 * {@link ToolParam}. The schema for the tool is derived from the method
 * signature at runtime, and arguments returned by the model are bound back to
 * the method before it is invoked.</p>
 *
 * <pre>{@code
 * public class WeatherTools {
 *     @SynapseTool(name = "get_weather", description = "Gets the weather for a city")
 *     public String getWeather(@ToolParam("The city name") String city) {
 *         return "Sunny in " + city;
 *     }
 * }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SynapseTool {
    /**
     * The tool name exposed to the model. Defaults to the method name when
     * applied to a method, or to the simple class name when applied to a type.
     */
    String name() default "";

    /** Human-readable description of what the tool does. */
    String description() default "";
}
