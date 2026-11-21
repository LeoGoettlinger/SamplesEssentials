package org.mineskin.data;

import javax.annotation.Nullable;

public class SkinInfo implements Skin, MutableBreadcrumbed {
   private final String uuid;
   private final String name;
   private final Variant variant;
   private final Visibility visibility;
   private final TextureInfo texture;
   private final GeneratorInfo generator;
   private final int views;
   private final boolean duplicate;
   private String breadcrumb;

   public SkinInfo(String uuid, String name, Variant variant, Visibility visibility, TextureInfo texture, GeneratorInfo generator, int views, boolean duplicate) {
      this.uuid = uuid;
      this.name = name;
      this.variant = variant;
      this.visibility = visibility;
      this.texture = texture;
      this.generator = generator;
      this.views = views;
      this.duplicate = duplicate;
   }

   @Override
   public String uuid() {
      return this.uuid;
   }

   @Override
   public String name() {
      return this.name;
   }

   @Override
   public Visibility visibility() {
      return this.visibility;
   }

   @Override
   public Variant variant() {
      return this.variant;
   }

   @Override
   public TextureInfo texture() {
      return this.texture;
   }

   public GeneratorInfo generator() {
      return this.generator;
   }

   @Override
   public int views() {
      return this.views;
   }

   @Override
   public boolean duplicate() {
      return this.duplicate;
   }

   @Nullable
   @Override
   public String getBreadcrumb() {
      return this.breadcrumb;
   }

   @Override
   public void setBreadcrumb(String breadcrumb) {
      this.breadcrumb = breadcrumb;
   }

   @Override
   public String toString() {
      return "SkinInfo{uuid='"
         + this.uuid
         + "', name='"
         + this.name
         + "', variant="
         + this.variant
         + ", visibility="
         + this.visibility
         + ", texture="
         + this.texture
         + ", generator="
         + this.generator
         + ", views="
         + this.views
         + ", duplicate="
         + this.duplicate
         + ", breadcrumb='"
         + this.breadcrumb
         + "'}";
   }
}
