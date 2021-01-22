package com.hushow.demo.grpc.controller;

import com.alibaba.fastjson.JSONObject;
import com.hushow.demo.grpc.user.dto.SearchUserRequest;
import com.hushow.demo.grpc.user.dto.UserResponse;
import com.hushow.demo.grpc.user.service.UserGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * TODO
 *
 * @author hushow
 * @date 2020/12/18 下午4:05
 */
@RestController
@Slf4j
public class GrpcDemoController {

    @GrpcClient("userDemoClient")
    private UserGrpc.UserBlockingStub blockingStub;

    @GetMapping("/helloGrpc")
    public String helloGrpc(@RequestParam(value = "name", defaultValue = "World") String name) {
        log.info("开始helloGrpc");
        //AddUserRequest request = AddUserRequest.newBuilder().setAddress("长沙").setAge(28).setName("啊虎1").build();
        SearchUserRequest request = SearchUserRequest.newBuilder().build();

        List<UserResponse> respList = blockingStub.list(request).getUserList();
        Map<String, Object> user = null;
        List<Map> users = new ArrayList<>();
        for (UserResponse userResponse : respList) {
            user = new LinkedHashMap<>(16);
            user.put("Id", userResponse.getId());
            user.put("Name", userResponse.getName());
            user.put("Age", userResponse.getAge());
            user.put("Address", userResponse.getAddress());
            users.add(user);
        }

        log.info("testAdd结果:" + JSONObject.toJSONString(users));
        return JSONObject.toJSONString(users);
    }
}
