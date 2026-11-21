package org.mineskin.data;

import com.google.gson.annotations.SerializedName;

public enum Visibility {
   @SerializedName("public")
   PUBLIC("public"),
   @SerializedName("unlisted")
   UNLISTED("unlisted"),
   @SerializedName("private")
   PRIVATE("private");

   private final String name;

   private Visibility(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }
}
