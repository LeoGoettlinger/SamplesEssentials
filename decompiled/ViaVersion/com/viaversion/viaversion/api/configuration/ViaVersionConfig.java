package com.viaversion.viaversion.api.configuration;

import com.viaversion.viaversion.api.minecraft.WorldIdentifiers;
import com.viaversion.viaversion.api.protocol.version.BlockedProtocolVersions;
import com.viaversion.viaversion.libs.gson.JsonElement;
import java.util.concurrent.TimeUnit;

public interface ViaVersionConfig extends Config {
   boolean isCheckForUpdates();

   void setCheckForUpdates(boolean var1);

   boolean isPreventCollision();

   boolean isNewEffectIndicator();

   boolean isShowNewDeathMessages();

   boolean logEntityDataErrors();

   @Deprecated(
      forRemoval = true
   )
   default boolean isSuppressMetadataErrors() {
      return !this.logEntityDataErrors();
   }

   boolean isShieldBlocking();

   boolean isNoDelayShieldBlocking();

   boolean isShowShieldWhenSwordInHand();

   boolean isHologramPatch();

   boolean isPistonAnimationPatch();

   boolean isBossbarPatch();

   boolean isBossbarAntiflicker();

   double getHologramYOffset();

   boolean isAutoTeam();

   @Deprecated(
      forRemoval = true
   )
   default int getMaxPPS() {
      return this.getPacketTrackerConfig().maxRate();
   }

   @Deprecated(
      forRemoval = true
   )
   default String getMaxPPSKickMessage() {
      return this.getPacketTrackerConfig().maxRateKickMessage();
   }

   @Deprecated(
      forRemoval = true
   )
   default int getTrackingPeriod() {
      return (int)TimeUnit.NANOSECONDS.toSeconds(this.getPacketTrackerConfig().trackingPeriodNanos());
   }

   @Deprecated(
      forRemoval = true
   )
   default int getWarningPPS() {
      return this.getPacketTrackerConfig().warningRate();
   }

   @Deprecated(
      forRemoval = true
   )
   default int getMaxWarnings() {
      return this.getPacketTrackerConfig().maxWarnings();
   }

   @Deprecated(
      forRemoval = true
   )
   default String getMaxWarningsKickMessage() {
      return this.getPacketTrackerConfig().warningKickMessage();
   }

   RateLimitConfig getPacketTrackerConfig();

   RateLimitConfig getPacketSizeTrackerConfig();

   boolean isSendSupportedVersions();

   boolean isSimulatePlayerTick();

   boolean isItemCache();

   boolean isNMSPlayerTicking();

   boolean isReplacePistons();

   int getPistonReplacementId();

   boolean isChunkBorderFix();

   boolean isMultiReddustColorFix();

   boolean is1_13TeamColourFix();

   boolean shouldRegisterUserConnectionOnJoin();

   boolean is1_12QuickMoveActionFix();

   BlockedProtocolVersions blockedProtocolVersions();

   String getBlockedDisconnectMsg();

   boolean logBlockedJoins();

   String getReloadDisconnectMsg();

   boolean logOtherConversionWarnings();

   boolean logTextComponentConversionErrors();

   @Deprecated(
      forRemoval = true
   )
   default boolean isSuppressConversionWarnings() {
      return !this.logOtherConversionWarnings();
   }

   @Deprecated(
      forRemoval = true
   )
   default boolean isSuppressTextComponentConversionWarnings() {
      return !this.logTextComponentConversionErrors();
   }

   boolean isDisable1_13AutoComplete();

   boolean isServersideBlockConnections();

   String getBlockConnectionMethod();

   boolean isReduceBlockStorageMemory();

   boolean isStemWhenBlockAbove();

   boolean isVineClimbFix();

   boolean isSnowCollisionFix();

   boolean isInfestedBlocksFix();

   int get1_13TabCompleteDelay();

   boolean isTruncate1_14Books();

   boolean isLeftHandedHandling();

   boolean is1_9HitboxFix();

   boolean is1_14HitboxFix();

   boolean isNonFullBlockLightFix();

   boolean is1_14HealthNaNFix();

   boolean is1_15InstantRespawn();

   boolean isIgnoreLong1_16ChannelNames();

   boolean isForcedUse1_17ResourcePack();

   JsonElement get1_17ResourcePackPrompt();

   WorldIdentifiers get1_16WorldNamesMap();

   boolean cache1_17Light();

   boolean isArmorToggleFix();

   boolean translateOcelotToCat();

   boolean enforceSecureChat();

   boolean handleInvalidItemCount();

   boolean cancelBlockSounds();

   boolean hideScoreboardNumbers();

   boolean fix1_21PlacementRotation();

   boolean cancelSwingInInventory();

   int maxErrorLength();

   boolean use1_8HitboxMargin();

   boolean sendPlayerDetails();

   boolean sendServerDetails();
}
