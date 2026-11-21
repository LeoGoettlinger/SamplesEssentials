package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveCloseEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Close Event")
@Description({"Triggered when an inventory associated with a grave is closed. Provides access to the grave and inventory view."})
@Examples({"on grave close:", "\tbroadcast \"%event-player% closed grave %event-grave% at block %event-block% and inventory %event-inventory-view%\""})
public class EvtGraveClose extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;
   private Literal<InventoryView> inventoryView;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (!(e instanceof GraveCloseEvent event)) {
         return false;
      } else if (this.player != null && !this.player.check(event, playerValue -> playerValue.equals(event.getPlayer()))) {
         return false;
      } else {
         return this.grave != null && !this.grave.check(event, graveValue -> graveValue.equals(event.getGrave()))
            ? false
            : this.inventoryView == null || this.inventoryView.check(event, inventoryViewValue -> inventoryViewValue.equals(event.getInventoryView()));
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave close event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.inventoryView != null ? " with inventory view " + this.inventoryView.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveClose.class, "Grave Close")
                  .addEvent(GraveCloseEvent.class)
                  .addPatterns(new String[]{"[grave] clos(e|ing|ed)"}))
               .addDescription("Triggered when an inventory associated with a grave is closed. Provides access to the grave and inventory view.")
               .addExamples(
                  new String[]{
                     "on grave close:", "\tbroadcast \"%event-player% closed grave %event-grave% at block %event-block% and inventory %event-inventory-view%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveCloseEvent.class, Player.class).getter(GravePlayerEvent::getPlayer).patterns(new String[]{"player"}).build());
      registry.register(EventValue.builder(GraveCloseEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveCloseEvent.class, InventoryView.class)
            .getter(GravePlayerEvent::getInventoryView)
            .patterns(new String[]{"inventory[-]view"})
            .build()
      );
   }
}
