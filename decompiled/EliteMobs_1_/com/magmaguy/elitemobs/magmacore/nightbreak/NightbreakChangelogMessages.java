package com.magmaguy.elitemobs.magmacore.nightbreak;

public record NightbreakChangelogMessages(
   String prefix,
   String pendingNotice,
   String viewButton,
   String viewHover,
   String noUnread,
   String dialogTitle,
   String dialogExternalTitle,
   String omittedDialog,
   String releaseHeader,
   String dismissButton,
   String disableButton,
   String disableTooltip,
   String chatHeader,
   String omittedChat,
   String chatInstructions,
   String dismissedPage,
   String viewNextButton,
   String viewNextHover,
   String dismissedAll,
   String disabled,
   String unavailable,
   String adminOnly,
   String commandDescription,
   String commandUsage
) {
   public NightbreakChangelogMessages(
      String prefix,
      String pendingNotice,
      String viewButton,
      String viewHover,
      String noUnread,
      String dialogTitle,
      String dialogExternalTitle,
      String omittedDialog,
      String releaseHeader,
      String dismissButton,
      String disableButton,
      String disableTooltip,
      String chatHeader,
      String omittedChat,
      String chatInstructions,
      String dismissedPage,
      String viewNextButton,
      String viewNextHover,
      String dismissedAll,
      String disabled,
      String unavailable,
      String adminOnly,
      String commandDescription,
      String commandUsage
   ) {
      prefix = valueOrDefault(prefix, "§8[§6$plugin§8] §f");
      pendingNotice = valueOrDefault(pendingNotice, "$prefix§eNew update notes are waiting. ");
      viewButton = valueOrDefault(viewButton, "§a§l[VIEW CHANGELOG]");
      viewHover = valueOrDefault(viewHover, "§7See what changed since your last review");
      noUnread = valueOrDefault(noUnread, "$prefix§7There are no unread changelogs.");
      dialogTitle = valueOrDefault(dialogTitle, "§6§l$plugin Update Notes");
      dialogExternalTitle = valueOrDefault(dialogExternalTitle, "$plugin Changelog");
      omittedDialog = valueOrDefault(omittedDialog, "§7$count older tracked release(s) were omitted to keep this digest readable.");
      releaseHeader = valueOrDefault(releaseHeader, "$component $version\n");
      dismissButton = valueOrDefault(dismissButton, "§aDismiss These Updates");
      disableButton = valueOrDefault(disableButton, "§8Never Show Changelogs");
      disableTooltip = valueOrDefault(disableTooltip, "§7Permanently hide update-note reminders for this plugin");
      chatHeader = valueOrDefault(chatHeader, "§8§m---------------- §6§l$plugin Update Notes §8§m----------------");
      omittedChat = valueOrDefault(omittedChat, "§7$count older release(s) were omitted from this digest.");
      chatInstructions = valueOrDefault(
         chatInstructions, "§7Use §f/$command dismiss §7to dismiss these notes, or §f/$command disable §7to permanently hide reminders."
      );
      dismissedPage = valueOrDefault(dismissedPage, "$prefix§7These update notes were dismissed. Older notes remain. ");
      viewNextButton = valueOrDefault(viewNextButton, "§a§l[VIEW NEXT]");
      viewNextHover = valueOrDefault(viewNextHover, "§7Open the next page of unread update notes");
      dismissedAll = valueOrDefault(dismissedAll, "$prefix§7The currently available update notes were dismissed.");
      disabled = valueOrDefault(disabled, "$prefix§7Changelog reminders have been permanently disabled for you.");
      unavailable = valueOrDefault(unavailable, "§cThe changelog service is not currently available.");
      adminOnly = valueOrDefault(adminOnly, "§cThis command is only available to plugin administrators in-game.");
      commandDescription = valueOrDefault(commandDescription, "Views unread $plugin changelogs.");
      commandUsage = valueOrDefault(commandUsage, "/$command [dismiss|disable]");
      this.prefix = prefix;
      this.pendingNotice = pendingNotice;
      this.viewButton = viewButton;
      this.viewHover = viewHover;
      this.noUnread = noUnread;
      this.dialogTitle = dialogTitle;
      this.dialogExternalTitle = dialogExternalTitle;
      this.omittedDialog = omittedDialog;
      this.releaseHeader = releaseHeader;
      this.dismissButton = dismissButton;
      this.disableButton = disableButton;
      this.disableTooltip = disableTooltip;
      this.chatHeader = chatHeader;
      this.omittedChat = omittedChat;
      this.chatInstructions = chatInstructions;
      this.dismissedPage = dismissedPage;
      this.viewNextButton = viewNextButton;
      this.viewNextHover = viewNextHover;
      this.dismissedAll = dismissedAll;
      this.disabled = disabled;
      this.unavailable = unavailable;
      this.adminOnly = adminOnly;
      this.commandDescription = commandDescription;
      this.commandUsage = commandUsage;
   }

   public static NightbreakChangelogMessages defaults() {
      return new NightbreakChangelogMessages(
         null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
      );
   }

   private static String valueOrDefault(String value, String fallback) {
      return value == null ? fallback : value;
   }
}
