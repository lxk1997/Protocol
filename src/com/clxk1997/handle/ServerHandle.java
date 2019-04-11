package com.clxk1997.handle;

import com.clxk1997.ProtocolPack;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * @Description 服务端业务处理
 * @Author Clxk
 * @Date 2019/4/11 13:08
 * @Version 1.0
 */
public class ServerHandle extends IoHandlerAdapter {
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("server->sessionIdle");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        System.out.println("server->exceptionCaught");
    }

    @Override
    public void messageReceived(IoSession session, Object msg) throws Exception {
        ProtocolPack pack = (ProtocolPack) msg;
        System.out.println("server received: " + pack.toString());
    }
}
