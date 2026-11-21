package org.skriptlang.skript.bukkit.input.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Pressing Key")
@Description({"Checks if a player is pressing a certain input key."})
@Example("on player input:\n\tif player is pressing forward movement key:\n\t\tsend \"You are moving forward!\"\n")
@Since({"2.10"})
@Keywords({"press", "input"})
@RequiredPlugins({"Minecraft 1.21.2+"})
public class CondIsPressingKey extends Condition {
   private Expression<Player> players;
   private Expression<InputKey> inputKeys;
   private boolean past;
   private boolean delayed;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         SyntaxInfo.builder(CondIsPressingKey.class)
            .addPatterns(
               "%players% (is|are) pressing %inputkeys%",
               "%players% (isn't|is not|aren't|are not) pressing %inputkeys%",
               "%players% (was|were) pressing %inputkeys%",
               "%players% (wasn't|was not|weren't|were not) pressing %inputkeys%"
            )
            .supplier(CondIsPressingKey::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)expressions[0];
      this.inputKeys = (Expression<InputKey>)expressions[1];
      this.past = matchedPattern > 1;
      this.delayed = !isDelayed.isFalse();
      if (this.past) {
         if (!this.getParser().isCurrentEvent(PlayerInputEvent.class)) {
            Skript.warning("Checking the past state of a player's input outside the 'player input' event has no effect.");
         } else if (this.delayed) {
            Skript.warning("Checking the past state of a player's input after the event has passed has no effect.");
         }
      }

      this.setNegated(matchedPattern == 1 || matchedPattern == 3);
      return true;
   }

   @Override
   public boolean check(Event event) {
      Player eventPlayer = event instanceof PlayerInputEvent inputEvent ? inputEvent.getPlayer() : null;
      InputKey[] inputKeys = this.inputKeys.getAll(event);
      boolean and = this.inputKeys.getAnd();
      boolean delayed = this.delayed || Delay.isDelayed(event);
      return this.players.check(event, player -> {
         Input input;
         if (!delayed && !this.past && player.equals(eventPlayer)) {
            input = ((PlayerInputEvent)event).getInput();
         } else {
            input = player.getCurrentInput();
         }

         return CollectionUtils.check(inputKeys, inputKey -> inputKey.check(input), and);
      }, this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append(this.players);
      if (this.past) {
         builder.append(this.players.isSingle() ? "was" : "were");
      } else {
         builder.append(this.players.isSingle() ? "is" : "are");
      }

      if (this.isNegated()) {
         builder.append("not");
      }

      builder.append("pressing");
      builder.append(this.inputKeys);
      return builder.toString();
   }
}
