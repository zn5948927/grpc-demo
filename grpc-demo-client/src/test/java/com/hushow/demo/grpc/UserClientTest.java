package com.hushow.demo.grpc;

import com.alibaba.fastjson.JSONObject;
import com.hushow.demo.grpc.user.dto.AddUserRequest;
import com.hushow.demo.grpc.user.dto.SearchUserRequest;
import com.hushow.demo.grpc.user.dto.UserResponse;
import com.hushow.demo.grpc.user.service.UserGrpc;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;

/**
 * grpc client调用测试用例
 * @author hushow
 * @date 2020年8月27日 上午10:27:26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GrpcDemoClientApplication.class)
@Slf4j
public class UserClientTest {

    @GrpcClient("userDemoClient")
    private UserGrpc.UserBlockingStub blockingStub;

    @GrpcClient("userDemoClient")
    private UserGrpc.UserStub userStub;

    @Test
    public void addTest() {

        log.info("开始testAdd");
        AddUserRequest request = AddUserRequest.newBuilder().setAddress("长沙").setAge(28).setName("啊虎1").build();
        UserResponse response;
        try {
            response = blockingStub.add(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: " + e.getMessage(), e);
            throw e;
        }
        log.info("testAdd结果: " + response.getName());
    }


}
