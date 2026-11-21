package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.nio.charset.StandardCharsets;

public final class ConnectionDetails {
   public static final String PROXY_CHANNEL = "vv:proxy_details";
   public static final String SERVER_CHANNEL = "vv:server_details";
   public static final String MOD_CHANNEL = "vv:mod_details";
   public static final String APP_CHANNEL = "vv:app_details";
   private static final int VERSION = 1;

   public static void sendConnectionDetails(UserConnection connection, String channel) {
      sendPlayerDetails(connection, channel);
      sendServerDetails(connection, channel);
   }

   public static void sendPlayerDetails(UserConnection connection, String channel) {
      if (Via.getConfig().sendPlayerDetails()) {
         JsonObject payload = new JsonObject();
         payload.addProperty("specVersion", 1);
         String platformName = Via.getPlatform().getPlatformName();
         String platformVersion = Via.getPlatform().getPlatformVersion();
         ProtocolInfo protocolInfo = connection.getProtocolInfo();
         ProtocolVersion clientVersion = protocolInfo.protocolVersion();
         payload.addProperty("platformName", platformName);
         payload.addProperty("platformVersion", platformVersion);
         payload.addProperty("version", clientVersion.getOriginalVersion());
         payload.addProperty("versionName", clientVersion.getName());
         payload.addProperty("versionType", clientVersion.getVersionType().name());
         Via.getPlatform().modifyPlayerDetails(connection, payload);
         Via.getPlatform().sendCustomPayload(connection, channel, payload.toString().getBytes(StandardCharsets.UTF_8));
      }
   }

   public static void sendServerDetails(UserConnection connection, String channel) {
      if (Via.getConfig().sendServerDetails()) {
         JsonObject payload = new JsonObject();
         payload.addProperty("specVersion", 1);
         ProtocolInfo protocolInfo = connection.getProtocolInfo();
         ProtocolVersion serverVersion = protocolInfo.serverProtocolVersion();
         payload.addProperty("version", serverVersion.getOriginalVersion());
         payload.addProperty("versionName", serverVersion.getName());
         payload.addProperty("versionType", serverVersion.getVersionType().name());
         Via.getPlatform().modifyServerDetails(connection, payload);
         Via.getPlatform().sendCustomPayloadToClient(connection, channel, payload.toString().getBytes(StandardCharsets.UTF_8));
      }
   }
}
