package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.net.InetSocketAddress;
import java.util.Date;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Ban")
@Description({"Bans or unbans a player or an IP address.", "If a reason is given, it will be shown to the player when they try to join the server while banned.", "A length of ban may also be given to apply a temporary ban. If it is absent for any reason, a permanent ban will be used instead.", "We recommend that you test your scripts so that no accidental permanent bans are applied.", "", "Note that banning people does not kick them from the server.", "You can optionally use 'and kick' or consider using the <a href='#EffKick'>kick effect</a> after applying a ban."})
@Example.Examples({@Example("unban player"), @Example("ban \"127.0.0.1\""), @Example("IP-ban the player because \"he is an idiot\""), @Example("ban player due to \"inappropriate language\" for 2 days"), @Example("ban and kick player due to \"inappropriate language\" for 2 days")})
@Since({"1.4, 2.1.1 (ban reason), 2.5 (timespan), 2.9.0 (kick)"})
public class EffBan extends Effect {
   private static final String SKRIPT_BAN_SOURCE = "Skript ban effect";
   private Expression<?> players;
   @Nullable
   private Expression<Component> reason;
   @Nullable
   private Expression<Timespan> expires;
   private boolean ban;
   private boolean ipBan;
   private boolean kick;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffBan.class)
            .supplier(EffBan::new)
            .addPatterns(
               "ban [kick:and kick] %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
               "unban %strings/offlineplayers%",
               "ban [kick:and kick] %players% by IP [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
               "unban %players% by IP",
               "IP(-| )ban [kick:and kick] %players% [(by reason of|because [of]|on account of|due to) %-textcomponent%] [for %-timespan%]",
               "(IP(-| )unban|un[-]IP[-]ban) %players%"
            )
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = exprs[0];
      this.reason = (Expression<Component>)(exprs.length > 1 ? exprs[1] : null);
      this.expires = (Expression<Timespan>)(exprs.length > 1 ? exprs[2] : null);
      this.ban = matchedPattern % 2 == 0;
      this.ipBan = matchedPattern >= 2;
      this.kick = parseResult.hasTag("kick");
      return true;
   }

   @Override
   protected void execute(Event event) {
      Component reason = this.reason == null ? null : this.reason.getSingle(event);
      Timespan duration = this.expires == null ? null : this.expires.getSingle(event);
      Date expires = duration == null ? null : new Date(System.currentTimeMillis() + duration.getAs(Timespan.TimePeriod.MILLISECOND));

      for (Object object : this.players.getArray(event)) {
         switch (object) {
            case Player player:
               if (this.ipBan) {
                  InetSocketAddress address = player.getAddress();
                  if (address == null) {
                     return;
                  }

                  String ipx = address.getAddress().getHostAddress();
                  BanList<?> banList = Bukkit.getBanList(Type.IP);
                  if (this.ban) {
                     banList.addBan(ipx, toLegacyString(reason), expires, "Skript ban effect");
                  } else {
                     banList.pardon(ipx);
                  }
               } else {
                  BanList<?> banList = Bukkit.getBanList(Type.NAME);
                  if (this.ban) {
                     banList.addBan(player.getName(), toLegacyString(reason), expires, "Skript ban effect");
                  } else {
                     banList.pardon(player.getName());
                  }
               }

               if (this.kick) {
                  player.kick(reason);
               }
               break;
            case OfflinePlayer offlinePlayer:
               String name = offlinePlayer.getName();
               if (name == null) {
                  return;
               }

               BanList<?> banList = Bukkit.getBanList(Type.NAME);
               if (this.ban) {
                  banList.addBan(name, toLegacyString(reason), expires, "Skript ban effect");
               } else {
                  banList.pardon(name);
               }
               break;
            case String ip:
               BanList<?> ipBanList = Bukkit.getBanList(Type.IP);
               BanList<?> nameBanList = Bukkit.getBanList(Type.NAME);
               if (this.ban) {
                  String legacyReason = toLegacyString(reason);
                  ipBanList.addBan(ip, legacyReason, expires, "Skript ban effect");
                  nameBanList.addBan(ip, legacyReason, expires, "Skript ban effect");
               } else {
                  ipBanList.pardon(ip);
                  nameBanList.pardon(ip);
               }
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + object);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .appendIf(this.ipBan, "IP")
         .append(this.ban ? "ban" : "unban")
         .appendIf(this.kick, "and kick")
         .append(this.players)
         .appendIf(this.reason != null, "on account of", this.reason)
         .appendIf(this.expires != null, "for", this.expires)
         .toString();
   }

   @Nullable
   private static String toLegacyString(@Nullable Component component) {
      return component == null ? null : TextComponentParser.instance().toLegacyString(component);
   }
}
