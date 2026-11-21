package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveWalkOverEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Walk Over Event")
@Description({"Triggered when an entity walks over a grave. Provides access to the entity, grave, and location."})
@Examples({"on grave walk over:", "\tbroadcast \"Entity %event-entity% walked over and looted grave %event-grave% at location %event-location%\""})
public class EvtGraveWalkOver extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;
   private Literal<Location> location;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveWalkOverEvent event) {
         if (this.entity != null) {
            this.entity.check(event, new Predicate<Entity>() {
               public boolean test(Entity e) {
                  return e.equals(event.getEntity());
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

         if (this.location != null) {
            this.location.check(event, new Predicate<Location>() {
               public boolean test(Location l) {
                  return l.equals(event.getLocation());
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave walk over event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveWalkOver.class, "Grave Walk Over")
                  .addEvent(GraveWalkOverEvent.class)
                  .addPatterns(new String[]{"[grave] wal(k|ked|king) over"}))
               .addDescription("Triggered when an entity walks over a grave. Provides access to the entity, grave, and location.")
               .addExamples(
                  new String[]{
                     "on grave walk over:", "\tbroadcast \"Entity %event-entity% walked over and looted grave %event-grave% at location %event-location%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveWalkOverEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build());
      registry.register(EventValue.builder(GraveWalkOverEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveWalkOverEvent.class, String.class).getter(GraveEntityEvent::getEntityName).patterns(new String[]{"entity[-]name"}).build()
      );
      registry.register(
         EventValue.builder(GraveWalkOverEvent.class, UUID.class)
            .getter(GraveEntityEvent::getEntityUniqueId)
            .patterns(new String[]{"entity[-]uuid", "entity[-]unique[-]id"})
            .build()
      );
      registry.register(EventValue.builder(GraveWalkOverEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build());
      registry.register(
         EventValue.builder(GraveWalkOverEvent.class, UUID.class)
            .getter(GraveEvent::getGraveOwnerUniqueId)
            .patterns(new String[]{"grave[-]owner[-]uuid", "grave[-]owner[-]unique[-]id"})
            .build()
      );
      registry.register(
         EventValue.builder(GraveWalkOverEvent.class, String.class)
            .getter(GraveEvent::getGraveOwnerDisplayName)
            .patterns(new String[]{"grave[-]owner[-]display[-]name", "grave[-]owner[-]name"})
            .build()
      );
      registry.register(
         EventValue.builder(GraveWalkOverEvent.class, Number.class)
            .getter(event -> event.getGraveExperience())
            .patterns(new String[]{"grave[-]experience", "grave[-]exp"})
            .build()
      );
   }
}
