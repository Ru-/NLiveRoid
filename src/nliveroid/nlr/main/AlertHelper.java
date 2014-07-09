package nliveroid.nlr.main;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * プロセスを殺されないようにする為
 * 別プロセスのサービス同士をバインドしあう
 * @author Owner
 *
 */
public class AlertHelper extends Service{
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	private static Messenger bcMessenger;
	private AlertHelper me;
    private boolean isFinish;
    private static ServiceConnection mConnection = new ServiceConnection() {
//		private Messenger bcMessenger;//無効にメッセージを送信する必要は無い
		public void onServiceConnected(ComponentName className, IBinder service) {
        	bcMessenger = new Messenger(service);
        }
        public void onServiceDisconnected(ComponentName className) {
        	bcMessenger = null;
        }
    };
	@Override
	public void onCreate() {
		Log.d("NLiveRoid","AlertHelper oncreate ----------");
		me = this;
		Notification notif = new Notification();
		// 見えないアイコンをセット
		notif.icon = R.drawable.alert_notificon;
		// アイコンを右に寄せる
		if (NLiveRoid.apiLevel < 9){
			notif.when = Long.MAX_VALUE; // v2.3 未満
		}else{
			notif.when = Long.MIN_VALUE; // v2.3 以上
		}
		startForeground(0, notif);
		super.onCreate();
	}
	 /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
		Log.d("NLiveRoid","AlertHelper onBind --- ");
		Notification notif = new Notification();
		// 見えないアイコンをセット
		notif.icon = R.drawable.alert_notificon;
		// アイコンを右に寄せる
		if (NLiveRoid.apiLevel < 9){
			notif.when = Long.MAX_VALUE; // v2.3 未満
		}else{
			notif.when = Long.MIN_VALUE; // v2.3 以上
		}
		startForeground(0, notif);
		return mMessenger.getBinder();
    }
    @Override
    public void onRebind(Intent intent){
    	Log.d("NLiveRoid","AlertHelper onRebind ----");
    	Notification notif = new Notification();
		// 見えないアイコンをセット
		notif.icon = R.drawable.alert_notificon;
		// アイコンを右に寄せる
		if (NLiveRoid.apiLevel < 9){
			notif.when = Long.MAX_VALUE; // v2.3 未満
		}else{
			notif.when = Long.MIN_VALUE; // v2.3 以上
		}
		startForeground(0, notif);
    	super.onRebind(intent);
    }
    @Override
    public boolean onUnbind(Intent intent){
		Log.d("NLiveRoid","AlertHelper onUnbind --- ");
		return super.onUnbind(intent);
    }

	@Override
	public boolean stopService(Intent intent) {
		Log.d("NLiveRoid","AlertHelper stopService ----------");
		return super.stopService(intent);
	}

	@Override
	public void onDestroy(){
		Log.d("NLiveRoid","AlertHelper onDestroy ------------ " + isFinish +"  " + bcMessenger);
		if(!isFinish&&bcMessenger != null){
			Message msg = new Message();
			msg.what = 1;
			try {
				bcMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}
    /** Handler of incoming messages from clients. */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	Log.d("NLiveRoid","AlertHelper handleMessage " + msg.what);
            switch (msg.what) {
                case 1:
                	isFinish = true;
                	try{
            		unbindService(mConnection);
                	}catch(IllegalArgumentException e){
                		e.printStackTrace();
                	}
                	stopSelf();
                    break;
                case 2:
//                	Intent intent = new Intent();
//                	intent.setAction("return_f.NLR");
//                	intent.putExtra("r_code", CODE.ALERT);
//                	intent.putExtra("alert_h", Process.myPid());
//                	sendBroadcast(intent);
                	break;
                case 3:
			bindService(new Intent("nliveroid.nlr.main.BackGroundService"),
		            mConnection, Context.BIND_AUTO_CREATE);
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
