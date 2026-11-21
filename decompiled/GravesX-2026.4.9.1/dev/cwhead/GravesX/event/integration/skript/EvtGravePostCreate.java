package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GravePostCreateEvent;
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

@Name("Grave Post Create Event")
@Description({"Triggered after a grave create attempt completes (after placement). Provides access to the entity, grave, and placed location (nullable)."})
@Examples({"on grave post create:", "\tbroadcast \"Grave %event-grave% post-created for entity %event-entity%\"", "\tif event-placed location is set:", "\t\tbroadcast \"Placed at %event-placed location%\"", "\telse:", "\t\tbroadcast \"Grave was not placed\""})
public class EvtGravePostCreate extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GravePostCreateEvent event) {
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

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave post create event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGravePostCreate.class, "Grave Post Create")
                  .addEvent(GravePostCreateEvent.class)
                  .addPatterns(new String[]{"[grave] post creat(e|ing|ed)"}))
               .addDescription(
                  "Triggered after a grave create attempt completes (after placement). Provides access to the entity, grave, and placed location (nullable)."
               )
               .addExamples(
                  new String[]{
                     "on grave post create:",
                     "\tbroadcast \"Grave %event-grave% post-created for entity %event-entity%\"",
                     "\tif event-placed location is set:",
                     "\t\tbroadcast \"Placed at %event-placed location%\"",
                     "\telse:",
                     "\t\tbroadcast \"Grave was not placed\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GravePostCreateEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build()
      );
      registry.register(EventValue.builder(GravePostCreateEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GravePostCreateEvent.class, Location.class)
            .getter(GravePostCreateEvent::getPlacedLocation)
            .patterns(new String[]{"placed[-]location"})
            .build()
      );
   }
}
