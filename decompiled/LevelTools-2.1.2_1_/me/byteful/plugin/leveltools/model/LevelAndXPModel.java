package me.byteful.plugin.leveltools.model;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.jetbrains.annotations.NotNull;

public class LevelAndXPModel {
   private final int level;
   private final double xp;

   public LevelAndXPModel(int level, double xp) {
      this.level = level;
      this.xp = LevelToolsUtil.round(xp, 1);
   }

   @NotNull
   public static LevelAndXPModel fromItem(@NotNull LevelToolsItem item) {
      return new LevelAndXPModel(item.getLevel(), item.getXp());
   }

   public int getLevel() {
      return this.level;
   }

   public double getXp() {
      return this.xp;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LevelAndXPModel that = (LevelAndXPModel)o;
         return this.level == that.level && Double.compare(that.xp, this.xp) == 0;
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return "LevelAndXPModel{level=" + this.level + ", xp=" + this.xp + '}';
   }
}
