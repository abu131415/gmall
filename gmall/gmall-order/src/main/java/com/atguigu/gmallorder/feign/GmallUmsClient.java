package com.atguigu.gmallorder.feign;

import com.atguigu.gmallumsinterface.client.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
