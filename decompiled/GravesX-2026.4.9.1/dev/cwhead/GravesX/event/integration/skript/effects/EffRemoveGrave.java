package dev.cwhead.GravesX.event.integration.skript.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("GravesX Remove Grave")
@Description({"Removes a grave through the GravesX grave management API."})
@Examples({"remove grave event-grave"})
public class EffRemoveGrave extends Effect {
   private Expression<Grave> grave;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      this.grave = (Expression<Grave>)expressions[0];
      return true;
   }

   protected void execute(Event event) {
      GraveManagementAPI api = SkriptImpl.getGraveManagementAPI();
      Grave graveValue = (Grave)this.grave.getSingle(event);
      if (api != null && graveValue != null) {
         api.removeGrave(graveValue);
      }
   }

   public String toString(@Nullable Event event, boolean debug) {
      return "remove grave " + this.grave;
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.EFFECT,
            SyntaxInfo.builder(EffRemoveGrave.class).addPatterns(new String[]{"remove [the] grave %grave%", "delete [the] grave %grave%"}).build()
         );
   }
}
