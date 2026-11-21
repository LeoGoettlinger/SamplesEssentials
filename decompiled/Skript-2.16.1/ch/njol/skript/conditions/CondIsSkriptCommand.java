package ch.njol.skript.conditions;

import ch.njol.skript.command.Commands;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is a Skript command")
@Description({"Checks whether a command/string is a custom Skript command."})
@Example("on command:\n\tcommand is a skript command\n")
@Since({"2.6"})
public class CondIsSkriptCommand extends PropertyCondition<String> {
   public boolean check(String cmd) {
      return Commands.scriptCommandExists(cmd);
   }

   @Override
   protected String getPropertyName() {
      return "skript command";
   }

   static {
      register(CondIsSkriptCommand.class, "[a] s(k|c)ript (command|cmd)", "string");
   }
}
