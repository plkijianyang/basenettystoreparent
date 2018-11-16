package com.mqtt;

import com.alipay.remoting.BaseRemoting;
import com.alipay.remoting.CommandFactory;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public abstract class MqttRemoting extends BaseRemoting{

	public MqttRemoting(CommandFactory commandFactory) {
		super(commandFactory);
	}
}
