package com.mqtt;

import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public class RpcConnectionEventHandler extends ConnectionEventHandler {

	public RpcConnectionEventHandler() {
		super();
	}

	public RpcConnectionEventHandler(GlobalSwitch globalSwitch) {
		super(globalSwitch);
	}

}
