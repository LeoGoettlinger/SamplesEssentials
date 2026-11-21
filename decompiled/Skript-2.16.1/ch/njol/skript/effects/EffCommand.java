package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.invoke.StringConcatFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Command")
@Description({"Executes a command. This can be useful to use other plugins in triggers.", "If the command is a bungeecord side command, you can use the [bungeecord] option to execute command on the proxy."})
@Example.Examples({@Example("make player execute command \"/home\""), @Example("execute console command \"/say Hello everyone!\""), @Example("execute player bungeecord command \"/alert &6Testing Announcement!\"")})
@Since({"1.0, 2.8.0 (bungeecord command)"})
public class EffCommand extends Effect {
   public static final String MESSAGE_CHANNEL = "Message";
   @Nullable
   private Expression<CommandSender> senders;
   private Expression<String> commands;
   private boolean bungeecord;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         this.commands = (Expression<String>)exprs[0];
         this.senders = (Expression<CommandSender>)exprs[1];
      } else {
         this.senders = (Expression<CommandSender>)exprs[0];
         this.commands = (Expression<String>)exprs[1];
      }

      this.bungeecord = parseResult.hasTag("bungee");
      if (this.bungeecord && this.senders == null) {
         Skript.error("The commandsenders expression cannot be omitted when using the bungeecord option");
         return false;
      } else {
         this.commands = VariableString.setStringMode(this.commands, StringMode.COMMAND);
         return true;
      }
   }

   @Override
   public void execute(Event event) {
      for (String command : this.commands.getArray(event)) {
         assert command != null;

         if (command.startsWith("/")) {
            command = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(command.substring(1));
         }

         if (this.senders != null) {
            for (CommandSender sender : this.senders.getArray(event)) {
               if (this.bungeecord) {
                  if (sender instanceof Player player) {
                     Utils.sendPluginMessage(player, "BungeeCord", "Message", player.getName(), "/" + command);
                  }
               } else {
                  Skript.dispatchCommand(sender, command);
               }
            }
         } else {
            Skript.dispatchCommand(Bukkit.getConsoleSender(), command);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "make "
         + (this.senders != null ? this.senders.toString(event, debug) : "the console")
         + " execute "
         + (this.bungeecord ? "bungeecord " : "")
         + "command "
         + this.commands.toString(event, debug);
   }

   static {
      Skript.registerEffect(
         EffCommand.class,
         "[execute] [the] [bungee:bungee[cord]] command[s] %strings% [by %-commandsenders%]",
         "[execute] [the] %commandsenders% [bungee:bungee[cord]] command[s] %strings%",
         "(let|make) %commandsenders% execute [[the] [bungee:bungee[cord]] command[s]] %strings%"
      );
   }
}
