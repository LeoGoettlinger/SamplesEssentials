package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.EvtClick;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;

@Name("Cancel Event")
@Description({"Cancels the event (e.g. prevent blocks from being placed, or damage being taken)."})
@Example("on damage:\n\tvictim is a player\n\tvictim has the permission \"skript.god\"\n\tcancel the event\n")
@Since({"1.0"})
public class EffCancelEvent extends Effect {
   private boolean cancel;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (isDelayed == Kleenean.TRUE) {
         Skript.error("An event cannot be cancelled after it has already passed", ErrorQuality.SEMANTIC_ERROR);
         return false;
      } else {
         this.cancel = matchedPattern == 0;
         Class<? extends Event>[] currentEvents = this.getParser().getCurrentEvents();
         if (currentEvents == null) {
            return false;
         } else if (this.cancel && this.getParser().isCurrentEvent(EntityToggleSwimEvent.class)) {
            Skript.error("Cancelling a toggle swim event has no effect");
            return false;
         } else {
            for (Class<? extends Event> event : currentEvents) {
               if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event)) {
                  return true;
               }
            }

            if (this.getParser().isCurrentEvent(PlayerLoginEvent.class)) {
               Skript.error(
                  "A connect event cannot be cancelled, but the player may be kicked ('kick player by reason of \"...\"')", ErrorQuality.SEMANTIC_ERROR
               );
            } else {
               Skript.error(Utils.A(this.getParser().getCurrentEventName()) + " event cannot be cancelled", ErrorQuality.SEMANTIC_ERROR);
            }

            return false;
         }
      }
   }

   @Override
   public void execute(Event event) {
      if (event instanceof Cancellable cancellable) {
         cancellable.setCancelled(this.cancel);
      }

      if (event instanceof PlayerInteractEvent playerInteractEvent) {
         EvtClick.interactTracker.eventModified((Cancellable)event);
         playerInteractEvent.setUseItemInHand(this.cancel ? Result.DENY : Result.DEFAULT);
         playerInteractEvent.setUseInteractedBlock(this.cancel ? Result.DENY : Result.DEFAULT);
      } else if (event instanceof PlayerInteractEntityEvent) {
         EvtClick.interactTracker.eventModified((Cancellable)event);
      } else if (event instanceof BlockCanBuildEvent blockCanBuildEvent) {
         blockCanBuildEvent.setBuildable(!this.cancel);
      } else if (event instanceof PlayerDropItemEvent playerDropItemEvent) {
         PlayerUtils.updateInventory(playerDropItemEvent.getPlayer());
      } else if (event instanceof InventoryInteractEvent interactEvent) {
         PlayerUtils.updateInventory((Player)interactEvent.getWhoClicked());
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.cancel ? "" : "un") + "cancel event";
   }

   static {
      Skript.registerEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
   }
}
