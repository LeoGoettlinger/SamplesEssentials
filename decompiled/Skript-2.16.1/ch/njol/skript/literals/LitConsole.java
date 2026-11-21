package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Console")
@Description({"Represents the server's console which can receive messages and execute commands"})
@Example.Examples({@Example("execute console command \"/stop\""), @Example("send \"message to console\" to the console")})
@Since({"1.3.1"})
public class LitConsole extends SimpleLiteral<ConsoleCommandSender> {
   private static final ConsoleCommandSender console = Bukkit.getConsoleSender();

   public LitConsole() {
      super(new ConsoleCommandSender[]{console}, ConsoleCommandSender.class, true);
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the console";
   }

   static {
      Skript.registerExpression(LitConsole.class, ConsoleCommandSender.class, ExpressionType.SIMPLE, "[the] (console|server)");
   }
}
