package com.hutong.framework.util;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubQueueLogUtil {
	
	private static final PubQueueLogUtil pubQueueLogUtil = new PubQueueLogUtil();
	static {
		pubQueueLogUtil.initAndStart(1);
	}
	
	private static int MAX_ERROR_LOG_NUM = 20000;//请求日志最大累积数量  请求日志的优先级一般高一点  所以数字设置的大一些  优先级最高
	private static int MAX_WARN_LOG_NUM = 15000;//响应日志最大累积数量  优先级居中
	private static int MAX_INFO_LOG_NUM = 10000;//普通日志最大累积数量  优先级最低
	private static int MAX_DEBUG_LOG_NUM = 3000;//错误日志最大累积数量 
	
	private static final int OUTPUT_PER_COUNTS = 1000;//每累积多少个输出一次警告日志  注意  这里不能为0
	
	//丢弃的日志数量
	private long dropLogNum = 0;
	
	private Queue<LogContent> logQueue = new LinkedBlockingQueue<>();
	
	private ScheduledThreadPoolExecutor scheduleLogThread = null;
	
	public void initAndStart(int threadNum){
		scheduleLogThread = new ScheduledThreadPoolExecutor(threadNum, new MyThreadFactory("Public-LogThread-Group"));
		scheduleLogThread.scheduleAtFixedRate(new QueueLogThread(logQueue), 0, 1, TimeUnit.MILLISECONDS);
	}
	
	
	public static void logError(String errorStr){
		logError(errorStr, null);
	}
	
	public static void logError(String errorStr, Throwable throwable){
		if(pubQueueLogUtil.logQueue.size() >= MAX_ERROR_LOG_NUM){
			long dropNum = ++pubQueueLogUtil.dropLogNum;
			if(dropNum % OUTPUT_PER_COUNTS == 0){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.ERROR,"Pub offerErrorLog QueueLogService drop message num " + dropNum + " !!!"));
			}
		} else {
			if(throwable == null){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.ERROR, errorStr));
			} else {
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.ERROR, errorStr, throwable));
			}
		}
	}
	
	
	public static void logWarn(String warnStr){
		logWarn(warnStr, null);
	}
	
	public static void logWarn(String warnStr, Throwable throwable){
		if(pubQueueLogUtil.logQueue.size() >= MAX_WARN_LOG_NUM){
			long dropNum = ++pubQueueLogUtil.dropLogNum;
			if(dropNum % OUTPUT_PER_COUNTS == 0){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.WARN,"Pub offerWarnLog QueueLogService drop message num " + dropNum + " !!!"));
			}
		} else {
			if(throwable == null){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.WARN, warnStr));
			} else {
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.WARN, warnStr, throwable));
			}
		}
	}
	
	
	public static void logInfo(String infoStr){
		logInfo(infoStr, null);
	}
	
	public static void logInfo(String infoStr, Throwable throwable){
		if(pubQueueLogUtil.logQueue.size() >= MAX_INFO_LOG_NUM){
			long dropNum = ++pubQueueLogUtil.dropLogNum;
			if(dropNum % OUTPUT_PER_COUNTS == 0){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.INFO,"Pub offerInfoLog QueueLogService drop message num " + dropNum + " !!!"));
			}
		} else {
			if(throwable == null){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.INFO, infoStr));
			} else {
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.INFO, infoStr, throwable));
			}
		}
	}
	
	
	public static void logDebug(String debugStr){
		logDebug(debugStr, null);
	}
	
	public static void logDebug(String debugStr, Throwable throwable){
		if(pubQueueLogUtil.logQueue.size() >= MAX_DEBUG_LOG_NUM){
			long dropNum = ++pubQueueLogUtil.dropLogNum;
			if(dropNum % OUTPUT_PER_COUNTS == 0){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.DEBUG,"Pub offerDebugLog QueueLogService drop message num " + dropNum + " !!!"));
			}
		} else {
			if(throwable == null){
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.DEBUG, debugStr));
			} else {
				pubQueueLogUtil.logQueue.offer(new LogContent(LogLevel.DEBUG, debugStr, throwable));
			}
		}
	}
	
///======================================================
	private static enum LogLevel {
		DEBUG,INFO,WARN,ERROR;
	}
	
	private static class LogContent {
		LogLevel logLevel;
		String logContent;
		String threadName = Thread.currentThread().getName();
		
		Throwable throwable = null;
		
		public LogContent(LogLevel logLevel, String logContent) {
			this.logLevel = logLevel; 
			this.logContent = logContent;
		}
		
		public LogContent(LogLevel logLevel, String logContent, Throwable throwable){
			this(logLevel, logContent);
			this.throwable = throwable;
		}
	}
	
	
	public static class QueueLogThread implements Runnable{
		
		private static final Logger logger = LoggerFactory.getLogger(QueueLogThread.class);
		
		private Queue<LogContent> logQueue = null;
		
		public QueueLogThread(Queue<LogContent> queue){
			logQueue = queue;
		}
		
		@Override
        public void run() {
			
			LogContent logContent = logQueue.poll();
			
			while(logContent != null){
			
				if(logContent.logLevel == LogLevel.DEBUG){
					if(logContent.throwable != null){
						logger.debug("Public : [" + logContent.threadName + "]  " + logContent.logContent, logContent.throwable);
					} else {
						logger.debug("Public : [" + logContent.threadName + "]  " + logContent.logContent);
					}
				} else if(logContent.logLevel == LogLevel.INFO){
					if(logContent.throwable != null){
						logger.info("Public : [" + logContent.threadName + "]  " + logContent.logContent, logContent.throwable);
					} else {
						logger.info("Public : [" + logContent.threadName + "]  " + logContent.logContent);
					}
				} else if(logContent.logLevel == LogLevel.WARN){
					if(logContent.throwable != null){
						logger.warn("Public : [" + logContent.threadName + "]  " + logContent.logContent, logContent.throwable);
					} else {
						logger.warn("Public : [" + logContent.threadName + "]  " + logContent.logContent);
					}
				} else if(logContent.logLevel == LogLevel.ERROR){
					if(logContent.throwable != null){
						logger.error("Public : [" + logContent.threadName + "]  " + logContent.logContent, logContent.throwable);
					} else {
						logger.error("Public : [" + logContent.threadName + "]  " + logContent.logContent);
					}
				} else {
					if(logContent.throwable != null){
						logger.debug(" Public level is wrong, logLevel is " + logContent.logLevel + " [" + logContent.threadName + "]  " + logContent.logContent, logContent.throwable);
					} else {
						logger.debug(" Public level is wrong, logLevel is " + logContent.logLevel + " [" + logContent.threadName + "]  " + logContent.logContent);
					}				
				}
				
				logContent = logQueue.poll();
			}
		}
	}
	
	
	
	private class MyThreadFactory implements ThreadFactory {  
	   
		private String threadName;  
	   
	    public MyThreadFactory(String threadName) {  
	       this.threadName = threadName;  
	    }  
	   
	    @Override  
	    public Thread newThread(Runnable runnable) {  
	       Thread t = new Thread(runnable, threadName);  
	       return t;  
	    }  
	}
}
