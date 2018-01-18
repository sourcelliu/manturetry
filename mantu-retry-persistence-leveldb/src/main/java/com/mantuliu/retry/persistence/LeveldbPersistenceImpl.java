package com.mantuliu.retry.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.iq80.leveldb.WriteOptions;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mantuliu.retry.core.call.PersistenceService;
import com.mantuliu.retry.core.common.Commons;
import com.mantuliu.retry.core.common.ServiceSpi;
import com.mantuliu.retry.core.entity.RetryUnion;

public class LeveldbPersistenceImpl implements PersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(LeveldbPersistenceImpl.class);
    private static Charset charset = Charset.forName("utf-8");
    private static DB levelDBConnection;
    private static boolean isRebuild = false;
    private String leveldbPath = "./";

	@Override
    public void insert(RetryUnion retryUnion) {
        String jsonValue = "";
        try {
            //synchronized(LevelDBStorageServiceImpl.class) {
                jsonValue = JSONObject.toJSONString(retryUnion);
                WriteOptions writeOptions = new WriteOptions().sync(true);//线程安全
                DB db = getDB();
                db.put(retryUnion.getRetryKey().getBytes(charset),jsonValue.getBytes(charset));
            //}
        }catch(Exception ex) {
            logger.error("retry framework warn.insert to leveldb failure. the retryunion is {} ",jsonValue,ex);
            closeDB();
        }
    }
    private void delete(RetryUnion retryUnion) {
        try {
            DB db = getDB();
            WriteOptions writeOptions = new WriteOptions().sync(true);//线程安全
            db.delete(retryUnion.getRetryKey().getBytes(charset),writeOptions);
        }catch(Exception ex) {
            logger.error("retry framework warn.delete from leveldb failure. the retryunion is {} ",JSONObject.toJSONString(retryUnion),ex);
            closeDB();
        }
    }
    
    @Override
    public void executeSuccess(RetryUnion retryUnion) {
        delete(retryUnion);
        String retryKey = retryUnion.getRetryKey().replaceAll(Commons.RETRY_KEY, Commons.SUCCESS_KEY);
        retryUnion.setRetryKey(retryKey);
        insert(retryUnion);
        
    }
    @Override
    public void executeDead(RetryUnion retryUnion) {
    	logger.info("retry task dead.retry task go to dead queue.the retryunion is {}",retryUnion.toString());
        delete(retryUnion);
        String retryKey = retryUnion.getRetryKey().replaceAll(Commons.RETRY_KEY, Commons.DEAD_KEY);
        retryUnion.setRetryKey(retryKey);
        insert(retryUnion);
    }
    
    private DB getDB() {
        if(levelDBConnection==null) {
            synchronized(LeveldbPersistenceImpl.class) {
                try {
                	if(levelDBConnection!=null){
                		return levelDBConnection;
                	}
                    DBFactory factory = Iq80DBFactory.factory;
                    File dir = new File(leveldbPath);
                    Options options = new Options().createIfMissing(true);
                    levelDBConnection = factory.open(dir,options);
                } catch (Exception e) {
                    logger.error("retry framework warn.create levelDBConnection failure ",e);
                }
            }
        }
        return levelDBConnection;
    }
    
    private void closeDB() {
        if(levelDBConnection != null) {
            synchronized(LeveldbPersistenceImpl.class) {
                try {
                    levelDBConnection.close();
                } catch (Exception e) {
                    logger.error("close levelDBConnection failure ",e);
                }
                levelDBConnection = null;
            }
        }
    }
    
    @Override
    public void rebuildRetryTask() {
        if(LeveldbPersistenceImpl.isRebuild==false) {
            synchronized(LeveldbPersistenceImpl.class) {
                if(LeveldbPersistenceImpl.isRebuild==false) {
                    long beginTime = System.currentTimeMillis();
                    logger.info("begin rebuild retrytask from leveldb. ");
                    DB db = getDB();
                    Snapshot snapshot = db.getSnapshot();
                    ReadOptions readOptions = new ReadOptions();
                    readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
                    readOptions.snapshot(snapshot);//默认snapshot为当前
                    DBIterator iterator = db.iterator(readOptions);
                    int count = 0;
                    int failureCount = 0;
                    while (iterator.hasNext()) {
                        Map.Entry<byte[],byte[]> item = iterator.next();
                        String key = new String(item.getKey(),LeveldbPersistenceImpl.charset);
                        if(key.startsWith(Commons.RETRY_KEY+Commons.SERVICENAME)) {
                            count++;
                            String value = new String(item.getValue(),LeveldbPersistenceImpl.charset);
                            try {
                                if(StringUtils.isEmpty(value)) {
                                    continue;
                                }
                                RetryUnion retryUnion = JSONObject.parseObject(value, RetryUnion.class);
                                logger.info(retryUnion.toString());
                                if(retryUnion!=null) {
                                	ServiceSpi.scheduleService.scheduleRetry(retryUnion);
                                }else {
                                    failureCount++;
                                    logger.warn("Deserialize from leveldb failure the key is {} the value is {}",key,value);
                                }
                            }catch(Exception ex) {
                                failureCount++;
                                logger.warn("Deserialize from leveldb failure the key is {} the value is {}",key,value,ex);
                            }
                        }
                    }
                    try {
                        iterator.close();
                    } catch (IOException e) {}
                    LeveldbPersistenceImpl.isRebuild=true;
                    long costTime = (System.currentTimeMillis()-beginTime)/1000;
                    logger.info("end rebuild retrytask from leveldb cost {} seconds. rebuild {} tasks . failure {} tasks.",costTime,count,failureCount);
                }
            }
        }
    }
    public String getLeveldbPath() {
		return leveldbPath;
	}
	public void setLeveldbPath(String leveldbPath) {
		this.leveldbPath = leveldbPath;
	}
}
