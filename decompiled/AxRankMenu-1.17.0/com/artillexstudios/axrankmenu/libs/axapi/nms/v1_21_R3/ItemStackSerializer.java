package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R3;

import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
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
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public enum ItemStackSerializer {
   INSTANCE;

   public byte[] serializeAsBytes(ItemStack itemStack) {
      Preconditions.checkArgument(itemStack != null, "Can't serialise a null itemstack!");
      Preconditions.checkArgument(itemStack.getType() != Material.AIR, "Can't serialise air!");

      try {
         byte[] var4;
         try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NBTTagCompound compoundTag = (NBTTagCompound)CraftItemStack.asNMSCopy(itemStack).a(MinecraftServer.getServer().ba());
            compoundTag.a("DataVersion", SharedConstants.b().d().c());
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
         CraftItemStack var6;
         try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            NBTTagCompound compound = NBTCompressedStreamTools.a(stream, NBTReadLimiter.a());
            int dataVersion = compound.h("DataVersion");
            NBTTagCompound converted = (NBTTagCompound)MinecraftServer.getServer()
               .L
               .update(DataConverterTypes.t, new Dynamic(DynamicOpsNBT.a, compound), dataVersion, SharedConstants.b().d().c())
               .getValue();
            var6 = CraftItemStack.asCraftMirror(
               (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.a(MinecraftServer.getServer().ba(), converted).orElseThrow()
            );
         }

         return var6;
      } catch (Exception var9) {
         throw new RuntimeException(var9);
      }
   }

   public String serializeAsSnbt(ItemStack itemStack) {
      Preconditions.checkArgument(itemStack != null, "Can't serialise a null itemstack!");
      Preconditions.checkArgument(itemStack.getType() != Material.AIR, "Can't serialise air!");
      NBTTagCompound tag = (NBTTagCompound)CraftItemStack.asNMSCopy(itemStack).a(MinecraftServer.getServer().ba());
      tag.a("DataVersion", SharedConstants.b().d().c());
      return new SnbtPrinterTagVisitor().a(tag);
   }

   public ItemStack deserializeFromSnbt(String snbt) {
      try {
         NBTTagCompound parsed = MojangsonParser.a(snbt);
         int dataVersion = parsed.h("DataVersion");
         NBTTagCompound converted = (NBTTagCompound)MinecraftServer.getServer()
            .L
            .update(DataConverterTypes.t, new Dynamic(DynamicOpsNBT.a, parsed), dataVersion, SharedConstants.b().d().c())
            .getValue();
         return CraftItemStack.asCraftMirror(
            (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.a(MinecraftServer.getServer().ba(), converted).orElseThrow()
         );
      } catch (CommandSyntaxException var5) {
         LogUtils.error("Failed to parse SNBT into an ItemStack! Please, check out the exception for more details!", var5);
         throw new RuntimeException(var5);
      }
   }
}
