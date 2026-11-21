package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record Cursor(String token) {
   public Cursor(String token) {
      if (token == null) {
         throw new IllegalArgumentException("token");
      } else {
         this.token = token;
      }
   }
}
