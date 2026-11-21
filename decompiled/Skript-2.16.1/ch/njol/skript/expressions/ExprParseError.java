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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Parse Error")
@Description({"The error which caused the last <a href='#ExprParse'>parse operation</a> to fail, which might not be set if a pattern was used and the pattern didn't match the provided text at all."})
@Example("set {var} to line 1 parsed as integer\nif {var} is not set:\n\tparse error is set:\n\t\tmessage \"<red>Line 1 is invalid: %last parse error%\"\n\telse:\n\t\tmessage \"<red>Please put an integer on line 1!\"\n")
@Since({"2.0"})
public class ExprParseError extends SimpleExpression<String> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   protected String[] get(Event e) {
      return ExprParse.lastError == null ? new String[0] : new String[]{ExprParse.lastError};
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the last parse error";
   }

   static {
      Skript.registerExpression(ExprParseError.class, String.class, ExpressionType.SIMPLE, "[the] [last] [parse] error");
   }
}
