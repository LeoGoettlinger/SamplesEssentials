package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import java.util.Collection;
import java.util.SequencedCollection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.util.Priority;

public non-sealed interface SyntaxInfo<E extends SyntaxElement> extends DefaultSyntaxInfos {
   Priority SIMPLE = Priority.base();
   Priority COMBINED = Priority.after(SIMPLE);
   Priority PATTERN_MATCHES_EVERYTHING = Priority.after(COMBINED);

   @Contract("_, _, _ -> new")
   static <E extends SyntaxElement> SyntaxInfo<E> simple(Class<E> type, Supplier<E> instanceSupplier, String... patterns) {
      return builder(type).supplier(instanceSupplier).addPatterns(patterns).build();
   }

   @Contract("_ -> new")
   static <E extends SyntaxElement> SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E> builder(Class<E> type) {
      return (SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E>)(new SyntaxInfoImpl.BuilderImpl<>(type));
   }

   @Contract("-> new")
   SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E> toBuilder();

   Origin origin();

   Class<E> type();

   @Contract("-> new")
   E instance();

   @Unmodifiable
   SequencedCollection<String> patterns();

   Priority priority();

   public interface Builder<B extends SyntaxInfo.Builder<B, E>, E extends SyntaxElement> {
      @Contract("_ -> this")
      B origin(Origin var1);

      @Contract("_ -> this")
      B supplier(Supplier<E> var1);

      @Contract("_ -> this")
      B addPattern(String var1);

      @Contract("_ -> this")
      B addPatterns(String... var1);

      @Contract("_ -> this")
      B addPatterns(Collection<String> var1);

      @Contract("-> this")
      B clearPatterns();

      @Contract("_ -> this")
      B priority(Priority var1);

      @Contract("-> new")
      SyntaxInfo<E> build();

      void applyTo(SyntaxInfo.Builder<?, ?> var1);
   }
}
