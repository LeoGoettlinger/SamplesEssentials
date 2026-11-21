package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.util.function.Supplier;

public class ConfigurateException extends IOException {
   private static final long serialVersionUID = 1635526451813128733L;
   @Nullable
   private Supplier<NodePath> path;

   public static ConfigurateException wrap(final ConfigurationNode source, final IOException ex) {
      return ex instanceof ConfigurateException ? (ConfigurateException)ex : new ConfigurateException(source, ex);
   }

   public ConfigurateException() {
   }

   public ConfigurateException(final String message) {
      super(message);
   }

   public ConfigurateException(final Throwable cause) {
      super(cause);
   }

   public ConfigurateException(@Nullable final String message, @Nullable final Throwable cause) {
      super(message, cause);
   }

   public ConfigurateException(final ConfigurationNode pos, final String message) {
      super(message);
      this.path = pos::path;
   }

   public ConfigurateException(final ConfigurationNode pos, final Throwable cause) {
      super(cause);
      this.path = pos::path;
   }

   public ConfigurateException(final ConfigurationNode pos, @Nullable final String message, @Nullable final Throwable cause) {
      super(message, cause);
      this.path = pos::path;
   }

   public ConfigurateException(final NodePath path, @Nullable final String message, @Nullable final Throwable cause) {
      super(message, cause);
      this.path = () -> path;
   }

   public NodePath path() {
      Supplier<NodePath> path = this.path;
      return path == null ? NodePath.path() : path.get();
   }

   public void initPath(final Supplier<NodePath> path) {
      if (this.path == null) {
         this.path = path;
      }
   }

   @Nullable
   public String rawMessage() {
      return super.getMessage();
   }

   @Nullable
   @Override
   public String getMessage() {
      return this.path().toString() + ": " + super.getMessage();
   }
}
