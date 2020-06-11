package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class BaseAttrVo extends ProductAttrValueEntity {

    public void setValueSelected(List<String> attrValue) {
        if (CollectionUtils.isEmpty(attrValue)) {
            return;
        }
        this.setAttrValue(StringUtils.join(attrValue, ","));
    }
}
