package xyz.lychee.lagfixer.nms.v1_17_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
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
            GameProfile gameProfile = new GameProfile(uuid, uuid.toString().substring(0, 16));
            gameProfile.getProperties().put("textures", new Property("textures", base64));
            Method mtd = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, gameProfile);
            is.setItemMeta(meta);
            return is;
         } catch (Throwable var7) {
            return super.createSkull(base64);
         }
      }
   }

   @Override
   public int getTileEntitiesCount(Chunk c) {
      return c.isLoaded() ? ((CraftChunk)c).getHandle().l.size() : 0;
   }

   @Override
   public int getPlayerPing(Player player) {
      return player.getPing();
   }

   @Override
   public double getTps() {
      return ((CraftServer)Bukkit.getServer()).getServer().recentTps[2];
   }

   @Override
   public void setViewDistance(World world, int view) {
      int clampedView = Math.clamp((long)view, 2, 32);
      WorldServer level = ((CraftWorld)world).getHandle();
      if (level.spigotConfig.viewDistance != clampedView) {
         level.spigotConfig.viewDistance = clampedView;
         level.getChunkProvider().setViewDistance(clampedView);
      }
   }

   @Override
   public void hideEntity(Plugin plugin, Player player, Entity entity) {
   }

   @Override
   public void showEntity(Plugin plugin, Player player, Entity entity) {
   }

   @Override
   public void setEntityAi(Entity ent, boolean bl) {
      if (ent instanceof CraftCreature creature) {
         EntityCreature mob = creature.getHandle();
         if (mob.isNoAI() != bl) {
            return;
         }

         mob.setNoAI(!bl);
         mob.setAggressive(!bl);
         mob.setSilent(!bl);
         mob.collides = !bl;
      }
   }
}
