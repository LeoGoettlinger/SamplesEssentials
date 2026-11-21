package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

@Name("Command Info")
@Description({"Get information about a command."})
@Example.Examples({@Example("main command label of command \"skript\""), @Example("description of command \"help\""), @Example("label of command \"pl\""), @Example("usage of command \"help\""), @Example("aliases of command \"bukkit:help\""), @Example("permission of command \"/op\""), @Example("command \"op\"'s permission message"), @Example("command \"sk\"'s plugin owner"), @Example("command /greet <player>:\n\tusage: /greet <target>\n\ttrigger:\n\t\tif arg-1 is sender:\n\t\t\tsend \"&cYou can't greet yourself! Usage: %the usage%\"\n\t\t\tstop\n\t\tsend \"%sender% greets you!\" to arg-1\n\t\tsend \"You greeted %arg-1%!\"\n")})
@Since({"2.6"})
public class ExprCommandInfo extends SimpleExpression<String> {
   private ExprCommandInfo.InfoType type;
   @Nullable
   private Expression<String> commandName;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.commandName = (Expression<String>)exprs[0];
      if (this.commandName == null && !this.getParser().isCurrentEvent(ScriptCommandEvent.class, PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
         Skript.error("There's no command in " + Utils.a(this.getParser().getCurrentEventName()) + " event. Please provide a command");
         return false;
      } else {
         this.type = ExprCommandInfo.InfoType.values()[Math.floorDiv(matchedPattern, 2)];
         return true;
      }
   }

   @Nullable
   protected String[] get(Event event) {
      Command[] commands = this.getCommands(event);
      if (commands == null) {
         return new String[0];
      } else if (this.type == ExprCommandInfo.InfoType.ALIASES) {
         ArrayList<String> result = new ArrayList<>();

         for (Command command : commands) {
            result.addAll(getAliases(command));
         }

         return result.toArray(new String[0]);
      } else {
         String[] result = new String[commands.length];

         for (int i = 0; i < commands.length; i++) {
            result[i] = this.type.function.apply(commands[i]);
         }

         return result;
      }
   }

   @Override
   public boolean isSingle() {
      return this.type != ExprCommandInfo.InfoType.ALIASES && (this.commandName == null || this.commandName.isSingle());
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the "
         + this.type.name().toLowerCase(Locale.ENGLISH).replace("_", " ")
         + (this.commandName == null ? "" : " of command " + this.commandName.toString(event, debug));
   }

   @Nullable
   private Command[] getCommands(Event event) {
      if (event instanceof ScriptCommandEvent && this.commandName == null) {
         return new Command[]{((ScriptCommandEvent)event).getScriptCommand().getBukkitCommand()};
      } else {
         CommandMap map = Commands.getCommandMap();
         if (map == null) {
            return null;
         } else if (this.commandName != null) {
            return this.commandName.stream(event).<Command>map(map::getCommand).filter(Objects::nonNull).toArray(Command[]::new);
         } else {
            String commandName;
            if (event instanceof ServerCommandEvent) {
               commandName = ((ServerCommandEvent)event).getCommand();
            } else {
               if (!(event instanceof PlayerCommandPreprocessEvent)) {
                  return null;
               }

               commandName = ((PlayerCommandPreprocessEvent)event).getMessage().substring(1);
            }

            commandName = commandName.split(":")[0];
            Command command = map.getCommand(commandName);
            return command != null ? new Command[]{command} : null;
         }
      }
   }

   private static List<String> getAliases(Command command) {
      if (command instanceof PluginCommand && ((PluginCommand)command).getPlugin() == Skript.getInstance()) {
         ScriptCommand scriptCommand = Commands.getScriptCommand(command.getName());
         return scriptCommand == null ? command.getAliases() : scriptCommand.getAliases();
      } else {
         return command.getAliases();
      }
   }

   static {
      Skript.registerExpression(
         ExprCommandInfo.class,
         String.class,
         ExpressionType.PROPERTY,
         "[the] main command [label|name] [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] main command [label|name]",
         "[the] description [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] description",
         "[the] label [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] label",
         "[the] usage [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] usage",
         "[(all|the|all [of] the)] aliases [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] aliases",
         "[the] permission [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] permission",
         "[the] permission message [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] permission message",
         "[the] plugin [owner] [of [[the] command[s] %-strings%]]",
         "command[s] %strings%'[s] plugin [owner]"
      );
   }

   private static enum InfoType {
      NAME(Command::getName),
      DESCRIPTION(Command::getDescription),
      LABEL(Command::getLabel),
      USAGE(Command::getUsage),
      ALIASES(null),
      PERMISSION(Command::getPermission),
      PERMISSION_MESSAGE(Command::getPermissionMessage),
      PLUGIN(command -> {
         if (command instanceof PluginCommand) {
            return ((PluginCommand)command).getPlugin().getName();
         } else if (command instanceof BukkitCommand) {
            return "Bukkit";
         } else if (command.getClass().getPackage().getName().startsWith("org.spigot")) {
            return "Spigot";
         } else {
            return command.getClass().getPackage().getName().startsWith("com.destroystokyo.paper") ? "Paper" : "Unknown";
         }
      });

      @Nullable
      private final Function<Command, String> function;

      private InfoType(@Nullable Function<Command, String> function) {
         this.function = function;
      }
   }
}
