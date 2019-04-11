package com.clxk1997.codec;

import com.clxk1997.ProtocolPack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.charset.Charset;

/**
 * @Description 编码器
 * @Author Clxk
 * @Date 2019/4/11 12:00
 * @Version 1.0
 */
public class ProtocolEncoder extends ProtocolEncoderAdapter {

    private final Charset charset;

    public ProtocolEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(IoSession session, Object msj, ProtocolEncoderOutput out) throws Exception {
        ProtocolPack pack = (ProtocolPack) msj;
        IoBuffer buff = IoBuffer.allocate(pack.getLength());
        buff.setAutoExpand(true);
        buff.putInt(pack.getLength());
        buff.put(pack.getFlag());
        buff.put(pack.getContent().getBytes());
        buff.flip();
        out.write(buff);
    }
}
