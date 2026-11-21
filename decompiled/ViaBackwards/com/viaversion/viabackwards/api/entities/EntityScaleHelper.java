package com.viaversion.viabackwards.api.entities;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;

public class EntityScaleHelper {
   private final ClientboundPacketType updateAttributesPacket;
   private final EntityRewriter<?, ?> rewriter;

   public <CM extends ClientboundPacketType, P extends Protocol<?, CM, ?, ?>> EntityScaleHelper(EntityRewriter<?, P> rewriter, CM updateAttributesPacket) {
      this.rewriter = rewriter;
      this.updateAttributesPacket = updateAttributesPacket;
   }

   public void addBabyScale(EntityType type, float babyScaleFactor, int babyIndex) {
      this.rewriter.filter().type(type).index(babyIndex).handler((event, data) -> this.trackAndSend(event, data, babyScaleFactor));
   }

   public void trackAndSend(EntityDataHandlerEvent event, EntityData data, float babyScaleFactor) {
      if (event.trackedEntity() != null) {
         EntityScaleData scaleData = (EntityScaleData)event.trackedEntity().get(EntityScaleData.class);
         if (scaleData == null) {
            scaleData = new EntityScaleData();
            event.trackedEntity().put(scaleData);
         }

         boolean isBaby = (Boolean)data.value();
         if (scaleData.isBaby() != isBaby) {
            scaleData.setBaby(isBaby);
            scaleData.setScale(isBaby ? babyScaleFactor : 1.0F);
            FullMappings attributeMappings = this.rewriter.protocol().getMappingData().getAttributeMappings();
            int attributeId = attributeMappings.mappedId("scale");
            if (attributeId == -1) {
               attributeId = attributeMappings.mappedId("generic.scale");
               Preconditions.checkArgument(attributeId != -1);
            }

            PacketWrapper attributesPacket = PacketWrapper.create(this.updateAttributesPacket, event.user());
            attributesPacket.write(Types.VAR_INT, event.entityId());
            attributesPacket.write(Types.VAR_INT, 1);
            attributesPacket.write(Types.VAR_INT, attributeId);
            attributesPacket.write(Types.DOUBLE, (double)scaleData.scale());
            attributesPacket.write(Types.VAR_INT, 0);
            attributesPacket.send(this.rewriter.protocol().getClass());
         }
      }
   }
}
