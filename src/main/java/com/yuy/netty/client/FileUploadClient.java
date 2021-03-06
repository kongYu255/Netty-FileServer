package com.yuy.netty.client;

import com.yuy.netty.FileUploadFile;
import com.yuy.netty.util.IdUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;


public class FileUploadClient {
    public String connect(int port, String host, String filePath, File file) throws Exception {
        final FileUploadFile fileUploadFile = new FileUploadFile();
        fileUploadFile.setFilePath(filePath);
        fileUploadFile.setFile(file);
        fileUploadFile.setStarPos(0);
        fileUploadFile.setFile_md5(IdUtils.getId()+file.getName());
        EventLoopGroup group = new NioEventLoopGroup(1);
        FileUploadClientHandler clientHandler = new FileUploadClientHandler(fileUploadFile);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                    ch.pipeline().addLast(clientHandler);
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
        return clientHandler.getResult();
    }

    public static void main(String[] args) {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            File file = new File("C:\\Users\\xxx\\Desktop\\PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
            String result = new FileUploadClient().connect(port, "127.0.0.1", "/upload", file);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
