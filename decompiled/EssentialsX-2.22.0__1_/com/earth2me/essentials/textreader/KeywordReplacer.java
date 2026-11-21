package com.earth2me.essentials.textreader;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.ExecuteTimer;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.DescParseTickFormat;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ess3.api.IEssentials;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class KeywordReplacer implements IText {
   private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");
   private static final Pattern KEYWORD = Pattern.compile("\\{([^\\{\\}]+)\\}");
   private static final Pattern KEYWORDSPLIT = Pattern.compile("\\:");
   private final transient IText input;
   private final transient List<String> replaced;
   private final transient IEssentials ess;
   private final transient boolean includePrivate;
   private final transient boolean replaceSpacesWithUnderscores;
   private final EnumMap<KeywordType, Object> keywordCache = new EnumMap<>(KeywordType.class);

   public KeywordReplacer(final IText input, final CommandSource sender, final IEssentials ess) {
      this.input = input;
      this.replaced = new ArrayList<>(this.input.getLines().size());
      this.ess = ess;
      this.includePrivate = true;
      this.replaceSpacesWithUnderscores = false;
      this.replaceKeywords(sender);
   }

   public KeywordReplacer(final IText input, final CommandSource sender, final IEssentials ess, final boolean showPrivate) {
      this.input = input;
      this.replaced = new ArrayList<>(this.input.getLines().size());
      this.ess = ess;
      this.includePrivate = showPrivate;
      this.replaceSpacesWithUnderscores = false;
      this.replaceKeywords(sender);
   }

   public KeywordReplacer(
      final IText input, final CommandSource sender, final IEssentials ess, final boolean showPrivate, final boolean replaceSpacesWithUnderscores
   ) {
      this.input = input;
      this.replaced = new ArrayList<>(this.input.getLines().size());
      this.ess = ess;
      this.includePrivate = showPrivate;
      this.replaceSpacesWithUnderscores = replaceSpacesWithUnderscores;
      this.replaceKeywords(sender);
   }

   private void replaceKeywords(final CommandSource sender) {
      ExecuteTimer execTimer = new ExecuteTimer();
      execTimer.start();
      User user = null;
      if (sender.isPlayer()) {
         user = this.ess.getUser(sender.getPlayer());
      }

      execTimer.mark("User Grab");

      for (int i = 0; i < this.input.getLines().size(); i++) {
         String line = this.input.getLines().get(i);
         if (line.startsWith("@")) {
            this.replaced.add(line);
         } else {
            Matcher matcher = KEYWORD.matcher(line);

            while (matcher.find()) {
               String fullMatch = matcher.group(0);
               String keywordMatch = matcher.group(1);
               String[] matchTokens = KEYWORDSPLIT.split(keywordMatch);
               line = this.replaceLine(line, fullMatch, matchTokens, user);
            }

            this.replaced.add(line);
         }
      }

      execTimer.mark("Text Replace");
      String timeroutput = execTimer.end();
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "Keyword Replacer " + timeroutput);
      }
   }

   private String replaceLine(String line, final String fullMatch, final String[] matchTokens, final User user) {
      String keyword = matchTokens[0];

      try {
         String replacer = null;
         KeywordType validKeyword = KeywordType.valueOf(keyword);
         if (validKeyword.getType().equals(KeywordCachable.CACHEABLE) && this.keywordCache.containsKey(validKeyword)) {
            replacer = this.keywordCache.get(validKeyword).toString();
         } else if (validKeyword.getType().equals(KeywordCachable.SUBVALUE)) {
            String subKeyword = "";
            if (matchTokens.length > 1) {
               subKeyword = matchTokens[1].toLowerCase(Locale.ENGLISH);
            }

            if (this.keywordCache.containsKey(validKeyword)) {
               Map<String, String> values = (Map<String, String>)this.keywordCache.get(validKeyword);
               if (values.containsKey(subKeyword)) {
                  replacer = values.get(subKeyword);
               }
            }
         }

         if (validKeyword.isPrivate() && !this.includePrivate) {
            replacer = "";
         }

         if (replacer == null) {
            replacer = "";
            switch (validKeyword) {
               case PLAYER:
               case DISPLAYNAME:
                  if (user != null) {
                     replacer = user.getDisplayName();
                  }
                  break;
               case USERNAME:
                  if (user != null) {
                     replacer = user.getName();
                  }
                  break;
               case NICKNAME:
                  if (user != null) {
                     String nickname = user.getFormattedNickname();
                     replacer = nickname == null ? user.getName() : nickname;
                  }
                  break;
               case PREFIX:
                  if (user != null) {
                     String prefix = FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getPrefix(user.getBase()));
                     replacer = prefix == null ? "" : prefix;
                  }
                  break;
               case SUFFIX:
                  if (user != null) {
                     String suffix = FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getSuffix(user.getBase()));
                     replacer = suffix == null ? "" : suffix;
                  }
                  break;
               case GROUP:
                  if (user != null) {
                     replacer = user.getGroup();
                  }
                  break;
               case BALANCE:
                  if (user != null) {
                     replacer = this.ess.getAdventureFacet().miniToLegacy(NumberUtil.displayCurrency(user.getMoney(), this.ess));
                  }
                  break;
               case MAILS:
                  if (user != null) {
                     replacer = Integer.toString(user.getMailAmount());
                  }
                  break;
               case PLAYTIME:
                  if (user != null) {
                     long playtimeMs = System.currentTimeMillis() - user.getBase().getStatistic(PLAY_ONE_TICK) * 50L;
                     replacer = DateUtil.formatDateDiff(playtimeMs);
                  }
                  break;
               case WORLD:
               case WORLDNAME:
                  if (user != null) {
                     Location location = user.getLocation();
                     replacer = location != null && location.getWorld() != null ? location.getWorld().getName() : "";
                  }
                  break;
               case ONLINE:
                  int playerHidden = 0;

                  for (User u : this.ess.getOnlineUsers()) {
                     if (u.isHidden()) {
                        playerHidden++;
                     }
                  }

                  replacer = Integer.toString(this.ess.getOnlinePlayers().size() - playerHidden);
                  break;
               case UNIQUE:
                  replacer = NumberFormat.getInstance().format((long)this.ess.getUsers().getUserCount());
                  break;
               case WORLDS:
                  StringBuilder worldsBuilder = new StringBuilder();

                  for (World w : this.ess.getServer().getWorlds()) {
                     if (worldsBuilder.length() > 0) {
                        worldsBuilder.append(", ");
                     }

                     worldsBuilder.append(w.getName());
                  }

                  replacer = worldsBuilder.toString();
                  break;
               case PLAYERLIST:
                  Map<String, String> outputList;
                  if (this.keywordCache.containsKey(validKeyword)) {
                     outputList = (Map<String, String>)this.keywordCache.get(validKeyword);
                  } else {
                     boolean showHidden;
                     if (user == null) {
                        showHidden = true;
                     } else {
                        showHidden = user.isAuthorized("essentials.list.hidden") || user.canInteractVanished();
                     }

                     Map<String, List<User>> playerList = PlayerList.getPlayerLists(this.ess, user, showHidden);
                     outputList = new HashMap<>();

                     for (String groupName : playerList.keySet()) {
                        List<User> groupUsers = playerList.get(groupName);
                        if (groupUsers != null && !groupUsers.isEmpty()) {
                           outputList.put(groupName, this.ess.getAdventureFacet().miniToLegacy(PlayerList.listUsers(this.ess, groupUsers, " ")));
                        }
                     }

                     StringBuilder playerlistBuilder = new StringBuilder();

                     for (Player p : this.ess.getOnlinePlayers()) {
                        if (!this.ess.getUser(p).isHidden()) {
                           if (playerlistBuilder.length() > 0) {
                              playerlistBuilder.append(", ");
                           }

                           playerlistBuilder.append(p.getDisplayName());
                        }
                     }

                     outputList.put("", playerlistBuilder.toString());
                     this.keywordCache.put(validKeyword, outputList);
                  }

                  if (matchTokens.length == 1) {
                     replacer = outputList.get("");
                  } else if (outputList.containsKey(matchTokens[1].toLowerCase(Locale.ENGLISH))) {
                     replacer = outputList.get(matchTokens[1].toLowerCase(Locale.ENGLISH));
                  } else if (matchTokens.length > 2) {
                     replacer = matchTokens[2];
                  }

                  this.keywordCache.put(validKeyword, outputList);
                  break;
               case TIME:
                  replacer = DateFormat.getTimeInstance(2, this.ess.getI18n().getCurrentLocale()).format(new Date());
                  break;
               case DATE:
                  replacer = DateFormat.getDateInstance(2, this.ess.getI18n().getCurrentLocale()).format(new Date());
                  break;
               case WORLDTIME12:
                  if (user != null) {
                     replacer = DescParseTickFormat.format12(user.getWorld() == null ? 0L : user.getWorld().getTime());
                  }
                  break;
               case WORLDTIME24:
                  if (user != null) {
                     replacer = DescParseTickFormat.format24(user.getWorld() == null ? 0L : user.getWorld().getTime());
                  }
                  break;
               case WORLDDATE:
                  if (user != null) {
                     replacer = DateFormat.getDateInstance(2, this.ess.getI18n().getCurrentLocale())
                        .format(DescParseTickFormat.ticksToDate(user.getWorld() == null ? 0L : user.getWorld().getFullTime()));
                  }
                  break;
               case COORDS:
                  if (user != null) {
                     Location location = user.getLocation();
                     replacer = user.playerTl("coordsKeyword", location.getBlockX(), location.getBlockY(), location.getBlockZ());
                  }
                  break;
               case TPS:
                  replacer = NumberUtil.formatDouble(this.ess.getTimer().getAverageTPS());
                  break;
               case UPTIME:
                  replacer = DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime());
                  break;
               case IP:
                  if (user != null) {
                     replacer = user.getBase().getAddress() != null && user.getBase().getAddress().getAddress() != null
                        ? user.getBase().getAddress().getAddress().toString()
                        : "";
                  }
                  break;
               case ADDRESS:
                  if (user != null) {
                     replacer = user.getBase().getAddress() == null ? "" : user.getBase().getAddress().toString();
                  }
                  break;
               case PLUGINS:
                  StringBuilder pluginlistBuilder = new StringBuilder();

                  for (Plugin px : this.ess.getServer().getPluginManager().getPlugins()) {
                     if (pluginlistBuilder.length() > 0) {
                        pluginlistBuilder.append(", ");
                     }

                     pluginlistBuilder.append(px.getDescription().getName());
                  }

                  replacer = pluginlistBuilder.toString();
                  break;
               case VERSION:
                  replacer = this.ess.getServer().getVersion();
                  break;
               default:
                  replacer = "N/A";
            }

            if (this.replaceSpacesWithUnderscores && !line.startsWith("/") && validKeyword != KeywordType.USERNAME) {
               replacer = replacer.replace("_", "\\_").replaceAll("\\s", "_");
            }

            if (validKeyword.getType().equals(KeywordCachable.CACHEABLE)) {
               this.keywordCache.put(validKeyword, replacer);
            }
         }

         line = line.replace(fullMatch, replacer);
      } catch (IllegalArgumentException var16) {
      }

      return line;
   }

   @Override
   public List<String> getLines() {
      return this.replaced;
   }

   @Override
   public List<String> getChapters() {
      return this.input.getChapters();
   }

   @Override
   public Map<String, Integer> getBookmarks() {
      return this.input.getBookmarks();
   }
}
