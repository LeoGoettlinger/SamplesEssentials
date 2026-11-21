package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol;

public enum ConnectionState {
   HANDSHAKING,
   STATUS,
   LOGIN,
   PLAY,
   CONFIGURATION;

   public static ConnectionState getById(int id) {
      return id < values().length && id >= 0 ? values()[id] : null;
   }
}
