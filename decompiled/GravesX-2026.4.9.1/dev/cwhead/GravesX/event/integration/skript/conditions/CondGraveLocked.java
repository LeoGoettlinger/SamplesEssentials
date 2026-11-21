package dev.cwhead.GravesX.event.integration.skript.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("GravesX Grave Locked")
@Description({"Checks whether a grave is locked through the GravesX grave management API."})
@Examples({"if event-grave is locked:", "if event-grave is locked for player:"})
public class CondGraveLocked extends Condition {
   private Expression<Grave> grave;
   private Expression<Player> player;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      this.grave = (Expression<Grave>)expressions[0];
      this.setNegated(matchedPattern == 1 || matchedPattern == 3);
      if (matchedPattern == 2 || matchedPattern == 3) {
         this.player = (Expression<Player>)expressions[1];
      }

      return true;
   }

   public boolean check(Event event) {
      GraveManagementAPI api = SkriptImpl.getGraveManagementAPI();
      Grave graveValue = (Grave)this.grave.getSingle(event);
      if (api != null && graveValue != null) {
         boolean result;
         if (this.player != null) {
            Player playerValue = (Player)this.player.getSingle(event);
            result = playerValue != null && api.isGraveLocked(graveValue, playerValue);
         } else {
            result = api.isGraveLocked(graveValue);
         }

         return this.isNegated() != result;
      } else {
         return this.isNegated();
      }
   }

   public String toString(@Nullable Event event, boolean debug) {
      return this.grave + (this.isNegated() ? " is not locked" : " is locked") + (this.player != null ? " for " + this.player : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.CONDITION,
            SyntaxInfo.builder(CondGraveLocked.class)
               .addPatterns(new String[]{"%grave% is locked", "%grave% is not locked", "%grave% is locked for %player%", "%grave% is not locked for %player%"})
               .build()
         );
   }
}
