package nliveroid.nlr.main;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {
	private static Toast toast;
	private static TextView tv;

	private static Toast whiteToast;
	private static TextView whiteTv;

	public static void customToastShow(Context context,String message){
		if(context == null){
			return;
		}
		if(tv != null&&toast != null){
		tv.setText(message);
		toast.show();
		}else{
			LayoutInflater inflater = LayoutInflater.from(context);
			View layout = inflater.inflate(R.layout.custom_toastlayout, null);
			tv = (TextView)layout.findViewById(R.id.toast_text);
			tv.setText(message);
		toast = new Toast(context);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();//一番初回に呼ばれた時にもshowする
		}
	}

	public static void simpleToastShow(Context context,String message){
		if(context == null){
			return;
		}
		if(whiteTv != null&&whiteToast != null){
		whiteTv.setText(message);
		whiteToast.show();
		}else{
			LayoutInflater inflater = LayoutInflater.from(context);
			View layout = inflater.inflate(R.layout.simple_toastlayout, null);
			whiteTv = (TextView)layout.findViewById(R.id.toast_text);
			whiteToast = new Toast(context);
			whiteToast.setView(layout);
			whiteToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			whiteToast.setDuration(Toast.LENGTH_SHORT);
		}
	}

}
