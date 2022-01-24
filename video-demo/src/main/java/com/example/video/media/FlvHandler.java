package com.example.video.media;

import cn.hutool.core.convert.Convert;
import cn.hutool.crypto.digest.MD5;
import com.example.video.model.Camera;
import com.example.video.model.DeviceInfo;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author zhouyw
 */
@Slf4j
@Service
@Sharable
public class FlvHandler extends SimpleChannelInboundHandler<Object> {

	@Autowired
	private MediaService mediaService;

	private WebSocketServerHandshaker handshaker;

	/**
	 * 网络超时
	 */
	@Value("${mediaserver.netTimeout:15000000}")
	private String netTimeout;
	/**
	 * 读写超时
	 */
	@Value("${mediaserver.readOrWriteTimeout:15000000}")
	private String readOrWriteTimeout;

	/**
	 * 无人拉流观看是否自动关闭流
	 */
	@Value("${mediaserver.autoClose:true}")
	private boolean autoClose;

	/**
	 * 无人拉流观看持续多久自动关闭，1分钟
	 */
	@Value("${mediaserver.autoClose.noClientsDuration:60000}")
	private long noClientsDuration;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			QueryStringDecoder decoder = new QueryStringDecoder(req.uri());

			// 判断请求uri
			if (!"/live".equals(decoder.path())) {
				log.info("uri有误");
				sendError(ctx, HttpResponseStatus.BAD_REQUEST);
				return;
			}

			Camera camera = buildCamera(req.uri());

			if (StringUtils.isBlank(camera.getUrl())) {
				log.info("url有误");
				sendError(ctx, HttpResponseStatus.BAD_REQUEST);
				return;
			}

			// websocket握手，请求升级

			// 参数分别是ws地址，子协议，是否扩展，最大frame长度
			WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(req), null, true, 5 * 1024 * 1024);
			handshaker = factory.newHandshaker(req);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				HttpResponse rsp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				rsp.headers().set(HttpHeaderNames.SERVER, "media server");
				DefaultChannelPromise channelPromise = new DefaultChannelPromise(ctx.channel());

				handshaker.handshake(ctx.channel(), req, rsp.headers(), channelPromise);
				mediaService.playForWs(camera, ctx);
			}

		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketRequest(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 添加连接
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 断开连接
	}

	/**
	 * ws握手地址
	 */
	private String getWebSocketLocation(FullHttpRequest request) {
		String location = request.headers().get(HttpHeaderNames.HOST) + request.uri();
		return "ws://" + location;
	}

	/**
	 * 发送req header，告知浏览器是flv格式
	 *
	 * @param ctx
	 */
	private void sendFlvReqHeader(ChannelHandlerContext ctx) {
		HttpResponse rsp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		rsp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
				.set(HttpHeaderNames.CONTENT_TYPE, "video/x-flv").set(HttpHeaderNames.ACCEPT_RANGES, "bytes")
				.set(HttpHeaderNames.PRAGMA, "no-cache").set(HttpHeaderNames.CACHE_CONTROL, "no-cache")
				.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED).set(HttpHeaderNames.SERVER, "media server");
		ctx.writeAndFlush(rsp);
	}

	/**
	 * 错误请求响应
	 *
	 * @param ctx
	 * @param status
	 */
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer("请求地址有误: " + status + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * websocket处理
	 *
	 * @param ctx
	 * @param frame
	 */
	private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 关闭
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}

		// 握手PING/PONG
		if (frame instanceof PingWebSocketFrame) {
			ctx.write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}

		// 文本
		if (frame instanceof TextWebSocketFrame) {
			return;
		}

		if (frame instanceof BinaryWebSocketFrame) {
			return;
		}
	}

	/**
	 * 解析参数，构建camera，&&&参数必须加在url参数值后面，&&&autoClose=false&&&hls=true
	 * ws://localhost:18000/live?deviceId=6&&&autoClose=false
	 * @param url
	 * @return
	 */
	private Camera buildCamera(String url) {
		Camera camera = new Camera();
		setConfig(camera);

		String[] split = url.split("deviceId=");
		String urlParent = split[1];

		String[] split2 = urlParent.split("&&&");
		if (split2.length > 0) {
			for (String string : split2) {
				if (string.contains("autoClose=")) {
					String[] as = string.split("=");
					if (as.length <= 1) {
						throw new RuntimeException("autoClose参数有误");
					}
					camera.setAutoClose(Convert.toBool(as[1], false));
				} else if (string.contains("ffmpeg=")) {
					String[] as = string.split("=");
					if (as.length <= 1) {
						throw new RuntimeException("ffmpeg参数有误");
					}
					camera.setEnabledFFmpeg(Convert.toBool(as[1], false));
				} else if (string.contains("hls=")) {
					String[] as = string.split("=");
					if (as.length <= 1) {
						throw new RuntimeException("hls参数有误");
					}
					camera.setEnabledHls(Convert.toBool(as[1], false));
				} else {
					// todo get data
					DeviceInfo deviceInfo = new DeviceInfo();
					if (StringUtils.isBlank(deviceInfo.getVideoPath())) {
						throw new RuntimeException("设备ID参数有误");
					}
					camera.setUrl(deviceInfo.getVideoPath());
				}
			}
		}

		if (isLocalFile(camera.getUrl())) {
			camera.setType(1);
		}

		// 区分不同媒体
		String mediaKey = MD5.create().digestHex(camera.getUrl());
		camera.setMediaKey(mediaKey);

		return camera;
	}

	/**
	 * 配置默认参数
	 */
	private void setConfig(Camera camera) {
		camera.setNetTimeout(netTimeout);
		camera.setReadOrWriteTimeout(readOrWriteTimeout);
		camera.setAutoClose(autoClose);
		camera.setNoClientsDuration(noClientsDuration);
	}

	/**
	 * 是否是本地文件,判断前面长度是不是小于1个字符，认为是盘符
	 * @return boolean
	 */
	private boolean isLocalFile(String streamUrl) {
		String[] split = streamUrl.trim().split("\\:");
		if (split.length > 0) {
			if (split[0].length() <= 1) {
				return true;
			}
		}
		return false;
	}

}
