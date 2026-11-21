package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.util.ClassUtils;
import org.skriptlang.skript.util.Priority;

class SyntaxInfoImpl<T extends SyntaxElement> implements SyntaxInfo<T> {
   private final Origin origin;
   private final Class<T> type;
   @Nullable
   private final Supplier<T> supplier;
   private final SequencedCollection<String> patterns;
   private final Priority priority;

   private static Priority estimatePriority(Collection<String> patterns) {
      Priority priority = SyntaxInfo.SIMPLE;

      for (String pattern : patterns) {
         char[] chars = pattern.toCharArray();

         for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
               if (i <= 0 || chars[i - 1] != '\\') {
                  if (i > 1 && chars[i - 2] == '%' || i > 2 && chars[i - 3] == '%') {
                     return SyntaxInfo.PATTERN_MATCHES_EVERYTHING;
                  }

                  priority = SyntaxInfo.COMBINED;
               }
            } else if (chars[i] == '<' && (i <= 0 || chars[i - 1] != '\\')) {
               return SyntaxInfo.PATTERN_MATCHES_EVERYTHING;
            }
         }
      }

      return priority;
   }

   protected SyntaxInfoImpl(Origin origin, Class<T> type, @Nullable Supplier<T> supplier, SequencedCollection<String> patterns, @Nullable Priority priority) {
      Preconditions.checkArgument(
         supplier != null || ClassUtils.isNormalClass(type),
         "Failed to register a syntax info for '" + type.getName() + "'. Element classes must be a normal type unless a supplier is provided."
      );
      Preconditions.checkArgument(!patterns.isEmpty(), "Failed to register a syntax info for '" + type.getName() + "'. There must be at least one pattern.");
      this.origin = origin;
      this.type = type;
      this.supplier = supplier;
      this.patterns = ImmutableList.copyOf(patterns);
      if (priority == null) {
         priority = estimatePriority(patterns);
      }

      this.priority = priority;
   }

   @Override
   public SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, T>, T> toBuilder() {
      SyntaxInfoImpl.BuilderImpl<? extends SyntaxInfo.Builder<?, T>, T> builder = new SyntaxInfoImpl.BuilderImpl<>(this.type);
      builder.origin(this.origin);
      if (this.supplier != null) {
         builder.supplier(this.supplier);
      }

      builder.addPatterns(this.patterns);
      builder.priority(this.priority);
      return builder;
   }

   @Override
   public Origin origin() {
      return this.origin;
   }

   @Override
   public Class<T> type() {
      return this.type;
   }

   @Override
   public T instance() {
      try {
         return this.supplier == null ? this.type.getDeclaredConstructor().newInstance() : this.supplier.get();
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   @Unmodifiable
   @Override
   public SequencedCollection<String> patterns() {
      return this.patterns;
   }

   @Override
   public Priority priority() {
      return this.priority;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other instanceof SyntaxInfo<?> info) {
         return other.getClass() != SyntaxInfoImpl.class && !other.equals(this)
            ? false
            : this.type() == info.type() && Objects.equals(this.patterns(), info.patterns()) && Objects.equals(this.priority(), info.priority());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type(), this.patterns(), this.priority());
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
         .add("origin", this.origin())
         .add("type", this.type())
         .add("patterns", this.patterns())
         .add("priority", this.priority())
         .toString();
   }

   static class BuilderImpl<B extends SyntaxInfo.Builder<B, E>, E extends SyntaxElement> implements SyntaxInfo.Builder<B, E> {
      final Class<E> type;
      Origin origin = Origin.UNKNOWN;
      @Nullable
      Supplier<E> supplier;
      final List<String> patterns = new ArrayList<>();
      @Nullable
      Priority priority;

      BuilderImpl(Class<E> type) {
         this.type = type;
      }

      @Override
      public B origin(Origin origin) {
         this.origin = origin;
         return (B)this;
      }

      @Override
      public B supplier(Supplier<E> supplier) {
         this.supplier = supplier;
         return (B)this;
      }

      @Override
      public B addPattern(String pattern) {
         this.patterns.add(pattern);
         return (B)this;
      }

      @Override
      public B addPatterns(String... patterns) {
         Collections.addAll(this.patterns, patterns);
         return (B)this;
      }

      @Override
      public B addPatterns(Collection<String> patterns) {
         this.patterns.addAll(patterns);
         return (B)this;
      }

      @Override
      public B clearPatterns() {
         this.patterns.clear();
         return (B)this;
      }

      @Override
      public B priority(Priority priority) {
         this.priority = priority;
         return (B)this;
      }

      @Override
      public SyntaxInfo<E> build() {
         return new SyntaxInfoImpl<>(this.origin, this.type, this.supplier, this.patterns, this.priority);
      }

      @Override
      public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
         builder.origin(this.origin);
         if (this.supplier != null) {
            builder.supplier(this.supplier);
         }

         builder.addPatterns(this.patterns);
         if (this.priority != null) {
            builder.priority(this.priority);
         }
      }
   }
}
