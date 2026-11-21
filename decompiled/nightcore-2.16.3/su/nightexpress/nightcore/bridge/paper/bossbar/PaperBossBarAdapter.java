package su.nightexpress.nightcore.bridge.paper.bossbar;

import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.bossbar.NightBarColor;
import su.nightexpress.nightcore.bridge.bossbar.NightBarFlag;
import su.nightexpress.nightcore.bridge.bossbar.NightBarOverlay;

public class PaperBossBarAdapter {
   @NonNull
   public static Color adaptColor(@NonNull NightBarColor nightColor) {
      return switch (nightColor) {
         case RED -> Color.RED;
         case BLUE -> Color.BLUE;
         case PINK -> Color.PINK;
         case GREEN -> Color.GREEN;
         case WHITE -> Color.WHITE;
         case PURPLE -> Color.PURPLE;
         case YELLOW -> Color.YELLOW;
      };
   }

   @NonNull
   public static NightBarColor wrapColor(@NonNull Color color) {
      return switch (color) {
         case YELLOW -> NightBarColor.YELLOW;
         case PURPLE -> NightBarColor.PURPLE;
         case WHITE -> NightBarColor.WHITE;
         case GREEN -> NightBarColor.GREEN;
         case PINK -> NightBarColor.PINK;
         case BLUE -> NightBarColor.BLUE;
         case RED -> NightBarColor.RED;
         default -> throw new MatchException(null, null);
      };
   }

   @NonNull
   public static Overlay adaptOverlay(@NonNull NightBarOverlay nightOverlay) {
      return switch (nightOverlay) {
         case PROGRESS -> Overlay.PROGRESS;
         case NOTCHED_6 -> Overlay.NOTCHED_6;
         case NOTCHED_10 -> Overlay.NOTCHED_10;
         case NOTCHED_12 -> Overlay.NOTCHED_12;
         case NOTCHED_20 -> Overlay.NOTCHED_20;
      };
   }

   @NonNull
   public static NightBarOverlay wrapOverlay(@NonNull Overlay overlay) {
      return switch (overlay) {
         case NOTCHED_20 -> NightBarOverlay.NOTCHED_20;
         case NOTCHED_12 -> NightBarOverlay.NOTCHED_12;
         case NOTCHED_10 -> NightBarOverlay.NOTCHED_10;
         case NOTCHED_6 -> NightBarOverlay.NOTCHED_6;
         case PROGRESS -> NightBarOverlay.PROGRESS;
         default -> throw new MatchException(null, null);
      };
   }

   @NonNull
   public static Flag adaptFlag(@NonNull NightBarFlag nightFlag) {
      return switch (nightFlag) {
         case DARKEN_SCREEN -> Flag.DARKEN_SCREEN;
         case PLAY_BOSS_MUSIC -> Flag.PLAY_BOSS_MUSIC;
         case CREATE_WORLD_FOG -> Flag.CREATE_WORLD_FOG;
      };
   }

   @NonNull
   public static NightBarFlag wrapFlag(@NonNull Flag flag) {
      return switch (flag) {
         case CREATE_WORLD_FOG -> NightBarFlag.CREATE_WORLD_FOG;
         case PLAY_BOSS_MUSIC -> NightBarFlag.PLAY_BOSS_MUSIC;
         case DARKEN_SCREEN -> NightBarFlag.DARKEN_SCREEN;
         default -> throw new MatchException(null, null);
      };
   }
}
