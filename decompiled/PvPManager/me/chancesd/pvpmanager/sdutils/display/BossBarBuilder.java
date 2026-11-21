package me.chancesd.pvpmanager.sdutils.display;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class BossBarBuilder {
   private String barTitle = "";
   private BarColor barColor;
   private BarStyle barStyle;
   private BarFlag[] barFlags;

   public static BossBarBuilder create() {
      return new BossBarBuilder();
   }

   public BossBarBuilder title(String title) {
      this.barTitle = title;
      return this;
   }

   public BossBarBuilder barColor(BarColor color) {
      this.barColor = color;
      return this;
   }

   public BossBarBuilder barStyle(BarStyle style) {
      this.barStyle = style;
      return this;
   }

   public BossBarBuilder barFlags(BarFlag... flags) {
      this.barFlags = flags;
      return this;
   }

   public BossBar build() {
      return this.barFlags != null
         ? Bukkit.createBossBar(this.barTitle, this.barColor, this.barStyle, this.barFlags)
         : Bukkit.createBossBar(this.barTitle, this.barColor, this.barStyle, new BarFlag[0]);
   }
}
