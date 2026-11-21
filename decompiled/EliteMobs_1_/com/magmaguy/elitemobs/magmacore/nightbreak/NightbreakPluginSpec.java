package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.net.URI;
import java.util.function.Supplier;

public record NightbreakPluginSpec(
   String displayName,
   String rootCommand,
   String adminPermission,
   String setupPermission,
   String initializePermission,
   String contentUrl,
   String reloadSuccessMessage,
   boolean hasContentPackages,
   boolean hasPresetModes,
   boolean hasImportSystem,
   Supplier<NightbreakChangelogMessages> changelogMessagesSupplier,
   Supplier<NightbreakLogoutMessages> logoutMessagesSupplier
) {
   public NightbreakPluginSpec(
      String displayName,
      String rootCommand,
      String adminPermission,
      String setupPermission,
      String initializePermission,
      String contentUrl,
      String reloadSuccessMessage,
      boolean hasContentPackages,
      boolean hasPresetModes,
      boolean hasImportSystem,
      Supplier<NightbreakChangelogMessages> changelogMessagesSupplier
   ) {
      this(
         displayName,
         rootCommand,
         adminPermission,
         setupPermission,
         initializePermission,
         contentUrl,
         reloadSuccessMessage,
         hasContentPackages,
         hasPresetModes,
         hasImportSystem,
         changelogMessagesSupplier,
         NightbreakLogoutMessages::defaults
      );
   }

   public NightbreakPluginSpec(
      String displayName,
      String rootCommand,
      String adminPermission,
      String setupPermission,
      String initializePermission,
      String contentUrl,
      String reloadSuccessMessage,
      boolean hasContentPackages,
      boolean hasPresetModes,
      boolean hasImportSystem
   ) {
      this(
         displayName,
         rootCommand,
         adminPermission,
         setupPermission,
         initializePermission,
         contentUrl,
         reloadSuccessMessage,
         hasContentPackages,
         hasPresetModes,
         hasImportSystem,
         NightbreakChangelogMessages::defaults,
         NightbreakLogoutMessages::defaults
      );
   }

   public NightbreakPluginSpec(
      String displayName,
      String rootCommand,
      String adminPermission,
      String setupPermission,
      String initializePermission,
      String contentUrl,
      String reloadSuccessMessage
   ) {
      this(
         displayName,
         rootCommand,
         adminPermission,
         setupPermission,
         initializePermission,
         contentUrl,
         reloadSuccessMessage,
         true,
         false,
         true,
         NightbreakChangelogMessages::defaults,
         NightbreakLogoutMessages::defaults
      );
   }

   public NightbreakPluginSpec(
      String displayName,
      String rootCommand,
      String adminPermission,
      String setupPermission,
      String initializePermission,
      String contentUrl,
      String reloadSuccessMessage,
      Supplier<NightbreakChangelogMessages> changelogMessagesSupplier
   ) {
      this(
         displayName,
         rootCommand,
         adminPermission,
         setupPermission,
         initializePermission,
         contentUrl,
         reloadSuccessMessage,
         true,
         false,
         true,
         changelogMessagesSupplier,
         NightbreakLogoutMessages::defaults
      );
   }

   public NightbreakPluginSpec(
      String displayName,
      String rootCommand,
      String adminPermission,
      String setupPermission,
      String initializePermission,
      String contentUrl,
      String reloadSuccessMessage,
      Supplier<NightbreakChangelogMessages> changelogMessagesSupplier,
      Supplier<NightbreakLogoutMessages> logoutMessagesSupplier
   ) {
      this(
         displayName,
         rootCommand,
         adminPermission,
         setupPermission,
         initializePermission,
         contentUrl,
         reloadSuccessMessage,
         true,
         false,
         true,
         changelogMessagesSupplier,
         logoutMessagesSupplier
      );
   }

   public NightbreakChangelogMessages resolveChangelogMessages() {
      if (this.changelogMessagesSupplier == null) {
         return NightbreakChangelogMessages.defaults();
      } else {
         try {
            NightbreakChangelogMessages messages = this.changelogMessagesSupplier.get();
            return messages == null ? NightbreakChangelogMessages.defaults() : messages;
         } catch (RuntimeException var2) {
            return NightbreakChangelogMessages.defaults();
         }
      }
   }

   public NightbreakLogoutMessages resolveLogoutMessages() {
      if (this.logoutMessagesSupplier == null) {
         return NightbreakLogoutMessages.defaults();
      } else {
         try {
            NightbreakLogoutMessages messages = this.logoutMessagesSupplier.get();
            return messages == null ? NightbreakLogoutMessages.defaults() : messages;
         } catch (RuntimeException var2) {
            return NightbreakLogoutMessages.defaults();
         }
      }
   }

   public String pluginSlug() {
      if (this.contentUrl != null && !this.contentUrl.isBlank()) {
         try {
            URI uri = URI.create(this.contentUrl);
            String[] parts = uri.getPath().split("/");

            for (int i = 0; i < parts.length - 1; i++) {
               if ("plugin".equals(parts[i]) && !parts[i + 1].isBlank()) {
                  return parts[i + 1];
               }
            }
         } catch (IllegalArgumentException var4) {
         }

         return this.rootCommand;
      } else {
         return this.rootCommand;
      }
   }

   public String downloadPageUrl() {
      String slug = this.pluginSlug();
      return "https://nightbreak.io/plugin/" + slug + "/download/";
   }

   public String contentUrlWithTrailingSlash() {
      if (this.contentUrl != null && !this.contentUrl.isBlank()) {
         return this.contentUrl.endsWith("/") ? this.contentUrl : this.contentUrl + "/";
      } else {
         return "https://nightbreak.io/plugin/" + this.pluginSlug() + "/";
      }
   }
}
