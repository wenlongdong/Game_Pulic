package com.hutong.socketbase.codec.httpmessage;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class HttpRequestMessage {
        Logger logger = Logger.getLogger(getClass());

        private HttpMethod method;
        private HttpVersion version;
        private String remoteIp;
        private String uri;
        private Map<String, String> headsParams;
        private Map<String, List<String>> getParams;
        private Map<String, String> postParams;

        public HttpRequestMessage() {
                this.headsParams = new HashMap<String, String>();
                this.getParams = new HashMap<String, List<String>>();
                this.postParams = new HashMap<String, String>();
        }

        public Map<String, String> getHeadsParams() {
                return headsParams;
        }

        public void setHeadsParams(Map<String, String> headsParams) {
                this.headsParams = headsParams;
        }

        public HttpVersion getVersion() {
                return version;
        }

        public void setVersion(HttpVersion version) {
                this.version = version;
        }

        public String getRemoteIp() {
			return remoteIp;
		}
        
        public void setRemoteIp(String remoteIp) {
			this.remoteIp = remoteIp;
		}
        
        public String getUri() {
                return uri;
        }

        public void setUri(String uri) {
                this.uri = uri;
        }

        public HttpMethod getMethod() {
                return method;
        }

        public void setMethod(HttpMethod method) {
                this.method = method;
        }

        public Map<String, List<String>> getGetParams() {
                return getParams;
        }
        
        public Map<String, String> getGetStringParams() {
        	if(this.getParams != null && this.getParams.size() > 0){
        		Map<String, String> getStringParams = new HashMap<>(this.getParams.size());
        		for(Entry<String, List<String>> entry : this.getParams.entrySet()){
        			List<String> values = entry.getValue();
        			StringBuffer getString = new StringBuffer();
        			for(String value : values){
        				getString.append(value).append(",");
        			}
        			String get = null;
        			if(getString.length() > 0){
        				get = getString.substring(0, getString.length() - 1);
        			}
        			getStringParams.put(entry.getKey(), get);
        		}
        		return getStringParams;
        	}
            return null;
        }

        public void setGetParams(Map<String, List<String>> getParams) {
                this.getParams = getParams;
        }

        public Map<String, String> getPostParams() {
                return postParams;
        }

        public void setPostParams(Map<String, String> postParams) {
                this.postParams = postParams;
        }

        //工具方法=====================================
        public int getIntFromPost(String key) {
                String value = this.postParams.get(key);
                if (value == null) {
                        logger.warn("从post中获得【" + key + "】值为空");
                        return 0;
                }
                try {
                        return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                        logger.error("从post中获得【" + key + "】值为不为数字");
                        return 0;
                }
        }

        public long getLongFromPost(String key) {
                String value = this.postParams.get(key);
                if (value == null) {
                        logger.warn("从post中获得【" + key + "】值为空");
                        return 0;
                }
                try {
                        return Long.valueOf(value);
                } catch (NumberFormatException e) {
                        logger.error("从post中获得【" + key + "】值为不为long数字");
                        return 0;
                }
        }

        public String getStringFromPost(String key) {
                return this.postParams.get(key);
        }
}
