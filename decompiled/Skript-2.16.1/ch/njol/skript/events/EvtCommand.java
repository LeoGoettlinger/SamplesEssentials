package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

public class EvtCommand extends SkriptEvent {
   @Nullable
   private String[] commands = null;
   private Literal<String> commandsLit;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
      if (args[0] != null) {
         this.commandsLit = (Literal<String>)args[0];
         this.commands = this.commandsLit.getAll();

         for (int i = 0; i < this.commands.length; i++) {
            if (this.commands[i].startsWith("/")) {
               this.commands[i] = this.commands[i].substring(1);
            }
         }
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      if (event instanceof ServerCommandEvent serverCommandEvent && serverCommandEvent.getCommand().isEmpty()) {
         return false;
      } else if (this.commands == null) {
         return true;
      } else {
         String message;
         if (event instanceof PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
            assert playerCommandPreprocessEvent.getMessage().startsWith("/");

            message = playerCommandPreprocessEvent.getMessage().substring(1);
         } else {
            assert event instanceof ServerCommandEvent;

            message = ((ServerCommandEvent)event).getCommand();
         }

         return Arrays.stream(this.commands)
            .anyMatch(
               candidateCommand -> StringUtils.startsWithIgnoreCase(message, candidateCommand)
                  && (
                     candidateCommand.contains(" ")
                        || message.length() == candidateCommand.length()
                        || Character.isWhitespace(message.charAt(candidateCommand.length()))
                  )
            );
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "command" + (this.commandsLit != null ? " " + this.commandsLit.toString(event, debug) : "");
   }

   static {
      Skript.registerEvent(
            "Command", EvtCommand.class, CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class), "command [%-strings%]"
         )
         .description(
            "Called when a player enters a command (not necessarily a Skript command) but you can check if command is a skript command, see <a href='#CondIsSkriptCommand'>Is a Skript command condition</a>."
         )
         .examples("on command:", "on command \"/stop\":", "on command \"pm Njol \":")
         .since("2.0");
   }
}
