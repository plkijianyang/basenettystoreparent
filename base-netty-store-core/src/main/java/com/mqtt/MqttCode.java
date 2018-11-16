package com.mqtt;

import com.alipay.remoting.codec.Codec;
import io.netty.channel.ChannelHandler;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public class MqttCode implements Codec {
	@Override
	public ChannelHandler newEncoder() {
		return null;
	}

	@Override
	public ChannelHandler newDecoder() {
		return null;
	}
}
