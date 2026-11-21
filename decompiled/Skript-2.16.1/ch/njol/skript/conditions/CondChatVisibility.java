package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.ClientOption.ChatVisibility;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Can See Messages")
@Description({"Checks whether a player can see specific message types in chat."})
@Example.Examples({@Example("if player can see all messages:\n\tsend \"You can see all messages.\"\n"), @Example("if player can only see commands:\n\tsend \"This game doesn't work with commands-only chat.\"\n"), @Example("if player can't see any messages:\n\tsend action bar \"Server shutting down in 5 minutes!\"\n")})
@Since({"2.10"})
public class CondChatVisibility extends Condition {
   private int pattern = 0;
   private Expression<Player> player;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.pattern = matchedPattern;
      this.player = (Expression<Player>)expressions[0];
      this.setNegated(matchedPattern > 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      Player player = this.player.getSingle(event);
      if (player == null) {
         return false;
      } else {
         ChatVisibility current = (ChatVisibility)player.getClientOption(ClientOption.CHAT_VISIBILITY);

         return switch (this.pattern) {
            case 0 -> current == ChatVisibility.FULL;
            case 1 -> current == ChatVisibility.SYSTEM;
            case 2 -> current == ChatVisibility.HIDDEN;
            case 3 -> current != ChatVisibility.FULL;
            case 4 -> current != ChatVisibility.SYSTEM;
            default -> throw new IllegalStateException("Unexpected value: " + this.pattern);
         };
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.pattern) {
         case 0 -> this.player.toString(event, debug) + " can see all messages";
         case 1 -> this.player.toString(event, debug) + " can only see commands";
         case 2 -> this.player.toString(event, debug) + " can't see any messages";
         case 3 -> this.player.toString(event, debug) + " can't see all messages";
         case 4 -> this.player.toString(event, debug) + " can't only see commands";
         default -> throw new IllegalStateException("Unexpected value: " + this.pattern);
      };
   }

   static {
      if (Skript.classExists("com.destroystokyo.paper.ClientOption$ChatVisibility")) {
         Skript.registerCondition(
            CondChatVisibility.class,
            "%player% can see all messages [in chat]",
            "%player% can only see (commands|system messages) [in chat]",
            "%player% can('t|[ ]not) see any (command[s]|message[s]) [in chat]",
            "%player% can('t|[ ]not) see all messages [in chat]",
            "%player% can('t|[ ]not) only see (commands|system messages) [in chat]"
         );
      }
   }
}
