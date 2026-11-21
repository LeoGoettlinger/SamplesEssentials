package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActRulesMenu extends PagedMenu {
   private final Collection collection;
   private volatile boolean processing = false;

   public ActRulesMenu(Player player, Main plugin, Collection collection) {
      super(player, plugin);
      this.collection = collection;
   }

   @Override
   public void open() {
      this.processing = false;
      super.open();
   }

   @Override
   public void refresh() {
      this.processing = false;
      super.refresh();
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.rules.list.title", false, "%collection%", this.collection.getName());
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void performClick(InventoryClickEvent event) {
      if (!this.processing) {
         super.performClick(event);
      }
   }

   @Override
   public void setMenuItems() {
      List<ActRule> rules = this.collection.getActRules();
      this.hasNextPage = rules != null && (this.page + 1) * this.getMaxItemsPerPage() < rules.size();
      this.addMenuBorder();
      if (rules != null && !rules.isEmpty()) {
         this.addPagedButtons(rules.size());

         for (int i = 0; i < this.getMaxItemsPerPage(); i++) {
            this.index = this.getMaxItemsPerPage() * this.page + i;
            if (this.index >= rules.size()) {
               break;
            }

            ActRule rule = rules.get(this.index);
            if (rule != null) {
               Material material = rule.isEnabled() ? XMaterial.LIME_DYE.get() : XMaterial.GRAY_DYE.get();
               String actFormat = rule.getActFormat();
               Optional<ActFormatParser.ActSchedule> scheduleOpt = ActFormatParser.parse(actFormat);
               String dateRange = rule.getDateRange();
               String duration = rule.getDuration();
               String cron = rule.getCronExpression();
               String status = rule.isEnabled() ? "§aEnabled" : "§cDisabled";
               String nextTrigger = "§7N/A";
               if (scheduleOpt.isPresent() && rule.isEnabled()) {
                  Optional<ZonedDateTime> nextOpt = scheduleOpt.get().getNextTrigger(ZonedDateTime.now());
                  if (nextOpt.isPresent()) {
                     nextTrigger = "§f" + this.plugin.getMessageManager().formatDateTime(nextOpt.get());
                  }
               }

               this.addPagedItem(
                  i,
                  new ItemBuilder(material)
                     .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.rules.list.rule_item.name", false, "%name%", rule.getName()))
                     .setLore(
                        this.plugin
                           .getMessageManager()
                           .getMessageList(
                              "gui.act.rules.list.rule_item.lore",
                              false,
                              "%status%",
                              status,
                              "%date_range%",
                              dateRange,
                              "%duration%",
                              duration,
                              "%cron%",
                              cron,
                              "%next_trigger%",
                              nextTrigger
                           )
                           .toArray(new String[0])
                     )
                     .build(),
                  e -> {
                     if (e.getClick() == ClickType.SHIFT_LEFT) {
                        this.processing = true;
                        this.collection.removeActRule(rule.getId());
                        this.plugin
                           .getCollectionManager()
                           .saveCollection(this.collection)
                           .thenRun(
                              () -> this.plugin
                                 .getServer()
                                 .getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       this.playerMenuUtility
                                          .sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.rules.rule_deleted", "%name%", rule.getName()));
                                       this.refresh();
                                       this.processing = false;
                                    }
                                 )
                           )
                           .exceptionally(ex -> {
                              this.processing = false;
                              return null;
                           });
                     } else {
                        if (e.isLeftClick()) {
                           new ActRuleEditorMenu(this.playerMenuUtility, this.plugin, this.collection, rule).setPreviousMenu(this).open();
                        } else if (e.isRightClick()) {
                           this.processing = true;
                           rule.setEnabled(!rule.isEnabled());
                           this.plugin
                              .getCollectionManager()
                              .saveCollection(this.collection)
                              .thenRun(() -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                                 this.refresh();
                                 this.processing = false;
                              }))
                              .exceptionally(ex -> {
                                 this.processing = false;
                                 return null;
                              });
                        }
                     }
                  }
               );
            }
         }
      }

      this.addButton(
         52,
         new ItemBuilder(XMaterial.EMERALD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.rules.list.add_rule.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.rules.list.add_rule.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            this.processing = true;
            ActRule newRule = new ActRule(this.plugin.getCollectionManager().generateUniqueActRuleId(), this.collection.getId(), "New Rule");
            this.collection.addActRule(newRule);
            this.plugin
               .getCollectionManager()
               .saveCollection(this.collection)
               .thenRun(() -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                  new ActSetupMenu(this.playerMenuUtility, this.plugin, this.collection, newRule).setPreviousMenu(this).open();
                  this.processing = false;
               }))
               .exceptionally(ex -> {
                  this.processing = false;
                  return null;
               });
         }
      );
   }
}
