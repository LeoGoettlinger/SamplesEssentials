package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.packet;

import com.artillexstudios.axafkzone.libs.axapi.items.HashedStack;
import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3d;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.inventory.ItemStack;

public record FriendlyByteBufWrapper(PacketDataSerializer buf) implements FriendlyByteBuf {
   @Override
   public WrappedItemStack readItemStack(WrappedItemStack.CodecData codecData) {
      switch (codecData) {
         case OPTIONAL_UNTRUSTED_STREAM_CODEC:
         case OPTIONAL_STREAM_CODEC:
         case STREAM_CODEC:
            return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack(this.buf.q());
         default:
            throw new MatchException(null, null);
      }
   }

   @Override
   public void writeItemStack(WrappedItemStack wrappedItemStack, WrappedItemStack.CodecData codecData) {
      switch (codecData) {
         case OPTIONAL_UNTRUSTED_STREAM_CODEC:
         case OPTIONAL_STREAM_CODEC:
         case STREAM_CODEC:
            this.buf.a(((com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack)wrappedItemStack).asMinecraft());
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
      return ComponentSerializer.INSTANCE.fromVanilla(this.buf.l());
   }

   @Override
   public void writeComponent(Component component) {
      this.buf.a(ComponentSerializer.INSTANCE.toVanilla(component));
   }

   @Override
   public int readVarInt() {
      return this.buf.m();
   }

   @Override
   public void writeVarInt(int varInt) {
      this.buf.c(varInt);
   }

   @Override
   public CompoundTag readNBT() {
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.nbt.CompoundTag(this.buf.p());
   }

   @Override
   public void writeNBT(CompoundTag tag) {
      this.buf.a((NBTTagCompound)tag.getParent());
   }

   @Override
   public String readUTF() {
      return this.buf.r();
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
      MinecraftKey resourceLocation = this.buf.s();
      return Key.key(resourceLocation.b(), resourceLocation.a());
   }

   @Override
   public void writeResourceLocation(Key key) {
      this.buf.a(MinecraftKey.a(key.namespace(), key.value()));
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
      this.buf.l(value);
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
      this.buf.p(value);
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
      return this.buf.n();
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
      this.buf.k(value);
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
      this.buf.g(index);
   }

   @Override
   public int readerIndex() {
      return this.buf.readerIndex();
   }

   @Override
   public void writerIndex(int index) {
      this.buf.h(index);
   }

   @Override
   public int writerIndex() {
      return this.buf.writerIndex();
   }

   @Override
   public WrappedItemStack readItemCost() {
      return new com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack(this.buf.q());
   }

   @Override
   public void writeItemCost(WrappedItemStack itemCost) {
      this.buf.a(((com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack)itemCost).itemStack);
   }

   @Override
   public Optional<WrappedItemStack> readOptionalItemCost() {
      return Optional.of(this.buf.q()).map(com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack::new);
   }

   @Override
   public void writeOptionalItemCost(Optional<WrappedItemStack> itemCost) {
      WrappedItemStack item = itemCost.orElse(WrappedItemStack.wrap(new ItemStack(Material.AIR)));
      this.buf.a(((com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R2.items.WrappedItemStack)item).itemStack);
   }

   @Override
   public int readUnsignedShort() {
      return this.buf.readUnsignedShort();
   }

   @Override
   public void writeBlockData(BlockData blockData) {
      this.buf.c(Block.i(((CraftBlockData)blockData).getState()));
   }

   @Override
   public BlockData readBlockData() {
      return CraftBlockData.fromData((IBlockData)Block.o.a(this.readVarInt()));
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
      throw new UnsupportedOperationException();
   }

   @Override
   public void writeHashedStack(HashedStack stack) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void writeBytes(FriendlyByteBuf buf) {
      this.buf.b(((FriendlyByteBufWrapper)buf).buf());
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
