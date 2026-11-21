package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveItemTakeEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Item Take Event")
@Description({"Triggered when an item is clicked or moved in a grave."})
@Examples({"on grave item take:", "\tbroadcast \"Entity %event-entity% took %event-itemstack% using action %event-inventoryaction% from %event-grave% at location %event-location%\""})
public class EvtGraveItemTake extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;
   private Literal<ItemStack> itemStack;
   private Literal<InventoryAction> inventoryAction;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveItemTakeEvent event) {
         if (this.entity != null) {
            this.entity.check(event, new Predicate<Entity>() {
               public boolean test(Entity ent) {
                  return ent.equals(event.getEntity());
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

         if (this.itemStack != null) {
            this.itemStack.check(event, new Predicate<ItemStack>() {
               public boolean test(ItemStack is) {
                  return is.equals(event.getItem());
               }
            });
         }

         if (this.inventoryAction != null) {
            this.inventoryAction.check(event, new Predicate<InventoryAction>() {
               public boolean test(InventoryAction ia) {
                  return ia.equals(event.getAction());
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave item take event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.itemStack != null ? " with item stack " + this.itemStack.toString(e, debug) : "")
         + (this.inventoryAction != null ? " with inventory action " + this.inventoryAction.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveItemTake.class, "Grave Item Take")
                  .addEvent(GraveItemTakeEvent.class)
                  .addPatterns(new String[]{"[grave] Ite(m|ms) Tak(e|ing)"}))
               .addDescription("Triggered when an item is clicked or moved in a grave.")
               .addExamples(
                  new String[]{
                     "on grave item take:",
                     "\tbroadcast \"Entity %event-entity% took %event-itemstack% using action %event-inventoryaction% from %event-grave% at location %event-location%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveItemTakeEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build());
      registry.register(EventValue.builder(GraveItemTakeEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveItemTakeEvent.class, ItemStack.class).getter(GraveItemTakeEvent::getItem).patterns(new String[]{"item", "itemstack"}).build()
      );
      registry.register(
         EventValue.builder(GraveItemTakeEvent.class, InventoryAction.class)
            .getter(GraveItemTakeEvent::getAction)
            .patterns(new String[]{"inventory[-]action", "action"})
            .build()
      );
   }
}
