package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.magmacore.menus.ContentPackage;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class MetaPackage extends EMPackage {
   public MetaPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
      super(contentPackagesConfigFields);
   }

   public List<EMPackage> getPackages() {
      List<EMPackage> packages = new ArrayList<>();

      for (String packageFilename : this.contentPackagesConfigFields.getContainedPackages()) {
         EMPackage emPackage = EMPackage.getEmPackages().get(packageFilename);
         if (emPackage != null) {
            packages.add(emPackage);
         }
      }

      return packages;
   }

   @Override
   protected ContentPackage.ContentState getContentState() {
      boolean allInstalled = true;
      boolean someInstalled = false;
      boolean allDownloaded = true;

      for (EMPackage aPackage : this.getPackages()) {
         if (!aPackage.isInstalled()) {
            allInstalled = false;
         }

         if (aPackage.isInstalled()) {
            someInstalled = true;
         }

         if (!aPackage.isDownloaded()) {
            allDownloaded = false;
         }
      }

      if (allInstalled) {
         this.isInstalled = true;
         if (this.outOfDate) {
            String slug = this.contentPackagesConfigFields.getNightbreakSlug();
            if (slug == null || slug.isEmpty() || !NightbreakAccount.hasToken()) {
               return ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
            } else if (this.cachedAccessInfo != null && this.cachedAccessInfo.hasAccess) {
               return ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
            } else {
               return this.cachedAccessInfo != null && !this.cachedAccessInfo.hasAccess
                  ? ContentPackage.ContentState.OUT_OF_DATE_NO_ACCESS
                  : ContentPackage.ContentState.OUT_OF_DATE_UPDATABLE;
            }
         } else {
            return ContentPackage.ContentState.INSTALLED;
         }
      } else if (someInstalled) {
         this.isInstalled = true;
         return ContentPackage.ContentState.PARTIALLY_INSTALLED;
      } else if (allDownloaded) {
         this.isDownloaded = true;
         return ContentPackage.ContentState.NOT_INSTALLED;
      } else {
         return ContentPackage.ContentState.NOT_DOWNLOADED;
      }
   }

   @Override
   public void doInstall(Player player) {
      this.getPackages().forEach(emPackage -> emPackage.doInstall(player));
      super.isInstalled = true;
   }

   @Override
   public void doUninstall(Player player) {
      this.getPackages().forEach(emPackage -> emPackage.doUninstall(player));
      super.isInstalled = false;
   }

   @Override
   public void baseInitialization() {
   }

   @Override
   public void initializeContent() {
   }
}
