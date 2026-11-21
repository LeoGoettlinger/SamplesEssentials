package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.UUID;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Random UUID")
@Description({"Returns a random UUID."})
@Example("set {_uuid} to random uuid")
@Since({"2.5.1, 2.11 (return UUIDs)"})
public class ExprRandomUUID extends SimpleExpression<UUID> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Nullable
   protected UUID[] get(Event e) {
      return new UUID[]{UUID.randomUUID()};
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends UUID> getReturnType() {
      return UUID.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "random uuid";
   }

   static {
      Skript.registerExpression(ExprRandomUUID.class, UUID.class, ExpressionType.SIMPLE, "[a] random uuid");
   }
}
