package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GravePostTeleportEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
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

@Name("Grave Post Teleport Event")
@Description({"Triggered after an entity has teleported to a grave. Provides access to the grave, entity and location."})
@Examples({"on grave post teleport:", "\tbroadcast \"%event-entity% teleported to grave %event-grave% at location %event-location%\""})
public class EvtGravePostTeleport extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;
   private Literal<Location> location;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GravePostTeleportEvent event) {
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
      return "Grave post teleport event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGravePostTeleport.class, "Grave Post Teleport")
                  .addEvent(GravePostTeleportEvent.class)
                  .addPatterns(new String[]{"[grave] post telepor(t|ting|ted)"}))
               .addDescription("Triggered after an entity has teleported to a grave. Provides access to the grave, entity and location.")
               .addExamples(
                  new String[]{"on grave post teleport:", "\tbroadcast \"%event-entity% teleported to grave %event-grave% at location %event-location%\""}
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GravePostTeleportEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build()
      );
      registry.register(EventValue.builder(GravePostTeleportEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GravePostTeleportEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
   }
}
