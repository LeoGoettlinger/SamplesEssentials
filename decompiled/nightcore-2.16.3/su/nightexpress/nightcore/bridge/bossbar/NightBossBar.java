package su.nightexpress.nightcore.bridge.bossbar;

import java.util.Set;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public interface NightBossBar {
   float MIN_PROGRESS = 0.0F;
   float MAX_PROGRESS = 1.0F;

   @NonNull
   NightBossBar setName(@NonNull NightComponent var1);

   float getProgress();

   @NonNull
   NightBossBar setProgress(float var1);

   @NonNull
   NightBarColor getColor();

   @NonNull
   NightBossBar setColor(@NonNull NightBarColor var1);

   @NonNull
   NightBarOverlay getOverlay();

   @NonNull
   NightBossBar setOverlay(@NonNull NightBarOverlay var1);

   @NonNull
   Set<NightBarFlag> getFlags();

   @NonNull
   NightBossBar setFlags(@NonNull Set<NightBarFlag> var1);

   boolean hasFlag(@NonNull NightBarFlag var1);

   @NonNull
   NightBossBar addFlag(@NonNull NightBarFlag var1);

   @NonNull
   NightBossBar removeFlag(@NonNull NightBarFlag var1);

   @NonNull
   NightBossBar addFlags(NightBarFlag... var1);

   @NonNull
   Set<Player> getViewers();

   @NonNull
   NightBossBar addViewer(@NonNull Player var1);

   @NonNull
   NightBossBar removeViewer(@NonNull Player var1);

   @NonNull
   NightBossBar removeViewers();
}
