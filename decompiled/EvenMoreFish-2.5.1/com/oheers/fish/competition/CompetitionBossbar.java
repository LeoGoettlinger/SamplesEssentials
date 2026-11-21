package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class CompetitionBossbar {
   private BossBar bar;
   private boolean shouldShow = true;
   EMFMessage prefix;

   public CompetitionBossbar() {
      this.createBar();
   }

   public void setShouldShow(boolean shouldShow) {
      this.shouldShow = shouldShow;
   }

   public void timerUpdate(long timeLeft, long totalTime) {
      this.setTitle(timeLeft);
      this.setProgress(timeLeft, totalTime);
   }

   public void setProgress(long timeLeft, long totalTime) {
      float progress = (float)timeLeft / (float)totalTime;
      if (progress < 0.0F) {
         progress = 0.0F;
      } else if (progress > 1.0F) {
         progress = 1.0F;
      }

      this.bar.progress(progress);
   }

   public void setPrefix(EMFMessage prefix, CompetitionType type) {
      prefix.setVariable("{type}", type.getBossbarPrefix());
      this.prefix = prefix;
   }

   public void setColour(Color colour) {
      this.bar.color(colour);
   }

   public void setTitle(long timeLeft) {
      EMFMessage layoutMessage = ConfigMessage.BAR_LAYOUT.getMessage();
      layoutMessage.setVariable("{prefix}", this.prefix);
      layoutMessage.setVariable("{time-formatted}", FishUtils.timeFormat(timeLeft));
      layoutMessage.setVariable("{remaining}", ConfigMessage.BAR_REMAINING.getMessage());
      this.bar.name(layoutMessage.getComponentMessage());
   }

   public void show() {
      if (this.shouldShow) {
         Bukkit.getOnlinePlayers().forEach(this::addPlayer);
      }
   }

   public void hide() {
      Bukkit.getOnlinePlayers().forEach(this::removePlayer);
   }

   public void createBar() {
      Overlay barStyle = MainConfig.getInstance().getBarStyle();
      this.bar = BossBar.bossBar(Component.text("EMF BossBar"), 1.0F, Color.WHITE, barStyle);
   }

   public void removeAllPlayers() {
      this.bar.viewers().forEach(viewer -> {
         if (viewer instanceof Audience audience) {
            this.bar.removeViewer(audience);
         }
      });
   }

   private void addAllPlayers() {
      Bukkit.getOnlinePlayers().forEach(this.bar::addViewer);
   }

   public void addPlayer(@NonNull Player player) {
      if (this.shouldShow && !EvenMoreFish.getInstance().getToggle().isBossBarDisabled(player)) {
         this.bar.addViewer(player);
      }
   }

   public void removePlayer(@NonNull Player player) {
      this.bar.removeViewer(player);
   }
}
