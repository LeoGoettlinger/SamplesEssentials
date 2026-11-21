package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageType;
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemDamageResistant {
   private MappedEntitySet<DamageType> types;

   public ItemDamageResistant(ResourceLocation types) {
      this(new MappedEntitySet<>(types));
   }

   public ItemDamageResistant(MappedEntitySet<DamageType> types) {
      this.types = types;
   }

   public static ItemDamageResistant read(PacketWrapper<?> wrapper) {
      return wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)
         ? new ItemDamageResistant(MappedEntitySet.read(wrapper, DamageTypes.getRegistry()))
         : new ItemDamageResistant(wrapper.readIdentifier());
   }

   public static void write(PacketWrapper<?> wrapper, ItemDamageResistant resistant) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         MappedEntitySet.write(wrapper, resistant.types);
      } else {
         wrapper.writeIdentifier(resistant.getTypesTagKey());
      }
   }

   public MappedEntitySet<DamageType> getTypes() {
      return this.types;
   }

   public void setTypes(MappedEntitySet<DamageType> types) {
      this.types = types;
   }

   public ResourceLocation getTypesTagKey() {
      ResourceLocation tagKey = this.types.getTagKey();
      if (tagKey == null) {
         throw new IllegalStateException("No tag key present");
      } else {
         return tagKey;
      }
   }

   public void setTypesTagKey(ResourceLocation types) {
      this.types = new MappedEntitySet<>(types);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemDamageResistant)) {
         return false;
      } else {
         ItemDamageResistant that = (ItemDamageResistant)obj;
         return this.types.equals(that.types);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.types);
   }

   @Override
   public String toString() {
      return "ItemDamageResistant{types=" + this.types + '}';
   }
}
