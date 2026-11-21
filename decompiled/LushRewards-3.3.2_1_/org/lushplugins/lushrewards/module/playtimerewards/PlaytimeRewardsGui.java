package org.lushplugins.lushrewards.module.playtimerewards;

import com.google.common.collect.TreeMultimap;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.gui.GuiFormat;
import org.lushplugins.lushrewards.libraries.lushlib.gui.inventory.Gui;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushrewards.libraries.lushlib.utils.Pair;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTracker;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;
import org.lushplugins.lushrewards.utils.Debugger;
import org.lushplugins.lushrewards.utils.MathUtils;

public class PlaytimeRewardsGui extends Gui {
   private final PlaytimeRewardsModule module;
   private final GuiFormat.GuiTemplate guiTemplate;
   private final PriorityQueue<Pair<PlaytimeRewardCollection, Integer>> rewardQueue = new PriorityQueue<>(Comparator.comparingInt(Pair::second));

   public PlaytimeRewardsGui(PlaytimeRewardsModule module, Player player) {
      super(module.getGuiFormat().getTemplate().getRowCount() * 9, ChatColorHandler.translate(module.getGuiFormat().getTitle()), player);
      this.module = module;
      this.guiTemplate = module.getGuiFormat().getTemplate();
   }

   @Override
   public void refresh() {
      Inventory inventory = this.getInventory();
      Player player = this.getPlayer();
      inventory.clear();
      this.clearButtons();
      LushRewards.getInstance()
         .getModule("playtime-tracker")
         .ifPresent(
            playtimeTrackerModule -> {
               PlaytimeTracker playtimeTracker = ((PlaytimeTrackerModule)playtimeTrackerModule).getPlaytimeTracker(player.getUniqueId());
               this.module
                  .getOrLoadUserData(player.getUniqueId(), true)
                  .completeOnTimeout(null, 15L, TimeUnit.SECONDS)
                  .thenAccept(
                     userData -> LushRewards.getMorePaperLib()
                        .scheduling()
                        .globalRegionalScheduler()
                        .run(
                           () -> {
                              if (userData == null) {
                                 DisplayItemStack errorItem = DisplayItemStack.builder(Material.BARRIER)
                                    .setDisplayName("&#ff6969Failed to load rewards user data try relogging")
                                    .setLore(List.of("&7&oIf this continues please", "&7&oreport to your server administrator"))
                                    .build();
                                 inventory.setItem(4, errorItem.asItemStack(player, true));
                              } else {
                                 int playtime = playtimeTracker.getGlobalPlaytime() - userData.getPreviousDayEndPlaytime();
                                 int lastCollectedPlaytime = userData.getLastCollectedPlaytime() - userData.getPreviousDayEndPlaytime();
                                 Integer shortestFrequency = this.module.getShortestRepeatFrequency(playtime);

                                 int startPlaytime = switch (this.module.getScrollType()) {
                                    case SCROLL -> {
                                       if (shortestFrequency != null) {
                                          yield Math.max(
                                             (int)(playtime - shortestFrequency.intValue() * Math.floor(this.guiTemplate.countChar('R') / 2.0)),
                                             lastCollectedPlaytime
                                          );
                                       } else {
                                          yield lastCollectedPlaytime;
                                       }
                                    }
                                    default -> 0;
                                 };
                                 this.module.getRewards().forEach(reward -> {
                                    if (!reward.shouldHideFromGui()) {
                                       Integer minutes = MathUtils.findFirstNumInSequence(reward.getStartMinute(), reward.getRepeatFrequency(), startPlaytime);
                                       if (minutes != null) {
                                          this.rewardQueue.add(new Pair<>(reward, minutes));
                                       }
                                    }
                                 });
                                 TreeMultimap<Character, Integer> slotMap = this.guiTemplate.getSlotMap();

                                 for (Character character : slotMap.keySet()) {
                                    switch (character) {
                                       case ' ':
                                          slotMap.get(character).forEach(slot -> inventory.setItem(slot, new ItemStack(Material.AIR)));
                                          break;
                                       case 'A':
                                          slotMap.get(character).forEach(slot -> {
                                             DisplayItemStack displayItem = LushRewards.getInstance()
                                                .getConfigManager()
                                                .getItemTemplate("claim-all", this.module);
                                             if (this.module.hasClaimableRewards(player)) {
                                                displayItem = DisplayItemStack.builder(displayItem).setEnchantGlow(true).build();
                                             }

                                             inventory.setItem(slot, displayItem.asItemStack(player, true));
                                             this.addButton(slot, event -> {
                                                this.removeButton(slot);
                                                if (this.module.hasClaimableRewards(player)) {
                                                   this.module.claimRewards(player);
                                                }
                                             });
                                          });
                                          break;
                                       case 'R':
                                          slotMap.get(character)
                                             .forEach(
                                                slot -> {
                                                   if (!this.rewardQueue.isEmpty()) {
                                                      Pair<PlaytimeRewardCollection, Integer> rewardPair = this.rewardQueue.poll();
                                                      PlaytimeRewardCollection reward = rewardPair.first();
                                                      int minutes = rewardPair.second();
                                                      int nextMinute = minutes + reward.getRepeatFrequency();
                                                      if (reward.getRepeatFrequency() > 0 && nextMinute <= reward.getRepeatsUntil()) {
                                                         this.rewardQueue.add(new Pair<>(reward, nextMinute));
                                                      }

                                                      String itemTemplate;
                                                      if (minutes < lastCollectedPlaytime) {
                                                         itemTemplate = "collected-reward";
                                                      } else if (playtime > minutes) {
                                                         itemTemplate = "redeemable-reward";
                                                      } else {
                                                         itemTemplate = "default-reward";
                                                      }

                                                      DisplayItemStack.Builder displayItemBuilder = DisplayItemStack.builder(
                                                            LushRewards.getInstance().getConfigManager().getCategoryTemplate(reward.getCategory())
                                                         )
                                                         .overwrite(
                                                            DisplayItemStack.builder(
                                                               LushRewards.getInstance().getConfigManager().getItemTemplate(itemTemplate, this.module)
                                                            )
                                                         )
                                                         .overwrite(DisplayItemStack.builder(reward.getDisplayItem()));
                                                      if (displayItemBuilder.getDisplayName() != null) {
                                                         displayItemBuilder.setDisplayName(
                                                            displayItemBuilder.getDisplayName().replace("%minutes%", String.valueOf(minutes))
                                                         );
                                                      }

                                                      if (displayItemBuilder.hasLore()) {
                                                         displayItemBuilder.setLore(
                                                            displayItemBuilder.getLore()
                                                               .stream()
                                                               .map(line -> line.replace("%minutes%", String.valueOf(minutes)))
                                                               .toList()
                                                         );
                                                      }

                                                      displayItemBuilder.parseColors(player);
                                                      if (this.module.hasClaimableRewards(player, playtimeTracker.getGlobalPlaytime())) {
                                                         this.addButton(
                                                            slot,
                                                            event -> {
                                                               ItemStack currItem = event.getCurrentItem();
                                                               if (currItem != null) {
                                                                  this.removeButton(slot);
                                                                  DisplayItemStack.Builder collectedItemBuilder = DisplayItemStack.builder(currItem)
                                                                     .overwrite(
                                                                        DisplayItemStack.builder(
                                                                           LushRewards.getInstance()
                                                                              .getConfigManager()
                                                                              .getItemTemplate("collected-reward", this.module)
                                                                        )
                                                                     );
                                                                  if (collectedItemBuilder.getDisplayName() != null) {
                                                                     collectedItemBuilder.setDisplayName(
                                                                        collectedItemBuilder.getDisplayName().replace("%minutes%", String.valueOf(minutes))
                                                                     );
                                                                  }

                                                                  inventory.setItem(slot, collectedItemBuilder.build().asItemStack(player, true));
                                                                  Debugger.sendDebugMessage(
                                                                     "Starting reward process for " + player.getName(), Debugger.DebugMode.ALL
                                                                  );
                                                                  if (this.module.hasClaimableRewards(player, playtimeTracker.getGlobalPlaytime())) {
                                                                     this.module.claimRewards(player, playtimeTracker.getGlobalPlaytime());
                                                                  }

                                                                  this.refresh();
                                                               }
                                                            }
                                                         );
                                                      }

                                                      inventory.setItem(slot, displayItemBuilder.build().asItemStack(player, true));
                                                   }
                                                }
                                             );
                                          break;
                                       default:
                                          slotMap.get(character)
                                             .forEach(
                                                slot -> {
                                                   DisplayItemStack displayItem = LushRewards.getInstance()
                                                      .getConfigManager()
                                                      .getItemTemplate(String.valueOf(character), this.module);
                                                   if (!displayItem.hasType()) {
                                                      displayItem = DisplayItemStack.builder(displayItem).setType(Material.RED_STAINED_GLASS_PANE).build();
                                                      LushRewards.getInstance()
                                                         .getLogger()
                                                         .severe(
                                                            "Failed to display custom item-template '"
                                                               + character
                                                               + "' as it does not specify a valid material"
                                                         );
                                                   }

                                                   inventory.setItem(slot, displayItem.asItemStack(player, true));
                                                }
                                             );
                                    }
                                 }
                              }
                           }
                        )
                  );
            }
         );
   }

   @Override
   public void onClick(InventoryClickEvent event) {
      super.onClick(event, true);
   }

   @Override
   public void onClose(InventoryCloseEvent event) {
      super.onClose(event);
      this.rewardQueue.clear();
   }

   public static enum ScrollType {
      FIXED,
      SCROLL;
   }
}
