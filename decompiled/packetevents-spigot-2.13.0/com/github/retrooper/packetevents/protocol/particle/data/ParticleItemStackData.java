package com.github.retrooper.packetevents.protocol.particle.data;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.ItemStackSerialization;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ParticleItemStackData extends ParticleData implements LegacyConvertible {
   private ItemStack itemStack;

   public ParticleItemStackData(ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   public static ParticleItemStackData read(PacketWrapper<?> wrapper) {
      return wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)
         ? new ParticleItemStackData(ItemStackSerialization.readTemplate(wrapper))
         : new ParticleItemStackData(
            ItemStack.builder().type(ItemTypes.getRegistry().getByIdOrThrow(wrapper.getClientVersion(), wrapper.readVarInt())).wrapper(wrapper).build()
         );
   }

   public static void write(PacketWrapper<?> wrapper, ParticleItemStackData data) {
      ItemStackSerialization.writeTemplate(wrapper, data.getItemStack());
   }

   public static ParticleItemStackData decode(NBTCompound compound, ClientVersion version) {
      String key = version.isNewerThanOrEquals(ClientVersion.V_1_20_5) ? "item" : "value";
      ItemStack stack = ItemStack.decode(compound.getTagOrThrow(key), version);
      return new ParticleItemStackData(stack);
   }

   public static void encode(ParticleItemStackData data, ClientVersion version, NBTCompound compound) {
      String key = version.isNewerThanOrEquals(ClientVersion.V_1_20_5) ? "item" : "value";
      compound.setTag(key, ItemStack.encodeForParticle(data.itemStack, version));
   }

   @Override
   public boolean isEmpty() {
      return false;
   }

   @Override
   public LegacyParticleData toLegacy(ClientVersion version) {
      return LegacyParticleData.ofTwo(this.itemStack.getType().getId(version), this.itemStack.getLegacyData());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         ParticleItemStackData that = (ParticleItemStackData)obj;
         return this.itemStack.equals(that.itemStack);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.itemStack);
   }
}
