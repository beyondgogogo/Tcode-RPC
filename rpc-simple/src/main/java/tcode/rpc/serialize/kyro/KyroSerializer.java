package tcode.rpc.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import tcode.rpc.serialize.Serializer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KyroSerializer implements Serializer {

    // 使用 ThreadLocal 管理 Kryo 实例
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true); // 默认值为 true，避免循环引用导致的栈溢出
        kryo.setRegistrationRequired(false); // 设置为 false，可以不必注册类
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        Kryo kryo = kryoThreadLocal.get();
        try {
            kryo.writeClassAndObject(output, obj);
            output.close();
            return byteArrayOutputStream.toByteArray();
        } finally {
            output.close();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        Kryo kryo = kryoThreadLocal.get();
        try {
            return (T) kryo.readClassAndObject(input);
        } finally {
            input.close();
        }
    }
}
