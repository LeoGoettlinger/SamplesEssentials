package ac.grim.grimac.utils.latency;

public interface ILatencyUtils {
   void addRealTimeTask(int var1, Runnable var2);

   void addRealTimeTaskAsync(int var1, Runnable var2);

   void handleNettySyncTransaction(int var1);
}
