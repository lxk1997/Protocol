package com.clxk1997.cs;

import com.clxk1997.ProtocolFactory;
import com.clxk1997.ProtocolPack;
import com.clxk1997.handle.ClientHandle;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @Description 客户端
 * @Author Clxk
 * @Date 2019/4/11 13:11
 * @Version 1.0
 */
public class ProtocolClient {

    private static final int PORT = 7080;
    private static final String HOST = "127.0.0.1";
    static long start = 0;
    static long counter = 0;
    final static int fil = 100;

    public static void main(String[] args) {

        IoConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("coderc",new ProtocolCodecFilter(
                new ProtocolFactory(Charset.forName("UTF-8"))
        ));
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,10);
        connector.getSessionConfig().setReadBufferSize(1024);
        connector.setHandler(new ClientHandle());
        ConnectFuture future = connector.connect(new InetSocketAddress(HOST,PORT));
        future.addListener(new IoFutureListener<ConnectFuture>() {
            @Override
            public void operationComplete(ConnectFuture future) {

                if(future.isConnected()) {
                    IoSession session = future.getSession();
                    sendDate(session);
                }
            }
        });
    }

    private static void sendDate(IoSession session) {
        start = System.currentTimeMillis();
        for(int i = 0; i < fil; i++) {
            String content = "Hello Mina:" + i;
            ProtocolPack pack = new ProtocolPack((byte)i,content);
            session.write(pack);
            System.out.println("client send: " + pack.toString());
        }
    }
}
