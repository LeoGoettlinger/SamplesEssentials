package de.oliver.fancynpcs.api.skins;

import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import java.util.Objects;

public class SkinData {
   private String identifier;
   private SkinData.SkinVariant variant;
   private String textureValue;
   private String textureSignature;

   public SkinData(String identifier, SkinData.SkinVariant variant, String textureValue, String textureSignature) {
      this.identifier = identifier;
      this.variant = variant;
      this.textureValue = textureValue;
      this.textureSignature = textureSignature;
   }

   public SkinData(String identifier, SkinData.SkinVariant variant) {
      this(identifier, variant, null, null);
   }

   public boolean hasTexture() {
      return this.textureValue != null && this.textureSignature != null && !this.textureValue.isEmpty() && !this.textureSignature.isEmpty();
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public void setIdentifier(String identifier) {
      this.identifier = identifier;
   }

   public String getParsedIdentifier() {
      return (!this.identifier.startsWith("%") || !this.identifier.endsWith("%")) && (!this.identifier.startsWith("{") || !this.identifier.endsWith("}"))
         ? this.identifier
         : PaperColor.handler().translateRaw(this.identifier);
   }

   public SkinData.SkinVariant getVariant() {
      return this.variant;
   }

   public void setVariant(SkinData.SkinVariant variant) {
      this.variant = variant;
   }

   public String getTextureValue() {
      return this.textureValue;
   }

   public void setTextureValue(String textureValue) {
      this.textureValue = textureValue;
   }

   public String getTextureSignature() {
      return this.textureSignature;
   }

   public void setTextureSignature(String textureSignature) {
      this.textureSignature = textureSignature;
   }

   @Override
   public String toString() {
      return "SkinData{identifier='"
         + this.identifier
         + "', variant="
         + this.variant
         + ", textureValue='"
         + this.textureValue
         + "', textureSignature='"
         + this.textureSignature
         + "'}";
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         SkinData skinData = (SkinData)o;
         return Objects.equals(this.identifier, skinData.identifier)
            && this.variant == skinData.variant
            && Objects.equals(this.textureValue, skinData.textureValue)
            && Objects.equals(this.textureSignature, skinData.textureSignature);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.identifier, this.variant, this.textureValue, this.textureSignature);
   }

   public static enum SkinVariant {
      AUTO,
      SLIM;
   }
}
