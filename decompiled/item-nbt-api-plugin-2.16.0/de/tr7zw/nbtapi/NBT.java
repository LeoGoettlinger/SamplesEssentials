package de.tr7zw.nbtapi;

import com.mojang.authlib.GameProfile;
import de.tr7zw.nbtapi.iface.NBTFileHandle;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableItemNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.iface.ReadableNBTList;
import de.tr7zw.nbtapi.utils.CheckUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import de.tr7zw.nbtapi.utils.nmsmappings.ClassWrapper;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import de.tr7zw.nbtapi.wrapper.NBTProxy;
import de.tr7zw.nbtapi.wrapper.ProxyBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NBT {
   private NBT() {
   }

   public static boolean preloadApi() {
      try {
         if (MinecraftVersion.getVersion() == MinecraftVersion.UNKNOWN) {
            NbtApiException.confirmedBroken = true;
            return false;
         } else {
            for (ClassWrapper c : ClassWrapper.values()) {
               if (c.isEnabled() && c.getClazz() == null) {
                  NbtApiException.confirmedBroken = true;
                  return false;
               }
            }

            for (ReflectionMethod method : ReflectionMethod.values()) {
               if (method.isCompatible() && !method.isLoaded()) {
                  NbtApiException.confirmedBroken = true;
                  return false;
               }
            }

            return true;
         }
      } catch (Exception var4) {
         NbtApiException.confirmedBroken = true;
         MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error during the selfcheck!", (Throwable)var4);
         return false;
      }
   }

   public static ReadableNBT readNbt(ItemStack item) {
      return new NBTItem(item.clone(), false, true, false);
   }

   public static <T> T get(ItemStack item, Function<ReadableItemNBT, T> getter) {
      NBTItem nbt = new NBTItem(item, false, true, false);
      T ret = getter.apply(nbt);
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         nbt.setClosed();
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void get(ItemStack item, Consumer<ReadableItemNBT> getter) {
      NBTItem nbt = new NBTItem(item, false, true, false);
      getter.accept(nbt);
      nbt.setClosed();
   }

   public static <T> T get(Entity entity, Function<ReadableNBT, T> getter) {
      NBTEntity nbt = new NBTEntity(entity, true);
      T ret = getter.apply(nbt);
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         nbt.setClosed();
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void get(Entity entity, Consumer<ReadableNBT> getter) {
      NBTEntity nbt = new NBTEntity(entity, true);
      getter.accept(nbt);
      nbt.setClosed();
   }

   public static <T> T get(BlockState blockState, Function<ReadableNBT, T> getter) {
      NBTTileEntity nbt = new NBTTileEntity(blockState, true);
      T ret = getter.apply(nbt);
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         nbt.setClosed();
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void get(BlockState blockState, Consumer<ReadableNBT> getter) {
      NBTTileEntity nbt = new NBTTileEntity(blockState, true);
      getter.accept(nbt);
      nbt.setClosed();
   }

   public static <T> T getPersistentData(Entity entity, Function<ReadableNBT, T> getter) {
      T ret = getter.apply(new NBTEntity(entity).getPersistentDataContainer());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static <T> T getPersistentData(BlockState blockState, Function<ReadableNBT, T> getter) {
      T ret = getter.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static <T> T modify(ItemStack item, Function<ReadWriteItemNBT, T> function) {
      NBTItem nbti = new NBTItem(item, false, false, true);
      T val = function.apply(nbti);
      nbti.finalizeChanges();
      if (!(val instanceof ReadableNBT) && !(val instanceof ReadableNBTList)) {
         nbti.setClosed();
         return val;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void modify(ItemStack item, Consumer<ReadWriteItemNBT> consumer) {
      NBTItem nbti = new NBTItem(item, false, false, true);
      consumer.accept(nbti);
      nbti.finalizeChanges();
      nbti.setClosed();
   }

   public static <T> T modify(Entity entity, Function<ReadWriteNBT, T> function) {
      NBTEntity nbtEnt = new NBTEntity(entity);
      NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
      T ret = function.apply(cont);
      nbtEnt.setCompound(cont.getCompound());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         nbtEnt.setClosed();
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void modifyComponents(ItemStack item, Consumer<ReadWriteNBT> consumer) {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         throw new NbtApiException("This method only works for 1.20.5+!");
      } else {
         ReadWriteNBT nbti = itemStackToNBT(item);
         consumer.accept(nbti.getOrCreateCompound("components"));
         ItemStack tmp = itemStackFromNBT(nbti);
         item.setItemMeta(tmp.getItemMeta());
      }
   }

   public static <T> T modifyComponents(ItemStack item, Function<ReadWriteNBT, T> function) {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         throw new NbtApiException("This method only works for 1.20.5+!");
      } else {
         ReadWriteNBT nbti = itemStackToNBT(item);
         T ret = function.apply(nbti.getOrCreateCompound("components"));
         ItemStack tmp = itemStackFromNBT(nbti);
         item.setItemMeta(tmp.getItemMeta());
         return ret;
      }
   }

   public static void getComponents(ItemStack item, Consumer<ReadableNBT> consumer) {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         throw new NbtApiException("This method only works for 1.20.5+!");
      } else {
         ReadWriteNBT nbti = itemStackToNBT(item);
         consumer.accept(nbti.getOrCreateCompound("components"));
      }
   }

   public static <T> T getComponents(ItemStack item, Function<ReadableNBT, T> function) {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         throw new NbtApiException("This method only works for 1.20.5+!");
      } else {
         ReadWriteNBT nbti = itemStackToNBT(item);
         return function.apply(nbti.getOrCreateCompound("components"));
      }
   }

   public static void modify(Entity entity, Consumer<ReadWriteNBT> consumer) {
      NBTEntity nbtEnt = new NBTEntity(entity);
      NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
      consumer.accept(cont);
      nbtEnt.setCompound(cont.getCompound());
      nbtEnt.setClosed();
   }

   public static <T> T modifyPersistentData(Entity entity, Function<ReadWriteNBT, T> function) {
      T ret = function.apply(new NBTEntity(entity).getPersistentDataContainer());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void modifyPersistentData(Entity entity, Consumer<ReadWriteNBT> consumer) {
      consumer.accept(new NBTEntity(entity).getPersistentDataContainer());
   }

   public static <T> T modify(BlockState blockState, Function<ReadWriteNBT, T> function) {
      NBTTileEntity blockEnt = new NBTTileEntity(blockState);
      NBTContainer cont = new NBTContainer(blockEnt.getCompound());
      T ret = function.apply(cont);
      blockEnt.setCompound(cont.getCompound());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         blockEnt.setClosed();
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void modify(BlockState blockState, Consumer<ReadWriteNBT> consumer) {
      NBTTileEntity blockEnt = new NBTTileEntity(blockState);
      NBTContainer cont = new NBTContainer(blockEnt.getCompound());
      consumer.accept(cont);
      blockEnt.setCompound(cont.getCompound());
      blockEnt.setClosed();
   }

   public static <T> T modifyPersistentData(BlockState blockState, Function<ReadWriteNBT, T> function) {
      T ret = function.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
      if (!(ret instanceof ReadableNBT) && !(ret instanceof ReadableNBTList)) {
         return ret;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void modifyPersistentData(BlockState blockState, Consumer<ReadWriteNBT> consumer) {
      consumer.accept(new NBTTileEntity(blockState).getPersistentDataContainer());
   }

   public static ReadWriteNBT gameProfileToNBT(GameProfile profile) {
      return NBTGameProfile.toNBT(profile);
   }

   public static GameProfile gameProfileFromNBT(ReadableNBT compound) {
      return NBTGameProfile.fromNBT((NBTCompound)compound);
   }

   public static ReadWriteNBT itemStackToNBT(ItemStack itemStack) {
      return NBTItem.convertItemtoNBT(itemStack);
   }

   @Nullable
   public static ItemStack itemStackFromNBT(ReadableNBT compound) {
      return NBTItem.convertNBTtoItem((NBTCompound)compound);
   }

   public static ReadWriteNBT itemStackArrayToNBT(ItemStack[] itemStacks) {
      return NBTItem.convertItemArraytoNBT(itemStacks);
   }

   @Nullable
   public static ItemStack[] itemStackArrayFromNBT(ReadableNBT compound) {
      return NBTItem.convertNBTtoItemArray((NBTCompound)compound);
   }

   public static ReadWriteNBT createNBTObject() {
      return new NBTContainer();
   }

   public static ReadWriteNBT parseNBT(String nbtString) {
      return new NBTContainer(nbtString);
   }

   public static ReadWriteNBT readNBT(InputStream stream) {
      return new NBTContainer(stream);
   }

   public static ReadWriteNBT wrapNMSTag(Object nmsNbtTag) {
      return new NBTContainer(nmsNbtTag);
   }

   public static NBTFileHandle getFileHandle(File file) throws IOException {
      return new NBTFile(file);
   }

   public static ReadWriteNBT readFile(File file) throws IOException {
      return NBTFile.readFrom(file);
   }

   public static void writeFile(File file, ReadWriteNBT nbt) throws IOException {
      NBTFile.saveTo(file, (NBTCompound)nbt);
   }

   public static <T extends NBTProxy> T readNbt(ItemStack item, Class<T> wrapper) {
      return new ProxyBuilder<>(new NBTItem(item, false, true, false), wrapper).readOnly().build();
   }

   public static <T extends NBTProxy> T readNbt(Entity entity, Class<T> wrapper) {
      return new ProxyBuilder<>(new NBTEntity(entity, true), wrapper).readOnly().build();
   }

   public static <T extends NBTProxy> T readNbt(BlockState blockState, Class<T> wrapper) {
      return new ProxyBuilder<>(new NBTTileEntity(blockState, true), wrapper).readOnly().build();
   }

   public static <T, X extends NBTProxy> T modify(ItemStack item, Class<X> wrapper, Function<X, T> function) {
      NBTItem nbti = new NBTItem(item, false, false, true);
      T val = function.apply(new ProxyBuilder<>(nbti, wrapper).build());
      nbti.finalizeChanges();
      if (!(val instanceof ReadableNBT) && !(val instanceof ReadableNBTList)) {
         nbti.setClosed();
         return val;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static <X extends NBTProxy> void modify(ItemStack item, Class<X> wrapper, Consumer<X> consumer) {
      NBTItem nbti = new NBTItem(item, false, false, true);
      consumer.accept(new ProxyBuilder<>(nbti, wrapper).build());
      nbti.finalizeChanges();
      nbti.setClosed();
   }

   public static <X extends NBTProxy> void modify(Entity entity, Class<X> wrapper, Consumer<X> consumer) {
      NBTEntity nbtEnt = new NBTEntity(entity);
      NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
      consumer.accept(new ProxyBuilder<>(cont, wrapper).build());
      nbtEnt.setCompound(cont.getCompound());
      cont.setClosed();
   }

   public static <T, X extends NBTProxy> T modify(Entity entity, Class<X> wrapper, Function<X, T> function) {
      NBTEntity nbtEnt = new NBTEntity(entity);
      NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
      T val = function.apply(new ProxyBuilder<>(cont, wrapper).build());
      nbtEnt.setCompound(cont.getCompound());
      cont.setClosed();
      return val;
   }

   public static <X extends NBTProxy> void modify(BlockState blockState, Class<X> wrapper, Consumer<X> consumer) {
      NBTTileEntity blockEnt = new NBTTileEntity(blockState);
      NBTContainer cont = new NBTContainer(blockEnt.getCompound());
      consumer.accept(new ProxyBuilder<>(cont, wrapper).build());
      blockEnt.setCompound(cont);
      cont.setClosed();
   }

   public static <T, X extends NBTProxy> T modify(BlockState blockState, Class<X> wrapper, Function<X, T> function) {
      NBTTileEntity blockEnt = new NBTTileEntity(blockState);
      NBTContainer cont = new NBTContainer(blockEnt.getCompound());
      T val = function.apply(new ProxyBuilder<>(cont, wrapper).build());
      blockEnt.setCompound(cont);
      cont.setClosed();
      return val;
   }

   public static void readChunkPDC(Block block, Consumer<ReadableNBT> consumer) {
      processBlockChunkPDC(block, false, nbt -> {
         consumer.accept(nbt);
         return null;
      });
   }

   public static <T> T readAndGetChunkPDC(Block block, Function<ReadableNBT, T> function) {
      return processBlockChunkPDC(block, false, function::apply);
   }

   public static void modifyChunkPDC(Block block, Consumer<ReadWriteNBT> consumer) {
      processBlockChunkPDC(block, true, nbt -> {
         consumer.accept(nbt);
         return null;
      });
   }

   public static <T> T modifyAndGetChunkPDC(Block block, Function<ReadWriteNBT, T> function) {
      return processBlockChunkPDC(block, true, function::apply);
   }

   private static <T> T processBlockChunkPDC(Block block, boolean createIfAbsent, Function<NBTCompound, T> action) {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R3);
      String blockKey = block.getX() + "_" + block.getY() + "_" + block.getZ();
      NBTCompound chunkData = new NBTPersistentDataContainer(block.getChunk().getPersistentDataContainer(), !createIfAbsent);
      NBTCompound blocksData = createIfAbsent ? chunkData.getOrCreateCompound("blocks") : chunkData.getCompound("blocks");
      NBTCompound blockData;
      if (createIfAbsent) {
         blockData = blocksData.getOrCreateCompound(blockKey);
      } else {
         blockData = blocksData == null ? null : blocksData.getCompound(blockKey);
         if (blockData == null) {
            blockData = new NBTContainer().setReadOnly(true);
         }
      }

      T result = action.apply(blockData);
      if (createIfAbsent) {
         if (blockData.isEmpty()) {
            blocksData.removeKey(blockKey);
         }

         if (blocksData.isEmpty()) {
            chunkData.removeKey("blocks");
         }
      }

      if (!(result instanceof ReadableNBT) && !(result instanceof ReadableNBTList)) {
         return result;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }

   public static void readChunkPDC(Chunk chunk, Consumer<ReadableNBT> consumer) {
      processChunkPDC(chunk, false, nbt -> {
         consumer.accept(nbt);
         return null;
      });
   }

   public static <T> T readAndGetChunkPDC(Chunk chunk, Function<ReadableNBT, T> function) {
      return processChunkPDC(chunk, false, function::apply);
   }

   public static void modifyChunkPDC(Chunk chunk, Consumer<ReadWriteNBT> consumer) {
      processChunkPDC(chunk, true, nbt -> {
         consumer.accept(nbt);
         return null;
      });
   }

   public static <T> T modifyAndGetChunkPDC(Chunk chunk, Function<ReadWriteNBT, T> function) {
      return processChunkPDC(chunk, true, function::apply);
   }

   private static <T> T processChunkPDC(Chunk chunk, boolean createIfAbsent, Function<NBTCompound, T> action) {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R3);
      NBTCompound chunkData = new NBTPersistentDataContainer(chunk.getPersistentDataContainer(), !createIfAbsent);
      T result = action.apply(chunkData);
      if (!(result instanceof ReadableNBT) && !(result instanceof ReadableNBTList)) {
         return result;
      } else {
         throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
      }
   }
}
