package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveObituaryAddEvent;
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

@Name("Grave Obituary Add Event")
@Description({"Triggered when an obituary is to be added to a grave. Provides access to the entity, grave, and location."})
@Examples({"on grave obituary add:", "\tbroadcast \"Obituary added to %event-player%'s grave %event-grave% at location %event-location%\""})
public class EvtGraveObituaryAdd extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;
   private Literal<Location> location;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveObituaryAddEvent event) {
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
      return "Grave obituary add event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveObituaryAdd.class, "Grave Obituary Add")
                  .addEvent(GraveObituaryAddEvent.class)
                  .addPatterns(new String[]{"[grave] obituar(y|ies) ad(d|ded)"}))
               .addDescription("Triggered when an obituary is to be added to a grave. Provides access to the entity, grave, and location.")
               .addExamples(
                  new String[]{"on grave obituary add:", "\tbroadcast \"Obituary added to %event-player%'s grave %event-grave% at location %event-location%\""}
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveObituaryAddEvent.class, Entity.class).getter(GraveObituaryAddEvent::getEntity).patterns(new String[]{"entity"}).build()
      );
      registry.register(EventValue.builder(GraveObituaryAddEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveObituaryAddEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
   }
}
