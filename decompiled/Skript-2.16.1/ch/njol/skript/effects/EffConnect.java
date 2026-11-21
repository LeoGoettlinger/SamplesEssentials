package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Connect")
@Description({"Connect a player to a server running on your proxy, or any server supporting transfers. Read below for more information.", "If the server is running Minecraft 1.20.5 or above, you may specify an IP and Port to transfer a player over to that server.", "When transferring players using an IP, the transfer will not complete if the `accepts-transfers` option isn't enabled in `server.properties` for the server specified.", "If the port is not provided, it will default to `25565`."})
@Example.Examples({@Example("connect all players to proxy server \"hub\""), @Example("transfer player to server \"my.server.com\""), @Example("transfer player to server \"localhost\" on port 25566")})
@Since({"2.3, 2.10 (transfer)"})
public class EffConnect extends Effect {
   public static final String BUNGEE_CHANNEL = "BungeeCord";
   public static final String GET_SERVERS_CHANNEL = "GetServers";
   public static final String CONNECT_CHANNEL = "Connect";
   private static final boolean TRANSFER_METHOD_EXISTS = Skript.methodExists(Player.class, "transfer", String.class, int.class);
   private Expression<Player> players;
   private Expression<String> server;
   private Expression<Number> port;
   private boolean transfer;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.server = (Expression<String>)exprs[1];
      this.transfer = matchedPattern == 2;
      if (this.transfer) {
         this.port = (Expression<Number>)exprs[2];
         if (!TRANSFER_METHOD_EXISTS) {
            Skript.error("Transferring players via IP is not available on this version.");
            return false;
         }
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      String server = this.server.getSingle(event);
      Player[] players = this.players.stream(event).filter(OfflinePlayer::isOnline).toArray(Player[]::new);
      if (server != null && players.length != 0) {
         if (this.transfer) {
            if (this.port != null) {
               Number portNum = this.port.getSingle(event);
               if (portNum == null) {
                  return;
               }

               int port = portNum.intValue();

               for (Player player : players) {
                  player.transfer(server, port);
               }
            } else {
               int defaultPort = 25565;

               for (Player player : players) {
                  player.transfer(server, defaultPort);
               }
            }
         } else {
            Utils.sendPluginMessage(players[0], "BungeeCord", r -> "GetServers".equals(r.readUTF()), "GetServers").thenAccept(response -> {
               for (String validServer : response.readUTF().split(", ")) {
                  if (validServer.equalsIgnoreCase(server)) {
                     for (Player playerx : players) {
                        Utils.sendPluginMessage(playerx, "BungeeCord", "Connect", validServer);
                     }
                     break;
                  }
               }
            });
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.transfer) {
         String portString = this.port != null ? " on port " + this.port.toString(event, debug) : "";
         return "transfer " + this.players.toString(event, debug) + " to server " + this.server.toString(event, debug) + portString;
      } else {
         return "connect " + this.players.toString(event, debug) + " to proxy server " + this.server.toString(event, debug);
      }
   }

   static {
      Skript.registerEffect(
         EffConnect.class,
         "connect %players% to [proxy|bungeecord] [server] %string%",
         "send %players% to [proxy|bungeecord] server %string%",
         "transfer %players% to server %string% [on port %-number%]"
      );
   }
}
