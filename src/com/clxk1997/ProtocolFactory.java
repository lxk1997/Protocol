package com.clxk1997;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;

/**
 * @Description 编解码工厂
 * @Author Clxk
 * @Date 2019/4/11 12:53
 * @Version 1.0
 */
public class ProtocolFactory implements ProtocolCodecFactory {

    private com.clxk1997.codec.ProtocolDecoder decoder;
    private com.clxk1997.codec.ProtocolEncoder encoder;

    public ProtocolFactory(Charset charset) {
        this.decoder = new com.clxk1997.codec.ProtocolDecoder(charset);
        this.encoder = new com.clxk1997.codec.ProtocolEncoder(charset);
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
