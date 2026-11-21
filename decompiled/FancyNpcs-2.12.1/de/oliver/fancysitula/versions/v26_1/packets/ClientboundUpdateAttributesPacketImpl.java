package de.oliver.fancysitula.versions.v26_1.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundUpdateAttributesPacket;
import de.oliver.fancysitula.versions.v26_1.utils.VanillaPlayerAdapter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class ClientboundUpdateAttributesPacketImpl extends FS_ClientboundUpdateAttributesPacket {
   public ClientboundUpdateAttributesPacketImpl(int entityId, List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes) {
      super(entityId, attributes);
   }

   @Override
   public Object createPacket() {
      List<AttributeInstance> instances = new ArrayList<>();

      for (FS_ClientboundUpdateAttributesPacket.AttributeSnapshot snapshot : this.attributes) {
         Reference<Attribute> attributeRef = (Reference<Attribute>)BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(snapshot.attributeId())).orElse(null);
         if (attributeRef != null) {
            AttributeInstance instance = new AttributeInstance(attributeRef, a -> {});
            instance.setBaseValue(snapshot.baseValue());
            instances.add(instance);
         }
      }

      return new ClientboundUpdateAttributesPacket(this.entityId, instances);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundUpdateAttributesPacket packet = (ClientboundUpdateAttributesPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
