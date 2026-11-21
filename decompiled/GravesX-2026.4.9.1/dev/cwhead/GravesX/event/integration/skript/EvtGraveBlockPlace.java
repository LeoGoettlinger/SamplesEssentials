package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveBlockPlaceEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Block Place Event")
@Description({"Triggered when a block is placed for a grave. Provides access to the grave, block type, and location."})
@Examples({"on grave block place:", "\tbroadcast \"Block type %event-block-type% was placed for grave %event-grave% at location %event-location% by entity %event-entity%\""})
public class EvtGraveBlockPlace extends SkriptEvent {
   private Literal<Grave> grave;
   private Literal<Location> location;
   private Literal<BlockData.BlockType> blockType;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveBlockPlaceEvent event) {
         if (this.grave != null && !this.grave.check(event, graveValue -> graveValue.equals(event.getGrave()))) {
            return false;
         } else {
            return this.location != null && !this.location.check(event, locationValue -> locationValue.equals(event.getLocation()))
               ? false
               : this.blockType == null || this.blockType.check(event, blockTypeValue -> blockTypeValue.equals(event.getBlockType()));
         }
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave block place event "
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "")
         + (this.blockType != null ? " with block type " + this.blockType.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveBlockPlace.class, "Grave Block Place")
                  .addEvent(GraveBlockPlaceEvent.class)
                  .addPatterns(new String[]{"[grave] bloc(k|ks) plac(e|ing|ed)"}))
               .addDescription("Triggered when a block is placed for a grave. Provides access to the grave, block type, and location.")
               .addExamples(
                  new String[]{
                     "on grave block place:",
                     "\tbroadcast \"Block type %event-block-type% was placed for grave %event-grave% at location %event-location% by entity %event-entity%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveBlockPlaceEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveBlockPlaceEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
      registry.register(
         EventValue.builder(GraveBlockPlaceEvent.class, BlockData.BlockType.class)
            .getter(GraveEvent::getBlockType)
            .patterns(new String[]{"block[-]type"})
            .build()
      );
   }
}
