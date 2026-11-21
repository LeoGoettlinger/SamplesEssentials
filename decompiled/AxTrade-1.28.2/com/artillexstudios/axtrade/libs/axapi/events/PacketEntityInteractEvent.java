package com.artillexstudios.axtrade.libs.axapi.events;

import com.artillexstudios.axtrade.libs.axapi.packet.wrapper.serverbound.ServerboundInteractWrapper;
import com.artillexstudios.axtrade.libs.axapi.packetentity.PacketEntity;
import com.artillexstudios.axtrade.libs.axapi.utils.Vector3d;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketEntityInteractEvent extends Event {
   private static final HandlerList HANDLER_LIST = new HandlerList();
   private final PacketEntity packetEntity;
   private final boolean attack;
   private final Vector3d position;
   private final ServerboundInteractWrapper.InteractionHand hand;
   private final Player player;

   public PacketEntityInteractEvent(
      @NotNull Player player, PacketEntity packetEntity, boolean attack, Vector3d position, ServerboundInteractWrapper.InteractionHand hand
   ) {
      super(!Bukkit.isPrimaryThread());
      this.player = player;
      this.packetEntity = packetEntity;
      this.attack = attack;
      this.position = position;
      this.hand = hand;
   }

   public static HandlerList getHandlerList() {
      return HANDLER_LIST;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLER_LIST;
   }

   public PacketEntity getPacketEntity() {
      return this.packetEntity;
   }

   public boolean isAttack() {
      return this.attack;
   }

   public Vector3d getPosition() {
      return this.position;
   }

   public ServerboundInteractWrapper.InteractionHand getHand() {
      return this.hand;
   }

   public Player getPlayer() {
      return this.player;
   }
}
