package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.commands.RedisBloomCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.gears.RedisGearsCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.graph.RedisGraphCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.json.commands.RedisJsonCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.RediSearchCommands;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries.RedisTimeSeriesCommands;

public interface RedisModuleCommands
   extends RediSearchCommands,
   RedisJsonCommands,
   RedisTimeSeriesCommands,
   RedisBloomCommands,
   RedisGraphCommands,
   RedisGearsCommands {
}
