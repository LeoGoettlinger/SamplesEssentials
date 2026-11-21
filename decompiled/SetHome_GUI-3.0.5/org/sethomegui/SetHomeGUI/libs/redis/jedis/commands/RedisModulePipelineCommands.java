package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.commands.RedisBloomPipelineCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.graph.RedisGraphPipelineCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.json.commands.RedisJsonPipelineCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.RediSearchPipelineCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries.RedisTimeSeriesPipelineCommands;

public interface RedisModulePipelineCommands
   extends RediSearchPipelineCommands,
   RedisJsonPipelineCommands,
   RedisTimeSeriesPipelineCommands,
   RedisBloomPipelineCommands,
   RedisGraphPipelineCommands {
}
