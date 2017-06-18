package com.hutong.socketbase.codec.codec;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hutong.socketbase.codec.httpmessage.HttpRequestMessage;
import com.hutong.socketbase.codec.httpmessage.HttpResponseMessage;

public class CommonHttpCodec extends MessageToMessageCodec<HttpObject, HttpResponseMessage> {

        private Logger logger = Logger.getLogger(getClass());

        private HttpRequestMessage request;
        private boolean readingChunks;
        private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
        private HttpPostRequestDecoder decoder;

        @Override
        protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> list) throws Exception {
                if (msg instanceof HttpRequest) {
                        HttpRequest request = (HttpRequest) msg;
                        URI uri = new URI(request.getUri());
                        if (uri.getPath().equals("/favicon.ico")) {
                                return;
                        }

                        if (this.request == null) {
                                this.request = new HttpRequestMessage();
                        }

                        this.request.setVersion(request.getProtocolVersion());
                        this.request.setUri(uri.getPath());
                        this.request.setRemoteIp(ctx.channel().remoteAddress().toString());

                        //拼装head信息
                        for (Entry<String, String> entry : request.headers()) {
                                this.request.getHeadsParams().put(entry.getKey(), entry.getValue());
                        }

                        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
                        this.request.setGetParams(decoderQuery.parameters());

                        if (request.getMethod().equals(HttpMethod.GET)) {
                                list.add(this.request);
                                this.reset();
                                return;
                        }

                        //判断request请求是否是post请求
                        if (request.getMethod().equals(HttpMethod.POST)) {
                                try {
                                        decoder = new HttpPostRequestDecoder(factory, request);
                                } catch (ErrorDataDecoderException e1) {
                                        logger.error("post 请求实例化post解析器错误！", e1);
                                        ctx.channel().close();
                                        return;
                                }

                                readingChunks = HttpHeaders.isTransferEncodingChunked(request);
                                if (readingChunks) {
                                        readingChunks = true;
                                }
                        }
                }

                if (decoder != null) {
                        if (msg instanceof HttpContent) {
                                HttpContent chunk = (HttpContent) msg;
                                try {
                                        decoder.offer(chunk);
                                } catch (ErrorDataDecoderException e1) {
                                        logger.error("post 请求 data数据写入解码器错误！", e1);
                                        ctx.channel().close();
                                        return;
                                }
                                try {
                                        while (decoder.hasNext()) {
                                                InterfaceHttpData data = decoder.next();
                                                if (data != null) {
                                                        try {
                                                                writeHttpData(data);
                                                        } finally {
                                                                data.release();
                                                        }
                                                }
                                        }
                                } catch (EndOfDataDecoderException e1) {
                                        logger.debug("读取post数据信息读取到尾部！");
                                }

                                list.add(this.request);
                                if (chunk instanceof LastHttpContent) {
                                        readingChunks = false;
                                        reset();
                                }
                        }
                }
        }

        private void writeHttpData(InterfaceHttpData data) {
                /**
                 * HttpDataType有三种类型：Attribute, FileUpload, InternalAttribute
                 * 目前只处理Attribute
                 */
                if (data.getHttpDataType() == HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        try {
                                this.request.getPostParams().put(attribute.getName(), attribute.getValue());
                        } catch (IOException e1) {
                                logger.error("解析post内容的" + attribute.getHttpDataType().name() + ":" + attribute.getName() + " 出错", e1);
                                return;
                        }
                }
        }

        private void reset() {
                request = null;
                if (decoder != null) {
                        decoder.destroy();
                        decoder = null;
                }
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, HttpResponseMessage responseVo, List<Object> list) throws Exception {
                byte[] countBytes = responseVo.getContent().getBytes("UTF-8");
                ByteBuf buf = ctx.alloc().buffer(countBytes.length);
                buf.writeBytes(countBytes);
                FullHttpResponse response = new DefaultFullHttpResponse(responseVo.getVersion(), HttpResponseStatus.OK, buf);
                for (String key : responseVo.getHeadsParams().keySet()) {
                        response.headers().set(key, responseVo.getHeadsParams().get(key));
                }
                response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, buf.readableBytes());
                list.add(response);
        }

}
