package com.fastasyncworldedit.core.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SuggestInputParseException extends InputParseException {
   private final InputParseException cause;
   private final Supplier<List<String>> getSuggestions;

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public SuggestInputParseException(String msg, String prefix, Supplier<List<String>> getSuggestions) {
      this(new InputParseException(msg), getSuggestions);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public static SuggestInputParseException of(Throwable other, String prefix, Supplier<List<String>> getSuggestions) {
      return other instanceof InputParseException
         ? of((InputParseException)other, getSuggestions)
         : of(new InputParseException(other.getMessage()), getSuggestions);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public static SuggestInputParseException of(InputParseException other, String prefix, Supplier<List<String>> getSuggestions) {
      return other instanceof SuggestInputParseException ? (SuggestInputParseException)other : new SuggestInputParseException(other, getSuggestions);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public SuggestInputParseException(InputParseException other, String prefix, Supplier<List<String>> getSuggestions) {
      super(other.getRichMessage());
      Preconditions.checkNotNull(getSuggestions);
      Preconditions.checkNotNull(other);
      this.cause = other;
      this.getSuggestions = getSuggestions;
   }

   public SuggestInputParseException(Component message, Supplier<List<String>> getSuggestions) {
      this(new InputParseException(message), getSuggestions);
   }

   public static SuggestInputParseException of(Throwable other, Supplier<List<String>> getSuggestions) {
      return other instanceof InputParseException
         ? of((InputParseException)other, getSuggestions)
         : of(new InputParseException(other.getMessage()), getSuggestions);
   }

   public static SuggestInputParseException of(InputParseException other, Supplier<List<String>> getSuggestions) {
      return other instanceof SuggestInputParseException ? (SuggestInputParseException)other : new SuggestInputParseException(other, getSuggestions);
   }

   public SuggestInputParseException(InputParseException other, Supplier<List<String>> getSuggestions) {
      super(other.getRichMessage());
      Preconditions.checkNotNull(getSuggestions);
      Preconditions.checkNotNull(other);
      this.cause = other;
      this.getSuggestions = getSuggestions;
   }

   public static SuggestInputParseException get(InvocationTargetException e) {
      Throwable t = e;

      while (t.getCause() != null) {
         t = t.getCause();
         if (t instanceof SuggestInputParseException) {
            return (SuggestInputParseException)t;
         }
      }

      return null;
   }

   public static SuggestInputParseException of(String input, List<Object> values) {
      throw new SuggestInputParseException(
         Caption.of("fawe.error.no-value-for-input", input),
         () -> values.stream().map(Object::toString).filter(v -> v.startsWith(input)).collect(Collectors.toList())
      );
   }

   @Override
   public synchronized Throwable getCause() {
      return this.cause.getCause();
   }

   @Override
   public String getMessage() {
      return this.cause.getMessage();
   }

   public List<String> getSuggestions() {
      return this.getSuggestions.get();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public SuggestInputParseException prepend(String input) {
      return this;
   }
}
