package net.ess3.nms.refl.providers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.SpawnerBlockProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.block.CreatureSpawner;

@ProviderData(
   description = "Reflection Spawner Block Provider"
)
public class ReflSpawnerBlockProvider implements SpawnerBlockProvider {
   @Override
   public void setMaxSpawnDelay(final CreatureSpawner spawner, final int delay) {
      Class<?> mobSpawnerAbstract = ReflUtil.getNMSClass("MobSpawnerAbstract");
      Field maxSpawnDelay = ReflUtil.getFieldCached(mobSpawnerAbstract, "maxSpawnDelay");
      if (maxSpawnDelay != null) {
         try {
            maxSpawnDelay.setInt(this.getNMSSpawner(spawner), delay);
         } catch (IllegalAccessException var6) {
         }
      }
   }

   @Override
   public void setMinSpawnDelay(final CreatureSpawner spawner, final int delay) {
      Class<?> mobSpawnerAbstract = ReflUtil.getNMSClass("MobSpawnerAbstract");
      Field minSpawnDelay = ReflUtil.getFieldCached(mobSpawnerAbstract, "minSpawnDelay");
      if (minSpawnDelay != null) {
         try {
            minSpawnDelay.setInt(this.getNMSSpawner(spawner), delay);
         } catch (IllegalAccessException var6) {
         }
      }
   }

   private Object getNMSSpawner(final CreatureSpawner spawner) {
      try {
         Class<?> craftWorld = ReflUtil.getOBCClass("CraftWorld");
         Class<?> tileEntityMobSpawner = ReflUtil.getNMSClass("TileEntityMobSpawner");
         Method getSpawner = ReflUtil.getMethodCached(tileEntityMobSpawner, "getSpawner");
         Method getTileEntityAt = ReflUtil.getMethodCached(craftWorld, "getTileEntityAt", int.class, int.class, int.class);
         if (getSpawner != null && getTileEntityAt != null) {
            Object craftTileEntity = getTileEntityAt.invoke(spawner.getWorld(), spawner.getX(), spawner.getY(), spawner.getZ());
            return getSpawner.invoke(craftTileEntity);
         }
      } catch (InvocationTargetException | IllegalAccessException var7) {
      }

      return null;
   }
}
