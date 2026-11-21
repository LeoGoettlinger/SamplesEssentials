package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;

public class FakeTextSettings {
   private String text = "";
   private Color backgroundColor = null;
   private int backgroundArgb = 1073741824;
   private boolean hasBackgroundColor = false;
   private byte textOpacity = -1;
   private Billboard billboard = Billboard.CENTER;
   private FakeText.TextAlignment alignment = FakeText.TextAlignment.CENTER;
   private boolean shadow = false;
   private boolean seeThrough = false;
   private int lineWidth = 200;
   private float viewRange = 1.0F;
   private int teleportDuration = 0;
   private float scale = 1.0F;
   private float translationX = 0.0F;
   private float translationY = 0.0F;
   private float translationZ = 0.0F;
   private Predicate<Player> viewerFilter = player -> true;

   public FakeTextSettings() {
   }

   public FakeTextSettings(FakeTextSettings other) {
      this.text = other.text;
      this.backgroundColor = other.backgroundColor;
      this.backgroundArgb = other.backgroundArgb;
      this.hasBackgroundColor = other.hasBackgroundColor;
      this.textOpacity = other.textOpacity;
      this.billboard = other.billboard;
      this.alignment = other.alignment;
      this.shadow = other.shadow;
      this.seeThrough = other.seeThrough;
      this.lineWidth = other.lineWidth;
      this.viewRange = other.viewRange;
      this.teleportDuration = other.teleportDuration;
      this.scale = other.scale;
      this.translationX = other.translationX;
      this.translationY = other.translationY;
      this.translationZ = other.translationZ;
      this.viewerFilter = other.viewerFilter;
   }

   public String getText() {
      return this.text;
   }

   public FakeTextSettings setText(String text) {
      this.text = text;
      return this;
   }

   public Color getBackgroundColor() {
      return this.backgroundColor;
   }

   public FakeTextSettings setBackgroundColor(Color backgroundColor) {
      this.backgroundColor = backgroundColor;
      this.hasBackgroundColor = true;
      return this;
   }

   public int getBackgroundArgb() {
      return this.backgroundArgb;
   }

   public FakeTextSettings setBackgroundArgb(int backgroundArgb) {
      this.backgroundArgb = backgroundArgb;
      this.hasBackgroundColor = true;
      return this;
   }

   public boolean hasBackgroundColor() {
      return this.hasBackgroundColor;
   }

   public byte getTextOpacity() {
      return this.textOpacity;
   }

   public FakeTextSettings setTextOpacity(byte textOpacity) {
      this.textOpacity = textOpacity;
      return this;
   }

   public Billboard getBillboard() {
      return this.billboard;
   }

   public FakeTextSettings setBillboard(Billboard billboard) {
      this.billboard = billboard;
      return this;
   }

   public FakeText.TextAlignment getAlignment() {
      return this.alignment;
   }

   public FakeTextSettings setAlignment(FakeText.TextAlignment alignment) {
      this.alignment = alignment;
      return this;
   }

   public boolean hasShadow() {
      return this.shadow;
   }

   public FakeTextSettings setShadow(boolean shadow) {
      this.shadow = shadow;
      return this;
   }

   public boolean isSeeThrough() {
      return this.seeThrough;
   }

   public FakeTextSettings setSeeThrough(boolean seeThrough) {
      this.seeThrough = seeThrough;
      return this;
   }

   public int getLineWidth() {
      return this.lineWidth;
   }

   public FakeTextSettings setLineWidth(int lineWidth) {
      this.lineWidth = lineWidth;
      return this;
   }

   public float getViewRange() {
      return this.viewRange;
   }

   public FakeTextSettings setViewRange(float viewRange) {
      this.viewRange = viewRange;
      return this;
   }

   public int getTeleportDuration() {
      return this.teleportDuration;
   }

   public FakeTextSettings setTeleportDuration(int teleportDuration) {
      this.teleportDuration = Math.max(0, teleportDuration);
      return this;
   }

   public float getScale() {
      return this.scale;
   }

   public FakeTextSettings setScale(float scale) {
      this.scale = scale;
      return this;
   }

   public float getTranslationX() {
      return this.translationX;
   }

   public float getTranslationY() {
      return this.translationY;
   }

   public float getTranslationZ() {
      return this.translationZ;
   }

   public FakeTextSettings setTranslation(float x, float y, float z) {
      this.translationX = x;
      this.translationY = y;
      this.translationZ = z;
      return this;
   }

   public boolean hasTranslation() {
      return this.translationX != 0.0F || this.translationY != 0.0F || this.translationZ != 0.0F;
   }

   public FakeTextSettings setViewerFilter(Predicate<Player> viewerFilter) {
      this.viewerFilter = Objects.requireNonNull(viewerFilter, "viewerFilter");
      return this;
   }

   public boolean canBeSeenBy(Player player) {
      return this.viewerFilter.test(player);
   }
}
