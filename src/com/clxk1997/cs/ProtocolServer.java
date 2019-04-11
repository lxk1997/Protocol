package com.clxk1997.cs;

import com.clxk1997.ProtocolFactory;
import com.clxk1997.handle.ServerHandle;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @Description 服务端
 * @Author Clxk
 * @Date 2019/4/11 13:04
 * @Version 1.0
 */
public class ProtocolServer {

    private static final int PORT = 7080;

    public static void main(String[] args) throws IOException {

        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("coderc",new ProtocolCodecFilter(
                new ProtocolFactory(Charset.forName("utf-8"))
        ));
        acceptor.getSessionConfig().setReadBufferSize(1024);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,10);
        acceptor.setHandler(new ServerHandle());
        acceptor.bind(new InetSocketAddress(PORT));
        System.out.println("server start.....");
    }
}
