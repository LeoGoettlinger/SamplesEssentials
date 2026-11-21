package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveProtectionExpiredEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Protection Expired Event")
@Description({"Triggered when a grave's protection expires. Provides access to the grave and location."})
@Examples({"on grave protection expired:", "\tbroadcast \"Grave %event-grave% protection expired at location %event-location%\""})
public class EvtGraveProtectionExpired extends SkriptEvent {
   private Literal<Grave> grave;
   private Literal<Location> location;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveProtectionExpiredEvent event) {
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
      return "Grave protection expired event "
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveProtectionExpired.class, "Grave Protection Expired")
                  .addEvent(GraveProtectionExpiredEvent.class)
                  .addPatterns(new String[]{"[grave] protec(t|ting|ted|tion) expir(e|ing|ed)"}))
               .addDescription("Triggered when a grave's protection expires. Provides access to the grave and location.")
               .addExamples(new String[]{"on grave protection expired:", "\tbroadcast \"Grave %event-grave% protection expired at location %event-location%\""})
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveProtectionExpiredEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveProtectionExpiredEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
   }
}
