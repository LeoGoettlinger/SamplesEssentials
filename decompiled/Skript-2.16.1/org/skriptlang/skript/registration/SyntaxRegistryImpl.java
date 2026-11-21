package org.skriptlang.skript.registration;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Origin;

final class SyntaxRegistryImpl implements SyntaxRegistry {
   private final Map<SyntaxRegistry.Key<?>, SyntaxRegister<?>> registers = new ConcurrentHashMap<>();

   @Unmodifiable
   @Override
   public <I extends SyntaxInfo<?>> Collection<I> syntaxes(SyntaxRegistry.Key<I> key) {
      return this.register(key).syntaxes();
   }

   @Override
   public <I extends SyntaxInfo<?>> void register(SyntaxRegistry.Key<I> key, I info) {
      this.register(key).add(info);
      if (key instanceof SyntaxRegistry.ChildKey) {
         this.register(((SyntaxRegistry.ChildKey)key).parent(), info);
      }
   }

   @Override
   public void unregister(SyntaxInfo info) {
      for (SyntaxRegistry.Key key : this.registers.keySet()) {
         this.unregister(key, info);
      }
   }

   @Override
   public <I extends SyntaxInfo<?>> void unregister(SyntaxRegistry.Key<I> key, I info) {
      this.register(key).remove(info);
      if (key instanceof SyntaxRegistry.ChildKey) {
         this.unregister(((SyntaxRegistry.ChildKey)key).parent(), info);
      }
   }

   private <I extends SyntaxInfo<?>> SyntaxRegister<I> register(SyntaxRegistry.Key<I> key) {
      return (SyntaxRegister<I>)this.registers.computeIfAbsent(key, k -> new SyntaxRegister());
   }

   @Override
   public Collection<SyntaxInfo<?>> elements() {
      Builder<SyntaxInfo<?>> builder = ImmutableSet.builder();
      this.registers.values().forEach(register -> {
         synchronized (register.syntaxes) {
            builder.addAll(register.syntaxes);
         }
      });
      return builder.build();
   }

   static final class ChildKeyImpl<T extends P, P extends SyntaxInfo<?>> extends SyntaxRegistryImpl.KeyImpl<T> implements SyntaxRegistry.ChildKey<T, P> {
      private final SyntaxRegistry.Key<P> parent;

      ChildKeyImpl(SyntaxRegistry.Key<P> parent, String name) {
         super(name);
         this.parent = parent;
      }

      @Override
      public SyntaxRegistry.Key<P> parent() {
         return this.parent;
      }

      @Override
      public boolean equals(Object other) {
         return other instanceof SyntaxRegistry.ChildKey<?, ?> key && super.equals(other) && this.parent().equals(key.parent());
      }

      @Override
      public int hashCode() {
         return Objects.hash(super.hashCode(), this.parent());
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).add("name", this.name()).add("parent", this.parent()).toString();
      }
   }

   static class KeyImpl<T extends SyntaxInfo<?>> implements SyntaxRegistry.Key<T> {
      protected final String name;

      KeyImpl(String name) {
         this.name = name;
      }

      @Override
      public String name() {
         return this.name;
      }

      @Override
      public boolean equals(Object other) {
         return this == other ? true : other instanceof SyntaxRegistry.Key<?> key && this.name().equals(key.name());
      }

      @Override
      public int hashCode() {
         return this.name().hashCode();
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).add("name", this.name()).toString();
      }
   }

   static final class OriginApplyingRegistry implements SyntaxRegistry {
      private final SyntaxRegistry syntaxRegistry;
      private final Origin origin;

      OriginApplyingRegistry(SyntaxRegistry syntaxRegistry, Origin origin) {
         this.syntaxRegistry = syntaxRegistry;
         this.origin = origin;
      }

      @Unmodifiable
      @Override
      public <I extends SyntaxInfo<?>> Collection<I> syntaxes(SyntaxRegistry.Key<I> key) {
         return this.syntaxRegistry.syntaxes(key);
      }

      @Override
      public <I extends SyntaxInfo<?>> void register(SyntaxRegistry.Key<I> key, I info) {
         if (info.origin() == Origin.UNKNOWN) {
            info = (I)info.toBuilder().origin(this.origin).build();
         }

         this.syntaxRegistry.register(key, info);
      }

      @Override
      public void unregister(SyntaxInfo<?> info) {
         this.syntaxRegistry.unregister(info);
      }

      @Override
      public <I extends SyntaxInfo<?>> void unregister(SyntaxRegistry.Key<I> key, I info) {
         this.syntaxRegistry.unregister(key, info);
      }

      @Unmodifiable
      @Override
      public Collection<SyntaxInfo<?>> elements() {
         return this.syntaxRegistry.elements();
      }
   }

   static final class UnmodifiableRegistry implements SyntaxRegistry {
      private final SyntaxRegistry registry;

      UnmodifiableRegistry(SyntaxRegistry registry) {
         this.registry = registry;
      }

      @Unmodifiable
      @Override
      public Collection<SyntaxInfo<?>> elements() {
         return this.registry.elements();
      }

      @Unmodifiable
      @Override
      public <I extends SyntaxInfo<?>> Collection<I> syntaxes(SyntaxRegistry.Key<I> key) {
         return this.registry.syntaxes(key);
      }

      @Override
      public <I extends SyntaxInfo<?>> void register(SyntaxRegistry.Key<I> key, I info) {
         throw new UnsupportedOperationException("Cannot register syntax infos with an unmodifiable syntax registry.");
      }

      @Override
      public void unregister(SyntaxInfo<?> info) {
         throw new UnsupportedOperationException("Cannot unregister syntax infos from an unmodifiable syntax registry.");
      }

      @Override
      public <I extends SyntaxInfo<?>> void unregister(SyntaxRegistry.Key<I> key, I info) {
         throw new UnsupportedOperationException("Cannot unregister syntax infos from an unmodifiable syntax registry.");
      }

      @Override
      public SyntaxRegistry unmodifiableView() {
         return this;
      }
   }
}
