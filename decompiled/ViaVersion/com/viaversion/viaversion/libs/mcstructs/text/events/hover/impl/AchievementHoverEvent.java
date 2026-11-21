package com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import lombok.Generated;

public class AchievementHoverEvent extends HoverEvent {
   private String statistic;

   public AchievementHoverEvent(String statistic) {
      super(HoverEventAction.SHOW_ACHIEVEMENT);
      this.statistic = statistic;
   }

   public String getStatistic() {
      return this.statistic;
   }

   public AchievementHoverEvent setStatistic(String statistic) {
      this.statistic = statistic;
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("statistic", this.statistic).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof AchievementHoverEvent)) {
         return false;
      } else {
         AchievementHoverEvent other = (AchievementHoverEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$statistic = this.getStatistic();
            Object other$statistic = other.getStatistic();
            return this$statistic == null ? other$statistic == null : this$statistic.equals(other$statistic);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof AchievementHoverEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $statistic = this.getStatistic();
      return result * 59 + ($statistic == null ? 43 : $statistic.hashCode());
   }
}
