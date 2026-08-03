package org.abhi.synapse.core.tool;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a single parameter of an annotated tool method.
 *
 * <p>Applied to method parameters of a {@link SynapseTool @SynapseTool} method
 * to provide the parameter's schema name, description and whether it is
 * required. If {@link #name()} is empty the parameter's declared name is used.</p>
 *
 * <pre>{@code
 * @SynapseTool(description = "Adds two numbers")
 * public int add(@ToolParam("The first number") int a,
 *                @ToolParam("The second number") int b) { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ToolParam {
    /**
     * Shorthand description for the parameter, equivalent to {@link #description()}.
     */
    String value() default "";

    /** Overrides the parameter name in the generated schema; defaults to the declared parameter name. */
    String name() default "";

    /** Description of the parameter for the model. */
    String description() default "";

    /** Whether the model must supply this argument. */
    boolean required() default true;
}
