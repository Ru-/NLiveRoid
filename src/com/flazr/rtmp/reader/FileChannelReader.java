package com.flazr.rtmp.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import android.util.Log;

interface BufferReader {

    long size();

    long position();

    void position(long position);

    ChannelBuffer wrappedReadBytes(int size);
    byte[] readBytes(int size);

    int readInt();


    void close();

}

public class FileChannelReader implements BufferReader {

    private String absolutePath;
    private FileChannel in;
    private long fileSize;


    public int init(final String path){
    	File file = new File(path);
        absolutePath = file.getAbsolutePath();
        try {
            in = new FileInputStream(file).getChannel();
            fileSize = in.size();
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public long size() {
        return fileSize;
    }

    @Override
    public long position() {
        try {
            return in.position();
        } catch(Exception e) {
        	e.printStackTrace();
        	return -1;
        }
    }

    @Override
    public void position(final long newPosition) {
        try {
            in.position(newPosition);
        } catch(Exception e) {
        	close();
        }
    }


    @Override
    public int readInt() {
        return wrappedReadBytes(4).readInt();
    }

    @Override
    public ChannelBuffer wrappedReadBytes(final int size) {
        try {
            final byte[] bytes = new byte[size];
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            in.read(bb);
        	return ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,bytes);
        } catch(Exception e) {
        	Log.d("FileChannelReader","FileChannel FAILED - !!!!");
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
            e.printStackTrace();
            return ChannelBuffers.wrappedBuffer(ChannelBuffers.BIG_ENDIAN,new byte[1]);//失敗したら、空の配列を返す(これが今の所一番マシ)
        }
    }

    /*
     * とりあえずF4Vでしか使われないので適当(非 Javadoc)
     * @see com.flazr.rtmp.reader.BufferReader#readBytes(int)
     */
	@Override
	public byte[] readBytes(int size) {
        final byte[] bytes = new byte[size];
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        try {
        	in.read(bb);
        } catch(Exception e) {
            e.printStackTrace();
            try {
				in.position(in.position()+size);
			} catch (IOException e1) {
				Log.d("FileChannelReader","MURIPO?----------------");
				e1.printStackTrace();
			}
        }
        return bytes;
	}

    @Override
    public void close() {
        try {
            in.close();
        } catch(Exception e) {
            Log.d("FileChannelReader","Exception closing file "+ absolutePath +" "+ e.getMessage());
        }
    }

}


