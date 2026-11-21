package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

public class Commandskull extends EssentialsCommand {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
   private static final Pattern URL_VALUE_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");
   private static final Pattern BASE_64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]{180}$");
   private static final Material SKULL_ITEM = EnumUtil.getMaterial("PLAYER_HEAD", "SKULL_ITEM");
   private final boolean playerProfileSupported;

   public Commandskull() {
      super("skull");
      boolean playerProfileSupported = true;

      try {
         Class.forName("org.bukkit.profile.PlayerProfile");
      } catch (ClassNotFoundException var3) {
         playerProfileSupported = false;
      }

      this.playerProfileSupported = playerProfileSupported;
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      User player;
      if (args.length == 2) {
         player = this.getPlayer(server, args, 1, false, false);
      } else {
         player = user;
      }

      String owner;
      if (args.length <= 0 || !player.isAuthorized("essentials.skull.others")) {
         owner = user.getName();
      } else if (BASE_64_PATTERN.matcher(args[0]).matches()) {
         try {
            String decoded = new String(Base64.getDecoder().decode(args[0]));
            JsonObject jsonObject = JsonParser.parseString(decoded).getAsJsonObject();
            String url = jsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            owner = url.substring(url.lastIndexOf("/") + 1);
         } catch (Exception var10) {
            throw new TranslatableException("skullInvalidBase64");
         }

         if (!URL_VALUE_PATTERN.matcher(owner).matches()) {
            throw new TranslatableException("skullInvalidBase64");
         }
      } else {
         if (!NAME_PATTERN.matcher(args[0]).matches()) {
            throw new TranslatableException("alphaNames");
         }

         owner = args[0];
      }

      ItemStack itemSkull = user.getItemInHand();
      boolean spawn = false;
      SkullMeta metaSkull;
      if (itemSkull != null && MaterialUtil.isPlayerHead(itemSkull) && user == player) {
         metaSkull = (SkullMeta)itemSkull.getItemMeta();
      } else {
         if (user == player ? !user.isAuthorized("essentials.skull.spawn") : !user.isAuthorized("essentials.skull.spawn.others")) {
            throw new TranslatableException("invalidSkull");
         }

         itemSkull = new ItemStack(SKULL_ITEM, 1, (short)3);
         metaSkull = (SkullMeta)itemSkull.getItemMeta();
         spawn = true;
      }

      if (metaSkull.hasOwner() && !user.isAuthorized("essentials.skull.modify")) {
         throw new TranslatableException("noPermissionSkull");
      } else {
         this.editSkull(user, player, itemSkull, metaSkull, owner, spawn);
      }
   }

   private void editSkull(final User user, final User receive, final ItemStack stack, final SkullMeta skullMeta, final String owner, final boolean spawn) {
      this.ess.runTaskAsynchronously(() -> {
         String shortOwnerName;
         if (URL_VALUE_PATTERN.matcher(owner).matches()) {
            if (!this.playerProfileSupported) {
               user.sendTl("unsupportedFeature");
               return;
            }

            URL url;
            try {
               url = new URL("https://textures.minecraft.net/texture/" + owner);
            } catch (MalformedURLException var11) {
               throw new RuntimeException(var11);
            }

            PlayerProfile profile = this.ess.getServer().createPlayerProfile(UUID.randomUUID());
            profile.getTextures().setSkin(url);
            skullMeta.setOwnerProfile(profile);
            shortOwnerName = owner.substring(0, 7);
         } else if (this.playerProfileSupported) {
            try {
               PlayerProfile profile = this.ess.getServer().createPlayerProfile(null, owner);
               profile = (PlayerProfile)profile.update().join();
               if (profile != null) {
                  skullMeta.setOwnerProfile(profile);
               }

               if (skullMeta.getOwnerProfile() == null) {
                  OfflinePlayer offline = this.ess.getServer().getOfflinePlayer(owner);
                  skullMeta.setOwningPlayer(offline);
               }

               shortOwnerName = owner;
            } catch (Exception var10) {
               skullMeta.setOwner(owner);
               shortOwnerName = owner;
            }
         } else {
            skullMeta.setOwner(owner);
            shortOwnerName = owner;
         }

         skullMeta.setDisplayName("§fSkull of " + shortOwnerName);
         String shortNameFinal = shortOwnerName;
         this.ess.scheduleSyncDelayedTask(() -> {
            stack.setItemMeta(skullMeta);
            if (spawn) {
               Inventories.addItem(receive.getBase(), stack);
               receive.sendTl("givenSkull", shortNameFinal);
               if (user != receive) {
                  user.sendTl("givenSkullOther", receive.getDisplayName(), shortNameFinal);
               }
            } else {
               user.sendTl("skullChanged", shortNameFinal);
            }
         });
      });
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return (List<String>)(user.isAuthorized("essentials.skull.others") ? this.getPlayers(user) : Lists.newArrayList(new String[]{user.getName()}));
      } else if (args.length == 2) {
         return (List<String>)(user.isAuthorized("essentials.skull.others") ? this.getPlayers(user) : Lists.newArrayList(new String[]{user.getName()}));
      } else {
         return Collections.emptyList();
      }
   }
}
