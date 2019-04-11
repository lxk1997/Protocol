package com.clxk1997.handle;

import com.clxk1997.ProtocolPack;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * @Description 客户端业务处理
 * @Author Clxk
 * @Date 2019/4/11 13:14
 * @Version 1.0
 */
public class ClientHandle extends IoHandlerAdapter {
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if(status == IdleStatus.READER_IDLE) {
            session.closeNow();
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        ProtocolPack pack = (ProtocolPack) message;
        System.out.println("client-> " + pack);
    }

}
