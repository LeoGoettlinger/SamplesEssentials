package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NpcModifyEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @NotNull
   private final NpcModifyEvent.NpcModification modification;
   @NotNull
   private final Object newValue;
   @NotNull
   private final CommandSender modifier;
   private boolean isCancelled;

   public NpcModifyEvent(@NotNull Npc npc, @NotNull NpcModifyEvent.NpcModification modification, Object newValue, @NotNull CommandSender modifier) {
      this.npc = npc;
      this.modification = modification;
      this.newValue = newValue;
      this.modifier = modifier;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public Npc getNpc() {
      return this.npc;
   }

   @NotNull
   public NpcModifyEvent.NpcModification getModification() {
      return this.modification;
   }

   @NotNull
   public Object getNewValue() {
      return this.newValue;
   }

   @NotNull
   public CommandSender getModifier() {
      return this.modifier;
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

   public static enum NpcModification {
      ATTRIBUTE,
      COLLIDABLE,
      DISPLAY_NAME,
      EQUIPMENT,
      GLOWING,
      GLOWING_COLOR,
      INTERACTION_COOLDOWN,
      SCALE,
      VISIBILITY,
      VISIBILITY_DISTANCE,
      LOCATION,
      MIRROR_SKIN,
      PLAYER_COMMAND,
      ROTATION,
      SERVER_COMMAND,
      SHOW_IN_TAB,
      SKIN,
      TURN_TO_PLAYER,
      TURN_TO_PLAYER_DISTANCE,
      TYPE,
      MESSAGE_ADD,
      MESSAGE_SET,
      MESSAGE_REMOVE,
      MESSAGE_CLEAR,
      MESSAGE_SEND_RANDOMLY,
      PLAYER_COMMAND_ADD,
      PLAYER_COMMAND_SET,
      PLAYER_COMMAND_REMOVE,
      PLAYER_COMMAND_CLEAR,
      PLAYER_COMMAND_SEND_RANDOMLY,
      SERVER_COMMAND_ADD,
      SERVER_COMMAND_SET,
      SERVER_COMMAND_REMOVE,
      SERVER_COMMAND_CLEAR,
      SERVER_COMMAND_SEND_RANDOMLY;
   }
}
