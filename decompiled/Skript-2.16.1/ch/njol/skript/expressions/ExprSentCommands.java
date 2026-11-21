package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtPlayerCommandSend;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Sent Command List")
@Description({"The commands that will be sent to the player in a <a href='#send_command_list'>send commands to player event</a>.", "Modifications will affect what commands show up for the player to tab complete. They will not affect what commands the player can actually run.", "Adding new commands to the list is illegal behavior and will be ignored."})
@Example("on send command list:\n\tset command list to command list where [input does not contain \":\"]\n\tremove \"help\" from command list\n")
@Since({"2.8.0"})
@Events({"send command list"})
public class ExprSentCommands extends SimpleExpression<String> {
   private EvtPlayerCommandSend parent;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Structure structure = this.getParser().getCurrentStructure();
      if (!(structure instanceof EvtPlayerCommandSend)) {
         Skript.error("The 'command list' expression can only be used in a 'send command list' event");
         return false;
      } else if (!isDelayed.isFalse()) {
         Skript.error("Can't change the command list after the event has already passed");
         return false;
      } else {
         this.parent = (EvtPlayerCommandSend)structure;
         return true;
      }
   }

   @Nullable
   protected String[] get(Event event) {
      return !(event instanceof PlayerCommandSendEvent) ? null : ((PlayerCommandSendEvent)event).getCommands().toArray(new String[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case REMOVE:
         case DELETE:
         case SET:
         case RESET:
            return new Class[]{String[].class};
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerCommandSendEvent) {
         Collection<String> commands = ((PlayerCommandSendEvent)event).getCommands();
         if (mode == Changer.ChangeMode.DELETE) {
            commands.clear();
         } else {
            List<String> deltaCommands = delta != null && delta.length > 0 ? Lists.newArrayList((String[])delta) : new ArrayList<>();
            switch (mode) {
               case REMOVE:
                  commands.removeAll(deltaCommands);
                  break;
               case DELETE:
               default:
                  assert false;
                  break;
               case SET:
                  List<String> newCommands = new ArrayList<>(deltaCommands);
                  newCommands.removeAll(this.parent.getOriginalCommands());
                  deltaCommands.removeAll(newCommands);
                  commands.clear();
                  commands.addAll(deltaCommands);
                  break;
               case RESET:
                  commands.clear();
                  commands.addAll(this.parent.getOriginalCommands());
            }
         }
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
   public String toString(@Nullable Event event, boolean debug) {
      return "the sent server command list";
   }

   static {
      Skript.registerExpression(ExprSentCommands.class, String.class, ExpressionType.SIMPLE, "[the] [sent] [server] command[s] list");
   }
}
