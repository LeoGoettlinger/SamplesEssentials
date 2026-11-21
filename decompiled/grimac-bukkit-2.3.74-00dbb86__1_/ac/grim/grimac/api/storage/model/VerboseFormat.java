package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum VerboseFormat {
   TEXT((byte)0),
   STRUCTURED_V1((byte)1);

   private final byte code;

   private VerboseFormat(byte code) {
      this.code = code;
   }

   public byte code() {
      return this.code;
   }

   public static VerboseFormat fromCode(int code) {
      for (VerboseFormat f : values()) {
         if (f.code == code) {
            return f;
         }
      }

      return TEXT;
   }
}
