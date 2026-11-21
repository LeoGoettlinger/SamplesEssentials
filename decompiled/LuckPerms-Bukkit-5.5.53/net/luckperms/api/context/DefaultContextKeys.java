package net.luckperms.api.context;

public final class DefaultContextKeys {
   public static final String SERVER_KEY = "server";
   public static final String WORLD_KEY = "world";
   public static final String DIMENSION_TYPE_KEY = "dimension-type";
   public static final String GAMEMODE_KEY = "gamemode";

   private DefaultContextKeys() {
      throw new AssertionError();
   }
}
