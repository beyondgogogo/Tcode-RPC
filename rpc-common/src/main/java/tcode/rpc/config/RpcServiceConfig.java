package tcode.rpc.config;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;


    //2.再加上group来区分服务接口的不同实现，version来控制服务接口同一实现的不同版本，以更好的控制版本信息
    /**
     * 假设我们有一个支付服务 PaymentService，在不同的场景下可能需要不同的实现和版本控制
     *
     * 不同的支付渠道（使用 group）
     * group=alipay
     * group=wechatpay
     * group=paypal
     *
     * 版本控制（使用 version）：
     * version=v1
     * version=v2
     *
     * 通过组合，可以有以下服务标识符：
     * PaymentService_alipay_v1
     * PaymentService_alipay_v2
     * PaymentService_wechatpay_v1
     * PaymentService_paypal_v1
     * */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    //1.注意:这个地方是用来获取服务接口的权限类名的
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
