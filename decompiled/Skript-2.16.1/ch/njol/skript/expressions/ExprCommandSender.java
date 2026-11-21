package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.command.CommandSender;

@Name("Command Sender")
@Description({"The player or the console who sent a command. Mostly useful in <a href='commands'>commands</a> and <a href='#command'>command events</a>.", "If the command sender is a command block, its location can be retrieved by using %block's location%"})
@Example.Examples({@Example("make the command sender execute \"/say hi!\""), @Example("on command:\n\tlog \"%executor% used command /%command% %arguments%\" to \"commands.log\"\n")})
@Since({"2.0"})
@Events({"command"})
public class ExprCommandSender extends EventValueExpression<CommandSender> {
   public ExprCommandSender() {
      super(CommandSender.class);
   }

   static {
      register(ExprCommandSender.class, CommandSender.class, "[command['s]] (sender|executor)");
   }
}
