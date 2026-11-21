package dev.cwhead.GravesX.integration;

import com.ranull.graves.Graves;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NoteBlockAPI {
   private static final String ALL_PLAYERS_KEY = "allPlayers";
   private final Graves plugin;
   private final Map<Object, SongPlayer> activeSongPlayers = new HashMap<>();

   public NoteBlockAPI(Graves plugin) {
      this.plugin = plugin;
   }

   public void playSongForPlayer(Player player, String fileName) {
      this.stopSongForPlayer(player);
      File songFile = new File(this.plugin.getDataFolder(), "nbs/" + fileName + ".nbs");
      if (!songFile.exists()) {
         player.sendMessage("The specified song file does not exist.");
      } else {
         Song song = NBSDecoder.parse(songFile);
         if (song == null) {
            player.sendMessage("Failed to load the song file.");
         } else {
            SongPlayer songPlayer = new RadioSongPlayer(song);
            songPlayer.setTick((short)(20.0F / song.getSpeed()));
            songPlayer.addPlayer(player);
            songPlayer.setPlaying(true);
            this.activeSongPlayers.put(player, songPlayer);
         }
      }
   }

   public void playSongForAllPlayers(String fileName) {
      this.stopSongForAllPlayers();
      File songFile = new File(this.plugin.getDataFolder(), "nbs/" + fileName + ".nbs");
      if (!songFile.exists()) {
         Bukkit.getLogger().warning("The specified song file does not exist.");
      } else {
         Song song = NBSDecoder.parse(songFile);
         if (song == null) {
            Bukkit.getLogger().warning("Failed to load the song file.");
         } else {
            SongPlayer songPlayer = new RadioSongPlayer(song);
            songPlayer.setTick((short)(20.0F / song.getSpeed()));

            for (Player player : Bukkit.getOnlinePlayers()) {
               songPlayer.addPlayer(player);
            }

            songPlayer.setPlaying(true);
            this.activeSongPlayers.put("allPlayers", songPlayer);
         }
      }
   }

   public void stopSongForPlayer(Player player) {
      SongPlayer songPlayer = this.activeSongPlayers.remove(player);
      if (songPlayer != null) {
         songPlayer.setPlaying(false);
         songPlayer.destroy();
      }
   }

   public void stopSongForPlayer(UUID uuid) {
      SongPlayer songPlayer = this.activeSongPlayers.remove(uuid);
      if (songPlayer != null) {
         songPlayer.setPlaying(false);
         songPlayer.destroy();
      }
   }

   public void stopSongForAllPlayers() {
      SongPlayer songPlayer = this.activeSongPlayers.remove("allPlayers");
      if (songPlayer != null) {
         songPlayer.setPlaying(false);
         songPlayer.destroy();
      }
   }

   public void stopAllSongs() {
      for (SongPlayer songPlayer : this.activeSongPlayers.values()) {
         songPlayer.setPlaying(false);
         songPlayer.destroy();
      }

      this.activeSongPlayers.clear();
   }

   public boolean isSongPlayingForPlayer(Player player) {
      SongPlayer songPlayer = this.activeSongPlayers.get(player);
      return songPlayer != null && songPlayer.isPlaying();
   }

   public boolean isSongPlayingForPlayer(UUID uuid) {
      SongPlayer songPlayer = this.activeSongPlayers.get(uuid);
      return songPlayer != null && songPlayer.isPlaying();
   }

   public boolean isSongPlayingForAllPlayers() {
      SongPlayer songPlayer = this.activeSongPlayers.get("allPlayers");
      return songPlayer != null && songPlayer.isPlaying();
   }
}
