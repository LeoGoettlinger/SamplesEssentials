package vrx.economy.vRXClans;

public record WarHistoryEntry(
   long timestamp, String winner, String loser, String clanA, String clanB, int winnerKills, int loserKills, double pot, long durationMillis
) {
}
