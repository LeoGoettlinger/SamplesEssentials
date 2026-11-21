package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;

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
