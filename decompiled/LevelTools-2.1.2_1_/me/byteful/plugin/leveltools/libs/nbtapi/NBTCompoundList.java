package me.byteful.plugin.leveltools.libs.nbtapi;

import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadWriteNBT;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadWriteNBTCompoundList;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadableNBT;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.MinecraftVersion;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ClassWrapper;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ReflectionMethod;

public class NBTCompoundList extends NBTList<ReadWriteNBT> implements ReadWriteNBTCompoundList {
   protected NBTCompoundList(NBTCompound owner, String name, NBTType type, Object list) {
      super(owner, name, type, list);
   }

   public NBTListCompound addCompound() {
      return (NBTListCompound)this.addCompound(null);
   }

   public NBTCompound addCompound(NBTCompound comp) {
      if (this.getParent().isReadOnly()) {
         throw new NbtApiException("Tried setting data in read only mode!");
      } else {
         try {
            Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
               ReflectionMethod.LIST_ADD.run(this.listObject, this.size(), compound);
            } else {
               ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, compound);
            }

            this.getParent().saveCompound();
            NBTListCompound listcomp = new NBTListCompound(this, compound);
            if (comp != null) {
               listcomp.mergeCompound(comp);
            }

            return listcomp;
         } catch (Exception var4) {
            throw new NbtApiException(var4);
         }
      }
   }

   @Override
   public ReadWriteNBT addCompound(ReadableNBT comp) {
      return comp instanceof NBTCompound ? this.addCompound((NBTCompound)comp) : null;
   }

   @Deprecated
   public boolean add(ReadWriteNBT empty) {
      return this.addCompound(empty) != null;
   }

   public void add(int index, ReadWriteNBT element) {
      if (element != null) {
         throw new NbtApiException("You need to pass null! ListCompounds from other lists won't work.");
      } else if (this.getParent().isReadOnly()) {
         throw new NbtApiException("Tried setting data in read only mode!");
      } else {
         try {
            Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
               ReflectionMethod.LIST_ADD.run(this.listObject, index, compound);
            } else {
               ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, compound);
            }

            super.getParent().saveCompound();
         } catch (Exception var4) {
            throw new NbtApiException(var4);
         }
      }
   }

   public NBTListCompound get(int index) {
      try {
         Object compound = ReflectionMethod.LIST_GET_COMPOUND.run(this.listObject, index);
         return new NBTListCompound(this, compound);
      } catch (Exception var3) {
         throw new NbtApiException(var3);
      }
   }

   public NBTListCompound set(int index, ReadWriteNBT element) {
      throw new NbtApiException("This method doesn't work in the ListCompound context.");
   }

   protected Object asTag(ReadWriteNBT object) {
      return null;
   }
}
