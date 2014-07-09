package nliveroid.nlr.main;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.DownstreamChannelStateEvent;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketPipelineSink;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.channel.socket.nio.SelectorUtil;
import org.jboss.netty.util.internal.ExecutorUtil;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.flazr.rtmp.LoopedReader;
import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.RtmpWriter;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.RtmpPublisher;
import com.flazr.rtmp.message.BytesRead;
import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.Control;
import com.flazr.rtmp.message.MetadataAmf0;
import com.flazr.rtmp.message.SetPeerBw;
import com.flazr.rtmp.message.WindowAckSize;
import com.flazr.rtmp.reader.F4vReader;
import com.flazr.rtmp.reader.FlvReader;
import com.flazr.rtmp.reader.PictureReader;
import com.flazr.rtmp.reader.PreviewReader;
import com.flazr.rtmp.reader.RtmpReader;
import com.flazr.rtmp.reader.SnapReader;
import com.flazr.util.ChannelUtils;
import com.flazr.util.Utils;

public class ClientHandler implements ChannelUpstreamHandler{

	private LiveSettings liveSetting;
	private BCPlayer ACT;
	private NioSocketChannel ch;
    private int transactionId = 1;
    private Map<Integer, String> transactionToCommandMap;
    private byte[] swfvBytes;

    private RtmpWriter writer;

    private int bytesReadWindow = 2500000;
    private long bytesRead;
    private long bytesReadLastSent;
    private int bytesWrittenWindow = 2500000;
    private int streamId;

    private RtmpPublisher publisher;
    private Executor bossExecutor;
    private Executor workerExecutor;
    private NioClientSocketPipelineSink sink;
	private ClientHandshakeHandler handshaker;
	private RtmpDecoder decoder;
	private RtmpEncoder encoder;
	public static SocketAddress REMOTE_ADDR;

	private PublishTask task;
	private RtmpReader reader;
	private boolean isUnpublished;

    public ClientHandler(BCPlayer ACT,LiveSettings liveSetting){
    	this.ACT = ACT;
    	this.liveSetting = liveSetting;
    }

    public RtmpReader getReader(){
    	return reader;
    }

	class PublishTask extends AsyncTask<Void,Void,Integer>{
	    private ChannelFuture future;
		@Override
		protected Integer doInBackground(Void... arg0) {
			int settingsError = prepareSettings();
			if(settingsError < 0)return settingsError;
	        Log.d("ClientHandler","Start Stream  ---");
			try{
        	int connectionError = connectStream();
        	if(connectionError < 0)return connectionError;
        	startDataSend(getSink(),ch,future);
            future.getChannel().getCloseFuture().awaitUninterruptibly();//切断待ち
            releaseExternalResources();
            return 0;
	        }catch(IllegalStateException e){
	        	e.printStackTrace();
	        }catch(ClassCastException e){
	        	e.printStackTrace();
	        }catch(ChannelPipelineException e){
	        	e.printStackTrace();
	        }
			return -6;
		}

		private int prepareSettings(){
	        final int count = liveSetting.getLoad();
	        if(count != 1)return -8;
			if ("sdk".equals(Build.PRODUCT)) {
	            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");// エミュレータの場合はIPv6を無効    ----(エミュもWi-Fiのホスト名でいける)
	            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
	        }
    		Executor executor = Executors.newCachedThreadPool();
    		if (executor == null)return -7;
    		bossExecutor = executor;
    		workerExecutor = executor;
	        transactionToCommandMap = new HashMap<Integer, String>();
			return 0;
		}

		private int connectStream(){
			handshaker = new ClientHandshakeHandler(liveSetting,ClientHandler.this);
	        decoder = new RtmpDecoder(ClientHandler.this);
	        encoder = new RtmpEncoder();
            //DEFAULT_BOSS_COUNT(1),SelectorUtil.DEFAULT_ID_THREADS(Runtime.getRuntime().availableProcessors() * 2)
            sink = new NioClientSocketPipelineSink(
                    bossExecutor, workerExecutor, 1, SelectorUtil.DEFAULT_IO_THREADS,handshaker,decoder,encoder,ClientHandler.this,publisher);
            handshaker.setSink(sink);
            encoder.setSink(sink);
            ch = new NioSocketChannel(ClientHandler.this,sink, sink.nextWorker());
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("tcpNoDelay" , true);
            options.put("keepAlive", true);
            ch.getConfig().setOptions(new TreeMap<String, Object>(options));
            //remoteAddressに接続
            future = new DefaultChannelFuture(ch,true);
            InetSocketAddress remoteAddress = new InetSocketAddress(liveSetting.getHost(), liveSetting.getPort());
            getSink().connect(ch, future, remoteAddress);
            if(future == null)return -4;//ARMじゃない(複数あり)
            future.awaitUninterruptibly();//接続試行結果を待つ
            Log.d("ClientHandler","AWAIT ------------ " + future.isSuccess());
            if (future.isSuccess()) {
            	return 0;
            }else{//接続失敗
                future.getCause().printStackTrace();
                final String msg = future.getCause().getMessage();
            if(msg == null){
 	        	return -2;
 	        }else if(msg.equals("connection timed out")){
 	        	return -3;
 	        }else if(msg.equals("No route to host")){
 	        	return -4;
 	        }else if(msg.equals("Broken pipe")){
 	        	return -5;
 	        }else{
 	        	return -99;
 	        }
            }
		}

		/**
		 * データ送信開始
		 * まず、handshakerから
		 *
		 */
		private void startDataSend(NioClientSocketPipelineSink sink, NioSocketChannel nioSockCH,ChannelFuture future){
			Log.d("ClientHandler","StartDataSend ------ ");
			try {
				REMOTE_ADDR = nioSockCH.getRemoteAddress();
				sink.offerWrite(new DownstreamMessageEvent(nioSockCH, future, handshaker.getHandshake().encodeClient0(), nioSockCH.getRemoteAddress()));
				sink.offerWrite(new DownstreamMessageEvent(nioSockCH, future, handshaker.getHandshake().encodeClient1(), nioSockCH.getRemoteAddress()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(Integer arg){
			if(arg < 0)liveSetting.setStreamStarted(false);
			switch(arg){
			case -1:
				Toast.makeText(ACT, "ストリームロジック確立に失敗",Toast.LENGTH_LONG).show();
				break;
			case -2:
//				Toast.makeText(ACT,"不明のエラー" , Toast.LENGTH_LONG).show();
				break;
			case -3:
 	        	Toast.makeText(ACT,"ストリーム接続がタイムアウトしました" , Toast.LENGTH_LONG).show();
 	        	break;
			case -4:
 	        	Toast.makeText(ACT,"接続先がみつかりませんでした" , Toast.LENGTH_LONG).show();
 	        	break;
			case -5:
 	        	Toast.makeText(ACT,"十分な帯域が無く、切断されました" , Toast.LENGTH_LONG).show();
 	        	break;
			case -6:
 	        	Toast.makeText(ACT,"初期化処理失敗又は、ARMアーキテクチャでない" , Toast.LENGTH_LONG).show();
 	        	break;
			case -7:
 	        	Toast.makeText(ACT,"スレッド初期化時に致命的エラー" , Toast.LENGTH_LONG).show();
 	        	break;
			case -8:
 	        	Toast.makeText(ACT,"設定値不正エラー" , Toast.LENGTH_LONG).show();
 	        	break;
			case -99:
 	        	Toast.makeText(ACT,"接続絡みで不明のエラー" , Toast.LENGTH_LONG).show();
 	        	break;

			}
		}

		/**
		 * sinkを取得します。
		 * @return sink
		 */
		public NioClientSocketPipelineSink getSink() {
		    return sink;
		}
		public void releaseExternalResources() {
			 ExecutorUtil.terminate(bossExecutor, workerExecutor);
		}
	}//End of PublishTask




    public void setSwfvBytes(byte[] swfvBytes) {
        this.swfvBytes = swfvBytes;
        Log.d("setSwfvBytes", Utils.toHex(swfvBytes,0,swfvBytes.length,false));
    }


    private void writeCommandExpectingResult(NioSocketChannel channel, Command command) {
        final int id = transactionId++;
        command.setTransactionId(id);
        transactionToCommandMap.put(id, command.getName());//ChannelState.CONNECTEDなどを書き込み要求に追加する
        Log.d("ClientHandler","SEND COMMAND "+ command);
        ChannelFuture future = new DefaultChannelFuture(channel, false);
        try {
			sink.eventSunkMessageEvent(new DownstreamMessageEvent( channel,future, encoder.encode(command), channel.getRemoteAddress()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }

    public void channelConnected( ChannelStateEvent e) {
        Log.d("ClientHandler","channelConnected "+e.getValue());
        Channels.setStreamPhase(1);//次のフェーズにする
        writeCommandExpectingResult(e.getChannel(), Command.connect(liveSetting));
    }
    @Override
    public void handleUpstream(
               ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
       	Log.d("ClientHandler handleUpstream","ctx "+ctx.getName());
       }


    public void messageReceived(final MessageEvent me) {
        Log.d("ClientHandler","messageReceived "+me);
        if(publisher != null && publisher.isStarted()) {
        	Log.d("ClientHandler","startWriteLoop");
//        	publisher.startWriteLoop(me.getChannel());
//            return;
        }
        final NioSocketChannel localChannel = me.getChannel();
        final RtmpMessage message = (RtmpMessage) me.getMessage();
        switch(message.getHeader().getMessageType()) {
            case CHUNK_SIZE: // handled by decoder
                break;
            case CONTROL:
                Control control = (Control) message;
                Log.d("ClientHandler","Control "+control);
                switch(control.getType()) {
                    case PING_REQUEST:
                        final int time = control.getTime();
                        Log.d("ClientHandler PING_REQUEST"," "+ time);
                        Control pong = Control.pingResponse(time);
                        Log.d("ClientHandler PING_REQUEST response"," "+ pong);
                        ChannelFuture pingFuture = new DefaultChannelFuture(localChannel, false);
					try {
		    			sink.eventSunkMessageEvent(new DownstreamMessageEvent(localChannel, pingFuture, encoder.encode(pong), null));
					} catch (Exception e1) {//エラー時何もしてないが。。
						e1.printStackTrace();
					}
                        break;
                    case SWFV_REQUEST:
                        if(swfvBytes == null) {
                            Log.d("swf verification not initialized!"
                                , " not sending response, server likely to stop responding / disconnect");
                        } else {
                            Control swfv = Control.swfvResponse(swfvBytes);
                            Log.d("sending swf verification response","swfv "+ swfv);
                            ChannelFuture swfvFuture = new DefaultChannelFuture(localChannel, false);
                            try {
                    			sink.eventSunkMessageEvent(new DownstreamMessageEvent(localChannel, swfvFuture, encoder.encode((RtmpMessage) swfv), null));
                    		} catch (Exception e1) {
                    			e1.printStackTrace();
                    		}//エラー時何もしてないが。。
                        }
                        break;
                    case STREAM_BEGIN:
                        if(streamId !=0) {
                        	Log.d("Buffer ------------- "," " + liveSetting.getBuffer());
                            ChannelFuture beginFuture = new DefaultChannelFuture(localChannel, false);
                            try {
                    			sink.eventSunkMessageEvent(new DownstreamMessageEvent(localChannel, beginFuture, encoder.encode(Control.setBuffer(streamId, liveSetting.getBuffer())), null));
                    		} catch (Exception e1) {
                    			e1.printStackTrace();
                    		}//エラー時何もしてないが。。
                        }
                        break;
                    default:
                        Log.d("ignoring control message", " "+control);
                }
                break;
            case METADATA_AMF0:
            case METADATA_AMF3:
                Log.d("ClientHandler "," METADATA_AMF3");
            	MetadataAmf0 metadata = (MetadataAmf0) message;
                if(metadata.getName().equals("onMetaData")) {
                    Log.d("writing 'onMetaData'"," "+ metadata);
                    writer.write(message);
                } else {
                    Log.d("ignoring metadata: "," "+ metadata);
                }
                break;
            case AUDIO:
            case VIDEO:
            case AGGREGATE:
                Log.d("ClientHandler "," AGGREGATE");
                writer.write(message);
                bytesRead += message.getHeader().getSize();
                if((bytesRead - bytesReadLastSent) > bytesReadWindow) {
                    Log.d("ClientHandler AGGREGATE"," bytes read ack "+bytesRead);
                    bytesReadLastSent = bytesRead;
                    localChannel.write(new BytesRead(bytesRead),null);
                }
                break;
            case COMMAND_AMF0:
            case COMMAND_AMF3:
                Command command = (Command) message;
                String name = command.getName();
                Log.d("COMMAND_AMF0/3 ", "name:"+name);
                if(name.equals("_result")) {//ストリームの開始
                    String resultFor = transactionToCommandMap.get(command.getTransactionId());
                    Log.d("COMMAND_AMF3 messageReceived ", " "+resultFor);
                    if(resultFor.equals("connect")) {
                        writeCommandExpectingResult(localChannel, Command.createStream());
                    } else if(resultFor.equals("createStream")) {
                        streamId = ((Double) command.getArg(0)).intValue();
                        Log.d("STREAM ID :", "["+streamId+"]");
                            reader = null;
                            if(liveSetting.getMode() == 0){//カメラモードを開始する
                        		Log.d("ClientHandler","MODE= CAM READER");
                            	if(reader == null){
                            		reader = new PreviewReader(ACT,liveSetting);
                            	}
                            	if(!((PreviewReader)reader).isInited()){
                            		int val = reader.init(null);
                                    Log.d("ClientHandler","CamPreviewReader" + val);
                                    if(val < 0)showNofileToast(val);
                            	}

                            }else if(liveSetting.getMode() == 1){//スナップモードを開始する
                        		Log.d("ClientHandler","MODE= CAM READER");
                            	reader = new SnapReader(liveSetting);
                            	int val = reader.init(null);
                                Log.d("ClientHandler","SnapReader" + val);
                                if(val < 0)showNofileToast(-2);
                            }else if(liveSetting.getMode() == 2){//静止画モードを開始する
                        		Log.d("ClientHandler","MODE= PIC READER ");
                        		//画像をエンコ済みの再接続の場合はnewしない
                            	reader = new PictureReader(ACT,  liveSetting);
                            	int val = reader.init(null);//画像でかすぎとメッセージ分岐したい
                                Log.d("ClientHandler","PictureReader" + val);
                                if(val == -1)exceptionCaught(new DefaultExceptionEvent(localChannel,new Throwable("画像リーダの初期化に失敗")));

                            }else if(liveSetting.getMode() == 3){//動画再生モード
                            	if(liveSetting.getFilePath() == null || liveSetting.getFilePath().equals("")){
                            		showNofileToast(-2);
                            		return;
                            	}
                            	if(liveSetting.getFilePath().toLowerCase().endsWith(".flv")) {
                            		Log.d("ClientHandler","MODE= FLV READER");
                            		reader =  new FlvReader(liveSetting);
                                	int val =reader.init(liveSetting.getFilePath());
                                	  if(val == -10){
                                      	ACT.runOnUiThread(new Runnable(){
                                      		public void run(){
                                      			MyToast.customToastShow(ACT, "ファイルが不正でした");
                                      		}
                                      	});
                                      	return ;
                                      }else if(val < 0){
                                		showNofileToast(-2);
                                    	return;
                                	}
                            	}else if(liveSetting.getFilePath().toLowerCase().startsWith("mp4:")) {
                            		Log.d("ClientHandler","MODE= FL4V1 READER");
                                    reader =  new F4vReader();
                                    int val = reader.init(null);
                                    Log.d("ClientHandler","ReaderINIT" + val);
                                    if(val < 0){
                                		showNofileToast(-2);
                                    	return;
                                	}
                                } else if (liveSetting.getFilePath().toLowerCase().endsWith(".f4v")) {
                            		Log.d("ClientHandler","MODE= F4V2 READER");
                                    reader =  new F4vReader();
                                    int val = reader.init(liveSetting.getFilePath());
                                    if(val < 0){
                                		showNofileToast(-2);
                                    	return;
                                	}
                                }
                            }else{//パブリッシュモードが無い場合エラーに変更
                            	Log.d("ClientHandler","NO PUBLISH MODE=(FilePath == null && !isUseCamera)");
                            	this.exceptionCaught(new DefaultExceptionEvent(localChannel, new Exception("NO PUBLISH MODE")));
                            }
                            if(liveSetting.getLoopCount() > 1) {
                                reader = new LoopedReader(reader, liveSetting.getLoopCount());
                            }
                            publisher = new RtmpPublisher(reader,sink, streamId, liveSetting.getBuffer(), false, false, encoder);
                            localChannel.setPublisher(publisher);
                            try {
                    			sink.eventSunkMessageEvent(new DownstreamMessageEvent(localChannel,new DefaultChannelFuture(localChannel, false), encoder.encode(Command.publish(streamId, liveSetting)), localChannel.getRemoteAddress()));
                    		} catch (Exception e1) {
                    			e1.printStackTrace();
                    		}//エラー時何もしてないが。。
                           return;
                    } else {
                        Log.d("un-handled server result for", resultFor);
                    }
                } else if(name.equals("onStatus")) {
                    final Map<String, Object> temp = (Map) command.getArg(0);
                    final String code = (String) temp.get("code");
                    Log.d("ClientHandler","onStatus code:"+code);
                    if (code.equals("NetStream.Failed") // TODO cleanup
                            || code.equals("NetStream.Play.Failed")
                            || code.equals("NetStream.Play.Stop")
                            || code.equals("NetStream.Play.StreamNotFound")) {
                    	Log.d("ClientHandler","disconnecting");
                    	try {
							sink.eventSunkStateEvent(new DownstreamChannelStateEvent(
							        localChannel, localChannel.getCloseFuture(), ChannelState.OPEN, Boolean.FALSE) );
						} catch (Exception e) {
							e.printStackTrace();
						}
                        return;
                    }
                    if(code.equals("NetStream.Publish.Start")
                            && publisher != null && !publisher.isStarted()) {
                            publisher.start(this,localChannel, liveSetting.getStart(),
                            		liveSetting.getLength(), new ChunkSize(4096));
                        return;
                    }
                    if (publisher != null && code.equals("NetStream.Unpublish.Success")) {
                        Log.d("ClientHandler","unpublish");
                        ChannelFuture unpublishFuture = new DefaultChannelFuture(localChannel, false);
                        try {
                			sink.eventSunkMessageEvent(new DownstreamMessageEvent(localChannel, unpublishFuture, encoder.encode(Command.closeStream(streamId)), null));
                		} catch (Exception e1) {
                			e1.printStackTrace();
                		}//エラー時未チェック
                        unpublishFuture.addListener(ChannelFutureListener.CLOSE);
                        isUnpublished = true;
                        return;
                    }
                } else if(name.equals("close")) {
                    Log.d("server called close, closing channel"," ");
                    localChannel.close();
                    return;
                } else if(name.equals("_error")) {
                    Log.d("ERROR - ","closing channel server resonded with error"+ command);
                    localChannel.close();
                    return;
                } else {
                    Log.d("ignoring server command", " "+command);
                }
                break;
            case BYTES_READ://両サイドで x byte 読み込むごとに送信
            	message.decode(message.encode());
                Log.d("ClientHandler","ack from serverBYTES_READ "+message.getHeader().getSize());
                Log.d("ClientHandler","ack from serverBYTES_READ "+message);
                Log.d("ClientHandler","ack from serverBYTES_READ "+message.encode());
                byte[] d = message.encode().array();
                Log.d("ClientHandler","ack from serverBYTES_READ "+Utils.toHex(d,0,d.length,true));
//                int aggregateDuration = (d[0] )

        		int data = (d[0]<<24) | (d[1]<<16 & 0x00FF0000) | (d[2]<<8 & 0x0000FF00)| (d[3] & 0x000000FF);
        		Log.d("ClientHandler","BYTES_READ" + data);
        		if(liveSetting.getMode() == 3){
        		Log.d("ClientHandler","DataDuration " + this.publisher.getDataDuration());
        		Log.d("ClientHandler","DIFF " + (publisher.getDataDuration()-data));
//        		publisher.setAggregateDuration(data);
        		}
                break;
            case WINDOW_ACK_SIZE:
                Log.d("ClientHandler","WINDOW_ACK_SIZE ");
                WindowAckSize was = (WindowAckSize) message;
                if(was.getValue() != bytesReadWindow) {
                    localChannel.write(SetPeerBw.dynamic(bytesReadWindow),null);
                }
                break;
            case SET_PEER_BW:
                Log.d("ClientHandler","SET_PEER_BW ");
                SetPeerBw spb = (SetPeerBw) message;
                if(spb.getValue() != bytesWrittenWindow) {
                    localChannel.write(new WindowAckSize(bytesWrittenWindow),null);
                }
                break;
            default:
            Log.d("ClientHandler","ignore "+message.toString());
        }
        if(publisher != null && publisher.isStarted()) { // TODO better state machine
//            publisher.fireNext(channel, 0);
        }
    }//End of messageReveived


	public void exceptionCaught(final ExceptionEvent e) {
		ChannelUtils.exceptionCaught(e);
		new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... arg0) {
				ACT.stopStream();
				return null;
			}
			@Override
			protected void onPostExecute(Void arg){
				Toast.makeText(ACT, e.getCause().toString(), Toast.LENGTH_LONG).show();
			}
		}.execute();
	}
    private void showNofileToast(final int val) {
    	ACT.runOnUiThread(new Runnable(){
    		@Override
    		public void run(){
    			Builder alert = new AlertDialog.Builder(ACT);
				switch(val){
				case -1:
					break;
				case -2:
					alert.setMessage("ファイルが見つかりませんでした");
					break;
				}
				alert.create().show();
    		}
		});
	}

	public void startPublish() {
		Log.d("ClientHandler","startPublish");
		Channels.setStreamPhase(0);
		if(task != null && task.getStatus() != AsyncTask.Status.FINISHED){
			task.cancel(true);
		}
		task = new PublishTask();
		task.execute();
	}


	public void stopStream() {
		Log.d("ClientHandler","StopStream");
		liveSetting.setStreamStarted(false);
		if(publisher != null)publisher.close(ch);
	}
	public void stopStream(NioSocketChannel channel) {
		Log.d("ClientHandler","StopStream from NioWorker ---- ");
		liveSetting.setStreamStarted(false);
		if(publisher != null)publisher.close(channel);
	}




	public boolean getUnpublish() {
		if(isUnpublished){
			isUnpublished = false;
			return true;
		}
		return false;
	}

	public void setReader(RtmpReader reader2) {
		this.reader = reader2;
	}




}
