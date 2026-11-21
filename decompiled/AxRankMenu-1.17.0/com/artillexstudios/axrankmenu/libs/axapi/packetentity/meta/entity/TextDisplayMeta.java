package com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.entity;

import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.serializer.Accessors;
import net.kyori.adventure.text.Component;

public class TextDisplayMeta extends DisplayMeta {
   public TextDisplayMeta(Metadata metadata) {
      super(metadata);
   }

   public void component(Component component) {
      this.metadata.set(Accessors.TEXT_COMPONENT, component);
   }

   public Component component() {
      return this.metadata.get(Accessors.TEXT_COMPONENT);
   }

   public void lineWidth(int lineWidth) {
      this.metadata.set(Accessors.LINE_WIDTH, lineWidth);
   }

   public int lineWidth() {
      return this.metadata.get(Accessors.LINE_WIDTH);
   }

   public void backgroundColor(int backgroundColor) {
      this.metadata.set(Accessors.BACKGROUND_COLOR, backgroundColor);
   }

   public int backgroundColor() {
      return this.metadata.get(Accessors.BACKGROUND_COLOR);
   }

   public void opacity(byte opacity) {
      this.metadata.set(Accessors.TEXT_OPACITY, opacity);
   }

   public byte opacity() {
      return this.metadata.get(Accessors.TEXT_OPACITY);
   }

   public void shadow(boolean shadow) {
      this.flag((byte)1, shadow);
   }

   public boolean shadow() {
      return this.flag(this.metadata.get(Accessors.TEXT_DISPLAY_DATA), (byte)1);
   }

   public void seeThrough(boolean seeThrough) {
      this.flag((byte)2, seeThrough);
   }

   public boolean seeThrough() {
      return this.flag(this.metadata.get(Accessors.TEXT_DISPLAY_DATA), (byte)2);
   }

   public void defaultBackground(boolean defaultBackground) {
      this.flag((byte)4, defaultBackground);
   }

   public boolean defaultBackground() {
      return this.flag(this.metadata.get(Accessors.TEXT_DISPLAY_DATA), (byte)4);
   }

   public void alignment(TextDisplayMeta.Alignment alignment) {
      switch (alignment) {
         case LEFT:
            this.flag((byte)8, true);
            this.flag((byte)16, false);
            break;
         case RIGHT:
            this.flag((byte)8, false);
            this.flag((byte)16, true);
            break;
         case CENTER:
            this.flag((byte)8, false);
            this.flag((byte)16, false);
      }
   }

   public TextDisplayMeta.Alignment alignment() {
      byte flags = this.metadata.get(Accessors.TEXT_DISPLAY_DATA);
      if ((flags & 8) != 0) {
         return TextDisplayMeta.Alignment.LEFT;
      } else {
         return (flags & 16) != 0 ? TextDisplayMeta.Alignment.RIGHT : TextDisplayMeta.Alignment.CENTER;
      }
   }

   private boolean flag(byte currentValue, byte mask) {
      return (currentValue & mask) != 0;
   }

   private void flag(byte flag, boolean value) {
      byte flags = this.metadata.get(Accessors.TEXT_DISPLAY_DATA);
      if (value) {
         flags = (byte)(flags | flag);
      } else {
         flags = (byte)(flags & (byte)(~flag));
      }

      this.metadata.set(Accessors.TEXT_DISPLAY_DATA, flags);
   }

   @Override
   protected void defineDefaults() {
      super.defineDefaults();
      this.metadata.define(Accessors.TEXT_COMPONENT, Component.empty());
      this.metadata.define(Accessors.LINE_WIDTH, 200);
      this.metadata.define(Accessors.BACKGROUND_COLOR, 1073741824);
      this.metadata.define(Accessors.TEXT_OPACITY, (byte)-1);
      this.metadata.define(Accessors.TEXT_DISPLAY_DATA, (byte)0);
   }

   public static enum Alignment {
      LEFT,
      RIGHT,
      CENTER;
   }
}
