package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.commands;

public interface RedisBloomPipelineCommands
   extends BloomFilterPipelineCommands,
   CuckooFilterPipelineCommands,
   CountMinSketchPipelineCommands,
   TopKFilterPipelineCommands,
   TDigestSketchPipelineCommands {
}
