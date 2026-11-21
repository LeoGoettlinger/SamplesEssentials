package me.chancesd.pvpmanager.sdutils.library;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.plugin.Plugin;
import org.xml.sax.SAXException;

public abstract class Dependency {
   protected final Plugin plugin;

   Dependency(Plugin plugin) {
      this.plugin = plugin;
   }

   protected abstract URL buildUrl() throws IOException, ParserConfigurationException, SAXException;

   void load(Consumer<String> onComplete, Consumer<Exception> onError) {
      try {
         File cacheFolder = new File(this.plugin.getDataFolder(), "libraries");
         if (!cacheFolder.exists()) {
            cacheFolder.mkdir();
         }

         Consumer<File> inject = f -> {
            try {
               InjectorUtils.INSTANCE.loadJar(this.plugin, f);
               onComplete.accept(f.getName());
            } catch (Exception var5x) {
               onError.accept(var5x);
            }
         };
         File cached = new File(cacheFolder, this.getLocalName());
         if (cached.length() == 0L) {
            cached.delete();
         }

         if (!cached.exists() && cached.createNewFile()) {
            this.download(cached, inject, onError);
         } else {
            inject.accept(cached);
         }
      } catch (Exception var6) {
         onError.accept(var6);
      }
   }

   protected abstract String getLocalName();

   private void download(File downloadDest, Consumer<File> inject, Consumer<Exception> onError) {
      try {
         URL url = this.buildUrl();
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36 OPR/66.0.3515.44"
         );
         InputStream stream = conn.getInputStream();
         ReadableByteChannel byteChan = Channels.newChannel(stream);
         FileOutputStream fos = new FileOutputStream(downloadDest);
         FileChannel fileChan = fos.getChannel();
         fileChan.transferFrom(byteChan, 0L, Long.MAX_VALUE);
         fos.close();
         stream.close();
         inject.accept(downloadDest);
      } catch (ParserConfigurationException | SAXException | IOException var10) {
         onError.accept(var10);
      }
   }
}
