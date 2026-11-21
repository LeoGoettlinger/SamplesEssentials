package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_R1.nbt;

import com.sk89q.jnbt.LazyCompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import org.enginehub.linbus.tree.LinCompoundTag;

public class PaperweightLazyCompoundTag extends LazyCompoundTag {
   private final Supplier<CompoundTag> compoundTagSupplier;
   private com.sk89q.jnbt.CompoundTag compoundTag;

   public PaperweightLazyCompoundTag(Supplier<CompoundTag> compoundTagSupplier) {
      super(new HashMap<>());
      this.compoundTagSupplier = compoundTagSupplier;
   }

   public PaperweightLazyCompoundTag(CompoundTag compoundTag) {
      this((Supplier<CompoundTag>)(() -> compoundTag));
   }

   public CompoundTag get() {
      return this.compoundTagSupplier.get();
   }

   @Override
   public Map<String, Tag<?, ?>> getValue() {
      if (this.compoundTag == null) {
         this.compoundTag = (com.sk89q.jnbt.CompoundTag)WorldEditPlugin.getInstance().getBukkitImplAdapter().toNative(this.compoundTagSupplier.get());
      }

      return this.compoundTag.getValue();
   }

   @Override
   public LinCompoundTag toLinTag() {
      this.getValue();
      return this.compoundTag.toLinTag();
   }

   @Override
   public boolean containsKey(String key) {
      return this.compoundTagSupplier.get().contains(key);
   }

   @Override
   public byte[] getByteArray(String key) {
      return this.compoundTagSupplier.get().getByteArray(key);
   }

   @Override
   public byte getByte(String key) {
      return this.compoundTagSupplier.get().getByte(key);
   }

   @Override
   public double getDouble(String key) {
      return this.compoundTagSupplier.get().getDouble(key);
   }

   @Override
   public double asDouble(String key) {
      return this.compoundTagSupplier.get().get(key) instanceof NumericTag numTag ? numTag.getAsDouble() : 0.0;
   }

   @Override
   public float getFloat(String key) {
      return this.compoundTagSupplier.get().getFloat(key);
   }

   @Override
   public int[] getIntArray(String key) {
      return this.compoundTagSupplier.get().getIntArray(key);
   }

   @Override
   public int getInt(String key) {
      return this.compoundTagSupplier.get().getInt(key);
   }

   @Override
   public int asInt(String key) {
      return this.compoundTagSupplier.get().get(key) instanceof NumericTag numTag ? numTag.getAsInt() : 0;
   }

   @Override
   public List<? extends Tag<?, ?>> getList(String key) {
      if (this.compoundTagSupplier.get().get(key) instanceof ListTag nbtList) {
         ArrayList<Tag<?, ?>> list = new ArrayList<>();

         for (net.minecraft.nbt.Tag elem : nbtList) {
            if (elem instanceof CompoundTag compoundTag) {
               list.add(new PaperweightLazyCompoundTag(compoundTag));
            } else {
               list.add(WorldEditPlugin.getInstance().getBukkitImplAdapter().toNative(elem));
            }
         }

         return list;
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public com.sk89q.jnbt.ListTag getListTag(String key) {
      net.minecraft.nbt.Tag tag = this.compoundTagSupplier.get().get(key);
      return tag instanceof ListTag
         ? (com.sk89q.jnbt.ListTag)WorldEditPlugin.getInstance().getBukkitImplAdapter().toNative(tag)
         : new com.sk89q.jnbt.ListTag(StringTag.class, Collections.emptyList());
   }

   @Override
   public <T extends Tag<?, ?>> List<T> getList(String key, Class<T> listType) {
      com.sk89q.jnbt.ListTag listTag = this.getListTag(key);
      return listTag.getType().equals(listType) ? listTag.getValue() : Collections.emptyList();
   }

   @Override
   public long[] getLongArray(String key) {
      return this.compoundTagSupplier.get().getLongArray(key);
   }

   @Override
   public long getLong(String key) {
      return this.compoundTagSupplier.get().getLong(key);
   }

   @Override
   public long asLong(String key) {
      return this.compoundTagSupplier.get().get(key) instanceof NumericTag numTag ? numTag.getAsLong() : 0L;
   }

   @Override
   public short getShort(String key) {
      return this.compoundTagSupplier.get().getShort(key);
   }

   @Override
   public String getString(String key) {
      return this.compoundTagSupplier.get().getString(key);
   }

   @Override
   public String toString() {
      return this.compoundTagSupplier.get().toString();
   }
}
