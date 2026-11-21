package com.artillexstudios.axafkzone.libs.axapi.nms;

import com.artillexstudios.axafkzone.libs.axapi.gui.AnvilInput;
import com.artillexstudios.axafkzone.libs.axapi.gui.SignInput;
import com.artillexstudios.axafkzone.libs.axapi.items.component.DataComponentImpl;
import com.artillexstudios.axafkzone.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axafkzone.libs.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axafkzone.libs.axapi.loot.LootTable;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.WrapperMapper;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packetentity.PacketEntity;
import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface NMSHandler {
   Serializer<Object, Component> componentSerializer();

   int getProtocolVersionId(Player player);

   PacketEntity createEntity(EntityType entityType, Location location);

   CompoundTag newTag();

   void openSignInput(SignInput signInput);

   void setTitle(Inventory inventory, Component title);

   DataComponentImpl dataComponents();

   ServerPlayerWrapper dummyPlayer();

   LootTable lootTable(Key key);

   void openAnvilInput(AnvilInput anvilInput);

   <T extends WrapperMapper<?>> T mapper(String id);

   FriendlyByteBuf newBuf();

   DataComponent<?> getDataComponent(String id);
}
