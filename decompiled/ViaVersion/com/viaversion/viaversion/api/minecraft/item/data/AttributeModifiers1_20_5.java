package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AttributeModifiers1_20_5(AttributeModifiers1_20_5.AttributeModifier[] modifiers, boolean showInTooltip) implements Copyable, Rewritable {
   public static final Type<AttributeModifiers1_20_5> TYPE = new Type<AttributeModifiers1_20_5>(AttributeModifiers1_20_5.class) {
      public AttributeModifiers1_20_5 read(ByteBuf buffer) {
         AttributeModifiers1_20_5.AttributeModifier[] modifiers = AttributeModifiers1_20_5.AttributeModifier.ARRAY_TYPE.read(buffer);
         boolean showInTooltip = buffer.readBoolean();
         return new AttributeModifiers1_20_5(modifiers, showInTooltip);
      }

      public void write(ByteBuf buffer, AttributeModifiers1_20_5 value) {
         AttributeModifiers1_20_5.AttributeModifier.ARRAY_TYPE.write(buffer, value.modifiers());
         buffer.writeBoolean(value.showInTooltip());
      }
   };

   public AttributeModifiers1_20_5 copy() {
      return new AttributeModifiers1_20_5(Copyable.copy(this.modifiers), this.showInTooltip);
   }

   public AttributeModifiers1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      FullMappings mappings = protocol.getMappingData().getAttributeMappings();
      if (mappings == null) {
         return this;
      } else {
         List<AttributeModifiers1_20_5.AttributeModifier> modifiers = new ArrayList<>(this.modifiers.length);

         for (AttributeModifiers1_20_5.AttributeModifier modifier : this.modifiers) {
            int mappedId = clientbound ? mappings.getNewId(modifier.attribute()) : mappings.inverse().getNewId(modifier.attribute());
            if (mappedId != -1) {
               modifiers.add(new AttributeModifiers1_20_5.AttributeModifier(mappedId, modifier.modifier(), modifier.slotType()));
            }
         }

         return new AttributeModifiers1_20_5(modifiers.toArray(AttributeModifiers1_20_5.AttributeModifier[]::new), this.showInTooltip);
      }
   }

   public record AttributeModifier(int attribute, AttributeModifiers1_20_5.ModifierData modifier, int slotType) {
      public static final Type<AttributeModifiers1_20_5.AttributeModifier> TYPE = new Type<AttributeModifiers1_20_5.AttributeModifier>(
         AttributeModifiers1_20_5.AttributeModifier.class
      ) {
         public AttributeModifiers1_20_5.AttributeModifier read(ByteBuf buffer) {
            int attribute = Types.VAR_INT.readPrimitive(buffer);
            AttributeModifiers1_20_5.ModifierData modifier = AttributeModifiers1_20_5.ModifierData.TYPE.read(buffer);
            int slot = Types.VAR_INT.readPrimitive(buffer);
            return new AttributeModifiers1_20_5.AttributeModifier(attribute, modifier, slot);
         }

         public void write(ByteBuf buffer, AttributeModifiers1_20_5.AttributeModifier value) {
            Types.VAR_INT.writePrimitive(buffer, value.attribute);
            AttributeModifiers1_20_5.ModifierData.TYPE.write(buffer, value.modifier);
            Types.VAR_INT.writePrimitive(buffer, value.slotType);
         }
      };
      public static final Type<AttributeModifiers1_20_5.AttributeModifier[]> ARRAY_TYPE = new ArrayType<>(TYPE);
   }

   public record ModifierData(UUID uuid, String name, double amount, int operation) {
      public static final Type<AttributeModifiers1_20_5.ModifierData> TYPE = new Type<AttributeModifiers1_20_5.ModifierData>(
         AttributeModifiers1_20_5.ModifierData.class
      ) {
         public AttributeModifiers1_20_5.ModifierData read(ByteBuf buffer) {
            UUID uuid = Types.UUID.read(buffer);
            String name = Types.STRING.read(buffer);
            double amount = buffer.readDouble();
            int operation = Types.VAR_INT.readPrimitive(buffer);
            return new AttributeModifiers1_20_5.ModifierData(uuid, name, amount, operation);
         }

         public void write(ByteBuf buffer, AttributeModifiers1_20_5.ModifierData value) {
            Types.UUID.write(buffer, value.uuid);
            Types.STRING.write(buffer, value.name);
            buffer.writeDouble(value.amount);
            Types.VAR_INT.writePrimitive(buffer, value.operation);
         }
      };
   }
}
