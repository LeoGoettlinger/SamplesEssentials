package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.yaml.NodeStyle;
import com.earth2me.essentials.libs.configurate.yaml.YamlConfigurationLoader;
import com.earth2me.essentials.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.PasteUtil;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import net.ess3.provider.SerializationProvider;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandcreatekit extends EssentialsCommand {
   public Commandcreatekit() {
      super("createkit");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 2) {
         throw new NotEnoughArgumentsException();
      } else {
         long delay = Long.parseLong(args[1]);
         String kitname = args[0];
         ItemStack[] items = Inventories.getInventory(user.getBase(), true);
         List<String> list = new ArrayList<>();
         SerializationProvider serializationProvider = this.ess.provider(SerializationProvider.class);
         boolean useSerializationProvider = this.ess.getSettings().isUseBetterKits();
         if (useSerializationProvider && serializationProvider == null) {
            this.ess.showError(user.getSource(), new TranslatableException("createKitUnsupported"), commandLabel);
            useSerializationProvider = false;
         }

         for (int i = 0; i < items.length; i++) {
            ItemStack is = items[i];
            if (is != null && is.getType() != null && is.getType() != Material.AIR) {
               String serialized;
               if (useSerializationProvider) {
                  serialized = "slot:" + i + " @" + Base64Coder.encodeLines(serializationProvider.serializeItem(is));
               } else {
                  serialized = "slot:" + i + " " + this.ess.getItemDb().serialize(is);
               }

               list.add(serialized);
            }
         }

         if (!this.ess.getSettings().isPastebinCreateKit()) {
            this.ess.getKits().addKit(kitname, list, delay);
            user.sendTl("createdKit", kitname, list.size(), delay);
         } else {
            this.uploadPaste(user.getSource(), kitname, delay, list);
         }
      }
   }

   private void uploadPaste(final CommandSource sender, final String kitName, final long delay, final List<String> list) {
      this.ess
         .runTaskAsynchronously(
            () -> {
               try {
                  StringWriter sw = new StringWriter();
                  YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                     .sink(() -> new BufferedWriter(sw))
                     .indent(2)
                     .nodeStyle(NodeStyle.BLOCK)
                     .build();
                  ConfigurationNode config = loader.createNode();
                  config.node("kits", kitName, "delay").set(delay);
                  config.node("kits", kitName, "items").set(list);
                  sw.append("# Copy the kit code below into the kits section in your config.yml file\n");
                  loader.save(config);
                  String fileContents = sw.toString();
                  CompletableFuture<PasteUtil.PasteResult> future = PasteUtil.createPaste(
                     Collections.singletonList(new PasteUtil.PasteFile("kit_" + kitName + ".yml", fileContents))
                  );
                  future.thenAccept(result -> {
                     if (result != null) {
                        String separator = I18n.tlLiteral("createKitSeparator");
                        String delayFormat = delay <= 0L ? "0" : DateUtil.formatDateDiff(System.currentTimeMillis() + delay * 1000L);
                        sender.sendMessage(separator);
                        sender.sendTl("createKitSuccess", kitName, delayFormat, result.getPasteUrl());
                        sender.sendMessage(separator);
                        if (this.ess.getSettings().isDebug()) {
                           this.ess.getLogger().info(sender.getSender().getName() + " created a kit: " + result.getPasteUrl());
                        }
                     }
                  });
                  future.exceptionally(throwable -> {
                     sender.sendTl("createKitFailed", kitName);
                     this.ess.getLogger().log(Level.SEVERE, "Error creating kit: ", throwable);
                     return null;
                  });
               } catch (Exception var11) {
                  sender.sendTl("createKitFailed", kitName);
                  this.ess.getLogger().log(Level.SEVERE, "Error creating kit: ", (Throwable)var11);
               }
            }
         );
   }
}
