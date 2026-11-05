package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Data;
// 订单实体
@Data
@TableName("parking_order")
public class ParkingOrder {
    @TableId(type = IdType.AUTO)
    private Integer orderId;
    private String orderNo;
    private String carNum;
    private LocalDateTime enterTime;
    private LocalDateTime exitTime;
    private Integer parkDuration;
    private BigDecimal totalAmount;
    private Integer payStatus; // 0未支付 1已支付 2欠费
    private LocalDateTime payTime;
    private Integer spaceId;
}