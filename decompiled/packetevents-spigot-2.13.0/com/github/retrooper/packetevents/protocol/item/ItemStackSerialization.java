package com.github.retrooper.packetevents.protocol.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.PatchableComponentMap;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public final class ItemStackSerialization {
   private ItemStackSerialization() {
   }

   public static ItemStack readOptionalTemplate(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)) {
         return read(wrapper);
      } else {
         return wrapper.readBoolean() ? readTemplate0(wrapper) : ItemStack.EMPTY;
      }
   }

   public static ItemStack readTemplate(PacketWrapper<?> wrapper) {
      return wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1) ? read(wrapper) : readTemplate0(wrapper);
   }

   public static ItemStack readTemplate0(PacketWrapper<?> wrapper) {
      ItemType item = wrapper.readMappedEntity(ItemTypes.getRegistry());
      int count = wrapper.readVarInt();
      PatchableComponentMap components = PatchableComponentMap.read(wrapper, item, false);
      ItemStack ret = ItemStack.builder().type(item).amount(count).components(components).build();
      if (ret.isEmpty()) {
         throw new IllegalStateException("Can't read empty item stack template: " + ret);
      } else {
         return ret;
      }
   }

   public static void writeOptionalTemplate(PacketWrapper<?> wrapper, ItemStack stack) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)) {
         write(wrapper, stack);
      } else {
         if (!stack.isEmpty()) {
            wrapper.writeBoolean(true);
            writeTemplate0(wrapper, stack);
         } else {
            wrapper.writeBoolean(false);
         }
      }
   }

   public static void writeTemplate(PacketWrapper<?> wrapper, ItemStack stack) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)) {
         write(wrapper, stack);
      } else {
         writeTemplate0(wrapper, stack);
      }
   }

   private static void writeTemplate0(PacketWrapper<?> wrapper, ItemStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalStateException("Can't write empty item stack template: " + stack);
      } else {
         wrapper.writeMappedEntity(stack.getType());
         wrapper.writeVarInt(stack.getAmount());
         PatchableComponentMap.write(wrapper, stack, false);
      }
   }

   public static ItemStack read(PacketWrapper<?> wrapper) {
      return wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5) ? readModern(wrapper) : readLegacy(wrapper);
   }

   public static void write(PacketWrapper<?> wrapper, @Nullable ItemStack stack) {
      ItemStack replacedStack = stack == null ? ItemStack.EMPTY : stack;
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
         writeModern(wrapper, replacedStack);
      } else {
         writeLegacy(wrapper, replacedStack);
      }
   }

   private static ItemStack readLegacy(PacketWrapper<?> wrapper) {
      boolean v1_13_2 = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13_2);
      if (v1_13_2 && !wrapper.readBoolean()) {
         return ItemStack.EMPTY;
      } else {
         int typeId = v1_13_2 ? wrapper.readVarInt() : wrapper.readShort();
         if (typeId < 0 && !v1_13_2) {
            return ItemStack.EMPTY;
         } else {
            ClientVersion version = wrapper.getServerVersion().toClientVersion();
            ItemType type = ItemTypes.getRegistry().getByIdOrThrow(version, typeId);
            int amount = wrapper.readByte();
            int legacyData = version.isOlderThan(ClientVersion.V_1_13) ? wrapper.readShort() : -1;
            NBTCompound nbt = wrapper.readNBT();
            return ItemStack.builder().type(type).amount(amount).nbt(nbt).legacyData(legacyData).wrapper(wrapper).build();
         }
      }
   }

   private static void writeLegacy(PacketWrapper<?> wrapper, ItemStack stack) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_13_2)) {
         int typeId = stack.isEmpty() ? -1 : stack.getType().getId(wrapper.getServerVersion().toClientVersion());
         wrapper.writeShort(typeId);
         if (typeId != -1) {
            wrapper.writeByte(stack.getAmount());
            if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_13)) {
               wrapper.writeShort(stack.getLegacyData());
            }

            wrapper.writeNBT(stack.getNBT());
         }
      } else if (stack.isEmpty()) {
         wrapper.writeBoolean(false);
      } else {
         wrapper.writeBoolean(true);
         wrapper.writeMappedEntity(stack.getType());
         wrapper.writeByte(stack.getAmount());
         wrapper.writeNBT(stack.getNBT());
      }
   }

   public static ItemStack readModern(PacketWrapper<?> wrapper) {
      return readModern(wrapper, false);
   }

   public static ItemStack readUntrusted(PacketWrapper<?> wrapper) {
      return readModern(wrapper, true);
   }

   private static ItemStack readModern(PacketWrapper<?> wrapper, boolean lengthPrefixed) {
      int count = wrapper.readVarInt();
      if (count <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemType item = wrapper.readMappedEntity(ItemTypes.getRegistry());
         PatchableComponentMap components = PatchableComponentMap.read(wrapper, item, lengthPrefixed);
         return ItemStack.builder().type(item).amount(count).components(components).wrapper(wrapper).build();
      }
   }

   public static void writeModern(PacketWrapper<?> wrapper, ItemStack stack) {
      writeModern(wrapper, stack, false);
   }

   public static void writeUntrusted(PacketWrapper<?> wrapper, ItemStack stack) {
      writeModern(wrapper, stack, true);
   }

   private static void writeModern(PacketWrapper<?> wrapper, ItemStack stack, boolean lengthPrefixed) {
      if (stack.isEmpty()) {
         wrapper.writeByte(0);
      } else {
         wrapper.writeVarInt(stack.getAmount());
         wrapper.writeMappedEntity(stack.getType());
         PatchableComponentMap.write(wrapper, stack, lengthPrefixed);
      }
   }
}
