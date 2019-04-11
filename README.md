# Protocol
mina实战Demo -- c/s自定义协议传输

##### 为什么要进行自定义协议传输?
因为传输过程往往不是一个字符串就可以传输全部信息，并且应用程序和网络通信之间存在对象与二进制之间的转换关系。所以需要结合业务编写自定义协议包进行传输。

##### 编写自定义协议的基本步骤
通过mina文档可以看到，要实现自定义协议传输需要实现ProtocolCodecFactory接口，而ProtocolCodecFactory接口有两个抽象方法，getDecoder(IoSession session)
和	getEncoder(IoSession session) 方法。所以要先实现ProtocolDecoder和ProtocolEncoder接口实现编解码器。
###### 1. 自定义协议数据包
协议数据包包含协议头和协议体，协议头保存协议(协议头+协议体)的长度length和版本信息flag;协议体保存协议内容。
```java
public class ProtocolPack {

    private int length;
    private byte flag;
    private String content;

    public ProtocolPack(byte flag, String content) {
        this.flag = flag;
        this.content = content;
        int len1 = content==null ? 0 : content.getBytes().length;
        this.length = len1 + 5;//协议头的长度：sizeof(int) + sizeof(byte)
    }

   getter and setters...
}
```
###### 2. 自定义编码器
编码器将对象转成字节存入缓冲区中，并传递到下一层。
```java
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
```
###### 3. 自定义解码器
解码器将对应的字节数组解析成对象,在传输过程中可能会出现半包(数据传输不完整)的问题，所以需要一个Context上下文保存传输的buffer,并将Context
放入session中，下次有新的传输来之后就从本类的session中找到上下文，以追加的方式添加buffer
```java
private class Context {

    private IoBuffer buff;
    private final CharsetDecoder decoder;

    constructor...

    public void append(IoBuffer in) {
        this.getBuff().put(in);
    }

    public void reset() {
        decoder.reset();
    }
    
    getters and setters...
}
```
```java
@Override
public void decode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput out) throws Exception {
    final int packHeadlength = 5;
    //通过session获取指定上下文，追加方式存入buffer
    Context ctx = this.getContext(ioSession);
    ctx.append(ioBuffer);
    IoBuffer buff = ctx.getBuff();
    buff.flip();
    //buff至少存储了一个协议包
    while(buff.remaining() >= packHeadlength) {
        buff.mark();//标记一下当前position,
        int length = buff.getInt();
        byte flag = buff.get();
        if(length < 0 || length > maxPackLength) { //长度不合法
            buff.reset();
            break;
        } else if(length >= packHeadlength && length-packHeadlength <= buff.remaining()) { //长度合法并且当前buff中存储了完整的协议content
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
    if(buff.hasRemaining()) { //buff中还有未读完数据
        IoBuffer tmp = IoBuffer.allocate(maxPackLength).setAutoExpand(true);
        tmp.put(buff);
        tmp.flip();
        buff.reset();
        buff.put(tmp);
    } else {
        buff.reset();
    }
}
```
###### 4. 自定义编解码工厂
编解码工厂实例化编解码器
```java
public class ProtocolFactory implements ProtocolCodecFactory {

    private com.clxk1997.codec.ProtocolDecoder decoder;
    private com.clxk1997.codec.ProtocolEncoder encoder;

    public ProtocolFactory(Charset charset) {
        this.decoder = new com.clxk1997.codec.ProtocolDecoder(charset);
        this.encoder = new com.clxk1997.codec.ProtocolEncoder(charset);
    }

    getEncoder and getDecoder...
}
 ```
###### 5. 服务端
服务端主要实现编解码过滤器，session参数设置，绑定操作
```java
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
```
###### 6. 服务端handle
主要实现了异常捕获、等待时间、消息接收方法
```java
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
```
###### 7. 客户端
客户端除去过滤器，session设置还通过ConnectFuture监听客户端的连接状态，发送数据
```java
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
```
###### 8. 客户端handle
主要实现了等待时间、消息接收方法
```java
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
```
