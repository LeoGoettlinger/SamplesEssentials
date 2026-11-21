package com.artillexstudios.axrankmenu.rank;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.hooks.currency.CurrencyHook;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axrankmenu.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axrankmenu.libs.axapi.utils.NumberUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.libs.gui.guis.GuiItem;
import com.artillexstudios.axrankmenu.utils.ItemBuilderUtil;
import com.artillexstudios.axrankmenu.utils.PlaceholderUtils;
import java.util.ArrayList;
import java.util.List;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rank {
   private static final LuckPerms luckPerms = LuckPermsProvider.get();
   private final Group group;
   private final Section section;
   private final Player requester;

   public Rank(@NotNull Section section, @NotNull Player requester) {
      this.section = section;
      this.requester = requester;
      this.group = luckPerms.getGroupManager().getGroup(section.getString("rank"));
   }

   public Node[] getNodes() {
      return this.group.getNodes().stream().filter(node -> !node.isNegated()).toArray(Node[]::new);
   }

   public GuiItem getItem() {
      List<String> lore = new ArrayList<>();

      for (String line : this.section.getStringList("item.lore")) {
         if (line.contains("%permission%")) {
            AxRankMenu.LANG.getBackingDocument().setGeneralSettings(GeneralSettings.builder().setRouteSeparator('倀').build());

            for (Node node : this.getNodes()) {
               ImmutableContextSet set = luckPerms.getContextManager().getStaticContext();
               if (!this.section.getString("server", "").isEmpty()) {
                  set = ImmutableContextSet.of("server", this.section.getString("server"));
               }

               if ((AxRankMenu.CONFIG.getBoolean("include-global-permissions") || node.getContexts().equals(set))
                  && (!AxRankMenu.CONFIG.getBoolean("include-global-permissions") || node.getContexts().isEmpty() || node.getContexts().equals(set))) {
                  String permission = node.getKey();
                  Integer number = null;

                  for (String t1 : permission.split("\\.")) {
                     if (NumberUtils.isInt(t1)) {
                        number = Integer.parseInt(t1);
                     }
                  }

                  permission = permission.replace(number + "", "#");
                  if (AxRankMenu.LANG.getString("permissions倀" + permission) == null) {
                     AxRankMenu.LANG.set("permissions倀" + permission, permission);
                     AxRankMenu.LANG.save();
                  }

                  String tName = AxRankMenu.LANG.getString("permissions倀" + permission);
                  if (!tName.isEmpty()) {
                     lore.add(PlaceholderUtils.parsePlaceholders(this.requester, line.replace("%permission%", tName.replace("#", number + "")), this.section));
                  }
               }
            }

            AxRankMenu.LANG.getBackingDocument().setGeneralSettings(GeneralSettings.builder().setRouteSeparator('.').build());
         } else {
            lore.add(PlaceholderUtils.parsePlaceholders(this.requester, line, this.section));
         }
      }

      ItemStack it = ItemBuilderUtil.newBuilder(this.section.getSection("item"), this.requester).setLore(lore).get();
      return new GuiItem(
         it,
         event -> {
            String cGroupName = luckPerms.getUserManager().getUser(this.requester.getUniqueId()).getPrimaryGroup();
            Group cGroup = luckPerms.getGroupManager().getGroup(cGroupName);
            if (AxRankMenu.CONFIG.getBoolean("prevent-downgrading", true)
               && cGroup.getWeight().isPresent()
               && this.group.getWeight().isPresent()
               && this.group.getWeight().getAsInt() <= cGroup.getWeight().getAsInt()) {
               AxRankMenu.MESSAGEUTILS.sendLang(this.requester, "error.downgrade-disabled");
            } else {
               if (AxRankMenu.CONFIG.getBoolean("force-buy-order.enabled", false)) {
                  Track track = luckPerms.getTrackManager().getTrack(AxRankMenu.CONFIG.getString("force-buy-order.track"));
                  String nextGroup = track.getNext(cGroup);
                  if (nextGroup == null && !track.containsGroup(cGroup) && !((String)track.getGroups().get(0)).equals(this.group.getName())) {
                     AxRankMenu.MESSAGEUTILS.sendLang(this.requester, "error.buy-order");
                     return;
                  }

                  if (nextGroup != null && !this.group.getName().equals(nextGroup)) {
                     AxRankMenu.MESSAGEUTILS.sendLang(this.requester, "error.buy-order");
                     return;
                  }
               }

               double price = this.section.getDouble("price", -1.0);
               String currency = this.section.getString("currency", "Vault");
               if (price != -1.0) {
                  if (AxRankMenu.CONFIG.getBoolean("discount-ranks", false)) {
                     double currentPrice = Math.max(
                        AxRankMenu.RANKS.getDouble(cGroup.getName() + ".price", -1.0F),
                        AxRankMenu.RANKS.getDouble(cGroup.getName().toUpperCase() + ".price", -1.0F)
                     );
                     if (currentPrice != -1.0) {
                        price -= currentPrice;
                        price = Math.max(0.0, price);
                     }
                  }

                  CurrencyHook hook = HookManager.getCurrencyHook(currency);
                  if (hook != null) {
                     if (hook.getBalance(this.requester) < price) {
                        AxRankMenu.MESSAGEUTILS.sendLang(this.requester, "buy.no-currency");
                     } else {
                        hook.takeBalance(this.requester, price);
                        List<String> actions = this.section.getStringList("buy-actions");
                        if (actions.isEmpty()) {
                           Bukkit.getConsoleSender()
                              .sendMessage(
                                 StringUtils.formatToString(
                                    "&#FF0000[AxRankMenu] The buy-actions section is missing from the "
                                       + this.section.getString("rank")
                                       + " rank, this will cause issues!"
                                 )
                              );
                        }

                        for (String action : actions) {
                           String[] type = action.split(" ");
                           String ac = action.replace(type[0] + " ", "");
                           ac = ac.replace("%player%", this.requester.getName());
                           ac = ac.replace("%name%", this.section.getString("item.name"));
                           ac = ac.replace("%rank%", this.section.getString("rank"));
                           ac = ac.replace("%price%", this.section.getString("price", "---"));
                           ac = ac.replace("%server%", this.section.getString("server"));
                           String var13 = type[0];
                           switch (var13) {
                              case "[MESSAGE]":
                                 this.requester.sendMessage(StringUtils.formatToString(ac));
                                 break;
                              case "[CONSOLE]":
                                 Scheduler.get().execute(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ac));
                                 break;
                              case "[CLOSE]":
                                 this.requester.closeInventory();
                           }
                        }
                     }
                  }
               }
            }
         }
      );
   }

   @Nullable
   public Group getGroup() {
      return this.group;
   }

   public Section getSection() {
      return this.section;
   }
}
