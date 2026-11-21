package com.magmaguy.elitemobs.magmacore.location.api;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public final class LocationOwnership {
   private static final Map<Class<?>, LocationOwnership.EntryMethods> methodCache = new ConcurrentHashMap<>();
   private static final Set<Object> warnedProtectionFailures = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   private LocationOwnership() {
   }

   public static void register(Plugin plugin, String namespace, Predicate<Location> ownsLocation) {
      register(plugin, new LocationOwnershipEntry(namespace, ownsLocation, null, null));
   }

   public static void register(Plugin plugin, String namespace, Predicate<Location> ownsLocation, Predicate<Location> protectionFn) {
      register(plugin, new LocationOwnershipEntry(namespace, ownsLocation, null, protectionFn));
   }

   public static void registerTyped(Plugin plugin, String namespace, Function<Location, Set<String>> kindsFn) {
      registerTyped(plugin, namespace, kindsFn, null);
   }

   public static void registerTyped(Plugin plugin, String namespace, Function<Location, Set<String>> kindsFn, Predicate<Location> protectionFn) {
      Predicate<Location> ownsFn = loc -> {
         Set<String> k = kindsFn.apply(loc);
         return k != null && !k.isEmpty();
      };
      register(plugin, new LocationOwnershipEntry(namespace, ownsFn, kindsFn, protectionFn));
   }

   public static void register(Plugin plugin, LocationOwnershipEntry entry) {
      Bukkit.getServicesManager().register(Predicate.class, entry, plugin, ServicePriority.Normal);
      Logger.info("Registered location ownership provider: " + entry.getNamespace() + " (plugin=" + plugin.getName() + ")");
   }

   public static Set<String> ownersAt(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         Set<String> owners = new HashSet<>();

         for (Object provider : ourProviders()) {
            LocationOwnership.EntryMethods m = methodsFor(provider.getClass());
            if (m != null) {
               try {
                  if ((Boolean)m.test.invoke(provider, loc)) {
                     owners.add((String)m.getNamespace.invoke(provider));
                  }
               } catch (ReflectiveOperationException var6) {
                  Logger.warn("LocationOwnership.ownersAt dispatch failed: " + var6.getMessage());
               }
            }
         }

         return owners;
      } else {
         return Collections.emptySet();
      }
   }

   public static boolean ownedBy(Location loc, String namespace) {
      if (loc != null && loc.getWorld() != null && namespace != null) {
         for (Object provider : ourProviders()) {
            LocationOwnership.EntryMethods m = methodsFor(provider.getClass());
            if (m != null) {
               try {
                  if (namespace.equals(m.getNamespace.invoke(provider)) && (Boolean)m.test.invoke(provider, loc)) {
                     return true;
                  }
               } catch (ReflectiveOperationException var6) {
                  Logger.warn("LocationOwnership.ownedBy dispatch failed: " + var6.getMessage());
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static Set<String> kindsAt(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         Set<String> result = new HashSet<>();

         for (Object provider : ourProviders()) {
            LocationOwnership.EntryMethods m = methodsFor(provider.getClass());
            if (m != null) {
               try {
                  if ((Boolean)m.test.invoke(provider, loc)) {
                     Set<String> kinds = (Set<String>)m.kindsAt.invoke(provider, loc);
                     if (kinds != null) {
                        for (String kind : kinds) {
                           if (kind != null && !kind.isBlank()) {
                              result.add(kind.toLowerCase(Locale.ROOT));
                           }
                        }
                     }
                  }
               } catch (ReflectiveOperationException var8) {
                  Logger.warn("LocationOwnership.kindsAt dispatch failed: " + var8.getMessage());
               }
            }
         }

         return result;
      } else {
         return Collections.emptySet();
      }
   }

   public static boolean hasKind(Location loc, String kind) {
      return kind == null ? false : kindsAt(loc).contains(kind.toLowerCase(Locale.ROOT));
   }

   public static boolean anyOwnerAt(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         for (Object provider : ourProviders()) {
            LocationOwnership.EntryMethods m = methodsFor(provider.getClass());
            if (m != null) {
               try {
                  if ((Boolean)m.test.invoke(provider, loc)) {
                     return true;
                  }
               } catch (ReflectiveOperationException var5) {
                  Logger.warn("LocationOwnership.anyOwnerAt dispatch failed: " + var5.getMessage());
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean anyProtectedOwnerAt(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         for (Object provider : ourProviders()) {
            LocationOwnership.EntryMethods m = methodsFor(provider.getClass());
            if (m != null && m.isProtectedAt != null) {
               try {
                  if ((Boolean)m.isProtectedAt.invoke(provider, loc)) {
                     return true;
                  }
               } catch (ReflectiveOperationException var5) {
                  if (warnedProtectionFailures.add(provider)) {
                     Logger.warn(
                        "LocationOwnership.anyProtectedOwnerAt dispatch failed: "
                           + var5.getMessage()
                           + "; further failures suppressed until provider replacement"
                     );
                  }

                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Collection<Object> ourProviders() {
      Collection<RegisteredServiceProvider<Predicate>> regs = Bukkit.getServicesManager().getRegistrations(Predicate.class);
      Set<Object> ours = new HashSet<>();

      for (RegisteredServiceProvider<Predicate> rsp : regs) {
         Object p = rsp.getProvider();
         if (p != null && "LocationOwnershipEntry".equals(p.getClass().getSimpleName())) {
            ours.add(p);
         }
      }

      return ours;
   }

   private static LocationOwnership.EntryMethods methodsFor(Class<?> cls) {
      LocationOwnership.EntryMethods cached = methodCache.get(cls);
      if (cached != null) {
         return cached;
      } else {
         try {
            Method protectedM = null;

            try {
               protectedM = cls.getMethod("isProtectedAt", Location.class);
            } catch (NoSuchMethodException var4) {
            }

            LocationOwnership.EntryMethods m = new LocationOwnership.EntryMethods(
               cls.getMethod("getNamespace"), cls.getMethod("test", Object.class), cls.getMethod("kindsAt", Location.class), protectedM
            );
            methodCache.put(cls, m);
            return m;
         } catch (NoSuchMethodException var5) {
            Logger.warn("LocationOwnership: registration with unexpected shape " + cls.getName() + " — missing " + var5.getMessage());
            return null;
         }
      }
   }

   private record EntryMethods(Method getNamespace, Method test, Method kindsAt, Method isProtectedAt) {
   }
}
