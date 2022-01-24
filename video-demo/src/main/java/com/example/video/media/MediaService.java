package com.example.video.media;

import cn.hutool.core.thread.ThreadUtil;
import com.example.video.model.Camera;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体服务，支持全局网络超时、读写超时、无人拉流持续时长自动关闭流等配置
 * @author zhouyw
 */
@Service
public class MediaService {

	/**
	 * 缓存流转换线程
	 */
	public static ConcurrentHashMap<String, MediaTransferFlvByJavacv> cameras = new ConcurrentHashMap<>();

	/**
	 * ws-flv播放
	 * @param camera
	 * @param ctx
	 */
	public void playForWs(Camera camera, ChannelHandlerContext ctx) {

		if (cameras.containsKey(camera.getMediaKey())) {
			MediaTransferFlvByJavacv mediaTransferFlvByJavacv = cameras.get(camera.getMediaKey());
			//如果当前已经用ffmpeg，则重新拉流
			if(camera.isEnabledFFmpeg()) {
				mediaTransferFlvByJavacv.setRunning(false);
				cameras.remove(camera.getMediaKey());
				this.playForWs(camera, ctx);
			} else {
				mediaTransferFlvByJavacv.addClient(ctx);
			}
		} else {
			MediaTransferFlvByJavacv mediaConvert = new MediaTransferFlvByJavacv(camera);
			cameras.put(camera.getMediaKey(), mediaConvert);
			ThreadUtil.execute(mediaConvert);
			mediaConvert.addClient(ctx);
		}
	}

}
