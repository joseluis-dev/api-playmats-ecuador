package com.playmatsec.app.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca controladores o métodos que requieren validar el header X-User-Id
 * contra la base de datos antes de procesar la petición.
 *
 * Implementación ligera para no alterar lógica existente.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Authorized {
}
