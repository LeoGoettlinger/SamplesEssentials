package xyz.lychee.lagfixer.commands;

import java.awt.AWTError;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;
import org.bukkit.map.MapFont.CharacterSprite;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class MapCommand extends CommandManager.Subcommand {
   private MapCommand.MapHandler mapHandler;

   public MapCommand(CommandManager commandManager) {
      super(commandManager, "map", "server load map monitor with mspt chart");
   }

   @Override
   public void load() {
      FileConfiguration config = this.getCommandManager().getPlugin().getConfig();
      boolean enabled = config.getBoolean("main.monitor.map.enabled");
      if (enabled) {
         try {
            this.mapHandler = new MapCommand.MapHandler(this.getCommandManager().getPlugin());
            this.mapHandler.load();
         } catch (Throwable var4) {
         }
      } else if (this.mapHandler != null) {
         this.mapHandler.unload();
         this.mapHandler = null;
      }
   }

   @Override
   public void unload() {
      if (this.mapHandler != null) {
         this.mapHandler.unload();
      }
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (this.mapHandler == null) {
         MessageUtils.sendMessage(
            true,
            sender,
            "&7The map is currently unavailable.\nYou need to add the &e&n-Djava.awt.headless=true&7 flag when starting the application to resolve the issue.\nThis will enable headless mode and avoid the X11 display connection problem.\nPlease restart the server with this flag and check if the issue persists.\n"
         );
         return false;
      } else if (!(sender instanceof Player)) {
         Component text = Language.getMainValue("player_only", true);
         if (text != null) {
            LagFixer.getInstance().getAudiences().sender(sender).sendMessage(text);
         }

         return false;
      } else {
         this.mapHandler.giveMap((Player)sender);
         return true;
      }
   }

   private static class MapHandler extends MapRenderer {
      private static final int MAP_SIZE = 128;
      private static final int PADDING = 5;
      private static final Color GRID_COLOR = new Color(50, 50, 50, 100);
      private static final BasicStroke STROKE = new BasicStroke(2.0F);
      private static final int MAX_DATA_POINTS = 20;
      private static final LinearGradientPaint PAINT = new LinearGradientPaint(
         0.0F, 5.0F, 0.0F, 123.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{Color.RED, Color.YELLOW, Color.GREEN}
      );
      private final LagFixer plugin;
      private final ItemStack mapItem;
      private final MapView mapView;
      private final byte[] bytes = new byte[16384];
      private final int[] pixels;
      private final Graphics2D g2d;
      private final int[] valuesBuffer = new int[20];
      private final Double path = new Double();
      private int bufferIndex;
      private int dataCount;
      private volatile boolean shouldRender = true;
      private BukkitTask task;

      public MapHandler(LagFixer plugin) throws AWTError {
         this.plugin = plugin;
         this.mapItem = new ItemStack(Material.FILLED_MAP);
         FileConfiguration config = plugin.getConfig();
         MapView tempMap = null;
         if (config.isSet("main.monitor.map.id")) {
            tempMap = Bukkit.getServer().getMap(config.getInt("main.monitor.map.id"));
         }

         if (tempMap == null) {
            this.mapView = Bukkit.createMap((World)Bukkit.getWorlds().getFirst());
            config.set("main.monitor.map.id", this.mapView.getId());
            plugin.saveConfig();
         } else {
            this.mapView = tempMap;
         }

         BufferedImage image = new BufferedImage(128, 128, 1);
         this.pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
         this.g2d = image.createGraphics();
         this.g2d.setColor(Color.GRAY);
         this.g2d.fillRect(0, 0, 128, 128);
         MapMeta meta = (MapMeta)this.mapItem.getItemMeta();
         meta.setMapView(this.mapView);
         meta.setDisplayName(MessageUtils.fixColors(null, "&e⚡ &fServer monitor! &e⚡"));
         this.mapItem.setItemMeta(meta);
         this.mapView.getRenderers().clear();
         this.mapView.addRenderer(this);
      }

      public void load() {
         int interval = this.plugin.getConfig().getInt("main.monitor.map.interval");
         this.task = SupportManager.getInstance()
            .getFork()
            .runTimer(
               true,
               () -> {
                  SupportManager support = SupportManager.getInstance();
                  ResourceMonitor monitor = support.getResourceMonitor();
                  boolean supportMspt = support.getFork().isSupportMspt();
                  int pixelY = supportMspt ? this.msptToPixelY(monitor.getMspt()) : this.tpsToPixelY(monitor.getTps());
                  if (this.dataCount < 20) {
                     this.valuesBuffer[this.dataCount++] = pixelY;
                  } else {
                     this.valuesBuffer[this.bufferIndex] = pixelY;
                     this.bufferIndex = (this.bufferIndex + 1) % 20;
                  }

                  if (this.shouldRender) {
                     this.renderGraph();
                     this.renderText(
                        supportMspt
                           ? String.format("Mspt: %.1f Tps: %.1f", monitor.getMspt(), monitor.getTps())
                           : String.format("Tps: %.1f, Cpu: %.1f", monitor.getTps(), monitor.getCpuProcess())
                     );

                     for (int i = 0; i < this.pixels.length; i++) {
                        int rgb = this.pixels[i];
                        this.bytes[i] = MapPalette.matchColor(new Color(rgb));
                     }

                     this.shouldRender = false;
                  }
               },
               10L,
               interval,
               TimeUnit.SECONDS
            );
      }

      private void renderGraph() {
         this.g2d.setColor(Color.WHITE);
         this.g2d.fillRect(5, 5, 118, 118);
         this.path.reset();
         int validPoints = Math.min(this.dataCount, 20);

         for (int i = 0; i < validPoints; i++) {
            int dataIndex = (this.bufferIndex + i) % 20;
            int y = this.valuesBuffer[dataIndex];
            double x = 5.0 + i * 118.0 / (validPoints - 1);
            if (i == 0) {
               this.path.moveTo(x, y);
            } else {
               this.path.lineTo(x, y);
            }
         }

         this.path.lineTo(123.0, 123.0);
         this.path.lineTo(5.0, 123.0);
         this.path.closePath();
         this.g2d.setPaint(PAINT);
         this.g2d.fill(this.path);
         this.g2d.setStroke(STROKE);
         this.g2d.setColor(GRID_COLOR);

         for (int ix = 1; ix < 5; ix++) {
            int y = (int)(5.0 + ix * 118.0 / 5.0);
            this.g2d.drawLine(123, y, 5, y);
         }

         this.g2d.setColor(Color.BLACK);
         this.g2d.drawRect(5, 5, 118, 118);
      }

      private int msptToPixelY(double mspt) {
         double clamped = Math.min(mspt, 200.0);
         double scale = (clamped - 10.0) / 125.0;
         scale = Math.clamp(scale, 0.0, 1.0);
         return (int)Math.round(3.0 + (1.0 - scale) * 118.0);
      }

      private int tpsToPixelY(double tps) {
         double clamped = Math.clamp(tps, 15.0, 20.0);
         double scale = (clamped - 15.0) / 5.0;
         return (int)Math.round(3.0 + scale * 118.0);
      }

      private void renderText(String text) {
         MinecraftFont font = MinecraftFont.Font;
         int x = 10;
         int y = 10;
         int color = Color.BLACK.getRGB();

         for (char ch : text.toCharArray()) {
            CharacterSprite sprite = font.getChar(ch);
            if (sprite != null) {
               int width = sprite.getWidth();

               for (int row = 0; row < font.getHeight(); row++) {
                  for (int col = 0; col < width; col++) {
                     if (sprite.get(row, col)) {
                        int pixelIndex = (y + row) * 128 + x + col;
                        this.pixels[pixelIndex] = color;
                     }
                  }
               }

               x += width + 1;
            }
         }
      }

      public void unload() {
         if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
         }

         this.g2d.dispose();
      }

      public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
         if (mapView.equals(this.mapView)) {
            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().getType() == Material.FILLED_MAP || inv.getItemInOffHand().getType() == Material.FILLED_MAP) {
               this.shouldRender = true;
               mapCanvas.setCursors(new MapCursorCollection());
               int i = 0;

               while (i < 16384) {
                  int y = i >> 7;
                  int x = i & 127;
                  mapCanvas.setPixel(x, y, this.bytes[i++]);
               }
            }
         }
      }

      public void giveMap(Player p) {
         p.getInventory().addItem(new ItemStack[]{this.mapItem});
      }
   }
}
