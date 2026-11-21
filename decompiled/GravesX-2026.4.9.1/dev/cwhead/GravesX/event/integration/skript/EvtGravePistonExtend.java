package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GravePistonExtendEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Piston Move Event")
@Description({"Triggered when a player head is dropped at a grave site. Provides access to the grave and location."})
@Examples({"on grave piston extend:", "\tbroadcast \"Grave %event-grave% at location %event-location% extended by %event-piston-block%\""})
public class EvtGravePistonExtend extends SkriptEvent {
   private Literal<Grave> grave;
   private Literal<Location> location;
   private Literal<Block> pistonBlock;
   private Literal<BlockFace> direction;
   private Literal<List<Block>> movedBlocks;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (!(e instanceof GravePistonExtendEvent event)) {
         return false;
      } else {
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

         if (this.pistonBlock != null) {
            this.pistonBlock.check(event, new Predicate<Block>() {
               public boolean test(Block pb) {
                  return pb.equals(event.getPistonBlock());
               }
            });
         }

         if (this.direction != null) {
            this.direction.check(event, new Predicate<BlockFace>() {
               public boolean test(BlockFace d) {
                  return d.equals(event.getDirection());
               }
            });
         }

         if (this.movedBlocks != null) {
            this.movedBlocks.check(event, new Predicate<List<Block>>() {
               public boolean test(List<Block> mb) {
                  return mb.equals(event.getMovedBlocks());
               }
            });
         }

         return true;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave piston extend"
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "")
         + (this.pistonBlock != null ? " with piston block " + this.pistonBlock.toString(e, debug) : "")
         + (this.direction != null ? " with direction " + this.direction.toString(e, debug) : "")
         + (this.movedBlocks != null ? " with moved blocks " + this.movedBlocks.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGravePistonExtend.class, "Grave Piston Move")
                  .addEvent(GravePistonExtendEvent.class)
                  .addPatterns(new String[]{"[grave] pisto(n|ns) ex(te|pa)n(d|ded|ding)"}))
               .addDescription("Triggered when a player head is dropped at a grave site. Provides access to the grave and location.")
               .addExamples(
                  new String[]{"on grave piston extend:", "\tbroadcast \"Grave %event-grave% at location %event-location% extended by %event-piston-block%\""}
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GravePistonExtendEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GravePistonExtendEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
      registry.register(
         EventValue.builder(GravePistonExtendEvent.class, Block.class)
            .getter(GravePistonExtendEvent::getPistonBlock)
            .patterns(new String[]{"piston[-]block"})
            .build()
      );
      registry.register(
         EventValue.builder(GravePistonExtendEvent.class, BlockFace.class)
            .getter(GravePistonExtendEvent::getDirection)
            .patterns(new String[]{"direction"})
            .build()
      );
      registry.register(
         EventValue.builder(GravePistonExtendEvent.class, List.class)
            .getter(GravePistonExtendEvent::getMovedBlocks)
            .patterns(new String[]{"moved[-]blocks"})
            .build()
      );
   }
}
