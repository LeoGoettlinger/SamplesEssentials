package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
@Alpha
public interface ExtensionHandlerCustomizer {
   ExtensionHandler customize(ExtensionHandler handler, Class<?> extensionType, Method method);
}
