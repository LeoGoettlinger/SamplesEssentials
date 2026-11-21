package me.byteful.plugin.leveltools.profile.reward;

import me.byteful.plugin.leveltools.api.RewardType;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RewardEntry {
   private final RewardType type;
   private final String[] arguments;
   private final String rawString;

   public RewardEntry(@NotNull RewardType type, @NotNull String[] arguments, @NotNull String rawString) {
      this.type = type;
      this.arguments = (String[])arguments.clone();
      this.rawString = rawString;
   }

   @NotNull
   public RewardType getType() {
      return this.type;
   }

   @NotNull
   public String[] getArguments() {
      return (String[])this.arguments.clone();
   }

   @NotNull
   public String getRawString() {
      return this.rawString;
   }

   public void apply(@NotNull LevelToolsItem tool, @NotNull Player player) {
      this.type.apply(tool, this.arguments, player);
   }

   public boolean shouldUpdateItem() {
      return this.type.isShouldUpdate();
   }

   @Override
   public String toString() {
      return this.rawString;
   }
}
