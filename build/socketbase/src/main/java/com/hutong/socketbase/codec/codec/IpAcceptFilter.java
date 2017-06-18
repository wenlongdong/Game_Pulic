package com.hutong.socketbase.codec.codec;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;

/**
 * 白名单过滤器
 * @author Jay
 *
 */
@Sharable
public class IpAcceptFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {
	private final IpFilterRule[] rules;

    public IpAcceptFilter(IpFilterRule... rules) {
        if (rules == null) {
            throw new NullPointerException("rules");
        }

        this.rules = rules;
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        for (IpFilterRule rule : rules) {
            if (rule == null) {
                break;
            }

            if (rule.matches(remoteAddress)) {
                return rule.ruleType() == IpFilterRuleType.ACCEPT;
            }
        }

        return false;
    }
}
