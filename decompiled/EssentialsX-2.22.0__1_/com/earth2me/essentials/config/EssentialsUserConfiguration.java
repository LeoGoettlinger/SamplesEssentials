package com.earth2me.essentials.config;

import com.earth2me.essentials.Essentials;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

public class EssentialsUserConfiguration extends EssentialsConfiguration {
   private String username;
   private final UUID uuid;

   public EssentialsUserConfiguration(final String username, final UUID uuid, final File configFile) {
      super(configFile);
      this.username = username;
      this.uuid = uuid;
   }

   public String getUsername() {
      return this.username;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public void setUsername(final String username) {
      this.username = username;
   }

   @Override
   public boolean legacyFileExists() {
      return this.username == null ? false : new File(this.configFile.getParentFile(), this.username + ".yml").exists();
   }

   @Override
   public void convertLegacyFile() {
      File file = new File(this.configFile.getParentFile(), this.username + ".yml");

      try {
         Files.move(file, new File(this.configFile.getParentFile(), this.uuid + ".yml"));
      } catch (IOException var3) {
         Essentials.getWrappedLogger().log(Level.WARNING, "Failed to migrate user: " + this.username, (Throwable)var3);
      }

      this.setProperty("last-account-name", this.username);
   }

   private File getAltFile() {
      UUID fn = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.username.toLowerCase(Locale.ENGLISH)).getBytes(Charsets.UTF_8));
      return new File(this.configFile.getParentFile(), fn + ".yml");
   }

   @Override
   public boolean altFileExists() {
      return this.username != null && !this.username.equals(this.username.toLowerCase()) ? this.getAltFile().exists() : false;
   }

   @Override
   public void convertAltFile() {
      try {
         Files.move(this.getAltFile(), new File(this.configFile.getParentFile(), this.uuid + ".yml"));
      } catch (IOException var2) {
         Essentials.getWrappedLogger().log(Level.WARNING, "Failed to migrate user: " + this.username, (Throwable)var2);
      }
   }
}
