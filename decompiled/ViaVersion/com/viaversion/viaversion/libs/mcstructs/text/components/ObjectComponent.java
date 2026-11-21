package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.mcstructs.converter.SerializedData;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_21_5.NbtConverter_v1_21_5;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.font.AtlasSpriteFont;
import com.viaversion.viaversion.libs.mcstructs.text.font.FontDescription;
import com.viaversion.viaversion.libs.mcstructs.text.font.PlayerSpriteFont;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;

public class ObjectComponent extends TextComponent {
   private ObjectComponent.ObjectInfo objectInfo;
   @Nullable
   private TextComponent fallback;

   public ObjectComponent(ObjectComponent.ObjectInfo objectInfo) {
      this(objectInfo, null);
   }

   @Override
   public String asSingleString() {
      return this.objectInfo.getDescription();
   }

   @Override
   public TextComponent shallowCopy() {
      ObjectComponent component = new ObjectComponent(this.objectInfo, this.fallback == null ? null : this.fallback.copy());
      return component.setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .put("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .put("style", this.getStyle(), style -> !style.isEmpty())
         .put("objectInfo", this.objectInfo)
         .put("fallback", this.fallback, Objects::nonNull)
         .toString();
   }

   @Generated
   public ObjectComponent.ObjectInfo getObjectInfo() {
      return this.objectInfo;
   }

   @Nullable
   @Generated
   public TextComponent getFallback() {
      return this.fallback;
   }

   @Generated
   public void setObjectInfo(ObjectComponent.ObjectInfo objectInfo) {
      this.objectInfo = objectInfo;
   }

   @Generated
   public void setFallback(@Nullable TextComponent fallback) {
      this.fallback = fallback;
   }

   @Generated
   public ObjectComponent(ObjectComponent.ObjectInfo objectInfo, @Nullable TextComponent fallback) {
      this.objectInfo = objectInfo;
      this.fallback = fallback;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ObjectComponent)) {
         return false;
      } else {
         ObjectComponent other = (ObjectComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$objectInfo = this.getObjectInfo();
            Object other$objectInfo = other.getObjectInfo();
            if (this$objectInfo == null ? other$objectInfo == null : this$objectInfo.equals(other$objectInfo)) {
               Object this$fallback = this.getFallback();
               Object other$fallback = other.getFallback();
               return this$fallback == null ? other$fallback == null : this$fallback.equals(other$fallback);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof ObjectComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $objectInfo = this.getObjectInfo();
      result = result * 59 + ($objectInfo == null ? 43 : $objectInfo.hashCode());
      Object $fallback = this.getFallback();
      return result * 59 + ($fallback == null ? 43 : $fallback.hashCode());
   }

   public static class AtlasSprite implements ObjectComponent.ObjectInfo {
      public static final Identifier DEFAULT_ATLAS = Identifier.defaultNamespace("blocks");
      private Identifier atlas;
      private Identifier sprite;

      public AtlasSprite(Identifier sprite) {
         this.atlas = DEFAULT_ATLAS;
         this.sprite = sprite;
      }

      @Override
      public FontDescription getUpdatedFont() {
         return new AtlasSpriteFont(this.atlas, this.sprite);
      }

      @Override
      public String getDescription() {
         String shortSprite = Identifier.stripDefaultNamespace(this.sprite);
         if (this.atlas.equals(DEFAULT_ATLAS)) {
            return "[" + shortSprite + "]";
         } else {
            String shortAtlas = Identifier.stripDefaultNamespace(this.atlas);
            return "[" + shortAtlas + "@" + shortSprite + "]";
         }
      }

      @Override
      public String toString() {
         return ToString.of(this).put("atlas", this.atlas).put("sprite", this.sprite).toString();
      }

      @Generated
      public Identifier getAtlas() {
         return this.atlas;
      }

      @Generated
      public Identifier getSprite() {
         return this.sprite;
      }

      @Generated
      public void setAtlas(Identifier atlas) {
         this.atlas = atlas;
      }

      @Generated
      public void setSprite(Identifier sprite) {
         this.sprite = sprite;
      }

      @Generated
      public AtlasSprite(Identifier atlas, Identifier sprite) {
         this.atlas = atlas;
         this.sprite = sprite;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ObjectComponent.AtlasSprite)) {
            return false;
         } else {
            ObjectComponent.AtlasSprite other = (ObjectComponent.AtlasSprite)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$atlas = this.getAtlas();
               Object other$atlas = other.getAtlas();
               if (this$atlas == null ? other$atlas == null : this$atlas.equals(other$atlas)) {
                  Object this$sprite = this.getSprite();
                  Object other$sprite = other.getSprite();
                  return this$sprite == null ? other$sprite == null : this$sprite.equals(other$sprite);
               } else {
                  return false;
               }
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ObjectComponent.AtlasSprite;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $atlas = this.getAtlas();
         result = result * 59 + ($atlas == null ? 43 : $atlas.hashCode());
         Object $sprite = this.getSprite();
         return result * 59 + ($sprite == null ? 43 : $sprite.hashCode());
      }
   }

   public interface ObjectInfo {
      FontDescription getUpdatedFont();

      String getDescription();

      @Override
      String toString();
   }

   public static class PlayerSprite implements ObjectComponent.ObjectInfo {
      private SerializedData<?> profile;
      private boolean hat;

      @Override
      public FontDescription getUpdatedFont() {
         return new PlayerSpriteFont(this.profile, this.hat);
      }

      @Override
      public String getDescription() {
         String profileName = this.profile.convert(NbtConverter_v1_21_5.INSTANCE).map(tag -> (CompoundTag)tag).mapResult(compound -> {
            String name = compound.getString("name", null);
            return name == null ? Result.error("No name in profile") : Result.success(name);
         }).orElse(null);
         return profileName == null ? "[unknown player head]" : "[" + profileName + " head]";
      }

      @Override
      public String toString() {
         return ToString.of(this).put("profile", this.profile).put("hat", this.hat).toString();
      }

      @Generated
      public SerializedData<?> getProfile() {
         return this.profile;
      }

      @Generated
      public boolean isHat() {
         return this.hat;
      }

      @Generated
      public void setProfile(SerializedData<?> profile) {
         this.profile = profile;
      }

      @Generated
      public void setHat(boolean hat) {
         this.hat = hat;
      }

      @Generated
      public PlayerSprite(SerializedData<?> profile, boolean hat) {
         this.profile = profile;
         this.hat = hat;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ObjectComponent.PlayerSprite)) {
            return false;
         } else {
            ObjectComponent.PlayerSprite other = (ObjectComponent.PlayerSprite)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.isHat() != other.isHat()) {
               return false;
            } else {
               Object this$profile = this.getProfile();
               Object other$profile = other.getProfile();
               return this$profile == null ? other$profile == null : this$profile.equals(other$profile);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ObjectComponent.PlayerSprite;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + (this.isHat() ? 79 : 97);
         Object $profile = this.getProfile();
         return result * 59 + ($profile == null ? 43 : $profile.hashCode());
      }
   }
}
