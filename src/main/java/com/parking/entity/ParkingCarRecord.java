package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("parking_car_record")
public class ParkingCarRecord {
    @TableId(type = IdType.AUTO)
    private Integer recordId;
    private String carNum;
    private Integer carType;
    private LocalDateTime enterTime;
    private LocalDateTime exitTime;
    private Integer spaceId;
    private String operator;
}