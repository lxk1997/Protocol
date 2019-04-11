package com.clxk1997.codec;

import com.clxk1997.ProtocolPack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.awt.image.ImageObserver;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * @Description 解码器
 * @Author Clxk
 * @Date 2019/4/11 12:22
 * @Version 1.0
 */
public class ProtocolDecoder implements org.apache.mina.filter.codec.ProtocolDecoder {

    private final Charset charset;
    private final AttributeKey CONTEXT = new AttributeKey(this.getClass(), "context");
    private int maxPackLength = 100;

    public int getMaxPackLength() {
        return maxPackLength;
    }

    public void setMaxPackLength(int maxPackLength) {
        if(maxPackLength < 0) {
            throw new IllegalArgumentException("maxPackLength: " + maxPackLength);
        }
        this.maxPackLength = maxPackLength;
    }

    public ProtocolDecoder() {
        this(Charset.defaultCharset());
    }
    public ProtocolDecoder(Charset charset) {
        this.charset = charset;
    }

    public Context getContext(IoSession session) {
        Context context = (Context) session.getAttribute(CONTEXT);
        if(context==null) {
            context = new Context();
            session.setAttribute(CONTEXT,context);
        }
        return context;
    }

    @Override
    public void decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput out) throws Exception {
        final int packHeadlength = 5;
        Context ctx = this.getContext(ioSession);
        ctx.append(ioBuffer);
        IoBuffer buff = ctx.getBuff();
        buff.flip();
        while(buff.remaining() >= packHeadlength) {
            buff.mark();
            int length = buff.getInt();
            byte flag = buff.get();
            if(length < 0 || length > maxPackLength) {
                buff.reset();
                break;
            } else if(length >= packHeadlength && length-packHeadlength <= buff.remaining()) {
                int oldLimit = buff.limit();
                buff.limit(buff.position() + length - packHeadlength);
                String content = buff.getString(ctx.getDecoder());
                buff.limit(oldLimit);
                ProtocolPack pack = new ProtocolPack(flag,content);
                out.write(pack);
            } else {  //半包
                buff.clear();
                break;
            }
        }
        if(buff.hasRemaining()) {
            IoBuffer tmp = IoBuffer.allocate(maxPackLength).setAutoExpand(true);
            tmp.put(buff);
            tmp.flip();
            buff.reset();
            buff.put(tmp);
        } else {
            buff.reset();
        }
    }

    @Override
    public void finishDecode(IoSession ioSession, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {

    }

    @Override
    public void dispose(IoSession ioSession) throws Exception {
        Context context = (Context) ioSession.getAttribute(CONTEXT);
        if(context != null) {
            ioSession.removeAttribute(CONTEXT);
        }
    }

    private class Context {

        private IoBuffer buff;
        private final CharsetDecoder decoder;

        private Context() {
            this.decoder = charset.newDecoder();
            buff = IoBuffer.allocate(80).setAutoExpand(true);
        }

        public void append(IoBuffer in) {
            this.getBuff().put(in);
        }

        public void reset() {
            decoder.reset();
        }

        public IoBuffer getBuff() {
            return buff;
        }

        public void setBuff(IoBuffer buff) {
            this.buff = buff;
        }

        public CharsetDecoder getDecoder() {
            return decoder;
        }
    }
}
