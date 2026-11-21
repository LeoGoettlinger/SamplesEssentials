package github.nighter.smartspawner.language.file;

import lombok.Generated;

public enum LanguageFileType {
   MESSAGES("messages.yml"),
   GUI("gui.yml"),
   COMMAND_GUI("command_gui.yml"),
   FORMATTING("formatting.yml"),
   ITEMS("items.yml"),
   COMMAND_MESSAGES("command_messages.yml"),
   HOLOGRAM("hologram.yml");

   private final String fileName;

   private LanguageFileType(String fileName) {
      this.fileName = fileName;
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }
}
