package de.oliver.fancynpcs.skins;

import de.oliver.fancynpcs.api.skins.SkinData;
import java.util.Objects;
import org.mineskin.request.GenerateRequest;

public class SkinGenerationRequest {
   private final String id;
   private final SkinData.SkinVariant variant;
   private final GenerateRequest mineskinRequest;

   public SkinGenerationRequest(String id, SkinData.SkinVariant variant, GenerateRequest mineskinRequest) {
      this.id = id;
      this.variant = variant;
      this.mineskinRequest = mineskinRequest;
   }

   public SkinGenerationRequest(String id, SkinData.SkinVariant variant) {
      this.id = id;
      this.variant = variant;
      this.mineskinRequest = null;
   }

   public String getID() {
      return this.id;
   }

   public SkinData.SkinVariant getVariant() {
      return this.variant;
   }

   public GenerateRequest getMineskinRequest() {
      return this.mineskinRequest;
   }

   @Override
   public String toString() {
      return "SkinGenerationRequest{id='" + this.id + "', variant=" + this.variant + ", mineskinRequest=" + this.mineskinRequest + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         SkinGenerationRequest that = (SkinGenerationRequest)o;
         return Objects.equals(this.id, that.id) && this.variant == that.variant && Objects.equals(this.mineskinRequest, that.mineskinRequest);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.variant, this.mineskinRequest);
   }
}
