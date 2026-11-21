package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.manager.ItemResetHandler;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.bukkit.utils.reflection.PaperUtils;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class BukkitItemResetHandler implements ItemResetHandler {
   private static final Consumer<Player> resetItemUsage;
   private static final Predicate<Player> isUsingItem;
   private static final Function<Player, InteractionHand> getItemUsageHand;

   @Override
   public void resetItemUsage(@Nullable PlatformPlayer player) {
      if (player != null) {
         resetItemUsage.accept((Player)player.getNative());
      }
   }

   @Nullable
   @Override
   public InteractionHand getItemUsageHand(@Nullable PlatformPlayer player) {
      return player == null ? null : getItemUsageHand.apply((Player)player.getNative());
   }

   @Override
   public boolean isUsingItem(@Nullable PlatformPlayer player) {
      return player != null && isUsingItem.test((Player)player.getNative());
   }

   static {
      ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
      boolean legacy = version.isOlderThanOrEquals(ServerVersion.V_1_8_8);

      try {
         Class<?> CraftLivingEntity = ReflectionUtils.getClass("org.bukkit.craftbukkit.entity.CraftLivingEntity");
         Method getHandle;
         String nmsPackage;
         if (CraftLivingEntity != null) {
            getHandle = CraftLivingEntity.getMethod("getHandle");
            nmsPackage = null;
         } else {
            nmsPackage = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
            String className = legacy ? "CraftHumanEntity" : "CraftLivingEntity";
            getHandle = Class.forName("org.bukkit.craftbukkit." + nmsPackage + ".entity." + className).getMethod("getHandle");
         }

         boolean obfuscated = nmsPackage != null;
         Class<?> clazz = getHandle.getReturnType();
         if (version.isNewerThanOrEquals(ServerVersion.V_1_10)) {
            isUsingItem = HumanEntity::isHandRaised;
         } else {
            String method = Objects.requireNonNull(nmsPackage);

            Method method = clazz.getMethod(switch (method) {
               case "v1_8_R3" -> "bS";
               case "v1_9_R1" -> "cs";
               case "v1_9_R2" -> "ct";
               default -> throw new IllegalStateException("You are using an unsupported server version! (" + version.getReleaseName() + ")");
            });
            isUsingItem = player -> {
               try {
                  return (Boolean)method.invoke(getHandle.invoke(player));
               } catch (InvocationTargetException | IllegalAccessException var4x) {
                  throw new RuntimeException(var4x);
               }
            };
         }

         if (legacy) {
            getItemUsageHand = player -> isUsingItem.test(player) ? InteractionHand.MAIN_HAND : null;
         } else if (PaperUtils.PAPER && version.isNewerThanOrEquals(ServerVersion.V_1_16_5)) {
            getItemUsageHand = player -> player.isHandRaised()
               ? (player.getHandRaised() == EquipmentSlot.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)
               : null;
         } else {
            String var20;
            if (nmsPackage != null) {
               String var16 = Objects.requireNonNull(nmsPackage);
               switch (var16) {
                  case "v1_9_R1":
                     var20 = "ct";
                     break;
                  case "v1_9_R2":
                     var20 = "cu";
                     break;
                  case "v1_10_R1":
                     var20 = "cy";
                     break;
                  case "v1_11_R1":
                     var20 = "cz";
                     break;
                  case "v1_12_R1":
                     var20 = "cH";
                     break;
                  case "v1_13_R1":
                  case "v1_13_R2":
                  case "v1_14_R1":
                     var20 = "cU";
                     break;
                  case "v1_15_R1":
                  case "v1_16_R1":
                  case "v1_16_R2":
                  case "v1_16_R3":
                  case "v1_17_R1":
                     var20 = "getRaisedHand";
                     break;
                  case "v1_18_R1":
                     var20 = "eM";
                     break;
                  case "v1_18_R2":
                     var20 = "eN";
                     break;
                  case "v1_19_R1":
                     var20 = "eU";
                     break;
                  case "v1_19_R2":
                     var20 = "fa";
                     break;
                  case "v1_19_R3":
                     var20 = "ff";
                     break;
                  case "v1_20_R1":
                     var20 = "fj";
                     break;
                  case "v1_20_R2":
                     var20 = "fn";
                     break;
                  case "v1_20_R3":
                     var20 = "fo";
                     break;
                  case "v1_20_R4":
                     var20 = "fw";
                     break;
                  case "v1_21_R1":
                     var20 = "fs";
                     break;
                  case "v1_21_R2":
                  case "v1_21_R3":
                  case "v1_21_R4":
                     var20 = "fA";
                     break;
                  case "v1_21_R5":
                     var20 = "fH";
                     break;
                  case "v1_21_R6":
                     var20 = "fP";
                     break;
                  case "v1_21_R7":
                     var20 = "ga";
                     break;
                  default:
                     throw new IllegalStateException("You are using an unsupported server version! (" + version.getReleaseName() + ")");
               }
            } else {
               var20 = "getUsedItemHand";
            }

            Method method = clazz.getMethod(var20);
            getItemUsageHand = player -> {
               try {
                  return isUsingItem.test(player)
                     ? (((Enum)method.invoke(getHandle.invoke(player))).ordinal() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)
                     : null;
               } catch (InvocationTargetException | IllegalAccessException var4x) {
                  throw new RuntimeException(var4x);
               }
            };
         }

         Method setLivingEntityFlag;
         if (version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            String name = obfuscated ? "c" : "setLivingEntityFlag";
            setLivingEntityFlag = clazz.getDeclaredMethod(name, int.class, boolean.class);
            setLivingEntityFlag.setAccessible(true);
         } else {
            setLivingEntityFlag = null;
         }

         if (PaperUtils.PAPER && version.isNewerThan(ServerVersion.V_1_17)) {
            resetItemUsage = setLivingEntityFlag == null ? LivingEntity::clearActiveItem : player -> {
               try {
                  setLivingEntityFlag.invoke(getHandle.invoke(player), 1, false);
               } catch (InvocationTargetException | IllegalAccessException var4x) {
                  throw new RuntimeException(var4x);
               }

               player.clearActiveItem();
            };
         } else {
            String var21;
            if (obfuscated) {
               switch (nmsPackage) {
                  case "v1_8_R3":
                     var21 = "bV";
                     break;
                  case "v1_9_R1":
                     var21 = "cz";
                     break;
                  case "v1_9_R2":
                     var21 = "cA";
                     break;
                  case "v1_10_R1":
                     var21 = "cE";
                     break;
                  case "v1_11_R1":
                     var21 = "cF";
                     break;
                  case "v1_12_R1":
                     var21 = "cN";
                     break;
                  case "v1_13_R1":
                  case "v1_13_R2":
                     var21 = "da";
                     break;
                  case "v1_14_R1":
                     var21 = "dp";
                     break;
                  case "v1_15_R1":
                     var21 = "dH";
                     break;
                  case "v1_16_R1":
                  case "v1_16_R2":
                  case "v1_16_R3":
                  case "v1_17_R1":
                     var21 = "clearActiveItem";
                     break;
                  case "v1_18_R1":
                     var21 = "eR";
                     break;
                  case "v1_18_R2":
                     var21 = "eS";
                     break;
                  case "v1_19_R1":
                     var21 = "eZ";
                     break;
                  case "v1_19_R2":
                     var21 = "ff";
                     break;
                  case "v1_19_R3":
                     var21 = "fk";
                     break;
                  case "v1_20_R1":
                     var21 = "fo";
                     break;
                  case "v1_20_R2":
                     var21 = "fs";
                     break;
                  case "v1_20_R3":
                     var21 = "ft";
                     break;
                  case "v1_20_R4":
                     var21 = "fB";
                     break;
                  case "v1_21_R1":
                     var21 = "fx";
                     break;
                  case "v1_21_R2":
                  case "v1_21_R3":
                  case "v1_21_R4":
                     var21 = "fF";
                     break;
                  case "v1_21_R5":
                     var21 = "fM";
                     break;
                  case "v1_21_R6":
                     var21 = "fU";
                     break;
                  case "v1_21_R7":
                     var21 = "gf";
                     break;
                  default:
                     throw new IllegalStateException("You are using an unsupported server version! (" + version.getReleaseName() + ")");
               }
            } else {
               var21 = "stopUsingItem";
            }

            Method method = clazz.getMethod(var21);
            if (legacy) {
               resetItemUsage = player -> {
                  try {
                     method.invoke(getHandle.invoke(player));
                     if (isUsingItem.test(player)) {
                        player.updateInventory();
                     }
                  } catch (InvocationTargetException | IllegalAccessException var4x) {
                     throw new RuntimeException(var4x);
                  }
               };
            } else if (setLivingEntityFlag == null) {
               resetItemUsage = player -> {
                  try {
                     method.invoke(getHandle.invoke(player));
                  } catch (InvocationTargetException | IllegalAccessException var4x) {
                     throw new RuntimeException(var4x);
                  }
               };
            } else {
               resetItemUsage = player -> {
                  try {
                     Object handle = getHandle.invoke(player);
                     setLivingEntityFlag.invoke(handle, 1, false);
                     method.invoke(handle);
                  } catch (InvocationTargetException | IllegalAccessException var5x) {
                     throw new RuntimeException(var5x);
                  }
               };
            }
         }
      } catch (Throwable var11) {
         throw var11 instanceof RuntimeException e ? e : new RuntimeException(var11);
      }
   }
}
