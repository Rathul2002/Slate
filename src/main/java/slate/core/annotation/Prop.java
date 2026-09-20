package slate.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a component behavior field as a consumer of a Slate prop.
 *
 * <p>By default, the Java field name is used as the prop name.</p>
 *
 * <p>A custom prop name can be supplied when the XML prop name does not
 * match the Java field name.</p>
 *
 * <example>
 * <pre>
 * {@code
 * @Prop
 * private String title;
 *
 * @Prop("user-name")
 * private String userName;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Prop {

    /**
     * Optional explicit Slate prop name.
     *
     * <p>An empty value means that the Java field name is used.</p>
     */
    String value() default "";
}