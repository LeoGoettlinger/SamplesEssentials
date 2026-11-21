package com.earth2me.essentials;

import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public final class PlayerList {
   private PlayerList() {
   }

   public static String listUsers(final IEssentials ess, final List<User> users, final String seperator) {
      StringBuilder groupString = new StringBuilder();
      Collections.sort(users);
      boolean needComma = false;

      for (User user : users) {
         if (needComma) {
            groupString.append(seperator);
         }

         needComma = true;
         if (user.isAfk()) {
            groupString.append(I18n.tlLiteral("listAfkTag"));
         }

         if (user.isHidden()) {
            groupString.append(I18n.tlLiteral("listHiddenTag"));
         }

         user.setDisplayNick();
         groupString.append(ess.getAdventureFacet().legacyToMini(user.getDisplayName()));
         String strippedNick = FormatUtil.stripFormat(user.getNickname());
         if (ess.getSettings().realNamesOnList() && strippedNick != null && !strippedNick.equals(user.getName())) {
            groupString.append(" ").append(I18n.tlLiteral("listRealName", user.getName()));
         }

         groupString.append("<white>");
      }

      return groupString.toString();
   }

   public static String listSummary(final IEssentials ess, final User user, final boolean showHidden) {
      Server server = ess.getServer();
      int playerHidden = 0;
      int hiddenCount = 0;

      for (User onlinePlayer : ess.getOnlineUsers()) {
         if (onlinePlayer.isHidden() || user != null && onlinePlayer.isHiddenFrom(user.getBase())) {
            playerHidden++;
            if (showHidden || user != null && !onlinePlayer.isHiddenFrom(user.getBase())) {
               hiddenCount++;
            }
         }
      }

      String tlKey;
      Object[] objects;
      if (hiddenCount > 0) {
         tlKey = "listAmountHidden";
         objects = new Object[]{ess.getOnlinePlayers().size() - playerHidden, hiddenCount, server.getMaxPlayers()};
      } else {
         tlKey = "listAmount";
         objects = new Object[]{ess.getOnlinePlayers().size() - playerHidden, server.getMaxPlayers()};
      }

      return user == null ? I18n.tlLiteral(tlKey, objects) : user.playerTl(tlKey, objects);
   }

   public static Map<String, List<User>> getPlayerLists(final IEssentials ess, final IUser sender, final boolean showHidden) {
      Map<String, List<User>> playerList = new HashMap<>();

      for (User onlineUser : ess.getOnlineUsers()) {
         if ((sender != null || showHidden || !onlineUser.isHidden()) && (sender == null || showHidden || !onlineUser.isHiddenFrom(sender.getBase()))) {
            String group = FormatUtil.stripFormat(FormatUtil.stripEssentialsFormat(onlineUser.getGroup().toLowerCase()));
            List<User> list = playerList.computeIfAbsent(group, k -> new ArrayList<>());
            list.add(onlineUser);
         }
      }

      return playerList;
   }

   public static List<User> getMergedList(final IEssentials ess, final Map<String, List<User>> playerList, final String groupName) {
      Set<String> configGroups = ess.getSettings().getListGroupConfig().keySet();
      List<User> users = new ArrayList<>();

      for (String configGroup : configGroups) {
         if (configGroup.equalsIgnoreCase(groupName)) {
            String[] groupValues = ess.getSettings().getListGroupConfig().get(configGroup).toString().trim().split(" ");

            for (String groupValue : groupValues) {
               groupValue = groupValue.toLowerCase(Locale.ENGLISH);
               if (!groupValue.isEmpty()) {
                  List<User> u = playerList.get(groupValue.trim());
                  if (u != null && !u.isEmpty()) {
                     playerList.remove(groupValue);
                     users.addAll(u);
                  }
               }
            }
         }
      }

      return users;
   }

   public static String listGroupUsers(final IEssentials ess, final Map<String, List<User>> playerList, final String groupName) throws Exception {
      List<User> users = getMergedList(ess, playerList, groupName);
      List<User> groupUsers = playerList.get(groupName);
      if (groupUsers != null && !groupUsers.isEmpty()) {
         users.addAll(groupUsers);
      }

      if (users.isEmpty()) {
         throw new TranslatableException("groupDoesNotExist");
      } else {
         String displayGroupName = Character.toTitleCase(groupName.charAt(0)) + groupName.substring(1);
         return outputFormat(displayGroupName, listUsers(ess, users, ", "));
      }
   }

   public static String outputFormat(final String group, final String message) {
      return I18n.tlLiteral("listGroupTag", FormatUtil.replaceFormat(group)) + message;
   }

   public static List<String> prepareGroupedList(
      final IEssentials ess, final CommandSource source, final String commandLabel, final Map<String, List<User>> playerList
   ) {
      List<String> output = new ArrayList<>();
      Set<String> configGroups = ess.getSettings().getListGroupConfig().keySet();
      List<String> asterisk = new ArrayList<>();

      for (String oConfigGroup : configGroups) {
         String groupValue = ess.getSettings().getListGroupConfig().get(oConfigGroup).toString().trim();
         String configGroup = oConfigGroup.toLowerCase();
         if (groupValue.equals("*")) {
            asterisk.add(oConfigGroup);
         } else if (groupValue.equalsIgnoreCase("hidden")) {
            playerList.remove(configGroup);
         } else {
            List<User> matchedList = playerList.get(configGroup);
            if (NumberUtil.isInt(groupValue) && matchedList != null && !matchedList.isEmpty()) {
               playerList.remove(configGroup);
               List<User> outputUserList = new ArrayList<>(matchedList);
               int limit = Integer.parseInt(groupValue);
               if (matchedList.size() > limit) {
                  String tlKey = "groupNumber";
                  Object[] objects = new Object[]{matchedList.size(), commandLabel, FormatUtil.stripFormat(configGroup)};
                  output.add(outputFormat(oConfigGroup, source == null ? I18n.tlLiteral("groupNumber", objects) : source.tl("groupNumber", objects)));
               } else {
                  output.add(outputFormat(oConfigGroup, listUsers(ess, outputUserList, ", ")));
               }
            } else {
               List<User> outputUserList = getMergedList(ess, playerList, configGroup);
               if (!outputUserList.isEmpty()) {
                  output.add(outputFormat(oConfigGroup, listUsers(ess, outputUserList, ", ")));
               }
            }
         }
      }

      Set<String> var = playerList.keySet();
      String[] onlineGroups = var.toArray(new String[0]);
      Arrays.sort(onlineGroups, String.CASE_INSENSITIVE_ORDER);
      if (!asterisk.isEmpty()) {
         List<User> asteriskUsers = new ArrayList<>();

         for (String onlineGroup : onlineGroups) {
            asteriskUsers.addAll(playerList.get(onlineGroup));
         }

         for (String key : asterisk) {
            playerList.put(key, asteriskUsers);
         }

         onlineGroups = asterisk.toArray(new String[0]);
      }

      for (String onlineGroup : onlineGroups) {
         List<User> users = playerList.get(onlineGroup);
         String groupName = asterisk.isEmpty() ? users.get(0).getGroup() : onlineGroup;
         if (ess.getPermissionsHandler().getName().equals("ConfigPermissions")) {
            groupName = source == null ? I18n.tlLiteral("connectedPlayers") : source.tl("connectedPlayers");
         }

         if (users != null && !users.isEmpty()) {
            output.add(outputFormat(groupName, listUsers(ess, users, ", ")));
         }
      }

      return output;
   }
}
