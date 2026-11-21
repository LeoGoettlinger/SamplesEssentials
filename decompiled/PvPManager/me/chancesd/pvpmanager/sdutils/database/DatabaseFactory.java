package me.chancesd.pvpmanager.sdutils.database;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseFactory {
   private final JavaPlugin plugin;
   private final List<Converter> converters = new ArrayList<>();

   public DatabaseFactory(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public DatabaseFactory registerConverter(Converter converter) {
      this.converters.add(converter);
      return this;
   }

   public Database getDatabase(DatabaseConfigBuilder builder) {
      return new Database(this, builder);
   }

   protected void doConversion(Database database) {
      boolean anyConversion = false;

      for (Converter converter : this.converters) {
         if (converter.onDatabaseLoad(database)) {
            converter.onComplete();
         }

         if (converter.needsConversion(database)) {
            anyConversion = true;
         }
      }

      if (anyConversion) {
         database.getPlugin().reloadConfig();
         Configuration defaults = database.getPlugin().getConfig().getDefaults();
         Preconditions.checkNotNull(defaults, "Error doing plugin conversion, config defaults is null");
         database.getPlugin().getConfig().set("Database Version", defaults.getInt("Database Version"));
         database.getPlugin().saveConfig();
      }
   }

   public JavaPlugin getPlugin() {
      return this.plugin;
   }
}
