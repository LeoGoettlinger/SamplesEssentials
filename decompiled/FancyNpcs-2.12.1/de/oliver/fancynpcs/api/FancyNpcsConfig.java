package de.oliver.fancynpcs.api;

import java.util.List;
import java.util.Map;

public interface FancyNpcsConfig {
   boolean isSkipInvisibleNpcs();

   boolean isInteractionCooldownMessageDisabled();

   boolean isMuteVersionNotification();

   boolean isEnableAutoSave();

   int getAutoSaveInterval();

   int getNpcUpdateInterval();

   int getNpcUpdateVisibilityInterval();

   int getTurnToPlayerTrackerInterval();

   int getTurnToPlayerDistance();

   boolean setTurnToPlayerDistance(int var1);

   boolean isTurnToPlayerResetToInitialDirection();

   int getVisibilityDistance();

   int getRemoveNpcsFromPlayerlistDelay();

   boolean isSwingArmOnUpdate();

   String getMineSkinApiKey();

   int getMineSkinTimeout();

   List<String> getBlockedCommands();

   Map<String, Integer> getMaxNpcsPerPermission();
}
