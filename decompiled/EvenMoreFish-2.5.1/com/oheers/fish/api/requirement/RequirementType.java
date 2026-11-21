package com.oheers.fish.api.requirement;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import java.util.List;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class RequirementType implements RegistryItem {
   public abstract boolean checkRequirement(@NonNull RequirementContext var1, @NonNull List<String> var2);

   @NonNull
   public abstract String getIdentifier();

   @NonNull
   @Override
   public String getKey() {
      return this.getIdentifier();
   }

   @NonNull
   public abstract String getAuthor();

   @NonNull
   public abstract Plugin getPlugin();

   public boolean register() {
      return EMFRegistry.REQUIREMENT_TYPE.register(this);
   }

   public boolean unregister() {
      return EMFRegistry.REQUIREMENT_TYPE.unregister(this);
   }

   protected void debugLogStatus(boolean passed, @Nullable String checked) {
      String status = passed ? "passed" : "failed";
      StringBuilder log = new StringBuilder(this.getIdentifier() + " Requirement " + status + ".");
      if (checked != null) {
         log.append(" Checked: ").append(checked);
      }

      Logging.debug(log.toString());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RequirementType type ? this.getIdentifier().equals(type.getIdentifier()) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.getIdentifier().hashCode();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static Map<String, RequirementType> getLoadedTypes() {
      return EMFRegistry.REQUIREMENT_TYPE.getRegistry();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static void unregisterAll() {
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   @Nullable
   public static RequirementType get(@NonNull String identifier) {
      return EMFRegistry.REQUIREMENT_TYPE.get(identifier);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static boolean unregister(@NonNull String identifier) {
      return EMFRegistry.REQUIREMENT_TYPE.unregister(identifier);
   }
}
