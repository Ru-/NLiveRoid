/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.client;

import java.util.concurrent.TimeUnit;

import nliveroid.nlr.main.ClientHandler;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketPipelineSink;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import android.os.AsyncTask;
import android.util.Log;

import com.flazr.rtmp.RtmpConfig;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.reader.FlvReader;
import com.flazr.rtmp.reader.PictureReader;
import com.flazr.rtmp.reader.PreviewReader;
import com.flazr.rtmp.reader.RtmpReader;

public class RtmpPublisher {

	private boolean isDataSending;
    private Timer timer = new HashedWheelTimer(RtmpConfig.TIMER_TICK_SIZE, TimeUnit.MILLISECONDS);

    private final int timerTickSize;
    private final boolean usingSharedTimer;
    private final boolean aggregateModeEnabled = true;

    private final RtmpReader reader;
    private int streamId;
    private long startTime;
    private long seekTime;
    private long timePosition;
    private int playLength = -1;
    private int bufferDuration;
    private int debugCount;

	private NioClientSocketPipelineSink sink;
	private RtmpEncoder encoder;
	private boolean waitSend;
	private AsyncTask<NioSocketChannel, Void, Void> writeThread;



    public RtmpPublisher(final RtmpReader reader, NioClientSocketPipelineSink sink,final int streamId, final int bufferDuration,
            boolean useSharedTimer, boolean aggregateModeEnabled,RtmpEncoder encoder) {
    	Log.d("RtmpPublisher","Constractor ------ ");
//        this.aggregateModeEnabled = aggregateModeEnabled;
        this.usingSharedTimer = useSharedTimer;
        timerTickSize = RtmpConfig.TIMER_TICK_SIZE;//デフォ300
        this.reader = reader;
        this.streamId = streamId;
        this.bufferDuration = bufferDuration;
        this.sink = sink;
        this.encoder = encoder;
    }

    public boolean isStarted() {
    	return isDataSending;
    }



    public void start(final ClientHandler handler,final NioSocketChannel channel, final int seekTimeRequested, final int playLength, final RtmpMessage ... messages) {
    	//playLengthに全体の長さをセットする
    	Log.d("RtmpPublisher","rtmp start send message");
        this.playLength = playLength;
        this.isDataSending = true;
        //開始時間
        startTime = System.currentTimeMillis();
        //timePositionはシークとか含めた現在位置
        timePosition = seekTime;
        for(final RtmpMessage message : messages) {
            writeToStream(channel, message);
        }

        //スタートメッセージを全て書き込む
        for(final RtmpMessage message : reader.getStartMessages()) {
            writeToStream(channel, message);
        }
        if(reader != null){
		        if(reader instanceof PreviewReader){
		        	if(((PreviewReader)reader).startEncode()<0){
		        		handler.exceptionCaught(new DefaultExceptionEvent(channel,new Throwable("カメラ又はマイクの起動に失敗しました")));//エラー出して終了
		        	}
		        }else if(reader instanceof PictureReader){
		        	if(((PictureReader)reader).startEncode()<0){
		        		handler.exceptionCaught(new DefaultExceptionEvent(channel,new Throwable("画像送信ループ起動に失敗しました")));//エラー出して終了
		        	}
		        }
        }
        startWriteLoop(channel);
    }

    public void startWriteLoop(final NioSocketChannel channel){
    	//データを書き込む
    	writeThread = new WriteThread().execute(channel);
    }
    class WriteThread extends AsyncTask<NioSocketChannel,Void,Void>{
		@Override
		protected Void doInBackground(NioSocketChannel... params) {
			Log.d("RtmpPublisher","WRITETHREAD");
	        for(;isDataSending;){
		        float time = System.nanoTime();
	        write(params[0]);
	        Log.d("RtmpPublisher","TIME " + (System.nanoTime()-time));
	        }
			return null;
		}
	}

    private void writeToStream(final NioSocketChannel channel, final RtmpMessage message) {
    	Log.d("RtmpPublisher","WriteToStream ------ ");
        if(message.getHeader().getChannelId() > 2) {
            message.getHeader().setStreamId(streamId);
            message.getHeader().setTime((int) timePosition);
        }
        try {
			sink.eventSunkMessageEvent(new DownstreamMessageEvent(channel, new DefaultChannelFuture(channel, false), encoder.encode(message), channel.getRemoteAddress()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }


    /**
     * データ送る時に此処が呼ばれまくる
     * @param channel
     */
    private void write(final NioSocketChannel channel) {
//    	Log.d("RtmpPublisher","write---- ");

        if(!channel.isWritable()) {
        	Log.d("RtmpPublisher","FAILED WRITABLE ---- !!!! ");//一旦読み込み(サーバとの通信(BYTES_READ))を確認される？
        	while(!channel.isWritable()){
        		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
            return;
        }
        final RtmpMessage message;
        synchronized(reader) { //=============== SYNCHRONIZE ! =================
        	 if(reader.hasNext()) {
                message = reader.next();
            } else {
            	Log.d("RtmpPublisher","hasNextNULL   ----");
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                message = null;
            }
        } //====================================================================
        if(!isDataSending||message == null){
        	Log.d("RtmpPublisher","!DataSending||message_null ----");
        	return;
        }
        final RtmpHeader header = message.getHeader();
        header.setStreamId(streamId);
        /**
         *
        if (message == null || playLength >= 0 && timePosition > (seekTime + playLength)) {//playLength再生長さよりtimePosition現在位置が超えてたら終了
            stop(channel);
            return;
        }
		//経過時間
        final long elapsedTime = System.currentTimeMillis() - startTime;
		//経過時間+シーク時間
        final long elapsedTimeAndSeek = elapsedTime + seekTime;
		//再生のクライアント側の余裕?
        final double clientBuffer = timePosition - elapsedTimeAndSeek;
        if(aggregateModeEnabled && clientBuffer > timerTickSize) { // TODO cleanup
            reader.setAggregateDuration((int) clientBuffer);
        } else {
            reader.setAggregateDuration(0);
        }
        final double compensationFactor = clientBuffer / (bufferDuration + timerTickSize);
        final long delay = (long) ((header.getTime() - timePosition) * compensationFactor);
        timePosition = header.getTime();
         */

//        Log.d("RtmpPublisher","OFFER --- " + message.getHeader().getSize());
//        boolean offered = channel.writeBuffer.offer(new DownstreamMessageEvent(channel, new DefaultChannelFuture(channel, false), encoder.dataSendEncode(message)));//書き込み予約をセットする
//        if(!offered){
//        	Log.d("RtmpPublisher","FAILED OFFER---------");
//        	return;
//        }
//        channel.worker.writeFromUserCode(channel);//ここが実際にNioWorkerのwrite0からSendBufferPoolに渡されて送信される

        Log.d("NLiveRoid","SEND_DATA " +message.getMessageType());
        Log.d("NLiveRoid","SEND_DATA_ " +message.toString());
        Log.d("NLiveRoid","SEND_DATA_ " +message.getHeader().getTime());
        Log.d("NLiveRoid","SEND_DATA_ " +message.getHeader().getHeaderType());


        //futureをオブジェクト化しているので意味がない
      //送信ビーンを作成して送信
        DefaultChannelFuture future = new DefaultChannelFuture(channel, false);
      boolean offered = channel.writeBuffer.offer(new DownstreamMessageEvent(channel, future, encoder.encode(message)));//書き込み予約をセットする
      assert offered;
      channel.worker.writeFromUserCode(channel);//ここが実際にNioWorkerのwrite0からSendBufferPoolに渡されて送信される
      future.addListener(new ChannelFutureListener() {
            @Override public void operationComplete(final ChannelFuture cf) {
            	Log.d("RtmpPublisher","futureComp " +cf.isSuccess());
                //メインルーティンの次呼び
//                fireNext(channel, 0);
            }
        });
//      if(!future.isSuccess()){
//    	  Log.d("RtmpPublisher","FUTURE FAILED --------- !!!!");
//      }
    }



    public void fireNext(final NioSocketChannel channel, final long delay) {
//        Log.d("RtmpPublisher", "fireNext "+delay);
        if(delay > timerTickSize) {
            timer.newTimeout(new TimerTask() {//送信時間に余裕がでている時だと思う→ほぼ無い
                @Override
                public void run(Timeout timeout) {
                        Log.d("Timeout delay: ", ""+delay);
                        if(!isDataSending)return;
                	write(channel);
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
        	//エラーが毎回発生する
        	try{
            	write(channel);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
    }

    public void close(NioSocketChannel channel) {
    	Log.d("RtmpPublisher","RtmpPublisher CLOSE");
		isDataSending = false;
        if(!usingSharedTimer) {
            timer.stop();
        }
        RtmpMessage closeMessage = Command.unpublish(streamId);
        writeToStream(channel, closeMessage);
        reader.close();
    }

	public void waitDataSend() {
		this.waitSend = true;
		Log.d("RtmpPublisher","waitDataSend");
		if(writeThread != null && writeThread.getStatus() == AsyncTask.Status.RUNNING){
			try{
				writeThread.cancel(true);
			}catch(Exception e){
				Log.d("RtmpPublisher","Interrupted");
			}
		}
	}
	public void restartDataSend(final NioSocketChannel channel) {
		Log.d("RtmpPublisher","RESTART");
		this.waitSend = false;
		if(writeThread != null && writeThread.getStatus() == AsyncTask.Status.RUNNING){
		writeThread.cancel(true);
		}
		writeThread = new WriteThread().execute(channel);
	}

	public boolean isWaitDataSend() {
		return waitSend;
	}

	public void setAggregateDuration(int duration){
		if(reader instanceof FlvReader){
			((FlvReader)reader).setAggregateDuration(duration);
		}
	}

	public long getDataDuration() {
		return ((FlvReader)reader).getDuration();
	}

}
