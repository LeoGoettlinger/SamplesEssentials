package com.earth2me.essentials.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.ess3.api.IUser;
import net.ess3.api.TranslatableException;
import net.ess3.provider.WorldInfoProvider;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public final class LocationUtil {
   public static final int RADIUS = 3;
   public static final LocationUtil.Vector3D[] VOLUME;
   private static final Set<Material> WATER_TYPES = EnumUtil.getAllMatching(Material.class, "FLOWING_WATER", "WATER");
   private static final Set<Material> DAMAGING_TYPES = EnumUtil.getAllMatching(
      Material.class, "CACTUS", "CAMPFIRE", "FIRE", "MAGMA_BLOCK", "SOUL_CAMPFIRE", "SOUL_FIRE", "SWEET_BERRY_BUSH", "WITHER_ROSE"
   );
   private static final Set<Material> LAVA_TYPES = EnumUtil.getAllMatching(Material.class, "FLOWING_LAVA", "LAVA", "STATIONARY_LAVA");
   private static final Material PORTAL = EnumUtil.getMaterial("NETHER_PORTAL", "PORTAL");
   private static final Material LIGHT = EnumUtil.getMaterial("LIGHT");
   private static final Material PATH = EnumUtil.getMaterial("GRASS_PATH");
   private static final Material FARMLAND = EnumUtil.getMaterial("FARMLAND");
   private static final Set<Material> HOLLOW_MATERIALS = EnumSet.noneOf(Material.class);
   private static final Set<Material> TRANSPARENT_MATERIALS = EnumSet.noneOf(Material.class);

   private LocationUtil() {
   }

   public static void setIsWaterSafe(final boolean isWaterSafe) {
      if (isWaterSafe) {
         HOLLOW_MATERIALS.addAll(WATER_TYPES);
      } else {
         HOLLOW_MATERIALS.removeAll(WATER_TYPES);
      }
   }

   public static ItemStack convertBlockToItem(final Block block) {
      return new ItemStack(block.getType(), 1);
   }

   public static Location getTarget(final LivingEntity entity) throws Exception {
      return getTarget(entity, 300);
   }

   public static Location getTarget(final LivingEntity entity, final int maxDistance) throws Exception {
      Block block = null;

      try {
         block = entity.getTargetBlock(TRANSPARENT_MATERIALS, maxDistance);
      } catch (NoSuchMethodError var4) {
      }

      if (block == null) {
         throw new Exception("Not targeting a block");
      } else {
         return block.getLocation();
      }
   }

   public static boolean isBlockAboveAir(IEssentials ess, final World world, final int x, final int y, final int z) {
      return y > ess.provider(WorldInfoProvider.class).getMaxHeight(world) || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
   }

   public static boolean isBlockOutsideWorldBorder(final World world, final int x, final int z) {
      Location center = world.getWorldBorder().getCenter();
      int radius = (int)world.getWorldBorder().getSize() / 2;
      int x1 = center.getBlockX() - radius;
      int x2 = center.getBlockX() + radius;
      int z1 = center.getBlockZ() - radius;
      int z2 = center.getBlockZ() + radius;
      return x < x1 || x > x2 || z < z1 || z > z2;
   }

   public static int getXInsideWorldBorder(final World world, final int x) {
      Location center = world.getWorldBorder().getCenter();
      int radius = (int)world.getWorldBorder().getSize() / 2;
      int x1 = center.getBlockX() - radius;
      int x2 = center.getBlockX() + radius;
      if (x < x1) {
         return x1;
      } else {
         return x > x2 ? x2 : x;
      }
   }

   public static int getZInsideWorldBorder(final World world, final int z) {
      Location center = world.getWorldBorder().getCenter();
      int radius = (int)world.getWorldBorder().getSize() / 2;
      int z1 = center.getBlockZ() - radius;
      int z2 = center.getBlockZ() + radius;
      if (z < z1) {
         return z1;
      } else {
         return z > z2 ? z2 : z;
      }
   }

   public static boolean isBlockUnsafeForUser(IEssentials ess, final IUser user, final World world, final int x, final int y, final int z) {
      if (user.getBase().isOnline()
         && world.equals(user.getBase().getWorld())
         && (user.getBase().getGameMode() == GameMode.CREATIVE || user.getBase().getGameMode() == GameMode.SPECTATOR || user.isGodModeEnabled())
         && user.getBase().getAllowFlight()) {
         return false;
      } else if (isBlockDamaging(world, x, y, z)) {
         return true;
      } else {
         return isBlockAboveAir(ess, world, x, y, z) ? true : isBlockOutsideWorldBorder(world, x, z);
      }
   }

   public static boolean isBlockUnsafe(IEssentials ess, final World world, final int x, final int y, final int z) {
      return isBlockDamaging(world, x, y, z) || isBlockAboveAir(ess, world, x, y, z);
   }

   public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
      Material block = world.getBlockAt(x, y, z).getType();
      Material below = world.getBlockAt(x, y - 1, z).getType();
      Material above = world.getBlockAt(x, y + 1, z).getType();
      if (DAMAGING_TYPES.contains(below) || LAVA_TYPES.contains(below) || MaterialUtil.isBed(below)) {
         return true;
      } else {
         return block == PORTAL ? true : !HOLLOW_MATERIALS.contains(block) || !HOLLOW_MATERIALS.contains(above);
      }
   }

   public static Location getRoundedDestination(final Location loc) {
      World world = loc.getWorld();
      int x = loc.getBlockX();
      int y = (int)Math.round(loc.getY());
      int z = loc.getBlockZ();
      return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
   }

   @Deprecated
   public static Location getSafeDestination(final IUser user, final Location loc) throws Exception {
      return getSafeDestination(null, user, loc);
   }

   public static Location getSafeDestination(final IEssentials ess, final IUser user, final Location loc) throws Exception {
      if (user.getBase().isOnline()
         && (ess == null || !ess.getSettings().isAlwaysTeleportSafety())
         && (user.getBase().getGameMode() == GameMode.CREATIVE || user.getBase().getGameMode() == GameMode.SPECTATOR || user.isGodModeEnabled())) {
         if (shouldFly(ess, loc) && user.getBase().getAllowFlight()) {
            user.getBase().setFlying(true);
         }

         return ess != null && !ess.getSettings().isTeleportToCenterLocation() ? loc : getRoundedDestination(loc);
      } else {
         return getSafeDestination(ess, loc);
      }
   }

   public static Location getSafeDestination(IEssentials ess, final Location loc) throws Exception {
      if (loc != null && loc.getWorld() != null) {
         WorldInfoProvider worldInfoProvider = ess.provider(WorldInfoProvider.class);
         World world = loc.getWorld();
         int worldMinY = worldInfoProvider.getMinHeight(world);
         int worldLogicalY = worldInfoProvider.getLogicalHeight(world);
         int worldMaxY = loc.getBlockY() < worldLogicalY ? worldLogicalY : worldInfoProvider.getMaxHeight(world);
         int x = loc.getBlockX();
         int y = (int)Math.round(loc.getY());
         int z = loc.getBlockZ();
         if (isBlockOutsideWorldBorder(world, x, z)) {
            x = getXInsideWorldBorder(world, x);
            z = getZInsideWorldBorder(world, z);
         }

         int origX = x;
         int origY = y;
         int origZ = z;

         while (isBlockAboveAir(ess, world, x, y, z)) {
            if (--y < 0) {
               y = origY;
               break;
            }
         }

         if (isBlockUnsafe(ess, world, x, y, z)) {
            x = Math.round(loc.getX()) == x ? x - 1 : x + 1;
            z = Math.round(loc.getZ()) == z ? z - 1 : z + 1;
         }

         for (int i = 0; isBlockUnsafe(ess, world, x, y, z); z = origZ + VOLUME[i].z) {
            if (++i >= VOLUME.length) {
               x = origX;
               y = NumberUtil.constrainToRange(origY + 3, worldMinY, worldMaxY);
               z = origZ;
               break;
            }

            x = origX + VOLUME[i].x;
            y = NumberUtil.constrainToRange(origY + VOLUME[i].y, worldMinY, worldMaxY);
         }

         while (isBlockUnsafe(ess, world, x, y, z)) {
            if (++y >= worldMaxY) {
               x++;
               break;
            }
         }

         while (isBlockUnsafe(ess, world, x, y, z)) {
            if (--y <= worldMinY + 1) {
               y = Math.min(world.getHighestBlockYAt(++x, z) + 1, worldMaxY);
               if (x - 48 > loc.getBlockX()) {
                  throw new TranslatableException("holeInFloor");
               }
            }
         }

         return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
      } else {
         throw new TranslatableException("destinationNotSet");
      }
   }

   public static boolean shouldFly(IEssentials ess, final Location loc) {
      WorldInfoProvider worldInfoProvider = ess.provider(WorldInfoProvider.class);
      World world = loc.getWorld();
      int x = loc.getBlockX();
      int y = (int)Math.round(loc.getY());
      int z = loc.getBlockZ();
      int count = 0;

      while (isBlockUnsafe(ess, world, x, y, z) && y >= worldInfoProvider.getMinHeight(world)) {
         y--;
         if (++count > 2) {
            return true;
         }
      }

      return y < worldInfoProvider.getMinHeight(world);
   }

   static {
      if (!Essentials.TESTING) {
         for (Material mat : Material.values()) {
            if (mat.isTransparent()) {
               HOLLOW_MATERIALS.add(mat);
            }
         }
      }

      TRANSPARENT_MATERIALS.addAll(HOLLOW_MATERIALS);
      TRANSPARENT_MATERIALS.addAll(WATER_TYPES);
      HOLLOW_MATERIALS.remove(Material.BARRIER);
      HOLLOW_MATERIALS.remove(PATH);
      HOLLOW_MATERIALS.remove(FARMLAND);
      if (LIGHT != null) {
         HOLLOW_MATERIALS.add(LIGHT);
      }

      List<LocationUtil.Vector3D> pos = new ArrayList<>();

      for (int x = -3; x <= 3; x++) {
         for (int y = -3; y <= 3; y++) {
            for (int z = -3; z <= 3; z++) {
               pos.add(new LocationUtil.Vector3D(x, y, z));
            }
         }
      }

      pos.sort(Comparator.comparingInt(a -> a.x * a.x + a.y * a.y + a.z * a.z));
      VOLUME = pos.toArray(new LocationUtil.Vector3D[0]);
   }

   public static class Vector3D {
      public final int x;
      public final int y;
      public final int z;

      Vector3D(final int x, final int y, final int z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}
