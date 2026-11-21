package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.util.List;

public record NightbreakFirstTimeSetupSpec(
   String pluginDisplayName,
   String adminPermission,
   String initializeCommand,
   String setupCommand,
   String downloadAllCommand,
   String contentUrl,
   String supportUrl,
   List<String> warningNotes,
   List<String> recommendedNotes
) {
   public NightbreakFirstTimeSetupSpec(
      String pluginDisplayName,
      String adminPermission,
      String initializeCommand,
      String setupCommand,
      String downloadAllCommand,
      String contentUrl,
      String supportUrl,
      List<String> warningNotes,
      List<String> recommendedNotes
   ) {
      initializeCommand = initializeCommand == null ? "" : initializeCommand;
      downloadAllCommand = downloadAllCommand == null ? "" : downloadAllCommand.trim();
      warningNotes = warningNotes == null ? List.of() : List.copyOf(warningNotes);
      recommendedNotes = recommendedNotes == null ? List.of() : List.copyOf(recommendedNotes);
      this.pluginDisplayName = pluginDisplayName;
      this.adminPermission = adminPermission;
      this.initializeCommand = initializeCommand;
      this.setupCommand = setupCommand;
      this.downloadAllCommand = downloadAllCommand;
      this.contentUrl = contentUrl;
      this.supportUrl = supportUrl;
      this.warningNotes = warningNotes;
      this.recommendedNotes = recommendedNotes;
   }

   public static String normalizeContentDownloadCommand(String command) {
      return command != null && !command.isBlank() ? command.trim() : "";
   }
}
