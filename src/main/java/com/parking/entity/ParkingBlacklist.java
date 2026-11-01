package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parking_blacklist")
public class ParkingBlacklist {
    @TableId(type = IdType.AUTO)
    private Integer blackId;
    private String carNum;
    private String reason;
    private LocalDateTime createTime;
}