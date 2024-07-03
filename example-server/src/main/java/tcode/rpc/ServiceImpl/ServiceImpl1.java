package tcode.rpc.ServiceImpl;


import tcode.rpc.tianService;

public class ServiceImpl1 implements tianService {
    @Override
    public String method(String str) {
        return "Tcode-Service1:"+str;
    }
}
