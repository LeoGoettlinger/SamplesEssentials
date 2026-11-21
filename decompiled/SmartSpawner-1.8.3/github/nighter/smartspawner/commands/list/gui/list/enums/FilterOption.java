package github.nighter.smartspawner.commands.list.gui.list.enums;

import lombok.Generated;

public enum FilterOption {
   ALL("filter.option_name.all"),
   ACTIVE("filter.option_name.active"),
   INACTIVE("filter.option_name.inactive");

   private final String langPath;

   private FilterOption(String langPath) {
      this.langPath = langPath;
   }

   public FilterOption getNextOption() {
      return switch (this) {
         case ALL -> ACTIVE;
         case ACTIVE -> INACTIVE;
         case INACTIVE -> ALL;
      };
   }

   public String getColorPath() {
      return this.langPath + ".color";
   }

   public String getName() {
      return this.name().toLowerCase();
   }

   @Generated
   public String getLangPath() {
      return this.langPath;
   }
}
