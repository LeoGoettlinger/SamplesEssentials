package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.HumanoidArm;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings;
import net.kyori.adventure.util.Index;

public class WrapperConfigClientSettings extends WrapperCommonClientSettings<WrapperConfigClientSettings> {
   public WrapperConfigClientSettings(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientSettings(
      String locale,
      int viewDistance,
      WrapperCommonClientSettings.ChatVisibility chatVisibility,
      boolean chatColors,
      byte skinMask,
      HumanoidArm mainHand,
      boolean textFilteringEnabled,
      boolean allowServerListings,
      WrapperCommonClientSettings.ParticleStatus particleStatus
   ) {
      super(
         PacketType.Configuration.Client.CLIENT_SETTINGS,
         locale,
         viewDistance,
         chatVisibility,
         chatColors,
         skinMask,
         mainHand,
         textFilteringEnabled,
         allowServerListings,
         particleStatus,
         (byte)0
      );
   }

   @Deprecated
   public WrapperConfigClientSettings(
      String locale,
      int viewDistance,
      WrapperConfigClientSettings.ChatVisibility visibility,
      boolean chatColorable,
      byte visibleSkinSectionMask,
      HumanoidArm hand,
      boolean textFilteringEnabled,
      boolean allowServerListings
   ) {
      this(
         locale,
         viewDistance,
         visibility.modern,
         chatColorable,
         visibleSkinSectionMask,
         hand,
         textFilteringEnabled,
         allowServerListings,
         WrapperCommonClientSettings.ParticleStatus.ALL
      );
   }

   @Deprecated
   public WrapperConfigClientSettings.ChatVisibility getVisibility() {
      return WrapperConfigClientSettings.ChatVisibility.MODERN_INDEX.valueOrThrow(this.getChatVisibility());
   }

   @Deprecated
   public void setVisibility(WrapperConfigClientSettings.ChatVisibility visibility) {
      this.setChatVisibility(visibility.modern);
   }

   @Deprecated
   public boolean isChatColorable() {
      return this.isChatColors();
   }

   @Deprecated
   public void setChatColorable(boolean chatColorable) {
      this.setChatColors(chatColorable);
   }

   @Deprecated
   public byte getVisibleSkinSectionMask() {
      return this.getSkinMask();
   }

   @Deprecated
   public void setVisibleSkinSectionMask(byte visibleSkinSectionMask) {
      this.setSkinMask(visibleSkinSectionMask);
   }

   @Deprecated
   public HumanoidArm getHand() {
      return this.getMainHand();
   }

   @Deprecated
   public void setHand(HumanoidArm hand) {
      this.setMainHand(hand);
   }

   @Deprecated
   public boolean isAllowServerListings() {
      return this.isServerListingAllowed();
   }

   @Deprecated
   public void setAllowServerListings(boolean allowServerListings) {
      this.setServerListingAllowed(allowServerListings);
   }

   @Deprecated
   public static enum ChatVisibility {
      FULL(WrapperCommonClientSettings.ChatVisibility.FULL),
      SYSTEM(WrapperCommonClientSettings.ChatVisibility.SYSTEM),
      HIDDEN(WrapperCommonClientSettings.ChatVisibility.HIDDEN);

      public static final WrapperConfigClientSettings.ChatVisibility[] VALUES = values();
      private static final Index<WrapperCommonClientSettings.ChatVisibility, WrapperConfigClientSettings.ChatVisibility> MODERN_INDEX = Index.create(
         WrapperConfigClientSettings.ChatVisibility.class, visibility -> visibility.modern
      );
      private final WrapperCommonClientSettings.ChatVisibility modern;

      private ChatVisibility(WrapperCommonClientSettings.ChatVisibility modern) {
         this.modern = modern;
      }
   }
}
