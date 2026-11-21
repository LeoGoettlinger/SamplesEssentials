package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import org.bukkit.entity.EntityType;

public final class CustomEntityDefinition {
   private final String identifier;
   private final EntityType carrierEntityType;
   private final float width;
   private final float height;
   private final float scale;
   private final Integer color;
   private final Integer variant;
   private final boolean tracked;
   private final CustomEntityPropertySchema propertySchema;
   private final CustomEntityViewerHook showHook;
   private final CustomEntityViewerHook hideHook;
   private final CustomEntityLifecycleHook removeHook;

   private CustomEntityDefinition(CustomEntityDefinition.Builder builder) {
      if (builder.identifier != null && !builder.identifier.isBlank()) {
         this.identifier = builder.identifier;
         this.carrierEntityType = builder.carrierEntityType;
         this.width = builder.width;
         this.height = builder.height;
         this.scale = builder.scale;
         this.color = builder.color;
         this.variant = builder.variant;
         this.tracked = builder.tracked;
         this.propertySchema = builder.propertySchema;
         this.showHook = builder.showHook;
         this.hideHook = builder.hideHook;
         this.removeHook = builder.removeHook;
      } else {
         throw new IllegalStateException("Custom entity identifier is required");
      }
   }

   public static CustomEntityDefinition.Builder builder(String identifier) {
      return new CustomEntityDefinition.Builder().identifier(identifier);
   }

   public String identifier() {
      return this.identifier;
   }

   public EntityType carrierEntityType() {
      return this.carrierEntityType;
   }

   public float width() {
      return this.width;
   }

   public float height() {
      return this.height;
   }

   public float scale() {
      return this.scale;
   }

   public Integer color() {
      return this.color;
   }

   public Integer variant() {
      return this.variant;
   }

   public boolean tracked() {
      return this.tracked;
   }

   public CustomEntityPropertySchema propertySchema() {
      return this.propertySchema;
   }

   public CustomEntityViewerHook showHook() {
      return this.showHook;
   }

   public CustomEntityViewerHook hideHook() {
      return this.hideHook;
   }

   public CustomEntityLifecycleHook removeHook() {
      return this.removeHook;
   }

   public static final class Builder {
      private String identifier;
      private EntityType carrierEntityType = EntityType.PIG;
      private float width = 0.1F;
      private float height = 0.1F;
      private float scale = 1.0F;
      private Integer color;
      private Integer variant;
      private boolean tracked;
      private CustomEntityPropertySchema propertySchema = CustomEntityPropertySchema.empty();
      private CustomEntityViewerHook showHook = (entity, player) -> {};
      private CustomEntityViewerHook hideHook = (entity, player) -> {};
      private CustomEntityLifecycleHook removeHook = entity -> {};

      public CustomEntityDefinition.Builder identifier(String identifier) {
         this.identifier = identifier;
         return this;
      }

      public CustomEntityDefinition.Builder carrierEntityType(EntityType carrierEntityType) {
         if (carrierEntityType == null) {
            throw new IllegalArgumentException("carrierEntityType cannot be null");
         } else {
            this.carrierEntityType = carrierEntityType;
            return this;
         }
      }

      public CustomEntityDefinition.Builder dimensions(float width, float height) {
         if (!(width < 0.0F) && !(height < 0.0F)) {
            this.width = width;
            this.height = height;
            return this;
         } else {
            throw new IllegalArgumentException("dimensions cannot be negative");
         }
      }

      public CustomEntityDefinition.Builder scale(float scale) {
         this.scale = scale;
         return this;
      }

      public CustomEntityDefinition.Builder color(Integer color) {
         this.color = color;
         return this;
      }

      public CustomEntityDefinition.Builder variant(Integer variant) {
         this.variant = variant;
         return this;
      }

      public CustomEntityDefinition.Builder tracked(boolean tracked) {
         this.tracked = tracked;
         return this;
      }

      public CustomEntityDefinition.Builder propertySchema(CustomEntityPropertySchema propertySchema) {
         this.propertySchema = propertySchema == null ? CustomEntityPropertySchema.empty() : propertySchema;
         return this;
      }

      public CustomEntityDefinition.Builder onShow(CustomEntityViewerHook showHook) {
         this.showHook = showHook == null ? (entity, player) -> {} : showHook;
         return this;
      }

      public CustomEntityDefinition.Builder onHide(CustomEntityViewerHook hideHook) {
         this.hideHook = hideHook == null ? (entity, player) -> {} : hideHook;
         return this;
      }

      public CustomEntityDefinition.Builder onRemove(CustomEntityLifecycleHook removeHook) {
         this.removeHook = removeHook == null ? entity -> {} : removeHook;
         return this;
      }

      public CustomEntityDefinition build() {
         return new CustomEntityDefinition(this);
      }
   }
}
