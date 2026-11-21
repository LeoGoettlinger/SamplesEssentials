package org.lushplugins.lushrewards.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.exceptions.InvalidRewardException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.utils.SchedulerType;

public abstract class Reward implements Cloneable {
   private String message = null;
   private String broadcast = null;

   public Reward() {
   }

   public Reward(@Nullable String message, @Nullable String broadcast) {
      this.message = message;
      this.broadcast = broadcast;
   }

   public Reward(Map<?, ?> map) {
      if (map.containsKey("message")) {
         if (map.get("message") instanceof List) {
            this.message = String.join("\n", (List)map.get("message"));
         } else {
            this.message = (String)map.get("message");
         }
      }

      if (map.containsKey("broadcast")) {
         if (map.get("broadcast") instanceof List) {
            this.broadcast = String.join("\n", (List)map.get("broadcast"));
         } else {
            this.broadcast = (String)map.get("broadcast");
         }
      }
   }

   protected abstract void giveTo(Player var1);

   protected abstract SchedulerType getSchedulerType();

   public abstract Map<String, Object> asMap();

   public void giveReward(Player player) {
      switch (this.getSchedulerType()) {
         case ASYNC:
            LushRewards.getMorePaperLib().scheduling().asyncScheduler().run(() -> {
               try {
                  this.giveTo(player);
               } catch (Exception var3) {
                  LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + this + ") to " + player.getName());
                  var3.printStackTrace();
               }
            });
            break;
         case PLAYER:
            LushRewards.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
               try {
                  this.giveTo(player);
               } catch (Exception var3) {
                  LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + this + ") to " + player.getName());
                  var3.printStackTrace();
               }
            }, () -> {});
            break;
         case GLOBAL:
            LushRewards.getMorePaperLib().scheduling().globalRegionalScheduler().run(() -> {
               try {
                  this.giveTo(player);
               } catch (Exception var3) {
                  LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + this + ") to " + player.getName());
                  var3.printStackTrace();
               }
            });
            break;
         case REGION:
            LushRewards.getMorePaperLib().scheduling().regionSpecificScheduler(player.getLocation()).run(() -> {
               try {
                  this.giveTo(player);
               } catch (Exception var3) {
                  LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + this + ") to " + player.getName());
                  var3.printStackTrace();
               }
            });
      }

      LushRewards.getMorePaperLib().scheduling().asyncScheduler().run(() -> {
         if (this.message != null) {
            ChatColorHandler.sendMessage(player, this.message.replace("%player%", player.getDisplayName()));
         }

         if (this.broadcast != null) {
            ChatColorHandler.broadcastMessage(this.broadcast.replace("%player%", player.getDisplayName()));
         }
      });
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public void setBroadcast(String broadcast) {
      this.broadcast = broadcast;
   }

   public static Reward loadReward(ConfigurationSection configurationSection) {
      return loadReward(configurationSection.getValues(false), configurationSection.getCurrentPath());
   }

   @Nullable
   public static Reward loadReward(Map<?, ?> rewardMap, String path) {
      Optional<RewardManager> optionalManager = LushRewards.getInstance().getManager(RewardManager.class);
      if (optionalManager.isEmpty()) {
         return null;
      } else {
         RewardManager rewardManager = optionalManager.get();
         String rewardType = (String)rewardMap.get("type");
         if (rewardType.equalsIgnoreCase("template")) {
            String template = (String)rewardMap.get("template");
            return LushRewards.getInstance().getConfigManager().getRewardTemplate(template);
         } else if (!rewardManager.isRegistered(rewardType)) {
            LushRewards.getInstance().getLogger().severe("Invalid reward type at '" + path + "'");
            return null;
         } else {
            try {
               Reward.Constructor rewardConstructor = rewardManager.getConstructor(rewardType);
               return rewardConstructor != null ? rewardConstructor.build(rewardMap) : null;
            } catch (InvalidRewardException var6) {
               LushRewards.getInstance().getLogger().warning(var6.getMessage());
               return null;
            }
         }
      }
   }

   @Nullable
   public static List<Reward> loadRewards(List<Map<?, ?>> maps, String path) {
      List<Reward> rewardList = new ArrayList<>();

      for (Map<?, ?> map : maps) {
         Reward reward = loadReward(map, path);
         if (reward != null) {
            rewardList.add(reward);
         }
      }

      return !rewardList.isEmpty() ? rewardList : null;
   }

   public Reward clone() {
      try {
         return (Reward)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError();
      }
   }

   @FunctionalInterface
   public interface Constructor {
      Reward build(Map<?, ?> var1);
   }
}
