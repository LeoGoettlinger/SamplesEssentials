package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R7;

import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.DataConverterTypes;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public enum ItemStackSerializer {
   INSTANCE;

   public byte[] serializeAsBytes(ItemStack itemStack) {
      Preconditions.checkArgument(itemStack != null, "Can't serialise a null itemstack!");
      Preconditions.checkArgument(itemStack.getType() != Material.AIR, "Can't serialise air!");

      try {
         byte[] var4;
         try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NBTTagCompound compoundTag = (NBTTagCompound)net.minecraft.world.item.ItemStack.b
               .encodeStart(MinecraftServer.getServer().bc().a(DynamicOpsNBT.a), CraftItemStack.asNMSCopy(itemStack))
               .getOrThrow();
            compoundTag.a("DataVersion", SharedConstants.b().a().b());
            NBTCompressedStreamTools.a(compoundTag, outputStream);
            var4 = outputStream.toByteArray();
         }

         return var4;
      } catch (Exception var7) {
         throw new RuntimeException(var7);
      }
   }

   public ItemStack deserializeFromBytes(byte[] bytes) {
      try {
         CraftItemStack var7;
         try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            NBTTagCompound compound = NBTCompressedStreamTools.a(stream, NBTReadLimiter.c());
            int dataVersion = compound.b("DataVersion", 0);
            NBTTagCompound converted = (NBTTagCompound)MinecraftServer.getServer()
               .M
               .update(DataConverterTypes.x, new Dynamic(DynamicOpsNBT.a, compound), dataVersion, SharedConstants.b().a().b())
               .getValue();
            net.minecraft.world.item.ItemStack minecraftStack = (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.b
               .parse(MinecraftServer.getServer().bc().a(DynamicOpsNBT.a), converted)
               .getOrThrow();
            var7 = CraftItemStack.asCraftMirror(minecraftStack);
         }

         return var7;
      } catch (Exception var10) {
         throw new RuntimeException(var10);
      }
   }

   public String serializeAsSnbt(ItemStack itemStack) {
      Preconditions.checkArgument(itemStack != null, "Can't serialise a null itemstack!");
      Preconditions.checkArgument(itemStack.getType() != Material.AIR, "Can't serialise air!");
      NBTTagCompound compoundTag = (NBTTagCompound)net.minecraft.world.item.ItemStack.b
         .encodeStart(MinecraftServer.getServer().bc().a(DynamicOpsNBT.a), CraftItemStack.asNMSCopy(itemStack))
         .getOrThrow();
      compoundTag.a("DataVersion", SharedConstants.b().a().b());
      return new SnbtPrinterTagVisitor().a(compoundTag);
   }

   public ItemStack deserializeFromSnbt(String snbt) {
      try {
         NBTTagCompound parsed = MojangsonParser.a(snbt);
         int dataVersion = parsed.b("DataVersion", 0);
         NBTTagCompound converted = (NBTTagCompound)MinecraftServer.getServer()
            .M
            .update(DataConverterTypes.x, new Dynamic(DynamicOpsNBT.a, parsed), dataVersion, SharedConstants.b().a().b())
            .getValue();
         net.minecraft.world.item.ItemStack minecraftStack = (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.b
            .parse(MinecraftServer.getServer().bc().a(DynamicOpsNBT.a), converted)
            .getOrThrow();
         return CraftItemStack.asCraftMirror(minecraftStack);
      } catch (CommandSyntaxException var6) {
         LogUtils.error("Failed to parse SNBT into an ItemStack! Please, check out the exception for more details!", var6);
         throw new RuntimeException(var6);
      }
   }
}
