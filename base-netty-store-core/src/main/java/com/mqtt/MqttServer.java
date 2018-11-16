package com.mqtt;

import com.alipay.remoting.*;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RpcAddressParser;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.remoting.util.NettyEventLoopUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @Author:wjy
 * @Date: 2018/11/16.
 */
public class MqttServer extends AbstractRemotingServer implements RemotingServer {

	private static final Logger logger                  = BoltLoggerFactory.getLogger("RpcRemoting");

	private ConnectionEventListener                     connectionEventListener = new ConnectionEventListener();

	private RemotingAddressParser                       addressParser;

	private ConnectionEventHandler                      connectionEventHandler;

	private DefaultConnectionManager                    connectionManager;

	protected MqttRemoting 								mqttRemoting;

	private ServerBootstrap 							bootstrap;

	private final EventLoopGroup bossGroup               = NettyEventLoopUtil
			.newEventLoopGroup(
					1,
					new NamedThreadFactory(
							"Rpc-netty-server-boss",
							false));
	/** worker event loop group. Reuse I/O worker threads between rpc servers. */
	private static final EventLoopGroup                 workerGroup             = NettyEventLoopUtil
			.newEventLoopGroup(
					Runtime
							.getRuntime()
							.availableProcessors() * 2,
					new NamedThreadFactory(
							"Rpc-netty-server-worker",
							true));

	private ConcurrentHashMap<String, UserProcessor<?>> userProcessors          = new ConcurrentHashMap<String, UserProcessor<?>>(
			4);

	public MqttServer(int port) {
		this(port, false);
	}

	public MqttServer(String ip, int port) {
		this(ip, port, false);
	}

	public MqttServer(int port, boolean manageConnection) {
		super(port);
		if(manageConnection){
			this.switches().turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);
		}
	}
	public MqttServer(String ip, int port, boolean manageConnection) {
		super(ip, port);
        /* server connection management feature enabled or not, default value false, means disabled. */
		if (manageConnection) {
			this.switches().turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);
		}
	}

	public MqttServer(int port, boolean manageConnection, boolean syncStop) {
		this(port, manageConnection);
		if (syncStop) {
			this.switches().turnOn(GlobalSwitch.SERVER_SYNC_STOP);
		}
	}


	@Override
	protected void doInit() {
		if(this.addressParser == null){
			this.addressParser = new RpcAddressParser();
		}

		if(this.switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)){
			this.connectionEventHandler = new RpcConnectionEventHandler(switches());
			this.connectionManager = new DefaultConnectionManager(new RandomSelectStrategy());
			this.connectionEventHandler.setConnectionManager(this.connectionManager);
			this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
		}else{
			this.connectionEventHandler = new ConnectionEventHandler(switches());
			this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
		}

		initMqttRemoting();

		this.bootstrap = new ServerBootstrap();
		this.bootstrap.group(bossGroup,workerGroup)
				.channel(NettyEventLoopUtil.getServerSocketChannelClass())
				.option(ChannelOption.SO_BACKLOG, ConfigManager.tcp_so_backlog())
				.option(ChannelOption.SO_REUSEADDR,ConfigManager.tcp_so_reuseaddr())
				.childOption(ChannelOption.TCP_NODELAY,ConfigManager.tcp_nodelay())
				.childOption(ChannelOption.SO_KEEPALIVE,ConfigManager.tcp_so_keepalive());

		initWriteBufferWaterMark();

		if (ConfigManager.netty_buffer_pooled()) {
			this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		} else {
			this.bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
					.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
		}

		NettyEventLoopUtil.enableTriggeredMode(bootstrap);

		final boolean idleSwitch = ConfigManager.tcp_idle_switch();
		final int idleTime = ConfigManager.tcp_server_idle();
		final ChannelHandler serverIdleHandler = new ServerIdleHandler();
		final MqttHandler mqttHandler = new MqttHandler(true,this.userProcessors);
		this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel Channel) throws Exception {
				ChannelPipeline pipeline = Channel.pipeline();
				//pipeline.addLast("decoder",)
				//pipeline.addLast("encoder", );
			}
		});
	}

	@Override
	protected boolean doStart() throws InterruptedException {
		return false;
	}

	@Override
	protected boolean doStop() {
		return false;
	}

	protected void initMqttRemoting(){
		//this.mqttRemoting = new MqttServer();
	}

	private void initWriteBufferWaterMark() {
		int lowWaterMark = this.netty_buffer_low_watermark();
		int highWaterMark = this.netty_buffer_high_watermark();
		if (lowWaterMark > highWaterMark) {
			throw new IllegalArgumentException(
					String
							.format(
									"[server side] bolt netty high water mark {%s} should not be smaller than low water mark {%s} bytes)",
									highWaterMark, lowWaterMark));
		} else {
			logger.warn(
					"[server side] bolt netty low water mark is {} bytes, high water mark is {} bytes",
					lowWaterMark, highWaterMark);
		}
		this.bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
				lowWaterMark, highWaterMark));
	}
	@Override
	public void registerProcessor(byte protocolCode, CommandCode commandCode, RemotingProcessor<?> processor) {

	}

	@Override
	public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {

	}

	@Override
	public void registerUserProcessor(UserProcessor<?> processor) {

	}



	public void addConnectionEventProcessor(ConnectionEventType type,
											ConnectionEventProcessor processor) {
		this.connectionEventListener.addConnectionEventProcessor(type, processor);
	}
}
