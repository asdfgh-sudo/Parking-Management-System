package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;
// 收费规则实体
@Data
@TableName("charging_rule")
public class ChargingRule {
    @TableId(type = IdType.AUTO)
    private Integer ruleId;
    private BigDecimal firstHourPrice;
    private BigDecimal perHourPrice;
    private BigDecimal dayMaxPrice;
    private BigDecimal monthPrice;
}