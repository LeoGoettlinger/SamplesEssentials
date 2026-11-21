package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.bukkit.boss.BarStyle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BossBarOverlaySerializer implements EMFSerializer<Overlay> {
   private static final BossBarOverlaySerializer INSTANCE = new BossBarOverlaySerializer();
   private static final Overlay DEFAULT = Overlay.NOTCHED_10;

   private BossBarOverlaySerializer() {
   }

   @NonNull
   public static BossBarOverlaySerializer get() {
      return INSTANCE;
   }

   @NonNull
   public String serialize(@NonNull Overlay element) {
      return element.name();
   }

   @NonNull
   public Overlay deserialize(@Nullable String element) {
      if (element == null) {
         return DEFAULT;
      } else {
         element = element.toUpperCase(Locale.ROOT);

         try {
            return Overlay.valueOf(element);
         } catch (IllegalArgumentException var4) {
            Logging.debug("Failed to parse modern BossBar.Overlay. Attempting legacy.");

            try {
               BarStyle legacy = BarStyle.valueOf(element);

               return switch (legacy) {
                  case SOLID -> Overlay.PROGRESS;
                  case SEGMENTED_6 -> Overlay.NOTCHED_6;
                  case SEGMENTED_10 -> Overlay.NOTCHED_10;
                  case SEGMENTED_12 -> Overlay.NOTCHED_12;
                  case SEGMENTED_20 -> Overlay.NOTCHED_20;
                  default -> throw new MatchException(null, null);
               };
            } catch (IllegalArgumentException var3) {
               Logging.debug("Failed to parse legacy BarStyle. Returning default.");
               return DEFAULT;
            }
         }
      }
   }
}
