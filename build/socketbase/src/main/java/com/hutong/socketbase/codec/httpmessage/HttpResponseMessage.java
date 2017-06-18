package com.hutong.socketbase.codec.httpmessage;

import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseMessage {
        private HttpVersion version;
        private Map<String, String> headsParams;
        private String content = "";

        public HttpResponseMessage(HttpRequestMessage request) {
                this.version = request.getVersion();
                this.headsParams = request.getHeadsParams();
        }

        public HttpResponseMessage() {
                this.version = HttpVersion.HTTP_1_1;
                this.headsParams = new HashMap<String, String>();
        }

        public void setVersion(HttpVersion version) {
                this.version = version;
        }

        public HttpVersion getVersion() {
                return version;
        }

        public Map<String, String> getHeadsParams() {
                return headsParams;
        }

        public void setHeadsParams(Map<String, String> headsParams) {
                this.headsParams = headsParams;
        }

        public String getContent() {
                return content;
        }

        public void setContent(String content) {
                this.content = content;
        }

}
