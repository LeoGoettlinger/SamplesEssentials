package ch.njol.skript.lang;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface SyntaxElement {
   default boolean preInit() {
      return true;
   }

   boolean init(Expression<?>[] var1, int var2, Kleenean var3, SkriptParser.ParseResult var4);

   default ParserInstance getParser() {
      return ParserInstance.get();
   }

   @Contract(
      pure = true
   )
   @NotNull
   String getSyntaxTypeName();
}
