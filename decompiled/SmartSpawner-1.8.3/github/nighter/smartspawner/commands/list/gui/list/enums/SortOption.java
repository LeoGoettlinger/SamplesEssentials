package github.nighter.smartspawner.commands.list.gui.list.enums;

import lombok.Generated;

public enum SortOption {
   DEFAULT("sort.option_name.default"),
   STACK_SIZE_DESC("sort.option_name.stack_size_desc"),
   STACK_SIZE_ASC("sort.option_name.stack_size_asc");

   private final String langPath;

   private SortOption(String langPath) {
      this.langPath = langPath;
   }

   public SortOption getNextOption() {
      return switch (this) {
         case DEFAULT -> STACK_SIZE_DESC;
         case STACK_SIZE_DESC -> STACK_SIZE_ASC;
         case STACK_SIZE_ASC -> DEFAULT;
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
