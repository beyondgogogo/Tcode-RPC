package tcode.rpc.compress;


import org.apache.dubbo.common.extension.SPI;

/**
 * @author 田成强
 * */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
