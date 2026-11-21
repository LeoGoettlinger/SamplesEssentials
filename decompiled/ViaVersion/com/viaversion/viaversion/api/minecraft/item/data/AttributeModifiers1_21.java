package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AttributeModifiers1_21(AttributeModifiers1_21.AttributeModifier[] modifiers, boolean showInTooltip) implements Copyable, Rewritable {
   public static final Type<AttributeModifiers1_21> TYPE1_21 = new Type<AttributeModifiers1_21>(AttributeModifiers1_21.class) {
      public AttributeModifiers1_21 read(ByteBuf buffer) {
         AttributeModifiers1_21.AttributeModifier[] modifiers = AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21.read(buffer);
         boolean showInTooltip = buffer.readBoolean();
         return new AttributeModifiers1_21(modifiers, showInTooltip);
      }

      public void write(ByteBuf buffer, AttributeModifiers1_21 value) {
         AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21.write(buffer, value.modifiers());
         buffer.writeBoolean(value.showInTooltip());
      }
   };
   public static final Type<AttributeModifiers1_21> TYPE1_21_5 = new Type<AttributeModifiers1_21>(AttributeModifiers1_21.class) {
      public AttributeModifiers1_21 read(ByteBuf buffer) {
         AttributeModifiers1_21.AttributeModifier[] modifiers = AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21.read(buffer);
         return new AttributeModifiers1_21(modifiers);
      }

      public void write(ByteBuf buffer, AttributeModifiers1_21 value) {
         AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21.write(buffer, value.modifiers());
      }

      public void write(Ops ops, AttributeModifiers1_21 value) {
         ops.write(AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21, value.modifiers);
      }
   };
   public static final Type<AttributeModifiers1_21> TYPE1_21_6 = new Type<AttributeModifiers1_21>(AttributeModifiers1_21.class) {
      public AttributeModifiers1_21 read(ByteBuf buffer) {
         AttributeModifiers1_21.AttributeModifier[] modifiers = AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21_6.read(buffer);
         return new AttributeModifiers1_21(modifiers);
      }

      public void write(ByteBuf buffer, AttributeModifiers1_21 value) {
         AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21_6.write(buffer, value.modifiers());
      }

      public void write(Ops ops, AttributeModifiers1_21 value) {
         ops.write(AttributeModifiers1_21.AttributeModifier.ARRAY_TYPE1_21_6, value.modifiers);
      }
   };

   public AttributeModifiers1_21(AttributeModifiers1_21.AttributeModifier[] modifiers) {
      this(modifiers, true);
   }

   public AttributeModifiers1_21 rewrite(Int2IntFunction rewriteFunction) {
      AttributeModifiers1_21.AttributeModifier[] modifiers = new AttributeModifiers1_21.AttributeModifier[this.modifiers.length];

      for (int i = 0; i < this.modifiers.length; i++) {
         AttributeModifiers1_21.AttributeModifier modifier = this.modifiers[i];
         modifiers[i] = new AttributeModifiers1_21.AttributeModifier(
            rewriteFunction.applyAsInt(modifier.attribute()), modifier.modifier(), modifier.slotType(), modifier.display()
         );
      }

      return new AttributeModifiers1_21(modifiers, this.showInTooltip);
   }

   public AttributeModifiers1_21 copy() {
      return new AttributeModifiers1_21(Copyable.copy(this.modifiers), this.showInTooltip);
   }

   public AttributeModifiers1_21 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      FullMappings mappings = protocol.getMappingData().getAttributeMappings();
      if (mappings == null) {
         return this;
      } else {
         List<AttributeModifiers1_21.AttributeModifier> modifiers = new ArrayList<>(this.modifiers.length);

         for (AttributeModifiers1_21.AttributeModifier modifier : this.modifiers) {
            int mappedId = clientbound ? mappings.getNewId(modifier.attribute()) : mappings.inverse().getNewId(modifier.attribute());
            if (mappedId != -1) {
               modifiers.add(new AttributeModifiers1_21.AttributeModifier(mappedId, modifier.modifier(), modifier.slotType(), modifier.display()));
            }
         }

         return new AttributeModifiers1_21(modifiers.toArray(AttributeModifiers1_21.AttributeModifier[]::new), this.showInTooltip);
      }
   }

   public record AttributeModifier(int attribute, AttributeModifiers1_21.ModifierData modifier, int slotType, AttributeModifiers1_21.Display display) {
      private static final String[] EQUIPMENT_SLOT_GROUPS = new String[]{
         "any", "mainhand", "offhand", "hand", "feet", "legs", "chest", "head", "armor", "body", "saddle"
      };
      private static final String[] OPERATION = new String[]{"add_value", "add_multiplied_base", "add_multiplied_total"};
      public static final Type<AttributeModifiers1_21.AttributeModifier> TYPE1_21 = new Type<AttributeModifiers1_21.AttributeModifier>(
         AttributeModifiers1_21.AttributeModifier.class
      ) {
         public AttributeModifiers1_21.AttributeModifier read(ByteBuf buffer) {
            int attribute = Types.VAR_INT.readPrimitive(buffer);
            AttributeModifiers1_21.ModifierData modifier = AttributeModifiers1_21.ModifierData.TYPE.read(buffer);
            int slot = Types.VAR_INT.readPrimitive(buffer);
            return new AttributeModifiers1_21.AttributeModifier(attribute, modifier, slot);
         }

         public void write(ByteBuf buffer, AttributeModifiers1_21.AttributeModifier value) {
            Types.VAR_INT.writePrimitive(buffer, value.attribute);
            AttributeModifiers1_21.ModifierData.TYPE.write(buffer, value.modifier);
            Types.VAR_INT.writePrimitive(buffer, value.slotType);
         }

         public void write(Ops ops, AttributeModifiers1_21.AttributeModifier value) {
            Key attribute = ops.context().registryAccess().attributeModifier(value.attribute);
            ops.writeMap(
               map -> map.write("type", Types.IDENTIFIER, attribute)
                  .write("id", Types.IDENTIFIER, Key.of(value.modifier.id()))
                  .write("amount", Types.DOUBLE, value.modifier.amount)
                  .write("operation", Types.STRING, AttributeModifiers1_21.AttributeModifier.OPERATION[value.modifier.operation()])
                  .writeOptional("slot", Types.STRING, AttributeModifiers1_21.AttributeModifier.EQUIPMENT_SLOT_GROUPS[value.slotType()], "any")
            );
         }
      };
      public static final Type<AttributeModifiers1_21.AttributeModifier[]> ARRAY_TYPE1_21 = new ArrayType<>(TYPE1_21);
      public static final Type<AttributeModifiers1_21.AttributeModifier> TYPE1_21_6 = new Type<AttributeModifiers1_21.AttributeModifier>(
         AttributeModifiers1_21.AttributeModifier.class
      ) {
         public AttributeModifiers1_21.AttributeModifier read(ByteBuf buffer) {
            int attribute = Types.VAR_INT.readPrimitive(buffer);
            AttributeModifiers1_21.ModifierData modifier = AttributeModifiers1_21.ModifierData.TYPE.read(buffer);
            int slot = Types.VAR_INT.readPrimitive(buffer);
            int displayType = Types.VAR_INT.readPrimitive(buffer);
            AttributeModifiers1_21.Display display = (AttributeModifiers1_21.Display)(displayType == 2
               ? new AttributeModifiers1_21.OverrideText(Types.TAG.read(buffer))
               : new AttributeModifiers1_21.Display(displayType));
            return new AttributeModifiers1_21.AttributeModifier(attribute, modifier, slot, display);
         }

         public void write(ByteBuf buffer, AttributeModifiers1_21.AttributeModifier value) {
            Types.VAR_INT.writePrimitive(buffer, value.attribute);
            AttributeModifiers1_21.ModifierData.TYPE.write(buffer, value.modifier);
            Types.VAR_INT.writePrimitive(buffer, value.slotType);
            value.display.write(buffer);
         }

         public void write(Ops ops, AttributeModifiers1_21.AttributeModifier value) {
            Key attribute = ops.context().registryAccess().attributeModifier(value.attribute);
            ops.writeMap(
               map -> {
                  map.write("type", Types.IDENTIFIER, attribute)
                     .write("id", Types.IDENTIFIER, Key.of(value.modifier.id()))
                     .write("amount", Types.DOUBLE, value.modifier.amount)
                     .write("operation", Types.STRING, AttributeModifiers1_21.AttributeModifier.OPERATION[value.modifier.operation()])
                     .writeOptional("slot", Types.STRING, AttributeModifiers1_21.AttributeModifier.EQUIPMENT_SLOT_GROUPS[value.slotType()], "any");
                  if (!value.display.equals(AttributeModifiers1_21.Display.DEFAULT)) {
                     map.writeMap("display", display -> {
                        display.write("type", Types.STRING, AttributeModifiers1_21.Display.DISPLAY_TYPES[value.display.id()]);
                        if (value.display instanceof AttributeModifiers1_21.OverrideText overrideText) {
                           display.write("value", Types.TEXT_COMPONENT_TAG, overrideText.component);
                        }
                     });
                  }
               }
            );
         }
      };
      public static final Type<AttributeModifiers1_21.AttributeModifier[]> ARRAY_TYPE1_21_6 = new ArrayType<>(TYPE1_21_6);

      public AttributeModifier(int attribute, AttributeModifiers1_21.ModifierData modifier, int slotType) {
         this(attribute, modifier, slotType, AttributeModifiers1_21.Display.DEFAULT);
      }
   }

   public static class Display implements Copyable {
      public static final String[] DISPLAY_TYPES = new String[]{"default", "hidden", "override"};
      public static final AttributeModifiers1_21.Display DEFAULT = new AttributeModifiers1_21.Display(0);
      private final int id;

      public Display(int id) {
         this.id = id;
      }

      public int id() {
         return this.id;
      }

      public void write(ByteBuf buf) {
         Types.VAR_INT.writePrimitive(buf, this.id);
      }

      public AttributeModifiers1_21.Display copy() {
         return this;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof AttributeModifiers1_21.Display display ? this.id == display.id : false;
      }

      @Override
      public int hashCode() {
         return this.id;
      }
   }

   public record ModifierData(String id, double amount, int operation) {
      public static final Type<AttributeModifiers1_21.ModifierData> TYPE = new Type<AttributeModifiers1_21.ModifierData>(
         AttributeModifiers1_21.ModifierData.class
      ) {
         public AttributeModifiers1_21.ModifierData read(ByteBuf buffer) {
            String id = Types.STRING.read(buffer);
            double amount = buffer.readDouble();
            int operation = Types.VAR_INT.readPrimitive(buffer);
            return new AttributeModifiers1_21.ModifierData(id, amount, operation);
         }

         public void write(ByteBuf buffer, AttributeModifiers1_21.ModifierData value) {
            Types.STRING.write(buffer, value.id);
            buffer.writeDouble(value.amount);
            Types.VAR_INT.writePrimitive(buffer, value.operation);
         }
      };
   }

   public static final class OverrideText extends AttributeModifiers1_21.Display {
      public static final int ID = 2;
      private final Tag component;

      public OverrideText(Tag component) {
         super(2);
         this.component = component;
      }

      public Tag component() {
         return this.component;
      }

      @Override
      public void write(ByteBuf buf) {
         super.write(buf);
         Types.TAG.write(buf, this.component);
      }

      public AttributeModifiers1_21.OverrideText copy() {
         return new AttributeModifiers1_21.OverrideText(this.component.copy());
      }

      @Override
      public boolean equals(Object o) {
         if (o instanceof AttributeModifiers1_21.OverrideText that) {
            return !super.equals(o) ? false : Objects.equals(this.component, that.component);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = super.hashCode();
         return 31 * result + Objects.hashCode(this.component);
      }
   }
}
