package org.jdbi.v3.core.extension;

import org.jdbi.v3.core.JdbiException;

public final class NoSuchExtensionException extends JdbiException {
   private static final long serialVersionUID = 1L;
   private static final String DEFAULT_MESSAGE = "Extension not found: %s [Hint: maybe you need to register a plugin, like SqlObjectPlugin]";

   public NoSuchExtensionException(Class<?> extensionClass) {
      super(formatMessage(extensionClass, "Extension not found: %s [Hint: maybe you need to register a plugin, like SqlObjectPlugin]"));
   }

   public NoSuchExtensionException(Class<?> extensionClass, String message) {
      super(formatMessage(extensionClass, message));
   }

   public NoSuchExtensionException(Class<?> extensionClass, Throwable cause) {
      super(formatMessage(extensionClass, "Extension not found: %s [Hint: maybe you need to register a plugin, like SqlObjectPlugin]"), cause);
   }

   public NoSuchExtensionException(Class<?> extensionClass, String message, Throwable cause) {
      super(formatMessage(extensionClass, message), cause);
   }

   private static String formatMessage(Class<?> extensionClass, String formatString) {
      return String.format(formatString, extensionClass == null ? "<null>" : extensionClass.getSimpleName());
   }
}
