package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R4.wrapper;

import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R4.packet.PacketTransformer;
import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketSide;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import com.mojang.authlib.GameProfile;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserCache;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;

public final class ServerWrapper implements com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerWrapper {
   public static final ServerWrapper INSTANCE = new ServerWrapper();
   private static final MinecraftServer server = MinecraftServer.getServer();
   private static final FieldAccessor entityCounterAccessor = FieldAccessor.builder().withClass(Entity.class).withField("c").build();
   private static final AtomicInteger entityCounter = entityCounterAccessor.get(null, AtomicInteger.class);

   @Override
   public int nextEntityId() {
      return entityCounter.incrementAndGet();
   }

   @Override
   public OfflinePlayer getCachedOfflinePlayer(String name) {
      OfflinePlayer result = Bukkit.getPlayerExact(name);
      if (result != null) {
         return result;
      } else {
         UserCache profileCache = server.au();
         if (profileCache == null) {
            return null;
         } else {
            GameProfile profile = profileCache.getProfileIfCached(name);
            return profile == null ? null : ((CraftServer)Bukkit.getServer()).getOfflinePlayer(profile);
         }
      }
   }

   @Override
   public Object transformPacket(FriendlyByteBuf buf) {
      return PacketTransformer.transformClientbound(buf);
   }

   @Override
   public String listPackets(PacketSide side) {
      return PacketTransformer.listPackets(side);
   }

   @Override
   public void update(boolean force) {
   }

   public Server wrapped() {
      return server.server;
   }

   public MinecraftServer asMinecraft() {
      return server;
   }
}
