package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

public class SerializationException extends ConfigurateException {
   public static final long serialVersionUID = -910568375387191784L;
   @Nullable
   private transient Type expectedType;

   public SerializationException() {
   }

   public SerializationException(final String message) {
      super(message);
   }

   public SerializationException(final Throwable cause) {
      super(cause);
   }

   public SerializationException(final Type expectedType, final String message) {
      super(message);
      this.expectedType = expectedType;
   }

   public SerializationException(final AnnotatedType expectedType, final String message) {
      super(message);
      this.expectedType = expectedType.getType();
   }

   public SerializationException(final Type expectedType, final Throwable cause) {
      super(cause);
      this.expectedType = expectedType;
   }

   public SerializationException(final AnnotatedType expectedType, final Throwable cause) {
      super(cause);
      this.expectedType = expectedType.getType();
   }

   public SerializationException(final Type expectedType, final String message, final Throwable cause) {
      super(message, cause);
      this.expectedType = expectedType;
   }

   public SerializationException(final AnnotatedType expectedType, final String message, final Throwable cause) {
      super(message, cause);
      this.expectedType = expectedType.getType();
   }

   public SerializationException(final ConfigurationNode node, final Type expectedType, final String message) {
      this(node, expectedType, message, null);
   }

   public SerializationException(final ConfigurationNode node, final AnnotatedType expectedType, final String message) {
      this(node, expectedType, message, null);
   }

   public SerializationException(final ConfigurationNode node, final Type expectedType, final Throwable cause) {
      this(node, expectedType, null, cause);
   }

   public SerializationException(final ConfigurationNode node, final AnnotatedType expectedType, final Throwable cause) {
      this(node, expectedType, null, cause);
   }

   public SerializationException(final ConfigurationNode node, final Type expectedType, @Nullable final String message, @Nullable final Throwable cause) {
      super(node, message, cause);
      this.expectedType = expectedType;
   }

   public SerializationException(
      final ConfigurationNode node, final AnnotatedType expectedType, @Nullable final String message, @Nullable final Throwable cause
   ) {
      super(node, message, cause);
      this.expectedType = expectedType.getType();
   }

   public SerializationException(final NodePath path, final Type expectedType, final String message) {
      super(path, message, null);
      this.expectedType = expectedType;
   }

   public SerializationException(final NodePath path, final AnnotatedType expectedType, final String message) {
      super(path, message, null);
      this.expectedType = expectedType.getType();
   }

   @Nullable
   public Type expectedType() {
      return this.expectedType;
   }

   @Nullable
   @Override
   public String getMessage() {
      return this.expectedType == null ? super.getMessage() : this.path() + " of type " + this.expectedType.getTypeName() + ": " + this.rawMessage();
   }

   public void initType(final Type type) {
      if (this.expectedType == null) {
         this.expectedType = type;
      }
   }

   public final void initType(final AnnotatedType type) {
      if (this.expectedType == null) {
         this.expectedType = type.getType();
      }
   }
}
