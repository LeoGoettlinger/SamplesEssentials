package org.lushplugins.lushrewards.module;

import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;

public interface UserDataModule<T extends UserDataModule.UserData> {
   String getId();

   T getUserData(UUID var1);

   default CompletableFuture<T> getOrLoadUserData(UUID uuid, boolean cacheUser) {
      return LushRewards.getInstance().getDataManager().getOrLoadUserData(uuid, this, cacheUser);
   }

   default CompletableFuture<T> loadUserData(UUID uuid, boolean cacheUser) {
      return LushRewards.getInstance().getDataManager().loadUserData(uuid, this, cacheUser);
   }

   default CompletableFuture<Boolean> saveUserData(T userData) {
      return LushRewards.getInstance().getDataManager().saveUserData(userData);
   }

   void cacheUserData(UUID var1, UserDataModule.UserData var2);

   void uncacheUserData(UUID var1);

   T getDefaultData(UUID var1);

   Class<T> getUserDataClass();

   public abstract static class UserData {
      private final UUID uuid;
      private final String moduleId;

      public UserData(UUID uuid, String moduleId) {
         this.uuid = uuid;
         this.moduleId = moduleId;
      }

      @NotNull
      public UUID getUniqueId() {
         return this.uuid;
      }

      public String getModuleId() {
         return this.moduleId;
      }

      public JsonObject asJson() {
         return LushRewards.getInstance().getGson().toJsonTree(this).getAsJsonObject();
      }
   }
}
