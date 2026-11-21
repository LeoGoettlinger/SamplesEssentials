package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.aggr;

import java.util.Collection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.ConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.SearchProtocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.JedisCommandIterationBase;

public class FtAggregateIteration extends JedisCommandIterationBase<AggregationResult, Row> {
   private final String indexName;
   private final CommandArguments args;

   public FtAggregateIteration(ConnectionProvider connectionProvider, String indexName, AggregationBuilder aggr) {
      super(connectionProvider, AggregationResult.SEARCH_AGGREGATION_RESULT_WITH_CURSOR);
      if (!aggr.isWithCursor()) {
         throw new IllegalArgumentException("cursor must be set");
      } else {
         this.indexName = indexName;
         this.args = new CommandArguments(SearchProtocol.SearchCommand.AGGREGATE).add(this.indexName).addParams(aggr);
      }
   }

   protected boolean isNodeCompleted(AggregationResult reply) {
      return reply.getCursorId() == 0L;
   }

   @Override
   protected CommandArguments initCommandArguments() {
      return this.args;
   }

   protected CommandArguments nextCommandArguments(AggregationResult lastReply) {
      return new CommandArguments(SearchProtocol.SearchCommand.CURSOR)
         .add((Rawable)SearchProtocol.SearchKeyword.READ)
         .add(this.indexName)
         .add(lastReply.getCursorId());
   }

   protected Collection<Row> convertBatchToData(AggregationResult batch) {
      return batch.getRows();
   }
}
