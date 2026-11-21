package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.textreader.TextPager;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.essentialsx.api.v2.services.BalanceTop;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.command.BlockCommandSender;

public class Commandbalancetop extends EssentialsCommand {
   public static final int MINUSERS = 50;
   private static final int CACHETIME = 120000;
   private static SimpleTextInput cache = new SimpleTextInput();

   public Commandbalancetop() {
      super("balancetop");
   }

   private void outputCache(final CommandSource sender, final int page) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(this.ess.getBalanceTop().getCacheAge());
      DateFormat format = DateFormat.getDateTimeInstance(3, 3);
      Runnable runnable = () -> {
         sender.sendTl("balanceTop", format.format(cal.getTime()));
         new TextPager(cache).showPage(Integer.toString(page), null, "balancetop", sender);
      };
      if (sender.getSender() instanceof BlockCommandSender) {
         this.ess.scheduleSyncDelayedTask(runnable);
      } else {
         runnable.run();
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      int page = 0;
      boolean force = false;
      if (args.length > 0) {
         try {
            page = Integer.parseInt(args[0]);
         } catch (NumberFormatException var8) {
            if (args[0].equalsIgnoreCase("force") && (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.balancetop.force"))) {
               force = true;
            }
         }
      }

      if (!force && this.ess.getBalanceTop().getCacheAge() > System.currentTimeMillis() - 120000L) {
         this.outputCache(sender, page);
      } else {
         if (this.ess.getUsers().getUserCount() > 50) {
            sender.sendTl("orderBalances", this.ess.getUsers().getUserCount());
         }

         this.ess.runTaskAsynchronously(new Commandbalancetop.Viewer(sender, page, force));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         return Collections.emptyList();
      } else {
         List<String> options = Lists.newArrayList(new String[]{"1"});
         if (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.balancetop.force")) {
            options.add("force");
         }

         return options;
      }
   }

   private class Viewer implements Runnable {
      private final transient CommandSource sender;
      private final transient int page;
      private final transient boolean force;

      Viewer(final CommandSource sender, final int page, final boolean force) {
         this.sender = sender;
         this.page = page;
         this.force = force;
      }

      @Override
      public void run() {
         if (Commandbalancetop.this.ess.getSettings().isEcoDisabled()) {
            if (Commandbalancetop.this.ess.getSettings().isDebug()) {
               Commandbalancetop.this.ess.getLogger().info("Internal economy functions disabled, aborting baltop.");
            }
         } else {
            boolean fresh = this.force
               || Commandbalancetop.this.ess.getBalanceTop().isCacheLocked()
               || Commandbalancetop.this.ess.getBalanceTop().getCacheAge() <= System.currentTimeMillis() - 120000L;
            CompletableFuture<Void> future = fresh
               ? Commandbalancetop.this.ess.getBalanceTop().calculateBalanceTopMapAsync()
               : CompletableFuture.completedFuture(null);
            future.thenRun(
               () -> {
                  if (fresh) {
                     SimpleTextInput newCache = new SimpleTextInput();
                     newCache.getLines()
                        .add(
                           Commandbalancetop.this.ess
                              .getAdventureFacet()
                              .miniToLegacy(
                                 I18n.tlLiteral(
                                    "serverTotal",
                                    AdventureUtil.parsed(
                                       NumberUtil.displayCurrency(Commandbalancetop.this.ess.getBalanceTop().getBalanceTopTotal(), Commandbalancetop.this.ess)
                                    )
                                 )
                              )
                        );
                     int pos = 1;

                     for (Entry<UUID, BalanceTop.Entry> entry : Commandbalancetop.this.ess.getBalanceTop().getBalanceTopCache().entrySet()) {
                        BigDecimal balance = entry.getValue().getBalance();
                        User user = Commandbalancetop.this.ess.getUser(entry.getKey());
                        Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");
                        boolean offlineStatisticSupported = VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_15_2_R01);
                        long playtime;
                        if (user.getBase() != null && user.getBase().isOnline()) {
                           playtime = user.getBase().getStatistic(PLAY_ONE_TICK);
                        } else if (offlineStatisticSupported) {
                           playtime = Bukkit.getServer().getOfflinePlayer(entry.getKey()).getStatistic(PLAY_ONE_TICK);
                        } else {
                           playtime = -1L;
                        }

                        long playTimeSecs = Math.max(playtime / 20L, 0L);
                        if ((Commandbalancetop.this.ess.getSettings().showZeroBaltop() || balance.compareTo(BigDecimal.ZERO) > 0)
                           && balance.compareTo(Commandbalancetop.this.ess.getSettings().getBaltopMinBalance()) >= 0
                           && (playtime == -1L || playTimeSecs >= Commandbalancetop.this.ess.getSettings().getBaltopMinPlaytime())) {
                           newCache.getLines()
                              .add(
                                 Commandbalancetop.this.ess
                                    .getAdventureFacet()
                                    .miniToLegacy(
                                       I18n.tlLiteral(
                                          "balanceTopLine",
                                          pos,
                                          entry.getValue().getDisplayName(),
                                          AdventureUtil.parsed(NumberUtil.displayCurrency(balance, Commandbalancetop.this.ess))
                                       )
                                    )
                              );
                        }

                        pos++;
                     }

                     Commandbalancetop.cache = newCache;
                  }

                  Commandbalancetop.this.outputCache(this.sender, this.page);
               }
            );
         }
      }
   }
}
