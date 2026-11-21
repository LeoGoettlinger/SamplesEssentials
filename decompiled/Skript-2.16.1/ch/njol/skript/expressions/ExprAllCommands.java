package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
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

@Name("All commands")
@Description({"Returns all registered commands or all script commands."})
@Example.Examples({@Example("send \"Number of all commands: %size of all commands%\""), @Example("send \"Number of all script commands: %size of all script commands%\"")})
@Since({"2.6"})
public class ExprAllCommands extends SimpleExpression<String> {
   private boolean scriptCommandsOnly;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.scriptCommandsOnly = parseResult.mark == 1;
      return true;
   }

   @Nullable
   protected String[] get(Event e) {
      if (this.scriptCommandsOnly) {
         return Commands.getScriptCommands().toArray(new String[0]);
      } else {
         return Commands.getCommandMap() == null
            ? null
            : Commands.getCommandMap().getCommands().parallelStream().map(command -> command.getLabel()).toArray(String[]::new);
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "all " + (this.scriptCommandsOnly ? "script " : " ") + "commands";
   }

   static {
      Skript.registerExpression(ExprAllCommands.class, String.class, ExpressionType.SIMPLE, "[(all|the|all [of] the)] [registered] [(1¦script)] commands");
   }
}
