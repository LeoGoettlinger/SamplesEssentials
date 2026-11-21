package su.nightexpress.nightcore.bridge.spigot.bossbar;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.bossbar.NightBarColor;
import su.nightexpress.nightcore.bridge.bossbar.NightBarFlag;
import su.nightexpress.nightcore.bridge.bossbar.NightBarOverlay;
import su.nightexpress.nightcore.bridge.bossbar.NightBossBar;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public class SpigotBossBar implements NightBossBar {
   private final BossBar backend;

   public SpigotBossBar(@NonNull BossBar backend) {
      this.backend = backend;
   }

   @NonNull
   public SpigotBossBar setName(@NonNull NightComponent name) {
      this.backend.setTitle(name.toLegacy());
      return this;
   }

   @Override
   public float getProgress() {
      return (float)this.backend.getProgress();
   }

   @NonNull
   public SpigotBossBar setProgress(float progress) {
      this.backend.setProgress(Math.clamp(progress, 0.0F, 1.0F));
      return this;
   }

   @NonNull
   @Override
   public NightBarColor getColor() {
      return SpigotBossBarAdapter.wrapColor(this.backend.getColor());
   }

   @NonNull
   public SpigotBossBar setColor(@NonNull NightBarColor color) {
      this.backend.setColor(SpigotBossBarAdapter.adaptColor(color));
      return this;
   }

   @NonNull
   @Override
   public NightBarOverlay getOverlay() {
      return SpigotBossBarAdapter.wrapOverlay(this.backend.getStyle());
   }

   @NonNull
   public SpigotBossBar setOverlay(@NonNull NightBarOverlay overlay) {
      this.backend.setStyle(SpigotBossBarAdapter.adaptOverlay(overlay));
      return this;
   }

   @NonNull
   @Override
   public Set<NightBarFlag> getFlags() {
      Set<NightBarFlag> flags = new HashSet<>();

      for (BarFlag value : BarFlag.values()) {
         if (this.backend.hasFlag(value)) {
            flags.add(SpigotBossBarAdapter.wrapFlag(value));
         }
      }

      return flags;
   }

   @NonNull
   public SpigotBossBar setFlags(@NonNull Set<NightBarFlag> flags) {
      flags.forEach(nightFlag -> this.backend.addFlag(SpigotBossBarAdapter.adaptFlag(nightFlag)));
      return this;
   }

   @Override
   public boolean hasFlag(@NonNull NightBarFlag flag) {
      return this.backend.hasFlag(SpigotBossBarAdapter.adaptFlag(flag));
   }

   @NonNull
   public SpigotBossBar addFlag(@NonNull NightBarFlag flag) {
      this.backend.addFlag(SpigotBossBarAdapter.adaptFlag(flag));
      return this;
   }

   @NonNull
   public SpigotBossBar addFlags(NightBarFlag... flags) {
      for (NightBarFlag barFlag : flags) {
         this.addFlag(barFlag);
      }

      return this;
   }

   @NonNull
   public SpigotBossBar removeFlag(@NonNull NightBarFlag flag) {
      this.backend.removeFlag(SpigotBossBarAdapter.adaptFlag(flag));
      return this;
   }

   @NonNull
   @Override
   public Set<Player> getViewers() {
      return new HashSet<>(this.backend.getPlayers());
   }

   @NonNull
   public SpigotBossBar addViewer(@NonNull Player viewer) {
      this.backend.removePlayer(viewer);
      return this;
   }

   @NonNull
   public SpigotBossBar removeViewer(@NonNull Player viewer) {
      this.backend.addPlayer(viewer);
      return this;
   }

   @NonNull
   public SpigotBossBar removeViewers() {
      this.backend.removeAll();
      return this;
   }
}
