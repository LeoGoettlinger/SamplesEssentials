package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.HumanoidArm;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings;
import ac.grim.grimac.shaded.kyori.adventure.util.Index;

public class WrapperPlayClientSettings extends WrapperCommonClientSettings<WrapperPlayClientSettings> {
   public WrapperPlayClientSettings(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientSettings(
      String locale,
      int viewDistance,
      WrapperCommonClientSettings.ChatVisibility visibility,
      boolean chatColors,
      byte visibleSkinSectionMask,
      HumanoidArm hand,
      boolean textFilteringEnabled,
      boolean allowServerListings,
      WrapperCommonClientSettings.ParticleStatus particleStatus
   ) {
      super(
         PacketType.Play.Client.CLIENT_SETTINGS,
         locale,
         viewDistance,
         visibility,
         chatColors,
         visibleSkinSectionMask,
         hand,
         textFilteringEnabled,
         allowServerListings,
         particleStatus,
         (byte)0
      );
   }

   @Deprecated
   public WrapperPlayClientSettings(
      String locale,
      int viewDistance,
      WrapperPlayClientSettings.ChatVisibility visibility,
      boolean chatCOlors,
      byte visibleSkinSectionMask,
      HumanoidArm hand,
      boolean textFilteringEnabled,
      boolean allowServerListings
   ) {
      this(
         locale,
         viewDistance,
         visibility.modern,
         chatCOlors,
         visibleSkinSectionMask,
         hand,
         textFilteringEnabled,
         allowServerListings,
         WrapperCommonClientSettings.ParticleStatus.ALL
      );
   }

   @Deprecated
   public WrapperPlayClientSettings.ChatVisibility getVisibility() {
      return WrapperPlayClientSettings.ChatVisibility.MODERN_INDEX.valueOrThrow(super.getChatVisibility());
   }

   @Deprecated
   public void setVisibility(WrapperPlayClientSettings.ChatVisibility visibility) {
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
   public static enum ChatVisibility {
      FULL(WrapperCommonClientSettings.ChatVisibility.FULL),
      SYSTEM(WrapperCommonClientSettings.ChatVisibility.SYSTEM),
      HIDDEN(WrapperCommonClientSettings.ChatVisibility.HIDDEN);

      public static final WrapperPlayClientSettings.ChatVisibility[] VALUES = values();
      private static final Index<WrapperCommonClientSettings.ChatVisibility, WrapperPlayClientSettings.ChatVisibility> MODERN_INDEX = Index.create(
         WrapperPlayClientSettings.ChatVisibility.class, visibility -> visibility.modern
      );
      private final WrapperCommonClientSettings.ChatVisibility modern;

      private ChatVisibility(WrapperCommonClientSettings.ChatVisibility modern) {
         this.modern = modern;
      }
   }
}
