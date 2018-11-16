package com;


import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.mqtt.MqttServer;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public abstract class BoltServer {
	private int port;
	private MqttServer server;

	public BoltServer(int port){
		this.port = port;
		this.server = new MqttServer(this.port);
	}

	public BoltServer(int port,boolean manageFeatureEnabled){
		this.port = port;
		this.server = new MqttServer(this.port,manageFeatureEnabled);
	}

	public BoltServer(int port,boolean manageFeatureEnabled,boolean syncStop){
		this.port = port;
		this.server = new MqttServer(this.port,manageFeatureEnabled,syncStop);
	}

	public boolean start() {
		this.server.start();
		return true;
	}

	public void stop() {
		this.server.stop();
	}

	public MqttServer getMqttServer(){
		return this.server;
	}

	public void registerUserProcessor(UserProcessor<?> processor) {
		this.server.registerUserProcessor(processor);
	}

	public void addConnectionEventProcessor(ConnectionEventType type,
											ConnectionEventProcessor processor) {
		this.server.addConnectionEventProcessor(type, processor);
	}
}
