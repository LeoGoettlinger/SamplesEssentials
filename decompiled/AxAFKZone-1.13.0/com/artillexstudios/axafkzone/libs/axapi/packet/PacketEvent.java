package com.artillexstudios.axafkzone.libs.axapi.packet;

import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import java.util.function.Supplier;
import org.bukkit.entity.Player;

public final class PacketEvent {
   private final Player player;
   private final PacketType type;
   private final PacketSide side;
   private final Supplier<FriendlyByteBuf> in;
   private FriendlyByteBuf cachedIn;
   private FriendlyByteBuf directOut;
   private PacketWrapper wrapper;
   private boolean cancelled = false;

   public PacketEvent(Player player, PacketSide side, PacketType type, Supplier<FriendlyByteBuf> in) {
      this.player = player;
      this.side = side;
      this.type = type;
      this.in = in;
   }

   public Player player() {
      return this.player;
   }

   public PacketType type() {
      return this.type;
   }

   public PacketSide side() {
      return this.side;
   }

   public FriendlyByteBuf in() {
      FriendlyByteBuf byteBuf = this.cachedIn;
      if (byteBuf == null) {
         this.cachedIn = byteBuf = this.in.get();
      }

      return byteBuf == null ? null : byteBuf.slice(byteBuf.readerIndex(), byteBuf.readableBytes());
   }

   public FriendlyByteBuf out() {
      FriendlyByteBuf buf = FriendlyByteBuf.alloc();
      buf.writeVarInt(this.side == PacketSide.CLIENT_BOUND ? ClientboundPacketTypes.forPacketType(this.type) : ServerboundPacketTypes.forPacketType(this.type));
      this.directOut = buf;
      return buf;
   }

   public void setWrapper(PacketWrapper wrapper) {
      this.wrapper = wrapper;
   }

   public PacketWrapper wrapper() {
      return this.wrapper;
   }

   public FriendlyByteBuf directOut() {
      return this.directOut;
   }

   public FriendlyByteBuf directIn() {
      return this.cachedIn;
   }

   public void cancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public boolean cancelled() {
      return this.cancelled;
   }
}
