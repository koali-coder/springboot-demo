package com.example.video.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 设备信息表
 * </p>
 *
 * @author zhouyw
 * @since 2021-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * UUID 设备编号，全局唯一
     */
    private String id;

    /**
     * 设备code NBIOS同步id
     */
    private String deviceCode;

    /**
     * 设备SN号
     */
    private String deviceSn;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备ip
     */
    private String deviceIp;

    /**
     * 设备类型(字典)
     */
    private Integer deviceType;

    /**
     * 功能类型(字典)
     */
    private Integer functionType;

    /**
     * 设备分组id
     */
    private String groupId;

    /**
     * 产品key | 设备类
     */
    private String productId;

    /**
     * 设备secret
     */
    private String secret;

    /**
     * 是否启用, 0禁用,1启用
     */
    private Boolean enable;

    /**
     * 是否激活, 0未激活,1已激活
     */
    private Boolean active;

    /**
     * 设备激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 是否可用0:启用 1:禁用
     */
    private Integer isAvailable;

    /**
     * 是否在线:0:在线,1:离线
     */
    private Integer isOnline;

    /**
     * 设备图片
     */
    private String imageUrl;

    /**
     * 相对坐标
     */
    private String relativeCoordinate;

    /**
     * 经纬度
     */
    private String lonAndLat;

    /**
     * 设备规格-长
     */
    private Double length;

    /**
     * 设备规格-宽
     */
    private Double width;

    /**
     * 设备规格-高
     */
    private Double height;

    /**
     * 摄像头视频流地址
     */
    private String videoPath;

    /**
     * 是否钉在首页(0否，1是)
     */
    private Integer isHomePage;

    /**
     * 所属子系统
     */
    private String subSystem;

    /**
     * 生产商
     */
    private String producer;

    /**
     * 销售商
     */
    private String dealer;

    /**
     * 售后商
     */
    private String postSale;

    /**
     * 运营商
     */
    private String carrierOperator;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 扩展字段,存储设备自定义信息json格式
     */
    private String extra;

    /**
     * 版本
     */
    private String version;

    private String createUser;

    private LocalDateTime createTime;

    private String updateUser;

    private LocalDateTime updateTime;


}
