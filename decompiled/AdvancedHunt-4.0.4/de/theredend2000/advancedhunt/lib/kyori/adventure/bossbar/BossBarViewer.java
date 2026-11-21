package de.theredend2000.advancedhunt.lib.kyori.adventure.bossbar;

import org.jetbrains.annotations.UnmodifiableView;

public interface BossBarViewer {
   @UnmodifiableView
   Iterable<? extends BossBar> activeBossBars();
}
