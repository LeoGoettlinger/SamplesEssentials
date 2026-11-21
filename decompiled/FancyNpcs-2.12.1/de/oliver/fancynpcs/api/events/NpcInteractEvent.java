package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NpcInteractEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @Nullable
   private final List<NpcAction.NpcActionData> actions;
   @NotNull
   private final Consumer<Player> onClick;
   @NotNull
   private final Player player;
   private final ActionTrigger actionTrigger;
   private boolean isCancelled;

   public NpcInteractEvent(
      @NotNull Npc npc,
      @NotNull Consumer<Player> onClick,
      @NotNull List<NpcAction.NpcActionData> actions,
      @NotNull Player player,
      @NotNull ActionTrigger actionTrigger
   ) {
      this.npc = npc;
      this.onClick = onClick;
      this.actions = actions;
      this.player = player;
      this.actionTrigger = actionTrigger;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public Npc getNpc() {
      return this.npc;
   }

   @NotNull
   public Consumer<Player> getOnClick() {
      return this.onClick;
   }

   @Nullable
   public List<NpcAction.NpcActionData> getActions() {
      return this.actions;
   }

   @NotNull
   public ActionTrigger getInteractionType() {
      return this.actionTrigger;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlerList;
   }
}
