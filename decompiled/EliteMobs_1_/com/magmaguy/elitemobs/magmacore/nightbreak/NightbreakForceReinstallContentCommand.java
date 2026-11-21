package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class NightbreakForceReinstallContentCommand<T extends NightbreakManagedContent> extends AdvancedCommand {
   private static final String PACKAGE_ARGUMENT = "package";
   private static final String ALL_PACKAGES = "all";
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec pluginSpec;
   private final Supplier<List<T>> packagesSupplier;
   private final AtomicBoolean guard;
   private final Consumer<CommandSender> reloadAction;

   public NightbreakForceReinstallContentCommand(
      JavaPlugin plugin, NightbreakPluginSpec pluginSpec, Supplier<List<T>> packagesSupplier, Consumer<CommandSender> reloadAction
   ) {
      this(plugin, pluginSpec, packagesSupplier, NightbreakPluginStateRegistry.getBulkOperationGuard(plugin), reloadAction);
   }

   public NightbreakForceReinstallContentCommand(
      JavaPlugin plugin, NightbreakPluginSpec pluginSpec, Supplier<List<T>> packagesSupplier, AtomicBoolean guard, Consumer<CommandSender> reloadAction
   ) {
      super(List.of("forcereinstallcontent"));
      this.plugin = plugin;
      this.pluginSpec = pluginSpec;
      this.packagesSupplier = packagesSupplier;
      this.guard = guard;
      this.reloadAction = reloadAction;
      this.setPermission(pluginSpec.adminPermission());
      this.setSenderType(SenderType.ANY);
      this.setDescription("Force reinstalls current " + pluginSpec.displayName() + " content from Nightbreak.");
      this.setUsage("/" + pluginSpec.rootCommand() + " forcereinstallcontent <package|all>");
      this.addArgument("package", new DynamicListStringCommandArgument(this::availableSelectors, "<package|all>"));
   }

   @Override
   public void execute(CommandData commandData) {
      CommandSender sender = commandData.getCommandSender();
      String selector = commandData.getStringArgument("package");
      if (selector != null && !selector.isBlank()) {
         List<T> packages = this.currentPackages();
         List<T> selected;
         if ("all".equalsIgnoreCase(selector)) {
            selected = downloadedPackages(packages);
            if (selected.isEmpty()) {
               sender.sendMessage(
                  ChatColorConverter.convert(
                     "&c["
                        + this.pluginSpec.displayName()
                        + "] No downloaded content was detected. Specify a package slug directly if its files are completely missing."
                  )
               );
               return;
            }
         } else {
            T selectedPackage = findBySlug(packages, selector);
            if (selectedPackage == null) {
               sender.sendMessage(
                  ChatColorConverter.convert(
                     "&c["
                        + this.pluginSpec.displayName()
                        + "] Unknown Nightbreak content package: "
                        + selector
                        + ". Use tab completion to select a package slug."
                  )
               );
               return;
            }

            selected = List.of(selectedPackage);
         }

         NightbreakBulkDownloader.executeForceReinstall(this.plugin, this.pluginSpec.displayName(), sender, selected, this.guard, this.reloadAction);
      } else {
         sender.sendMessage(this.getUsage());
      }
   }

   private List<String> availableSelectors() {
      LinkedHashSet<String> selectors = new LinkedHashSet<>();
      selectors.add("all");

      for (T contentPackage : this.currentPackages()) {
         String slug = contentPackage.getNightbreakSlug();
         if (slug != null && !slug.isBlank()) {
            selectors.add(slug);
         }
      }

      return List.copyOf(selectors);
   }

   private List<T> currentPackages() {
      List<T> packages = this.packagesSupplier.get();
      return packages == null ? List.of() : packages;
   }

   private static <T extends NightbreakManagedContent> List<T> downloadedPackages(List<T> packages) {
      List<T> selected = new ArrayList<>();
      Set<String> seenSlugs = new HashSet<>();

      for (T contentPackage : packages) {
         String slug = contentPackage.getNightbreakSlug();
         if (slug != null
            && !slug.isBlank()
            && seenSlugs.add(slug.toLowerCase(Locale.ROOT))
            && (contentPackage.isDownloaded() || contentPackage.isInstalled() || contentPackage.isOutOfDate())) {
            selected.add(contentPackage);
         }
      }

      return selected;
   }

   private static <T extends NightbreakManagedContent> T findBySlug(List<T> packages, String selector) {
      for (T contentPackage : packages) {
         String slug = contentPackage.getNightbreakSlug();
         if (slug != null && slug.equalsIgnoreCase(selector)) {
            return contentPackage;
         }
      }

      return null;
   }
}
