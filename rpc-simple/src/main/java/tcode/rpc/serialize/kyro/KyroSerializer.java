package tcode.rpc.serialize.kyro;

import tcode.rpc.serialize.Serializer;

public class KyroSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
