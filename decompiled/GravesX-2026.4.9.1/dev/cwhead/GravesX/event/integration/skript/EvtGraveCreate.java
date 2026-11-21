package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveCreateEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Create Event")
@Description({"Triggered when a grave is created. Provides access to the entity and grave."})
@Examples({"on grave create:", "\tbroadcast \"Grave %event-grave% created for entity %event-entity%\""})
public class EvtGraveCreate extends SkriptEvent {
   private Literal<Entity> entity;
   private Literal<Grave> grave;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveCreateEvent event) {
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
      return "Grave create event "
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveCreate.class, "Grave Create")
                  .addEvent(GraveCreateEvent.class)
                  .addPatterns(new String[]{"[grave] creat(e|ing|ed)"}))
               .addDescription("Triggered when a grave is created. Provides access to the entity and grave.")
               .addExamples(new String[]{"on grave create:", "\tbroadcast \"Grave %event-grave% created for entity %event-entity%\""})
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveCreateEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build());
      registry.register(EventValue.builder(GraveCreateEvent.class, Grave.class).getter(GraveCreateEvent::getGrave).patterns(new String[]{"grave"}).build());
   }
}
