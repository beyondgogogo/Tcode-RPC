package tcode.rpc.ServiceImpl;


import tcode.rpc.tianService;

public class ServiceImpl2 implements tianService {
    @Override
    public String method(String str) {
        return "Tcode-Service2:"+str;
    }
}
