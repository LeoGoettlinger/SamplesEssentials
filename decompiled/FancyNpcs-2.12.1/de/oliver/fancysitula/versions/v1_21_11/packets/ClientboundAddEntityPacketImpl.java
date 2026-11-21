package de.oliver.fancysitula.versions.v1_21_11.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundAddEntityPacket;
import de.oliver.fancysitula.api.utils.AngelConverter;
import de.oliver.fancysitula.versions.v1_21_11.utils.VanillaPlayerAdapter;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class ClientboundAddEntityPacketImpl extends FS_ClientboundAddEntityPacket {
   public ClientboundAddEntityPacketImpl(
      int entityId,
      UUID entityUUID,
      org.bukkit.entity.EntityType entityType,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      float headYaw,
      int velocityX,
      int velocityY,
      int velocityZ,
      int data
   ) {
      super(entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data);
   }

   @Override
   public Object createPacket() {
      EntityType<?> vanillaType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(CraftNamespacedKey.toMinecraft(this.entityType.getKey()));
      return new ClientboundAddEntityPacket(
         this.entityId,
         this.entityUUID,
         this.x,
         this.y,
         this.z,
         AngelConverter.degreesToVanillaByte(this.pitch),
         AngelConverter.degreesToVanillaByte(this.yaw),
         vanillaType,
         this.data,
         new Vec3(this.velocityX, this.velocityY, this.velocityZ),
         AngelConverter.degreesToVanillaByte(this.headYaw)
      );
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundAddEntityPacket packet = (ClientboundAddEntityPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
