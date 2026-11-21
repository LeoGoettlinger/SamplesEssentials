package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveLootedEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Looted Event")
@Description({"Triggered when an inventory associated with a grave is completely looted. Provides access to the grave and inventory view."})
@Examples({"on grave looted:", "\tbroadcast \"%event-player% completely looted grave %event-grave% at block %event-block% and inventory %event-inventory-view%\""})
public class EvtGraveLooted extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;
   private Literal<InventoryView> inventoryView;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveLootedEvent event) {
         if (this.player != null) {
            this.player.check(event, new Predicate<Player>() {
               public boolean test(Player p) {
                  return p.equals(event.getPlayer());
               }
            });
         }

         if (this.grave != null) {
            this.grave.check(event, new Predicate<Grave>() {
               public boolean test(Grave g) {
                  return g.equals(event.getGrave());
               }
            });
         }

         if (this.inventoryView != null) {
            this.inventoryView.check(event, new Predicate<InventoryView>() {
               public boolean test(InventoryView view) {
                  return view.equals(event.getInventoryView());
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave looted event "
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.inventoryView != null ? " with inventory view " + this.inventoryView.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveLooted.class, "Grave Looted")
                  .addEvent(GraveLootedEvent.class)
                  .addPatterns(new String[]{"[grave] loo(t|ting|ted)"}))
               .addDescription("Triggered when an inventory associated with a grave is completely looted. Provides access to the grave and inventory view.")
               .addExamples(
                  new String[]{
                     "on grave looted:",
                     "\tbroadcast \"%event-player% completely looted grave %event-grave% at block %event-block% and inventory %event-inventory-view%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveLootedEvent.class, Player.class).getter(GravePlayerEvent::getPlayer).patterns(new String[]{"player"}).build());
      registry.register(EventValue.builder(GraveLootedEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveLootedEvent.class, InventoryView.class)
            .getter(GravePlayerEvent::getInventoryView)
            .patterns(new String[]{"inventory[-]view"})
            .build()
      );
   }
}
