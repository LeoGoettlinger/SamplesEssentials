package xyz.lychee.lagfixer.nms.v1_21_R7;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R7.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import xyz.lychee.lagfixer.objects.ReflectionSupportNms;

public class SupportNms extends ReflectionSupportNms {
   @Override
   public ItemStack createSkull(String base64) {
      ItemStack is = new ItemStack(Material.PLAYER_HEAD);
      ItemMeta meta = is.getItemMeta();
      if (meta == null) {
         return is;
      } else {
         try {
            UUID uuid = UUID.randomUUID();
            GameProfile gameProfile = new GameProfile(
               uuid, uuid.toString().substring(0, 16), new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", base64)))
            );
            ResolvableProfile resolvableProfile = ResolvableProfile.a(gameProfile);
            Method mtd = meta.getClass().getDeclaredMethod("setProfile", ResolvableProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, resolvableProfile);
            is.setItemMeta(meta);
            return is;
         } catch (Throwable var8) {
            return super.createSkull(base64);
         }
      }
   }

   @Override
   public int getTileEntitiesCount(Chunk c) {
      return c.isLoaded() ? ((CraftChunk)c).getHandle(ChunkStatus.n).j.size() : 0;
   }

   @Override
   public int getPlayerPing(Player player) {
      return player.getPing();
   }

   @Override
   public double getTps() {
      return 1.0E9 / ((CraftServer)Bukkit.getServer()).getServer().aS();
   }

   @Override
   public void setViewDistance(World world, int view) {
      int clampedView = Math.clamp((long)view, 2, 32);
      WorldServer level = ((CraftWorld)world).getHandle();
      if (level.spigotConfig.viewDistance != clampedView) {
         level.spigotConfig.viewDistance = clampedView;
         level.p().a(clampedView);
      }
   }

   @Override
   public void setSimulationDistance(World world, int simulation) {
      WorldServer level = ((CraftWorld)world).getHandle();
      int clampedSimulation = Math.clamp((long)simulation, 1, level.spigotConfig.viewDistance);
      if (level.spigotConfig.simulationDistance != clampedSimulation) {
         level.spigotConfig.simulationDistance = clampedSimulation;
         level.p().b(clampedSimulation);
      }
   }

   @Override
   public void hideEntity(Plugin plugin, Player player, Entity entity) {
      player.hideEntity(plugin, entity);
   }

   @Override
   public void showEntity(Plugin plugin, Player player, Entity entity) {
      player.showEntity(plugin, entity);
   }

   @Override
   public void setEntityAi(Entity ent, boolean bl) {
      if (ent instanceof CraftCreature creature) {
         EntityCreature mob = creature.getHandle();
         if (mob.gG() != bl) {
            return;
         }

         mob.u(!bl);
         mob.w(!bl);
         mob.f(!bl);
         mob.collides = !bl;
      }
   }
}
