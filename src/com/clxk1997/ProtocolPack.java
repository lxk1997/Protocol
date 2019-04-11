package com.clxk1997;

/**
 * @Description 协议包
 * @Author Clxk
 * @Date 2019/4/11 11:50
 * @Version 1.0
 */
public class ProtocolPack {

    private int length;
    private byte flag;
    private String content;

    public ProtocolPack(byte flag, String content) {
        this.flag = flag;
        this.content = content;
        int len1 = content==null ? 0 : content.getBytes().length;
        this.length = len1 + 5;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int len) {
        this.length = len;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("length: ").append(this.length);
        buff.append(" flag: ").append(this.flag);
        buff.append(" content: ").append(this.content);
        return buff.toString();
    }
}
