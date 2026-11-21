package com.becerritoo.GravesX.integration;

import com.ranull.graves.Graves;
import java.lang.reflect.Method;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BagOfGoldIntegration {
   private static final String BAGOFGOLD_PLUGIN = "BagOfGold";
   private static final double EPSILON = 1.0E-9;
   private static final int FLOOR_SCALE = 5;
   private final Graves plugin;
   @Nullable
   private BagOfGoldIntegration.RewardReflectionBridge bridge;
   private boolean warnedMissingPlugin;
   private boolean warnedReflectionFailure;
   private boolean loggedHook;

   public BagOfGoldIntegration(@NotNull Graves plugin) {
      this.plugin = plugin;
   }

   public void splitPhysicalMoney(
      @NotNull LivingEntity livingEntity,
      @Nullable List<String> permissionList,
      @NotNull List<ItemStack> graveItems,
      @NotNull List<ItemStack> ignoredItems,
      boolean preserveSlots
   ) {
      if (livingEntity instanceof Player) {
         if (!graveItems.isEmpty()) {
            if (this.isIntegrationEnabled()) {
               ConfigurationSection section = this.plugin.getConfigManager().getConfigSection("physical-money.bagofgold.enabled", livingEntity, permissionList);
               if (section != null) {
                  if (section.getBoolean("physical-money.bagofgold.enabled", true)) {
                     if (this.isAllowedByDeathScope(livingEntity, section.getString("physical-money.bagofgold.apply-on", "pvp_and_pve"))) {
                        BagOfGoldIntegration.RewardReflectionBridge resolved = this.resolveBridge();
                        if (resolved != null) {
                           double dropPercent = this.getDropPercent(section);
                           if (!(dropPercent <= 0.0)) {
                              double totalMoney = this.computeTotalPhysicalMoney(graveItems, resolved);
                              if (!(totalMoney <= 0.0)) {
                                 double targetDropValue = floorToScale(totalMoney * (dropPercent / 100.0), 5);
                                 if (!(targetDropValue <= 0.0)) {
                                    double moved = 0.0;

                                    for (int i = 0; i < graveItems.size() && moved + 1.0E-9 < targetDropValue; i++) {
                                       ItemStack stack = graveItems.get(i);
                                       if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                                          double unitValue = resolved.getUnitMoney(stack);
                                          if (!(unitValue <= 0.0)) {
                                             double remainingTarget = targetDropValue - moved;
                                             if (remainingTarget > 1.0E-9 && stack.getAmount() == 1) {
                                                int maxTakeForSingle = (int)Math.floor((remainingTarget + 1.0E-9) / unitValue);
                                                if (maxTakeForSingle <= 0) {
                                                   double dropValue = floorToScale(Math.min(remainingTarget, unitValue), 5);
                                                   if (dropValue > 1.0E-9 && dropValue + 1.0E-9 < unitValue) {
                                                      ItemStack droppedPartial = resolved.rewriteMoney(stack, dropValue);
                                                      ItemStack keptPartial = resolved.rewriteMoney(stack, floorToScale(unitValue - dropValue, 5));
                                                      if (droppedPartial != null && keptPartial != null) {
                                                         ignoredItems.add(droppedPartial);
                                                         graveItems.set(i, keptPartial);
                                                         moved += dropValue;
                                                         continue;
                                                      }
                                                   }
                                                }
                                             }

                                             int maxTake = (int)Math.floor((targetDropValue - moved + 1.0E-9) / unitValue);
                                             if (maxTake > 0) {
                                                int available = stack.getAmount();
                                                int take = Math.min(available, maxTake);
                                                if (take > 0) {
                                                   if (take >= available) {
                                                      ItemStack dropped = stack.clone();
                                                      ignoredItems.add(dropped);
                                                      if (preserveSlots) {
                                                         graveItems.set(i, null);
                                                      } else {
                                                         graveItems.remove(i);
                                                         i--;
                                                      }
                                                   } else {
                                                      ItemStack dropped = stack.clone();
                                                      dropped.setAmount(take);
                                                      ignoredItems.add(dropped);
                                                      ItemStack remaining = stack.clone();
                                                      remaining.setAmount(available - take);
                                                      graveItems.set(i, remaining);
                                                   }

                                                   moved += unitValue * take;
                                                }
                                             }
                                          }
                                       }
                                    }

                                    boolean forceMinimumDrop = section.getBoolean("physical-money.bagofgold.force-minimum-drop-item", true);
                                    if (forceMinimumDrop && targetDropValue > 0.0 && moved <= 1.0E-9) {
                                       moved += this.forceSingleUnitDrop(graveItems, ignoredItems, preserveSlots, resolved);
                                    }

                                    if (moved > 0.0) {
                                       this.plugin
                                          .debugMessage(
                                             "BagOfGold physical split applied: moved "
                                                + floorToScale(moved, 5)
                                                + " value to ground drops ("
                                                + dropPercent
                                                + "% target).",
                                             1
                                          );
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isIntegrationEnabled() {
      FileConfiguration config = this.plugin.getConfig();
      if (config == null) {
         return true;
      } else {
         String path = "settings.integration.bagofgold.enabled";
         return !config.contains(path) || config.getBoolean(path, true);
      }
   }

   private double forceSingleUnitDrop(
      @NotNull List<ItemStack> graveItems,
      @NotNull List<ItemStack> ignoredItems,
      boolean preserveSlots,
      @NotNull BagOfGoldIntegration.RewardReflectionBridge resolved
   ) {
      int bestIndex = -1;
      double bestUnitValue = Double.MAX_VALUE;

      for (int i = 0; i < graveItems.size(); i++) {
         ItemStack stack = graveItems.get(i);
         if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
            double unitValue = resolved.getUnitMoney(stack);
            if (!(unitValue <= 0.0) && unitValue < bestUnitValue) {
               bestUnitValue = unitValue;
               bestIndex = i;
            }
         }
      }

      if (bestIndex < 0) {
         return 0.0;
      } else {
         ItemStack stack = graveItems.get(bestIndex);
         if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
            ItemStack dropped = stack.clone();
            dropped.setAmount(1);
            ignoredItems.add(dropped);
            if (stack.getAmount() <= 1) {
               if (preserveSlots) {
                  graveItems.set(bestIndex, null);
               } else {
                  graveItems.remove(bestIndex);
               }
            } else {
               ItemStack remaining = stack.clone();
               remaining.setAmount(stack.getAmount() - 1);
               graveItems.set(bestIndex, remaining);
            }

            return bestUnitValue;
         } else {
            return 0.0;
         }
      }
   }

   private boolean isAllowedByDeathScope(@NotNull LivingEntity livingEntity, @Nullable String modeRaw) {
      String mode = modeRaw == null ? "pvp_and_pve" : modeRaw.trim().toLowerCase();
      boolean pvp = livingEntity.getKiller() != null;

      return switch (mode) {
         case "pvp_only" -> pvp;
         case "pve_only" -> !pvp;
         default -> true;
      };
   }

   private double getDropPercent(@NotNull ConfigurationSection section) {
      String dropPath = "physical-money.bagofgold.drop-on-ground-percent";
      String keepPath = "physical-money.bagofgold.keep-in-grave-percent";
      double drop = section.contains("physical-money.bagofgold.drop-on-ground-percent")
         ? section.getDouble("physical-money.bagofgold.drop-on-ground-percent", 30.0)
         : 100.0 - section.getDouble("physical-money.bagofgold.keep-in-grave-percent", 70.0);
      if (!Double.isFinite(drop)) {
         return 0.0;
      } else if (drop < 0.0) {
         return 0.0;
      } else {
         return drop > 100.0 ? 100.0 : drop;
      }
   }

   private double computeTotalPhysicalMoney(@NotNull List<ItemStack> items, @NotNull BagOfGoldIntegration.RewardReflectionBridge resolved) {
      double total = 0.0;

      for (ItemStack item : items) {
         if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
            double unit = resolved.getUnitMoney(item);
            if (!(unit <= 0.0)) {
               total += unit * item.getAmount();
            }
         }
      }

      return floorToScale(total, 5);
   }

   @Nullable
   private BagOfGoldIntegration.RewardReflectionBridge resolveBridge() {
      if (this.bridge != null) {
         return this.bridge;
      } else {
         Plugin bag = this.plugin.getServer().getPluginManager().getPlugin("BagOfGold");
         if (bag != null && bag.isEnabled()) {
            try {
               this.bridge = new BagOfGoldIntegration.RewardReflectionBridge();
               if (!this.loggedHook) {
                  this.loggedHook = true;
                  this.plugin.integrationMessage("Hooked into BagOfGold physical item metadata via CustomItemsLib Reward reflection.");
               }

               return this.bridge;
            } catch (Throwable var3) {
               if (!this.warnedReflectionFailure) {
                  this.warnedReflectionFailure = true;
                  this.plugin.integrationMessage("Failed to initialize BagOfGold physical-money reflection bridge: " + var3.getMessage(), "warn");
               }

               return null;
            }
         } else {
            if (!this.warnedMissingPlugin) {
               this.warnedMissingPlugin = true;
               this.plugin.debugMessage("BagOfGold physical-money split is enabled, but BagOfGold is not loaded. Skipping split.", 2);
            }

            return null;
         }
      }
   }

   private static double floorToScale(double value, int scale) {
      if (!Double.isFinite(value)) {
         return 0.0;
      } else {
         double factor = Math.pow(10.0, scale);
         return Math.floor(value * factor) / factor;
      }
   }

   private static final class RewardReflectionBridge {
      private final Method rewardIsReward;
      private final Method rewardGetReward;
      private final Method rewardIsMoney;
      private final Method rewardGetMoney;
      private final Method rewardSetMoney;
      private final Method rewardSetDisplayAndLore;
      @Nullable
      private final Method rewardCheckHash;

      private RewardReflectionBridge() throws ClassNotFoundException, NoSuchMethodException {
         Class<?> rewardClass = Class.forName("one.lindegaard.CustomItemsLib.rewards.Reward");
         this.rewardIsReward = rewardClass.getMethod("isReward", ItemStack.class);
         this.rewardGetReward = rewardClass.getMethod("getReward", ItemStack.class);
         this.rewardIsMoney = rewardClass.getMethod("isMoney");
         this.rewardGetMoney = rewardClass.getMethod("getMoney");
         this.rewardSetMoney = rewardClass.getMethod("setMoney", double.class);
         this.rewardSetDisplayAndLore = rewardClass.getMethod("setDisplayNameAndHiddenLores", ItemStack.class, rewardClass);

         Method checkHash;
         try {
            checkHash = rewardClass.getMethod("checkHash");
         } catch (NoSuchMethodException var4) {
            checkHash = null;
         }

         this.rewardCheckHash = checkHash;
      }

      private double getUnitMoney(@Nullable ItemStack itemStack) {
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            try {
               Object isReward = this.rewardIsReward.invoke(null, itemStack);
               if (isReward instanceof Boolean && (Boolean)isReward) {
                  Object rewardObj = this.rewardGetReward.invoke(null, itemStack);
                  if (rewardObj == null) {
                     return 0.0;
                  } else {
                     if (this.rewardCheckHash != null) {
                        Object checkHash = this.rewardCheckHash.invoke(rewardObj);
                        if (!(checkHash instanceof Boolean) || !(Boolean)checkHash) {
                           return 0.0;
                        }
                     }

                     Object isMoney = this.rewardIsMoney.invoke(rewardObj);
                     if (isMoney instanceof Boolean && (Boolean)isMoney) {
                        Object money = this.rewardGetMoney.invoke(rewardObj);
                        if (!(money instanceof Number)) {
                           return 0.0;
                        } else {
                           double unitValue = ((Number)money).doubleValue();
                           return Double.isFinite(unitValue) && unitValue > 0.0 ? unitValue : 0.0;
                        }
                     } else {
                        return 0.0;
                     }
                  }
               } else {
                  return 0.0;
               }
            } catch (Throwable var8) {
               return 0.0;
            }
         } else {
            return 0.0;
         }
      }

      @Nullable
      private ItemStack rewriteMoney(@Nullable ItemStack base, double newMoney) {
         if (base == null || base.getType() == Material.AIR) {
            return null;
         } else if (Double.isFinite(newMoney) && !(newMoney <= 0.0)) {
            try {
               Object isReward = this.rewardIsReward.invoke(null, base);
               if (isReward instanceof Boolean && (Boolean)isReward) {
                  Object rewardObj = this.rewardGetReward.invoke(null, base);
                  if (rewardObj == null) {
                     return null;
                  } else {
                     Object isMoney = this.rewardIsMoney.invoke(rewardObj);
                     if (isMoney instanceof Boolean && (Boolean)isMoney) {
                        this.rewardSetMoney.invoke(rewardObj, newMoney);
                        ItemStack rewritten = (ItemStack)this.rewardSetDisplayAndLore.invoke(null, base.clone(), rewardObj);
                        if (rewritten != null && rewritten.getType() != Material.AIR) {
                           rewritten.setAmount(1);
                           return rewritten;
                        } else {
                           return null;
                        }
                     } else {
                        return null;
                     }
                  }
               } else {
                  return null;
               }
            } catch (Throwable var8) {
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
