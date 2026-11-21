package de.theredend2000.advancedhunt.lib.kyori.adventure.bossbar;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Index;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

public sealed interface BossBar permits BossBarImpl {
   float MIN_PROGRESS = 0.0F;
   float MAX_PROGRESS = 1.0F;

   static BossBar bossBar(final ComponentLike name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay) {
      BossBarImpl.checkProgress(progress);
      return bossBar(name.asComponent(), progress, color, overlay);
   }

   static BossBar bossBar(final Component name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay) {
      BossBarImpl.checkProgress(progress);
      return new BossBarImpl(name, progress, color, overlay);
   }

   static BossBar bossBar(
      final ComponentLike name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay, final Set<BossBar.Flag> flags
   ) {
      BossBarImpl.checkProgress(progress);
      return bossBar(name.asComponent(), progress, color, overlay, flags);
   }

   static BossBar bossBar(final Component name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay, final Set<BossBar.Flag> flags) {
      BossBarImpl.checkProgress(progress);
      return new BossBarImpl(name, progress, color, overlay, flags);
   }

   Component name();

   @Contract("_ -> this")
   default BossBar name(final ComponentLike name) {
      return this.name(name.asComponent());
   }

   @Contract("_ -> this")
   BossBar name(final Component name);

   float progress();

   @Contract("_ -> this")
   BossBar progress(final float progress);

   BossBar.Color color();

   @Contract("_ -> this")
   BossBar color(final BossBar.Color color);

   BossBar.Overlay overlay();

   @Contract("_ -> this")
   BossBar overlay(final BossBar.Overlay overlay);

   @UnmodifiableView
   Set<BossBar.Flag> flags();

   @Contract("_ -> this")
   BossBar flags(final Set<BossBar.Flag> flags);

   boolean hasFlag(final BossBar.Flag flag);

   @Contract("_ -> this")
   BossBar addFlag(final BossBar.Flag flag);

   @Contract("_ -> this")
   BossBar removeFlag(final BossBar.Flag flag);

   @Contract("_ -> this")
   BossBar addFlags(final BossBar.Flag... flags);

   @Contract("_ -> this")
   BossBar removeFlags(final BossBar.Flag... flags);

   @Contract("_ -> this")
   BossBar addFlags(final Iterable<BossBar.Flag> flags);

   @Contract("_ -> this")
   BossBar removeFlags(final Iterable<BossBar.Flag> flags);

   @Contract("_ -> this")
   BossBar addListener(final BossBar.Listener listener);

   @Contract("_ -> this")
   BossBar removeListener(final BossBar.Listener listener);

   @UnmodifiableView
   Iterable<? extends BossBarViewer> viewers();

   default BossBar addViewer(final Audience viewer) {
      viewer.showBossBar(this);
      return this;
   }

   default BossBar removeViewer(final Audience viewer) {
      viewer.hideBossBar(this);
      return this;
   }

   public static enum Color {
      PINK("pink"),
      BLUE("blue"),
      RED("red"),
      GREEN("green"),
      YELLOW("yellow"),
      PURPLE("purple"),
      WHITE("white");

      public static final Index<String, BossBar.Color> NAMES = Index.create(BossBar.Color.class, color -> color.name);
      private final String name;

      private Color(final String name) {
         this.name = name;
      }
   }

   public static enum Flag {
      DARKEN_SCREEN("darken_screen"),
      PLAY_BOSS_MUSIC("play_boss_music"),
      CREATE_WORLD_FOG("create_world_fog");

      public static final Index<String, BossBar.Flag> NAMES = Index.create(BossBar.Flag.class, flag -> flag.name);
      private final String name;

      private Flag(final String name) {
         this.name = name;
      }
   }

   public interface Listener {
      default void bossBarNameChanged(final BossBar bar, final Component oldName, final Component newName) {
      }

      default void bossBarProgressChanged(final BossBar bar, final float oldProgress, final float newProgress) {
      }

      default void bossBarColorChanged(final BossBar bar, final BossBar.Color oldColor, final BossBar.Color newColor) {
      }

      default void bossBarOverlayChanged(final BossBar bar, final BossBar.Overlay oldOverlay, final BossBar.Overlay newOverlay) {
      }

      default void bossBarFlagsChanged(final BossBar bar, final Set<BossBar.Flag> flagsAdded, final Set<BossBar.Flag> flagsRemoved) {
      }
   }

   public static enum Overlay {
      PROGRESS("progress"),
      NOTCHED_6("notched_6"),
      NOTCHED_10("notched_10"),
      NOTCHED_12("notched_12"),
      NOTCHED_20("notched_20");

      public static final Index<String, BossBar.Overlay> NAMES = Index.create(BossBar.Overlay.class, overlay -> overlay.name);
      private final String name;

      private Overlay(final String name) {
         this.name = name;
      }
   }
}
