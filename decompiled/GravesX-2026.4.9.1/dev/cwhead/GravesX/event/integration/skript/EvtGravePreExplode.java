package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GravePreExplodeEvent;
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

@Name("Grave Pre Explode Event")
@Description({"Triggered before an explosion affecting a grave is processed. Provides access to the grave, explosion location, source entity (if any), and radius."})
@Examples({"on grave pre explode:", "\tbroadcast \"Grave %event-grave% is about to be exploded at %event-location% with radius %event-number% by %event-entity%\""})
public class EvtGravePreExplode extends SkriptEvent {
   private Literal<Grave> grave;
   private Literal<Location> location;
   private Literal<Entity> source;
   private Literal<Number> radius;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GravePreExplodeEvent event) {
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
                  return l.equals(event.getExplosionLocation());
               }
            });
         }

         if (this.source != null) {
            this.source.check(event, new Predicate<Entity>() {
               public boolean test(Entity ent) {
                  return ent != null && ent.equals(event.getSource());
               }
            });
         }

         if (this.radius != null) {
            this.radius.check(event, new Predicate<Number>() {
               public boolean test(Number r) {
                  return r.floatValue() == event.getRadius();
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave pre explode event"
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " with location " + this.location.toString(e, debug) : "")
         + (this.source != null ? " with source " + this.source.toString(e, debug) : "")
         + (this.radius != null ? " with radius " + this.radius.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGravePreExplode.class, "Grave Pre Explode")
                  .addEvent(GravePreExplodeEvent.class)
                  .addPatterns(new String[]{"[grave] pre[-]explod(e|ing)"}))
               .addDescription("")
               .addExamples(
                  new String[]{
                     "on grave pre explode:",
                     "\tbroadcast \"Grave %event-grave% is about to be exploded at %event-location% with radius %event-number% by %event-entity%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GravePreExplodeEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GravePreExplodeEvent.class, Location.class)
            .getter(GravePreExplodeEvent::getExplosionLocation)
            .patterns(new String[]{"explosion[-]location", "location"})
            .build()
      );
      registry.register(
         EventValue.builder(GravePreExplodeEvent.class, Entity.class)
            .getter(GravePreExplodeEvent::getSource)
            .patterns(new String[]{"source", "explosion[-]source"})
            .build()
      );
      registry.register(
         EventValue.builder(GravePreExplodeEvent.class, Number.class).getter(GravePreExplodeEvent::getRadius).patterns(new String[]{"radius"}).build()
      );
   }
}
