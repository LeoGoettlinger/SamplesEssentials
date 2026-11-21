package org.skriptlang.skript.registration;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.Statement;
import java.util.Collection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

public interface SyntaxRegistry extends ViewProvider<SyntaxRegistry>, Registry<SyntaxInfo<?>> {
   SyntaxRegistry.Key<DefaultSyntaxInfos.Structure<?>> STRUCTURE = SyntaxRegistry.Key.of("structure");
   SyntaxRegistry.Key<SyntaxInfo<? extends Section>> SECTION = SyntaxRegistry.Key.of("section");
   SyntaxRegistry.Key<SyntaxInfo<? extends Statement>> STATEMENT = SyntaxRegistry.Key.of("statement");
   SyntaxRegistry.Key<SyntaxInfo<? extends Effect>> EFFECT = SyntaxRegistry.ChildKey.of(STATEMENT, "effect");
   SyntaxRegistry.Key<SyntaxInfo<? extends Condition>> CONDITION = SyntaxRegistry.ChildKey.of(STATEMENT, "condition");
   SyntaxRegistry.Key<DefaultSyntaxInfos.Expression<?, ?>> EXPRESSION = SyntaxRegistry.Key.of("expression");

   @Contract("-> new")
   static SyntaxRegistry empty() {
      return new SyntaxRegistryImpl();
   }

   static SyntaxRegistry withOrigin(SyntaxRegistry syntaxRegistry, Origin origin) {
      return new SyntaxRegistryImpl.OriginApplyingRegistry(syntaxRegistry, origin);
   }

   @Unmodifiable
   <I extends SyntaxInfo<?>> Collection<I> syntaxes(SyntaxRegistry.Key<I> var1);

   <I extends SyntaxInfo<?>> void register(SyntaxRegistry.Key<I> var1, I var2);

   void unregister(SyntaxInfo<?> var1);

   <I extends SyntaxInfo<?>> void unregister(SyntaxRegistry.Key<I> var1, I var2);

   @Contract("-> new")
   default SyntaxRegistry unmodifiableView() {
      return new SyntaxRegistryImpl.UnmodifiableRegistry(this);
   }

   @Unmodifiable
   @Override
   Collection<SyntaxInfo<?>> elements();

   public interface ChildKey<I extends P, P extends SyntaxInfo<?>> extends SyntaxRegistry.Key<I> {
      @Contract("_, _ -> new")
      static <I extends P, P extends SyntaxInfo<?>> SyntaxRegistry.ChildKey<I, P> of(SyntaxRegistry.Key<P> parent, String name) {
         return new SyntaxRegistryImpl.ChildKeyImpl<>(parent, name);
      }

      SyntaxRegistry.Key<P> parent();
   }

   public interface Key<I extends SyntaxInfo<?>> {
      @Contract("_ -> new")
      static <I extends SyntaxInfo<?>> SyntaxRegistry.Key<I> of(String name) {
         return new SyntaxRegistryImpl.KeyImpl<>(name);
      }

      String name();
   }
}
