package xyz.lychee.lagfixer.objects;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ReflectionSupportNms implements ISupportNms {
   private Method getServerMethod;
   private Field recentTpsField;
   private Method setProfileMethod;
   private Field profileField;
   private Method getHandleMethod;
   private Field pingField;

   public ReflectionSupportNms() {
      try {
         Object craftServer = Bukkit.getServer();
         this.getServerMethod = craftServer.getClass().getMethod("getServer");
         Object minecraftServer = this.getServerMethod.invoke(craftServer);
         this.recentTpsField = minecraftServer.getClass().getField("recentTps");
      } catch (Exception var5) {
      }

      ItemMeta tempMeta = Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
      if (tempMeta != null) {
         try {
            this.setProfileMethod = tempMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            this.setProfileMethod.setAccessible(true);
         } catch (NoSuchMethodException var4) {
         }

         try {
            this.profileField = tempMeta.getClass().getDeclaredField("profile");
            this.profileField.setAccessible(true);
         } catch (NoSuchFieldException var3) {
         }
      }
   }

   @Override
   public double getTps() {
      if (this.getServerMethod != null && this.recentTpsField != null) {
         try {
            Object craftServer = Bukkit.getServer();
            Object minecraftServer = this.getServerMethod.invoke(craftServer);
            if (minecraftServer == null) {
               return -1.0;
            } else {
               double[] tps = (double[])this.recentTpsField.get(minecraftServer);
               int index = Math.min(1, tps.length - 1);
               return Math.clamp(tps[index], 0.0, 20.0);
            }
         } catch (Exception var5) {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   @Override
   public ItemStack createSkull(String base64) {
      ItemStack is = new ItemStack(Material.PLAYER_HEAD);
      ItemMeta meta = is.getItemMeta();
      if (meta == null) {
         return is;
      } else {
         try {
            UUID uuid = UUID.randomUUID();
            GameProfile profile = new GameProfile(uuid, uuid.toString().substring(0, 8));
            profile.getProperties().put("textures", new Property("textures", base64));
            if (this.setProfileMethod != null) {
               try {
                  this.setProfileMethod.invoke(meta, profile);
                  is.setItemMeta(meta);
                  return is;
               } catch (Throwable var8) {
               }
            }

            if (this.profileField != null) {
               try {
                  this.profileField.set(meta, profile);
                  is.setItemMeta(meta);
                  return is;
               } catch (Throwable var7) {
               }
            }

            return is;
         } catch (Throwable var9) {
            return is;
         }
      }
   }

   @Override
   public int getTileEntitiesCount(Chunk chunk) {
      return 0;
   }

   @Override
   public int getPlayerPing(Player player) {
      if (this.getHandleMethod == null || this.pingField == null) {
         try {
            this.getHandleMethod = player.getClass().getMethod("getHandle");
            Object entityPlayer = this.getHandleMethod.invoke(player);
            this.pingField = entityPlayer.getClass().getField("ping");
            this.pingField.setAccessible(true);
         } catch (Exception var4) {
            return -1;
         }
      }

      try {
         Object handle = this.getHandleMethod.invoke(player);
         return this.pingField.getInt(handle);
      } catch (Exception var3) {
         return -1;
      }
   }

   @Override
   public void setViewDistance(World world, int view) {
   }

   @Override
   public void setSimulationDistance(World world, int simulation) {
   }

   @Override
   public void hideEntity(Plugin plugin, Player player, Entity entity) {
   }

   @Override
   public void showEntity(Plugin plugin, Player player, Entity entity) {
   }

   @Override
   public void setEntityAi(Entity ent, boolean bl) {
   }

   @Generated
   public Method getGetServerMethod() {
      return this.getServerMethod;
   }

   @Generated
   public Field getRecentTpsField() {
      return this.recentTpsField;
   }

   @Generated
   public Method getSetProfileMethod() {
      return this.setProfileMethod;
   }

   @Generated
   public Field getProfileField() {
      return this.profileField;
   }

   @Generated
   public Method getGetHandleMethod() {
      return this.getHandleMethod;
   }

   @Generated
   public Field getPingField() {
      return this.pingField;
   }

   @Generated
   public void setGetServerMethod(Method getServerMethod) {
      this.getServerMethod = getServerMethod;
   }

   @Generated
   public void setRecentTpsField(Field recentTpsField) {
      this.recentTpsField = recentTpsField;
   }

   @Generated
   public void setSetProfileMethod(Method setProfileMethod) {
      this.setProfileMethod = setProfileMethod;
   }

   @Generated
   public void setProfileField(Field profileField) {
      this.profileField = profileField;
   }

   @Generated
   public void setGetHandleMethod(Method getHandleMethod) {
      this.getHandleMethod = getHandleMethod;
   }

   @Generated
   public void setPingField(Field pingField) {
      this.pingField = pingField;
   }
}
