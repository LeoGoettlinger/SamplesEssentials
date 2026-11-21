package ch.njol.skript.lang;

import ch.njol.skript.lang.util.ConvertedLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import java.util.Optional;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

public class LiteralString extends VariableString implements Literal<String> {
   protected LiteralString(String input) {
      super(input);
   }

   public String[] getArray() {
      return new String[]{this.original};
   }

   public String getSingle() {
      return this.original;
   }

   public String[] getAll() {
      return new String[]{this.original};
   }

   @Override
   public Optional<String> getOptionalSingle(Event event) {
      return Optional.of(this.original);
   }

   @Nullable
   @Override
   public <R> Literal<? extends R> getConvertedExpression(Class<R>... to) {
      if (CollectionUtils.containsSuperclass(to, String.class)) {
         return this;
      } else {
         Class<R> superType = (Class<R>)Utils.getSuperType(to);
         R[] parsedData = Converters.convert(this.getArray(), to, superType);
         return parsedData.length != 1 ? null : new ConvertedLiteral<>(this, parsedData, superType);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "\"" + this.original + "\"";
   }
}
