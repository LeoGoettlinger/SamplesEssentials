package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import org.bukkit.Server;
import org.bukkit.WeatherType;

public class Commandpweather extends EssentialsLoopCommand {
   private static final List<String> getAliases = Arrays.asList("get", "list", "show", "display");
   private static final Map<String, WeatherType> weatherAliases = new HashMap<>();

   public Commandpweather() {
      super("pweather");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0 || getAliases.contains(args[0].toLowerCase())) {
         if (args.length > 1) {
            if (args[1].equals("*") || args[1].equals("**")) {
               sender.sendTl("pWeatherPlayers");
            }

            this.loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> this.getUserWeather(sender, player));
            return;
         }

         if (args.length == 1 || sender.isPlayer()) {
            if (sender.isPlayer()) {
               this.getUserWeather(sender, sender.getUser());
               return;
            }

            throw new NotEnoughArgumentsException();
         }

         if (this.ess.getOnlinePlayers().size() > 1) {
            sender.sendTl("pWeatherPlayers");
         }

         for (User player : this.ess.getOnlineUsers()) {
            this.getUserWeather(sender, player);
         }
      }

      if (args.length > 1 && !sender.isAuthorized("essentials.pweather.others") && !args[1].equalsIgnoreCase(sender.getSelfSelector())) {
         sender.sendTl("pWeatherOthersPermission");
      } else {
         String weather = args[0].toLowerCase();
         if (!weatherAliases.containsKey(weather) && !weather.equalsIgnoreCase("reset")) {
            throw new NotEnoughArgumentsException(sender.tl("pWeatherInvalidAlias"));
         } else {
            StringJoiner joiner = new StringJoiner(", ");
            this.loopOnlinePlayersConsumer(server, sender, false, true, args.length > 1 ? args[1] : sender.getSelfSelector(), player -> {
               this.setUserWeather(player, weather);
               joiner.add(player.getName());
            });
            if (weather.equalsIgnoreCase("reset")) {
               sender.sendTl("pWeatherReset", joiner.toString());
            } else {
               sender.sendTl("pWeatherSet", weather, joiner.toString());
            }
         }
      }
   }

   private void getUserWeather(final CommandSource sender, final IUser user) {
      if (user != null) {
         if (user.getBase().getPlayerWeather() == null) {
            sender.sendTl("pWeatherNormal", user.getName());
         } else {
            sender.sendTl("pWeatherCurrent", user.getName(), user.getBase().getPlayerWeather().toString().toLowerCase(Locale.ENGLISH));
         }
      }
   }

   private void setUserWeather(final User user, final String weatherType) {
      if (weatherType.equalsIgnoreCase("reset")) {
         user.getBase().resetPlayerWeather();
      } else {
         user.getBase().setPlayerWeather(weatherAliases.get(weatherType));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"get", "reset", "storm", "sun"});
      } else {
         return args.length != 2 || !getAliases.contains(args[0]) && user != null && !user.isAuthorized("essentials.pweather.others")
            ? Collections.emptyList()
            : this.getPlayers(user);
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
   }

   static {
      weatherAliases.put("sun", WeatherType.CLEAR);
      weatherAliases.put("clear", WeatherType.CLEAR);
      weatherAliases.put("storm", WeatherType.DOWNFALL);
      weatherAliases.put("thunder", WeatherType.DOWNFALL);
   }
}
