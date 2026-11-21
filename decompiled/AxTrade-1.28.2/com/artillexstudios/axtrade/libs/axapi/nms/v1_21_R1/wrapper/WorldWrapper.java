package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.wrapper;

import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.BlockSetterImpl;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.ParallelBlockSetterImpl;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import com.artillexstudios.axtrade.libs.axapi.selection.BlockSetter;
import com.artillexstudios.axtrade.libs.axapi.selection.ParallelBlockSetter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftSkull;

public final class WorldWrapper implements com.artillexstudios.axtrade.libs.axapi.nms.wrapper.WorldWrapper {
   private World wrapped;
   private WorldServer level;

   public WorldWrapper(World world) {
      this.wrapped = world;
   }

   public WorldWrapper(WorldServer level) {
      this.level = level;
   }

   @Override
   public BlockSetter setter() {
      this.update();
      return new BlockSetterImpl(this.level);
   }

   @Override
   public ParallelBlockSetter parallelSetter() {
      this.update();
      return new ParallelBlockSetterImpl(this.level);
   }

   @Override
   public List<com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper> players() {
      this.update();
      List<EntityPlayer> players = this.level.x();
      List<com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper> playerList = new ObjectArrayList(players.size());
      if (players instanceof ArrayList<EntityPlayer> arrayList) {
         for (int i = 0; i < players.size(); i++) {
            EntityPlayer serverPlayer = arrayList.get(i);
            if (serverPlayer != null) {
               playerList.add(new ServerPlayerWrapper(serverPlayer));
            }
         }
      } else {
         for (EntityPlayer serverPlayer : players.toArray(new EntityPlayer[0])) {
            playerList.add(new ServerPlayerWrapper(serverPlayer));
         }
      }

      return playerList;
   }

   @Override
   public void setBlockOwner(Location location, String texture) {
      BlockState state = location.getBlock().getState();
      if (state instanceof Skull skull) {
         CraftSkull craftSkull = (CraftSkull)skull;
         FieldAccessor accessor = FieldAccessor.builder().withClass(CraftSkull.class).withField("profile").build();
         GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "axapi");
         gameProfile.getProperties().put("textures", new Property("textures", texture));
         accessor.set(craftSkull, gameProfile);
         state.update();
      }
   }

   @Override
   public void update(boolean force) {
      if (this.level == null || force) {
         this.level = ((CraftWorld)this.wrapped).getHandle();
      }
   }

   public World wrapped() {
      World wrapped = this.wrapped;
      if (wrapped == null) {
         wrapped = this.level.getWorld();
         this.wrapped = wrapped;
      }

      return wrapped;
   }

   public WorldServer asMinecraft() {
      this.update();
      return this.level;
   }
}
