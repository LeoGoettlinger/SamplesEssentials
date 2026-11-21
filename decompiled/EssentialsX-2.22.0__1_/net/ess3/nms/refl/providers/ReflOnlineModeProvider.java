package net.ess3.nms.refl.providers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.OnlineModeProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.Bukkit;

@ProviderData(
   description = "Reflection Online Mode Provider"
)
public class ReflOnlineModeProvider implements OnlineModeProvider {
   private final MethodHandle spigotBungeeGetter;
   private final MethodHandle paperBungeeGetter;
   private final Object paperProxiesInstance;
   private final boolean fancyPaperCheck;

   public ReflOnlineModeProvider() {
      MethodHandle spigotBungeeGetter = null;
      MethodHandle paperBungeeGetter = null;
      Object paperProxiesInstance = null;
      boolean fancyCheck = false;

      try {
         Lookup lookup = MethodHandles.lookup();
         spigotBungeeGetter = lookup.findStaticGetter(Class.forName("org.spigotmc.SpigotConfig"), "bungee", boolean.class);
         Class<?> newPaperConfigClass = ReflUtil.getClassCached("io.papermc.paper.configuration.GlobalConfiguration");
         if (newPaperConfigClass != null) {
            Class<?> proxiesClass = Class.forName("io.papermc.paper.configuration.GlobalConfiguration$Proxies");
            Object globalConfig = (Object)lookup.findStatic(newPaperConfigClass, "get", MethodType.methodType(newPaperConfigClass)).invoke();
            paperProxiesInstance = (Object)lookup.findGetter(newPaperConfigClass, "proxies", proxiesClass).invoke((Object)globalConfig);
            paperBungeeGetter = lookup.findVirtual(proxiesClass, "isProxyOnlineMode", MethodType.methodType(boolean.class));
            fancyCheck = true;
         } else {
            Class<?> paperConfig = Class.forName("com.destroystokyo.paper.PaperConfig");
            paperBungeeGetter = lookup.findStaticGetter(paperConfig, "bungeeOnlineMode", boolean.class);
            paperBungeeGetter = lookup.findStatic(paperConfig, "isProxyOnlineMode", MethodType.methodType(boolean.class));
            fancyCheck = true;
         }
      } catch (Throwable var9) {
      }

      this.spigotBungeeGetter = spigotBungeeGetter;
      this.paperBungeeGetter = paperBungeeGetter;
      this.paperProxiesInstance = paperProxiesInstance;
      this.fancyPaperCheck = fancyCheck;
   }

   @Override
   public String getOnlineModeString() {
      if (this.spigotBungeeGetter == null) {
         return Bukkit.getOnlineMode() ? "Online Mode" : "Offline Mode";
      } else {
         try {
            if (Bukkit.getOnlineMode()) {
               return "Online Mode";
            } else if (this.fancyPaperCheck) {
               return this.paperProxiesInstance != null
                     ? (Object)this.paperBungeeGetter.invoke((Object)this.paperProxiesInstance)
                     : (Object)this.paperBungeeGetter.invoke()
                  ? "Proxy Mode"
                  : "Offline Mode";
            } else {
               return !(boolean)this.spigotBungeeGetter.invoke() || this.paperBungeeGetter != null && !(boolean)this.paperBungeeGetter.invoke()
                  ? "Offline Mode"
                  : "Bungee Mode";
            }
         } catch (Throwable var2) {
            return "Unknown";
         }
      }
   }
}
