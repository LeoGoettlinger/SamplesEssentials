package com.skyblockexp.ezrtp.protection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class WorldGuardProtectionProvider implements ProtectionProvider {
   private static final String GLOBAL_REGION_ID = "__global__";
   private final Logger logger;
   private boolean available;
   private Object regionContainer;
   private Method regionContainerGetMethod;
   private Method bukkitAdapterAdaptMethod;
   private Method blockVector3AtMethod;
   private boolean wg7;
   private Object wg6PluginInstance;
   private Method wg6GetRegionManagerMethod;

   public WorldGuardProtectionProvider(PluginManager pluginManager, Logger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
      Plugin plugin = pluginManager != null ? pluginManager.getPlugin("WorldGuard") : null;
      if (plugin != null && plugin.isEnabled()) {
         Object container = null;
         Method containerGet = null;
         Method adaptMethod = null;
         Method atMethod = null;
         boolean detectedWg7 = false;
         Object wg6Instance = null;
         Method wg6GetRegionManager = null;

         try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstance = findMethod(worldGuardClass, "getInstance", 0);
            Object wgInstance = getInstance.invoke(null);
            Method getPlatform = findMethod(wgInstance.getClass(), "getPlatform", 0);
            Object platform = getPlatform.invoke(wgInstance);
            Method getRegionContainer = findMethod(platform.getClass(), "getRegionContainer", 0);
            container = getRegionContainer.invoke(platform);
            if (container == null) {
               this.available = false;
               this.regionContainer = null;
               this.regionContainerGetMethod = null;
               this.bukkitAdapterAdaptMethod = null;
               this.blockVector3AtMethod = null;
               this.wg7 = false;
               this.wg6PluginInstance = null;
               this.wg6GetRegionManagerMethod = null;
               return;
            }

            containerGet = findMethod(container.getClass(), "get", 1);
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            adaptMethod = findMethod(bukkitAdapterClass, "adapt", 1);
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            atMethod = findMethod(blockVector3Class, "at", 3);
            detectedWg7 = true;
         } catch (ClassNotFoundException var20) {
            try {
               wg6Instance = plugin;
               wg6GetRegionManager = findMethod(plugin.getClass(), "getRegionManager", 1);
            } catch (RuntimeException var19) {
               logger.log(Level.WARNING, "WorldGuard/WorldEdit detected but reflection setup failed; treating as unavailable.", (Throwable)var19);
               this.available = false;
               this.regionContainer = null;
               this.regionContainerGetMethod = null;
               this.bukkitAdapterAdaptMethod = null;
               this.blockVector3AtMethod = null;
               this.wg7 = false;
               this.wg6PluginInstance = null;
               this.wg6GetRegionManagerMethod = null;
               return;
            }
         } catch (Exception var21) {
            logger.log(Level.WARNING, "WorldGuard/WorldEdit detected but reflection setup failed; treating as unavailable.", (Throwable)var21);
            this.available = false;
            this.regionContainer = null;
            this.regionContainerGetMethod = null;
            this.bukkitAdapterAdaptMethod = null;
            this.blockVector3AtMethod = null;
            this.wg7 = false;
            this.wg6PluginInstance = null;
            this.wg6GetRegionManagerMethod = null;
            return;
         }

         this.available = true;
         this.regionContainer = container;
         this.regionContainerGetMethod = containerGet;
         this.bukkitAdapterAdaptMethod = adaptMethod;
         this.blockVector3AtMethod = atMethod;
         this.wg7 = detectedWg7;
         this.wg6PluginInstance = wg6Instance;
         this.wg6GetRegionManagerMethod = wg6GetRegionManager;
      } else {
         this.available = false;
         this.regionContainer = null;
         this.regionContainerGetMethod = null;
         this.bukkitAdapterAdaptMethod = null;
         this.blockVector3AtMethod = null;
         this.wg7 = false;
         this.wg6PluginInstance = null;
         this.wg6GetRegionManagerMethod = null;
      }
   }

   @Override
   public String getId() {
      return "worldguard";
   }

   @Override
   public boolean isAvailable() {
      return this.available;
   }

   public Map<String, WorldGuardProtectionProvider.RegionBounds> getAllRegions(World world) {
      if (this.available && world != null) {
         Object manager = this.getRegionManagerForWorld(world);
         if (manager == null) {
            return Collections.emptyMap();
         } else {
            try {
               Method getRegions = findMethod(manager.getClass(), "getRegions", 0);
               if (getRegions.invoke(manager) instanceof Map<?, ?> regionsMap && !regionsMap.isEmpty()) {
                  Map<String, WorldGuardProtectionProvider.RegionBounds> output = new HashMap<>();

                  for (Entry<?, ?> entry : regionsMap.entrySet()) {
                     String regionId = null;
                     if (entry.getKey() instanceof String key) {
                        regionId = key;
                     } else if (entry.getValue() != null) {
                        Method getId = findMethod(entry.getValue().getClass(), "getId", 0);
                        if (getId.invoke(entry.getValue()) instanceof String idStr) {
                           regionId = idStr;
                        }
                     }

                     if (regionId != null && !"__global__".equalsIgnoreCase(regionId)) {
                        WorldGuardProtectionProvider.RegionBounds bounds = this.extractBounds(entry.getValue());
                        if (bounds != null) {
                           output.put(regionId.toLowerCase(Locale.ROOT), bounds);
                        }
                     }
                  }

                  return output;
               } else {
                  return Collections.emptyMap();
               }
            } catch (Exception var14) {
               this.logger.log(Level.WARNING, "Failed to query WorldGuard region list.", (Throwable)var14);
               return Collections.emptyMap();
            }
         }
      } else {
         return Collections.emptyMap();
      }
   }

   public Optional<WorldGuardProtectionProvider.RegionBounds> getRegionBounds(World world, String regionId) {
      if (regionId != null && !regionId.isBlank()) {
         Map<String, WorldGuardProtectionProvider.RegionBounds> regions = this.getAllRegions(world);
         return regions.isEmpty() ? Optional.empty() : Optional.ofNullable(regions.get(regionId.toLowerCase(Locale.ROOT)));
      } else {
         return Optional.empty();
      }
   }

   @Override
   public boolean isLocationProtected(Location location) {
      if (this.available && location != null) {
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else if (this.wg7) {
            Object manager = this.getRegionManager(world);
            if (manager == null) {
               return false;
            } else {
               try {
                  Object vector = this.blockVector3AtMethod.invoke(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                  Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 1);
                  Object regions = getApplicable.invoke(manager, vector);
                  if (regions == null) {
                     return false;
                  } else {
                     Method iteratorMethod = findMethod(regions.getClass(), "iterator", 0);
                     Iterator<?> it = (Iterator<?>)iteratorMethod.invoke(regions);

                     while (it.hasNext()) {
                        Object region = it.next();
                        Method getId = findMethod(region.getClass(), "getId", 0);
                        if (getId.invoke(region) instanceof String id && !"__global__".equalsIgnoreCase(id)) {
                           return true;
                        }
                     }

                     return false;
                  }
               } catch (Exception var14) {
                  this.logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", (Throwable)var14);
                  return false;
               }
            }
         } else if (this.wg6PluginInstance != null && this.wg6GetRegionManagerMethod != null) {
            try {
               Object manager = this.wg6GetRegionManagerMethod.invoke(this.wg6PluginInstance, world);
               if (manager == null) {
                  return false;
               } else {
                  try {
                     Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 1);
                     Object regions = getApplicable.invoke(manager, location);
                     if (regions == null) {
                        return false;
                     } else {
                        Method iteratorMethod = findMethod(regions.getClass(), "iterator", 0);
                        Iterator<?> it = (Iterator<?>)iteratorMethod.invoke(regions);

                        while (it.hasNext()) {
                           Object region = it.next();
                           Method getId = findMethod(region.getClass(), "getId", 0);
                           if (getId.invoke(region) instanceof String id && !"__global__".equalsIgnoreCase(id)) {
                              return true;
                           }
                        }

                        return false;
                     }
                  } catch (Exception var15) {
                     try {
                        Method getApplicablex = findMethod(manager.getClass(), "getApplicableRegions", 3);
                        Object regionsx = getApplicablex.invoke(manager, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        if (regionsx == null) {
                           return false;
                        } else {
                           Method iteratorMethod = findMethod(regionsx.getClass(), "iterator", 0);
                           Iterator<?> it = (Iterator<?>)iteratorMethod.invoke(regionsx);

                           while (it.hasNext()) {
                              Object region = it.next();
                              Method getId = findMethod(region.getClass(), "getId", 0);
                              if (getId.invoke(region) instanceof String id && !"__global__".equalsIgnoreCase(id)) {
                                 return true;
                              }
                           }

                           return false;
                        }
                     } catch (Exception var13) {
                        this.logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", (Throwable)var13);
                        return false;
                     }
                  }
               }
            } catch (InvocationTargetException | IllegalAccessException var16) {
               this.logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", (Throwable)var16);
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private Object getRegionManagerForWorld(World world) {
      if (this.wg7) {
         return this.getRegionManager(world);
      } else if (this.wg6PluginInstance != null && this.wg6GetRegionManagerMethod != null) {
         try {
            return this.wg6GetRegionManagerMethod.invoke(this.wg6PluginInstance, world);
         } catch (InvocationTargetException | IllegalAccessException var3) {
            this.logger.log(Level.WARNING, "Unable to access WorldGuard region manager for world " + world.getName(), (Throwable)var3);
            return null;
         }
      } else {
         return null;
      }
   }

   private WorldGuardProtectionProvider.RegionBounds extractBounds(Object region) {
      if (region == null) {
         return null;
      } else {
         try {
            Method getMinimumPoint = findMethod(region.getClass(), "getMinimumPoint", 0);
            Method getMaximumPoint = findMethod(region.getClass(), "getMaximumPoint", 0);
            Object minimum = getMinimumPoint.invoke(region);
            Object maximum = getMaximumPoint.invoke(region);
            if (minimum != null && maximum != null) {
               int minX = this.extractVectorAxis(minimum, "X");
               int minZ = this.extractVectorAxis(minimum, "Z");
               int maxX = this.extractVectorAxis(maximum, "X");
               int maxZ = this.extractVectorAxis(maximum, "Z");
               return new WorldGuardProtectionProvider.RegionBounds(Math.min(minX, maxX), Math.max(minX, maxX), Math.min(minZ, maxZ), Math.max(minZ, maxZ));
            } else {
               return null;
            }
         } catch (Exception var10) {
            return null;
         }
      }
   }

   private int extractVectorAxis(Object vector, String axis) throws InvocationTargetException, IllegalAccessException {
      try {
         Method getBlockAxis = findMethod(vector.getClass(), "getBlock" + axis, 0);
         if (getBlockAxis.invoke(vector) instanceof Number number) {
            return number.intValue();
         }
      } catch (RuntimeException var6) {
      }

      Method getAxis = findMethod(vector.getClass(), "get" + axis, 0);
      if (getAxis.invoke(vector) instanceof Number number) {
         return number.intValue();
      } else {
         throw new RuntimeException("Unsupported vector axis getter: " + vector.getClass().getName() + "." + axis);
      }
   }

   private Object getRegionManager(World world) {
      if (!this.wg7) {
         return null;
      } else {
         try {
            Object adapted = this.bukkitAdapterAdaptMethod.invoke(null, world);
            return this.regionContainerGetMethod.invoke(this.regionContainer, adapted);
         } catch (InvocationTargetException | IllegalAccessException var3) {
            this.logger.log(Level.WARNING, "Unable to access WorldGuard region manager for world " + world.getName(), (Throwable)var3);
            return null;
         }
      }
   }

   private static Method findMethod(Class<?> cls, String name, int paramCount) {
      for (Method m : cls.getMethods()) {
         if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
            return m;
         }
      }

      throw new RuntimeException("Method not found: " + cls.getName() + "." + name + "(" + paramCount + " params)");
   }

   public record RegionBounds(int minX, int maxX, int minZ, int maxZ) {
      public int centerX() {
         return this.minX + (this.maxX - this.minX) / 2;
      }

      public int centerZ() {
         return this.minZ + (this.maxZ - this.minZ) / 2;
      }

      public int maxRadiusInsideRegion() {
         int halfWidth = Math.max(1, (this.maxX - this.minX) / 2);
         int halfLength = Math.max(1, (this.maxZ - this.minZ) / 2);
         return Math.max(1, Math.min(halfWidth, halfLength));
      }
   }
}
