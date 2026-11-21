package com.earth2me.essentials.items;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.ess3.api.IItemDb;
import org.bukkit.inventory.ItemStack;

public class CustomItemResolver implements IItemDb.ItemResolver, IConf {
   private final EssentialsConfiguration config;
   private final Essentials ess;
   private final HashMap<String, String> map = new HashMap<>();

   public CustomItemResolver(final Essentials ess) {
      this.config = new EssentialsConfiguration(new File(ess.getDataFolder(), "custom_items.yml"), "/custom_items.yml");
      this.ess = ess;
   }

   @Override
   public ItemStack apply(String item) {
      item = item.toLowerCase();
      if (this.map.containsKey(item)) {
         try {
            return this.ess.getItemDb().get(this.map.get(item));
         } catch (Exception var3) {
         }
      }

      return null;
   }

   @Override
   public Collection<String> getNames() {
      return this.map.keySet();
   }

   public List<String> getAliasesFor(String item) throws Exception {
      List<String> results = new ArrayList<>();
      if (item != null) {
         for (Entry<String, String> entry : this.map.entrySet()) {
            if (item.equalsIgnoreCase(this.ess.getItemDb().name(this.ess.getItemDb().get(entry.getValue())))) {
               results.add(entry.getKey());
            }
         }
      }

      return results;
   }

   @Override
   public void reloadConfig() {
      this.map.clear();
      this.config.load();
      Map<String, Object> section = ConfigurateUtil.getRawMap(this.config.getSection("aliases"));
      if (section.isEmpty()) {
         this.ess.getLogger().warning("No aliases found in custom_items.yml.");
      } else {
         for (Entry<String, Object> entry : section.entrySet()) {
            if (entry.getValue() instanceof String) {
               String alias = entry.getKey().toLowerCase();
               String target = (String)entry.getValue();
               if (this.existsInItemDb(target)) {
                  this.map.put(alias, target);
               }
            }
         }
      }
   }

   public void setAlias(String alias, final String target) {
      alias = alias.toLowerCase();
      if (!this.map.containsKey(alias) || !this.map.get(alias).equalsIgnoreCase(target)) {
         this.map.put(alias, target);
         this.save();
      }
   }

   public void removeAlias(final String alias) {
      if (this.map.remove(alias.toLowerCase()) != null) {
         this.save();
      }
   }

   private void save() {
      this.config.setRaw("aliases", this.map);
      this.config.save();
   }

   private boolean existsInItemDb(final String item) {
      try {
         this.ess.getItemDb().get(item);
         return true;
      } catch (Exception var3) {
         return false;
      }
   }
}
