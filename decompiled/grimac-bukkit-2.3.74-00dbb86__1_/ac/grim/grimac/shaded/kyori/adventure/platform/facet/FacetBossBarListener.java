package ac.grim.grimac.shaded.kyori.adventure.platform.facet;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.bossbar.BossBar;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.util.Set;
import java.util.function.Function;

class FacetBossBarListener<V> implements Facet.BossBar<V> {
   private final Facet.BossBar<V> facet;
   private final Function<Component, Component> translator;

   FacetBossBarListener(@NotNull final Facet.BossBar<V> facet, @NotNull final Function<Component, Component> translator) {
      this.facet = facet;
      this.translator = translator;
   }

   @Override
   public void bossBarInitialized(@NotNull final BossBar bar) {
      this.facet.bossBarInitialized(bar);
      this.bossBarNameChanged(bar, bar.name(), bar.name());
   }

   @Override
   public void bossBarNameChanged(@NotNull final BossBar bar, @NotNull final Component oldName, @NotNull final Component newName) {
      this.facet.bossBarNameChanged(bar, oldName, this.translator.apply(newName));
   }

   @Override
   public void bossBarProgressChanged(@NotNull final BossBar bar, final float oldPercent, final float newPercent) {
      this.facet.bossBarProgressChanged(bar, oldPercent, newPercent);
   }

   @Override
   public void bossBarColorChanged(@NotNull final BossBar bar, @NotNull final BossBar.Color oldColor, @NotNull final BossBar.Color newColor) {
      this.facet.bossBarColorChanged(bar, oldColor, newColor);
   }

   @Override
   public void bossBarOverlayChanged(@NotNull final BossBar bar, @NotNull final BossBar.Overlay oldOverlay, @NotNull final BossBar.Overlay newOverlay) {
      this.facet.bossBarOverlayChanged(bar, oldOverlay, newOverlay);
   }

   @Override
   public void bossBarFlagsChanged(@NotNull final BossBar bar, @NotNull final Set<BossBar.Flag> flagsAdded, @NotNull final Set<BossBar.Flag> flagsRemoved) {
      this.facet.bossBarFlagsChanged(bar, flagsAdded, flagsRemoved);
   }

   @Override
   public void addViewer(@NotNull final V viewer) {
      this.facet.addViewer(viewer);
   }

   @Override
   public void removeViewer(@NotNull final V viewer) {
      this.facet.removeViewer(viewer);
   }

   @Override
   public boolean isEmpty() {
      return this.facet.isEmpty();
   }

   @Override
   public void close() {
      this.facet.close();
   }
}
