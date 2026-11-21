package me.chancesd.pvpmanager.sdutils.library;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.bukkit.plugin.Plugin;

public enum InjectorUtils {
   INSTANCE;

   private URLClassLoader classLoader;
   private Method addUrlMethod;

   private void ensureInitiated(Plugin pl) throws NoSuchMethodException {
      this.classLoader = (URLClassLoader)pl.getClass().getClassLoader();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      this.addUrlMethod = method;
   }

   public void loadJar(Plugin pl, File src) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
      this.ensureInitiated(pl);
      this.addUrlMethod.invoke(this.classLoader, src.toURI().toURL());
   }
}
