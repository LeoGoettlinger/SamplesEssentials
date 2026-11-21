package com.fancyinnovations.config.featureflags;

@Deprecated
public class FeatureFlag {
   private final String name;
   private final String description;
   private final boolean forceDisabled;
   private boolean enabled;

   public FeatureFlag(String name, String description, boolean forceDisabled) {
      this.name = name;
      this.description = description;
      this.forceDisabled = forceDisabled;
      this.enabled = false;
   }

   public boolean isEnabled() {
      return this.forceDisabled ? false : this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isForceDisabled() {
      return this.forceDisabled;
   }
}
