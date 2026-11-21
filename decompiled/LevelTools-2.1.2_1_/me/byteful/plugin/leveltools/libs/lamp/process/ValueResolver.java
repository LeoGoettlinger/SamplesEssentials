package me.byteful.plugin.leveltools.libs.lamp.process;

import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import org.jetbrains.annotations.NotNull;

public interface ValueResolver<T> {
   T resolve(@NotNull ValueResolver.ValueResolverContext var1) throws Throwable;

   public interface ValueResolverContext extends ParameterResolver.ParameterResolverContext {
      ArgumentStack arguments();

      String popForParameter();

      String pop();

      int popInt();

      double popDouble();

      byte popByte();

      short popShort();

      float popFloat();

      long popLong();
   }
}
