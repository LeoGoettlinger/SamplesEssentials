package com.artillexstudios.axrankmenu.libs.axapi.utils;

import com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundClearTitlesWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundSetSubtitleTextWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundSetTitleTextWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundSetTitlesAnimationWrapper;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class Title {
   private Component title;
   private Component subTitle;
   private int fadeIn;
   private int stay;
   private int fadeOut;
   private ClientboundSetTitleTextWrapper titleTextPacket;
   private ClientboundSetTitlesAnimationWrapper animationPacket;
   private ClientboundSetSubtitleTextWrapper subtitleTextPacket;

   public Title(Component title, Component subTitle, int fadeIn, int stay, int fadeOut) {
      this.title = title;
      this.subTitle = subTitle;
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
      this.updatePacket();
   }

   public static Title create(Component title) {
      return new Title(title, null, 10, 70, 20);
   }

   public static Title create(Component title, Component subTitle) {
      return new Title(title, subTitle, 10, 70, 20);
   }

   public static Title create(Component title, Component subTitle, int fadeIn, int stay, int fadeOut) {
      return new Title(title, subTitle, fadeIn, stay, fadeOut);
   }

   public static Title create(Component subTitle, int fadeIn, int stay, int fadeOut) {
      return new Title(null, subTitle, fadeIn, stay, fadeOut);
   }

   public static void send(Player player, Component title, Component subTitle) {
      Title titleObject = create(title, subTitle);
      titleObject.send(player);
   }

   public static void sendTitle(Player player, Component title) {
      Title titleObject = create(title);
      titleObject.send(player);
   }

   public static void sendSubTitle(Player player, Component subTitle) {
      Title titleObject = create(null, subTitle);
      titleObject.send(player);
   }

   public void setTitle(@Nullable Component title) {
      this.title = title;
      this.updatePacket();
   }

   public void setSubTitle(@Nullable Component subTitle) {
      this.subTitle = subTitle;
      this.updatePacket();
   }

   public void set(@Nullable Component title, @Nullable Component subTitle) {
      this.title = title;
      this.subTitle = subTitle;
      this.updatePacket();
   }

   public void set(@Nullable Component title, @Nullable Component subTitle, int fadeIn, int stay, int fadeOut) {
      this.title = title;
      this.subTitle = subTitle;
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
      this.updatePacket();
   }

   public void setTimes(int fadeIn, int stay, int fadeOut) {
      this.fadeIn = fadeIn;
      this.stay = stay;
      this.fadeOut = fadeOut;
      this.updatePacket();
   }

   public void setFadeIn(int fadeIn) {
      this.fadeIn = fadeIn;
      this.updatePacket();
   }

   public void setStay(int stay) {
      this.stay = stay;
      this.updatePacket();
   }

   public void setFadeOut(int fadeOut) {
      this.fadeOut = fadeOut;
      this.updatePacket();
   }

   public void send(Player player) {
      ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
      wrapper.sendPacket(this.animationPacket);
      if (this.titleTextPacket != null) {
         wrapper.sendPacket(this.titleTextPacket);
      }

      if (this.subtitleTextPacket != null) {
         wrapper.sendPacket(this.subtitleTextPacket);
      }
   }

   public void clear(Player player) {
      ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
      wrapper.sendPacket(new ClientboundClearTitlesWrapper(true));
   }

   private void updatePacket() {
      if (this.title != null && this.title != Component.empty()) {
         this.titleTextPacket = new ClientboundSetTitleTextWrapper(this.title);
      } else {
         this.titleTextPacket = null;
      }

      if (this.subTitle != null && this.subTitle != Component.empty()) {
         this.subtitleTextPacket = new ClientboundSetSubtitleTextWrapper(this.subTitle);
      } else {
         this.subtitleTextPacket = null;
      }

      this.animationPacket = new ClientboundSetTitlesAnimationWrapper(this.fadeIn, this.stay, this.fadeOut);
   }
}
