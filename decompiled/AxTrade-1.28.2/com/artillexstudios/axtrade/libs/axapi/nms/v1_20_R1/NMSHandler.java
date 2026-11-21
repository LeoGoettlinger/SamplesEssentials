package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1;

import com.artillexstudios.axtrade.libs.axapi.gui.AnvilInput;
import com.artillexstudios.axtrade.libs.axapi.gui.SignInput;
import com.artillexstudios.axtrade.libs.axapi.items.component.DataComponentImpl;
import com.artillexstudios.axtrade.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.entity.PacketEntity;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.items.datacomponents.DataComponentTypes;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.items.nbt.CompoundTag;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.loot.LootTable;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.packet.PacketTransformer;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.wrapper.WrapperMapperRegistry;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.WrapperMapper;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.WrapperRegistry;
import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.serializers.Serializer;
import com.artillexstudios.axtrade.libs.axapi.utils.ComponentSerializer;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class NMSHandler implements com.artillexstudios.axtrade.libs.axapi.nms.NMSHandler {
   @Override
   public Serializer<Object, Component> componentSerializer() {
      return new Serializer<Object, Component>() {
         public Component serialize(Object object) {
            if (object instanceof IChatBaseComponent component) {
               String gsonText = ChatSerializer.a(component);
               return GsonComponentSerializer.gson().deserialize(gsonText);
            } else {
               throw new IllegalArgumentException("Can only serialize component!");
            }
         }

         public Object deserialize(Component value) {
            return ChatSerializer.a(GsonComponentSerializer.gson().serializer().toJsonTree(value));
         }
      };
   }

   @Override
   public int getProtocolVersionId(Player player) {
      EntityPlayer serverPlayer = ((CraftPlayer)player).getHandle();
      return serverPlayer.c.h.protocolVersion;
   }

   public PacketEntity createEntity(EntityType entityType, Location location) {
      return new PacketEntity(entityType, location);
   }

   public CompoundTag newTag() {
      return new CompoundTag(new NBTTagCompound());
   }

   @Override
   public void openSignInput(SignInput signInput) {
      EntityPlayer player = ((CraftPlayer)signInput.getPlayer()).getHandle();
      BlockPosition pos = CraftLocation.toBlockPosition(signInput.getLocation());
      player.c.a(new PacketPlayOutBlockChange(pos, ((CraftBlockData)Material.OAK_SIGN.createBlockData()).getState()));
      PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
      buf.a(pos);
      buf.a(BuiltInRegistries.l, TileEntityTypes.h);
      NBTTagCompound tag = new NBTTagCompound();
      tag.a("x", pos.u());
      tag.a("y", pos.v());
      tag.a("z", pos.w());
      tag.a("id", "minecraft:oak_sign");
      if (!tag.e("front_text")) {
         tag.a("front_text", new NBTTagCompound());
      }

      NBTTagCompound sideTag = tag.p("front_text");
      if (!tag.e("messages")) {
         sideTag.a("messages", new NBTTagList());
      }

      NBTTagList messagesNbt = sideTag.c("messages", 8);

      for (int i = 0; i < 4; i++) {
         String gson = ComponentSerializer.INSTANCE.toGson((Component)(i > signInput.getLines().length ? Component.empty() : signInput.getLines()[i]));
         messagesNbt.c(i, NBTTagString.a(gson));
      }

      buf.a(tag);
      PacketPlayOutTileEntityData clientboundBlockEntityDataPacket = new PacketPlayOutTileEntityData(buf);
      PacketPlayOutOpenSignEditor openSignEditorPacket = new PacketPlayOutOpenSignEditor(pos, true);
      player.c.a(clientboundBlockEntityDataPacket);
      player.c.a(openSignEditorPacket);
      buf.release();
   }

   @Override
   public void setTitle(Inventory inventory, Component title) {
      IChatBaseComponent nmsTitle = ComponentSerializer.INSTANCE.toVanilla(title);

      for (HumanEntity viewer : inventory.getViewers()) {
         CraftPlayer craftPlayer = (CraftPlayer)viewer;
         EntityPlayer serverPlayer = craftPlayer.getHandle();
         int containerId = serverPlayer.bR.j;
         Containers<?> windowType = CraftContainer.getNotchInventoryType(inventory);
         serverPlayer.c.a(new PacketPlayOutOpenWindow(containerId, windowType, nmsTitle));
         serverPlayer.bR.b();
      }
   }

   @Override
   public DataComponentImpl dataComponents() {
      return null;
   }

   @Override
   public ServerPlayerWrapper dummyPlayer() {
      return WrapperRegistry.SERVER_PLAYER
         .map(
            new EntityPlayer(
               MinecraftServer.getServer(),
               WrapperMapperRegistry.WORLD.map(Bukkit.getWorlds().getFirst()).asMinecraft(),
               new GameProfile(UUID.randomUUID(), "dummy")
            )
         );
   }

   public LootTable lootTable(Key key) {
      return new LootTable(key);
   }

   @Override
   public void openAnvilInput(AnvilInput anvilInput) {
      Player player = anvilInput.player();
      player.closeInventory();
      CraftPlayer craftPlayer = (CraftPlayer)player;
      EntityPlayer serverPlayer = craftPlayer.getHandle();
      Location location = anvilInput.location();
      ContainerAnvil anvilMenu = new ContainerAnvil(
         serverPlayer.nextContainerCounter(),
         serverPlayer.fN(),
         ContainerAccess.a(((CraftWorld)location.getWorld()).getHandle(), new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
      );
      anvilMenu.checkReachable = false;
      anvilMenu.setTitle(ComponentSerializer.INSTANCE.toVanilla(anvilInput.title()));
      Inventory inventory = anvilMenu.getBukkitView().getTopInventory();
      inventory.setItem(0, anvilInput.itemStack().toBukkit());
      serverPlayer.c.a(new PacketPlayOutOpenWindow(anvilMenu.j, anvilMenu.a(), anvilMenu.getTitle()));
      serverPlayer.bR = anvilMenu;
      serverPlayer.a(anvilMenu);
   }

   @Override
   public <T extends WrapperMapper<?>> T mapper(String id) {
      return WrapperMapperRegistry.mapper(id);
   }

   @Override
   public FriendlyByteBuf newBuf() {
      return PacketTransformer.newByteBuf();
   }

   @Override
   public DataComponent<?> getDataComponent(String id) {
      return DataComponentTypes.component(id);
   }
}
