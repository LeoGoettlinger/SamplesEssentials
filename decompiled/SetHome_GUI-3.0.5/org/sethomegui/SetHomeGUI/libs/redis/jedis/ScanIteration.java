package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.util.Collection;
import java.util.function.Function;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.ScanParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.ConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.ScanResult;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.JedisCommandIterationBase;

public class ScanIteration extends JedisCommandIterationBase<ScanResult<String>, String> {
   private final int count;
   private final Function<String, CommandArguments> args;

   public ScanIteration(ConnectionProvider connectionProvider, int batchCount, String match) {
      super(connectionProvider, BuilderFactory.SCAN_RESPONSE);
      this.count = batchCount;
      this.args = cursor -> new CommandArguments(Protocol.Command.SCAN)
         .add(cursor)
         .add((Rawable)Protocol.Keyword.MATCH)
         .add(match)
         .add((Rawable)Protocol.Keyword.COUNT)
         .add(this.count);
   }

   public ScanIteration(ConnectionProvider connectionProvider, int batchCount, String match, String type) {
      super(connectionProvider, BuilderFactory.SCAN_RESPONSE);
      this.count = batchCount;
      this.args = cursor -> new CommandArguments(Protocol.Command.SCAN)
         .add(cursor)
         .add((Rawable)Protocol.Keyword.MATCH)
         .add(match)
         .add((Rawable)Protocol.Keyword.COUNT)
         .add(this.count)
         .add((Rawable)Protocol.Keyword.TYPE)
         .add(type);
   }

   protected boolean isNodeCompleted(ScanResult<String> reply) {
      return reply.isCompleteIteration();
   }

   @Override
   protected CommandArguments initCommandArguments() {
      return this.args.apply(ScanParams.SCAN_POINTER_START);
   }

   protected CommandArguments nextCommandArguments(ScanResult<String> lastReply) {
      return this.args.apply(lastReply.getCursor());
   }

   protected Collection<String> convertBatchToData(ScanResult<String> batch) {
      return batch.getResult();
   }
}
