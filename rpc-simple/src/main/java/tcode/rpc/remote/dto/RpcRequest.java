package tcode.rpc.remote.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author 田成强
 * 请求的关键信息封装在此类中
 * 实现 Serializable 接口可以明确表示这个类的实例是可序列化的。作为一种编程规范
 * 一眼就能看出这个类是可以被序列化的
 * */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    //Java的序列化机制通过这个字段来验证序列化的对象和反序列化的类是否匹配
    private static final long serialVersionUID = 1905122041950251207L;
    //唯一的请求ID
    private String requestId;

    //这四个字段属性比较好理解
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;

    //用来表示接口版本，可用于服务升级兼容等
    private String version;

    //接口可能有多个实现类，用来表示具体某个实现
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
