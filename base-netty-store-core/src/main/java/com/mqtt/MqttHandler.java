package com.mqtt;

import com.alipay.remoting.rpc.protocol.UserProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public class MqttHandler extends ChannelInboundHandlerAdapter {
	private boolean                                     serverSide;

	private ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

	public MqttHandler(){
		serverSide = false;
	}

	public MqttHandler(ConcurrentHashMap<String,UserProcessor<?>> userProcessors){
		serverSide = false;
		this.userProcessors = userProcessors;
	}

	public MqttHandler(boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> userProcessors){
		this.serverSide = serverSide;
		this.userProcessors = userProcessors;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
}
