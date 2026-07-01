package de.urmel_dl.dbt.rest.utils;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Name-binding for DBT entity message body providers.
 * <p>
 * Use this on JAX-RS resources or methods that must be handled by the
 * {@link de.urmel_dl.dbt.rest.utils.EntityMessageBodyWriter} and
 * {@link de.urmel_dl.dbt.rest.utils.EntityMessageBodyReader} instead of other
 * registered providers (e.g., Jackson).
 */
@NameBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityMessageBodyBinding {
}
