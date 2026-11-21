package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.ArgsMessage;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Hook<P extends Plugin> {
   private static final ArgsMessage m_hooked = new ArgsMessage("hooks.hooked");
   private static final ArgsMessage m_hook_error = new ArgsMessage("hooks.error");
   protected final P plugin;

   public final P getPlugin() {
      return this.plugin;
   }

   public Hook() throws IOException {
      P plugin = (P)Bukkit.getPluginManager().getPlugin(this.getName());
      this.plugin = plugin;
      if (plugin != null && plugin.isEnabled()) {
         if (!this.init()) {
            Skript.error(m_hook_error.toString(plugin.getName()));
         } else {
            this.loadClasses();
            if (Skript.logHigh()) {
               Skript.info(m_hooked.toString(plugin.getName()));
            }
         }
      } else {
         if (Documentation.canGenerateUnsafeDocs()) {
            this.loadClasses();
            if (Skript.logHigh()) {
               Skript.info(m_hooked.toString(this.getName()));
            }
         }
      }
   }

   protected void loadClasses() throws IOException {
      Skript.getAddonInstance().loadClasses(this.getClass().getPackage().getName());
   }

   public abstract String getName();

   protected boolean init() {
      return true;
   }
}
