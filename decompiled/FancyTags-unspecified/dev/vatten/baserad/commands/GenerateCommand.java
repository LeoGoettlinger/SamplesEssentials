package dev.vatten.baserad.commands;

import dev.vatten.baserad.Plugin;
import dev.vatten.baserad.TagStore;
import dev.vatten.baserad.TextFormatter;
import dev.vatten.baserad.VattenPlayer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;

public class GenerateCommand extends Command {
   private final Plugin plugin;
   private final Path importDirectory;

   public GenerateCommand(Plugin plugin, Path importDirectory) {
      super("generate", List.of());
      this.plugin = plugin;
      this.importDirectory = importDirectory;
   }

   @Override
   public void execute(VattenPlayer player, String[] args) {
      if (this.plugin.getTagGenerator() == null) {
         player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.SEVERE.format("Tag generation is disabled, API key is missing.")));
      } else if (args.length < 3) {
         player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.WARN.format("Usage: /tags generate <name> [url|file] <url/file>")));
      } else {
         String name = args[0];
         String type = args[1];
         String path = args[2];
         TagStore tagStore = this.plugin.getTagStore();
         if (tagStore.getTagNames().contains(name)) {
            player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.WARN.format("A tag with the name '" + name + "' already exists.")));
         } else {
            player.sendMessage(
               this.plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Starting generation of tag image located at '" + path + "'"))
            );

            BufferedImage image;
            try {
               if (type.equalsIgnoreCase("url")) {
                  try {
                     URL url = URI.create(path).toURL();
                     image = ImageIO.read(url);
                  } catch (MalformedURLException var9) {
                     player.sendMessage(TextFormatter.SEVERE.format(Component.text("Malformed URL. Double check that this is correct: '" + path + "'")));
                     return;
                  }
               } else {
                  if (!type.equalsIgnoreCase("file")) {
                     player.sendMessage(
                        TextFormatter.SEVERE.format(Component.text("Invalid import type '" + type + "'. Possible choices are 'url' and 'file'."))
                     );
                     return;
                  }

                  image = ImageIO.read(new File(this.importDirectory.resolve(path).toUri()));
               }
            } catch (Exception var10) {
               var10.printStackTrace();
               player.sendMessage(
                  TextFormatter.SEVERE
                     .format(Component.text("An error occurred while trying to read the image. '" + var10.getMessage() + "'. See more details in the console."))
               );
               return;
            }

            if (image == null) {
               player.sendMessage(TextFormatter.SEVERE.format(Component.text("Failed to read the image. Perhaps the format is not supported?")));
            } else {
               player.sendMessage(
                  this.plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Generating valid tag symbols. This may take a while..."))
               );
               this.plugin
                  .getTagGenerator()
                  .generate(
                     name,
                     image,
                     progress -> player.sendMessage(
                        TextFormatter.SUCCESS
                           .format(Component.text("[" + progress.getCurrent() + "/" + progress.getMax() + "] Successfully generated a tag symbol."))
                     )
                  )
                  .thenAccept(tag -> {
                     tagStore.addTag(name, tag);
                     player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Done!")));
                     player.sendMessage(this.plugin.getInterfaces().createTagInterface(name).asComponent());
                  })
                  .exceptionally(
                     ex -> {
                        ex.printStackTrace();
                        player.sendMessage(
                           TextFormatter.SEVERE
                              .format(Component.text("An error occurred while uploading the tag. '" + ex.getMessage() + "'. See more details in the console."))
                        );
                        return null;
                     }
                  );
            }
         }
      }
   }

   @Override
   public List<String> onTabComplete(VattenPlayer player, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 2) {
         completions.addAll(List.of("url", "file"));
      }

      return completions.stream().filter(s -> s.startsWith(args[args.length - 1].toLowerCase())).toList();
   }
}
