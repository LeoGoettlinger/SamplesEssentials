package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.packet;

import com.artillexstudios.axafkzone.libs.axapi.items.HashedStack;
import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3d;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;

public record FriendlyByteBufWrapper(RegistryFriendlyByteBuf buf) implements FriendlyByteBuf {
   public com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack readItemStack(WrappedItemStack.CodecData codecData) {
      return switch (codecData) {
         case OPTIONAL_UNTRUSTED_STREAM_CODEC -> new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack(
            (ItemStack)ItemStack.i.decode(this.buf)
         );
         case OPTIONAL_STREAM_CODEC -> new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack((ItemStack)ItemStack.h.decode(this.buf));
         case STREAM_CODEC -> new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack((ItemStack)ItemStack.j.decode(this.buf));
      };
   }

   @Override
   public void writeItemStack(WrappedItemStack wrappedItemStack, WrappedItemStack.CodecData codecData) {
      switch (codecData) {
         case OPTIONAL_UNTRUSTED_STREAM_CODEC:
            ItemStack.i.encode(this.buf, ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack)wrappedItemStack).asMinecraft());
            break;
         case OPTIONAL_STREAM_CODEC:
            ItemStack.h.encode(this.buf, ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack)wrappedItemStack).asMinecraft());
            break;
         case STREAM_CODEC:
            ItemStack.j.encode(this.buf, ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack)wrappedItemStack).asMinecraft());
      }
   }

   @Override
   public byte[] readByteArray() {
      return this.buf.b();
   }

   @Override
   public byte[] readByteArray(int maxSize) {
      return this.buf.a(maxSize);
   }

   @Override
   public void writeByteArray(byte[] bytes) {
      this.buf.a(bytes);
   }

   @Override
   public Component readComponent() {
      return ComponentSerializer.INSTANCE.fromVanilla(ComponentSerialization.d.decode(this.buf));
   }

   @Override
   public void writeComponent(Component component) {
      ComponentSerialization.d.encode(this.buf, (IChatBaseComponent)ComponentSerializer.INSTANCE.toVanilla(component));
   }

   @Override
   public int readVarInt() {
      return this.buf.l();
   }

   @Override
   public void writeVarInt(int varInt) {
      this.buf.c(varInt);
   }

   @Override
   public CompoundTag readNBT() {
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.nbt.CompoundTag(this.buf.o());
   }

   @Override
   public void writeNBT(CompoundTag tag) {
      this.buf.a((NBTTagCompound)tag.getParent());
   }

   @Override
   public String readUTF() {
      return this.buf.p();
   }

   @Override
   public String readUTF(int length) {
      return this.buf.d(length);
   }

   @Override
   public void writeUTF(String utf) {
      this.buf.a(utf);
   }

   @Override
   public Key readResourceLocation() {
      MinecraftKey resourceLocation = this.buf.q();
      return Key.key(resourceLocation.b(), resourceLocation.a());
   }

   @Override
   public void writeResourceLocation(Key key) {
      this.buf.a(MinecraftKey.b(key.namespace(), key.value()));
   }

   @Override
   public void writeFloat(float value) {
      this.buf.a(value);
   }

   @Override
   public float readFloat() {
      return this.buf.readFloat();
   }

   @Override
   public void writeShort(int value) {
      this.buf.m(value);
   }

   @Override
   public short readShort() {
      return this.buf.readShort();
   }

   @Override
   public void writeDouble(double value) {
      this.buf.a(value);
   }

   @Override
   public double readDouble() {
      return this.buf.readDouble();
   }

   @Override
   public void writeBoolean(boolean value) {
      this.buf.a(value);
   }

   @Override
   public boolean readBoolean() {
      return this.buf.readBoolean();
   }

   @Override
   public void writeInt(int value) {
      this.buf.q(value);
   }

   @Override
   public int readInt() {
      return this.buf.readInt();
   }

   @Override
   public void writeVarLong(long value) {
      this.buf.a(value);
   }

   @Override
   public long readVarLong() {
      return this.buf.m();
   }

   @Override
   public void writeLong(long value) {
      this.buf.b(value);
   }

   @Override
   public long readLong() {
      return this.buf.readLong();
   }

   @Override
   public void writeByte(int value) {
      this.buf.l(value);
   }

   @Override
   public byte readByte() {
      return this.buf.readByte();
   }

   @Override
   public short readUnsignedByte() {
      return this.buf.readUnsignedByte();
   }

   @Override
   public void readerIndex(int index) {
      this.buf.h(index);
   }

   @Override
   public int readerIndex() {
      return this.buf.readerIndex();
   }

   @Override
   public void writerIndex(int index) {
      this.buf.i(index);
   }

   @Override
   public int writerIndex() {
      return this.buf.writerIndex();
   }

   public com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack readItemCost() {
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack(((ItemCost)ItemCost.b.decode(this.buf)).d());
   }

   @Override
   public void writeItemCost(WrappedItemStack itemCost) {
      ItemStack nmsItem = ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack)itemCost).asMinecraft();
      ItemCost.b
         .encode(this.buf, new ItemCost(nmsItem.i(), nmsItem.M(), DataComponentExactPredicate.a(PatchedDataComponentMap.a(DataComponentMap.a, nmsItem.d()))));
   }

   @Override
   public Optional<WrappedItemStack> readOptionalItemCost() {
      return ((Optional)ItemCost.c.decode(this.buf)).map(cost -> new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack(cost.d()));
   }

   @Override
   public void writeOptionalItemCost(Optional<WrappedItemStack> itemCost) {
      ItemCost.c.encode(this.buf, itemCost.map(stack -> {
         ItemStack nmsItem = ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.WrappedItemStack)stack).itemStack;
         return new ItemCost(nmsItem.i(), nmsItem.M(), DataComponentExactPredicate.a(PatchedDataComponentMap.a(DataComponentMap.a, nmsItem.d())));
      }));
   }

   @Override
   public int readUnsignedShort() {
      return this.buf.readUnsignedShort();
   }

   @Override
   public void writeBlockData(BlockData blockData) {
      this.buf.c(Block.j(((CraftBlockData)blockData).getState()));
   }

   @Override
   public BlockData readBlockData() {
      return CraftBlockData.fromData((IBlockData)Block.k.a(this.readVarInt()));
   }

   @Override
   public Vector3d readLpVec3() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void writeLpVec3(Vector3d vector) {
      throw new UnsupportedOperationException();
   }

   @Override
   public HashedStack readHashedStack() {
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.HashedStack(
         (net.minecraft.network.HashedStack)net.minecraft.network.HashedStack.b.decode(this.buf)
      );
   }

   @Override
   public void writeHashedStack(HashedStack stack) {
      net.minecraft.network.HashedStack.b.encode(this.buf, ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R4.items.HashedStack)stack).stack());
   }

   @Override
   public void writeBytes(FriendlyByteBuf buf) {
      this.buf.l(((FriendlyByteBufWrapper)buf).buf());
   }

   @Override
   public FriendlyByteBuf readBytes(int length) {
      return PacketTransformer.wrap(this.buf.readBytes(length));
   }

   @Override
   public FriendlyByteBuf copy() {
      return PacketTransformer.copy(this);
   }

   @Override
   public FriendlyByteBuf slice(int beginIndex, int length) {
      return PacketTransformer.wrap(this.buf.slice(beginIndex, length));
   }

   @Override
   public int readableBytes() {
      return this.buf.readableBytes();
   }

   @Override
   public void release() {
      this.buf.release();
   }
}
