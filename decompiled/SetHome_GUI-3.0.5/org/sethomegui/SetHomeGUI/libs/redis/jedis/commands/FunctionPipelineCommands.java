package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.FlushMode;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.FunctionRestorePolicy;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.FunctionStats;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.LibraryInfo;

public interface FunctionPipelineCommands {
   Response<Object> fcall(String var1, List<String> var2, List<String> var3);

   Response<Object> fcallReadonly(String var1, List<String> var2, List<String> var3);

   Response<String> functionDelete(String var1);

   Response<byte[]> functionDump();

   Response<String> functionFlush();

   Response<String> functionFlush(FlushMode var1);

   Response<String> functionKill();

   Response<List<LibraryInfo>> functionList();

   Response<List<LibraryInfo>> functionList(String var1);

   Response<List<LibraryInfo>> functionListWithCode();

   Response<List<LibraryInfo>> functionListWithCode(String var1);

   Response<String> functionLoad(String var1);

   Response<String> functionLoadReplace(String var1);

   Response<String> functionRestore(byte[] var1);

   Response<String> functionRestore(byte[] var1, FunctionRestorePolicy var2);

   Response<FunctionStats> functionStats();
}
