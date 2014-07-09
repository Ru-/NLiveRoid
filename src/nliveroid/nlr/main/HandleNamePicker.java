package nliveroid.nlr.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nliveroid.nlr.main.ColorPickerView.OnColorChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class HandleNamePicker extends Dialog implements ColorPickable{

	private HandleNamePicker me;
	private Activity ACT;

    private OnColorChangedListener mListener;
    protected int mInitialBgColor;
    protected int mInitialFoColor;
	private String mInitialText;
	private String user_id;
    private ColorPickerView bgPickerView;
    private ColorPickerView foPickerView;
	private boolean isLiving;
    private ProgressDialog p;
	private TextView editText;
	public HandleNamePicker(Activity context,
			OnColorChangedListener listener, int initialBgColor, int initialFoColor,String id,String nickname,boolean isLiving) {
		super(context);
        ACT = context;
        this.mListener = listener;
        this.mInitialBgColor = initialBgColor;
        this.mInitialFoColor = initialFoColor;
        this.mInitialText = nickname;
        this.user_id = id;
        this.isLiving = isLiving;
	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		me = this;
		LayoutInflater inflater = LayoutInflater.from(ACT);
        View parent = inflater.inflate(R.layout.colordialog_extend, null);
        setTitle(user_id);
        OnColorChangedListener myColorChangeListener = new OnColorChangedListener() {
            public void colorChanged(int color) {
            	mListener.colorChanged(color);
                dismiss();
            }
        };
        editText = (EditText)parent.findViewById(R.id.chooseredit);
        editText.setText(mInitialText);
        editText.setBackgroundColor(mInitialBgColor);
        editText.setTextColor(mInitialFoColor);
        final LinearLayout rootLinearLayout = (LinearLayout)parent.findViewById(R.id.colorchooser_root);
        //親のLinearLayoutにピッカー部分をアドする
        bgPickerView = new ColorPickerView(ACT,this, myColorChangeListener, mInitialBgColor,true);
        foPickerView = new ColorPickerView(ACT,this, myColorChangeListener, mInitialFoColor,false);
        //初めは背景色にする
        rootLinearLayout.addView(bgPickerView);

        final SeekBar seek = (SeekBar)parent.findViewById(R.id.color_dialog_seek);
        seek.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
				if(seek.getProgress() == 0){
					seek.setProgress(1);
					rootLinearLayout.removeAllViews();
					rootLinearLayout.addView(foPickerView);
				}else{
					seek.setProgress(0);
					rootLinearLayout.removeAllViews();
					rootLinearLayout.addView(bgPickerView);
				}
				}
				return true;
			}
		});
        Button ok = (Button)parent.findViewById(R.id.chooser_button0);

        ok.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Matcher escape = Pattern.compile("<|>|/|\"").matcher(editText.getText().toString());
				if(escape.find()){
					MyToast.customToastShow(ACT, "<,>,/,\"は含めません");
					return;
				}
				((HandleNamable) ACT).setHandleName(bgPickerView.getCenterColor(),foPickerView.getCenterColor(),editText.getText().toString());
				me.dismiss();
			}
        });
        Button cancel = (Button)parent.findViewById(R.id.chooser_button1);
        cancel.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.dismiss();
			}
        });

        Button getusername = (Button)parent.findViewById(R.id.chooser_getusername);
        Button user_operation = (Button)parent.findViewById(R.id.chooser_userpage);

        Matcher mc = Pattern.compile("[^0-9]").matcher(user_id);//数値以外
        if(mc.find()){
        	user_operation.setEnabled(false);
        	getusername.setEnabled(false);
        }else{
        user_operation.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.dismiss();
				new AlertDialog.Builder(ACT)
				.setItems(new CharSequence[]{"ブラウザでユーザーページ","お気に入りユーザー"},new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							transitionUserPage();
							break;
						case 1:
							new FavoriteUser().execute();
							break;
						}
					}
				}).create().show();
			}
        });
        getusername.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				new GetSimpleUserName().execute();
			}
        });
        }
        Button commentlist = (Button)parent.findViewById(R.id.chooser_commentlist);
        if(!isLiving){
        	commentlist.setVisibility(View.GONE);
        }else{
        commentlist.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				me.dismiss();
				((HandleNamable)ACT).createCommentedList(user_id);
			}
        });
        }



        setContentView(parent);
	}
	class GetSimpleUserName extends AsyncTask<Void, Void, String> {
		private ErrorCode error;

		protected GetSimpleUserName() {
			NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
			error = app.getError();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			try {
				if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","GetSimpleUserName " + user_id);
				if (user_id == null || user_id.equals(""))
					return null;
				HttpURLConnection con = (HttpURLConnection) new URL(
						URLEnum.USERPAGE + user_id).openConnection();
				con.setRequestProperty("Cookie",
						Request.getSessionID(error));
				con.setRequestProperty("User-Agent", Request.user_agent);
				if(error == null || error.getErrorCode() != 0){
					return null;
				}
				InputStream is = con.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int size = 0;
				byte[] byteArray = new byte[1024];
				while ((size = is.read(byteArray)) != -1) {
					bos.write(byteArray, 0, size);
				}
				byteArray = bos.toByteArray();
				bos.close();
				String result = "";
				String test = new String(byteArray, "UTF-8");
				Log.d("NLiveRoid"," TEST " + test);
				Matcher mc = Pattern.compile("<h2><strong>.+</strong>さん</h2>")
						.matcher(test);
				if (mc.find()) {
					result = mc.group();
					result = result.substring(12, result.length() - 16);
					return result;
				}else{
					Matcher mc1 = Pattern.compile("<h2>.+<small>さん</small></h2>")
							.matcher(new String(byteArray, "UTF-8"));
					if (mc1.find()) {
						result = mc1.group();
						result = result.substring(4, result.length() - 22);
						return result;
					}
				}
				Log.d("NLiveRoid","Name not Found");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String arg) {
			if (arg == null||arg.equals("")) {
				if(error != null){
					error.showErrorToast();
				}else{
				MyToast.customToastShow(ACT, "ユーザ名取得に失敗");// 自動取得の場合ださないようにする必要がある
				}
			}
			editText.setText(arg);
		}
	}

	public void transitionUserPage() {
		if (user_id == null || user_id.equals(""))
			return;
		String url = URLEnum.USERPAGE + user_id;
		Uri uri = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setDataAndType(uri, "text/html");
		ACT.startActivity(i);// とりあえず何も返却値とらない
	}

	private class FavoriteUser extends AsyncTask<Void,Void,Integer>{

		private ErrorCode error;
		protected FavoriteUser(){
			NLiveRoid app = (NLiveRoid)ACT.getApplicationContext();
			error = app.getError();
			if(p==null)p = new ProgressDialog(ACT);
    		p.setMessage("情報取得中");
    		p.show();
		}
		@Override
		public void onCancelled(){
			super.onCancelled();
			if(p != null && p.isShowing())p.cancel();
		}
		@Override
		protected Integer doInBackground(Void... params) {
			if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","FavoriteUser " + user_id);
			if (user_id == null || user_id.equals(""))
				return -1;
			try{
			//PC版を見に行ってトークンを取得
			HttpURLConnection con = (HttpURLConnection) new URL(
					URLEnum.USERPAGE + user_id).openConnection();
			con.setRequestProperty("Cookie",
					Request.getSessionID(error));
			if(error == null || error.getErrorCode() != 0){
				return 0;
			}
			InputStream is = con.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int size = 0;
			byte[] byteArray = new byte[1024];
			while ((size = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, size);
			}
			byteArray = bos.toByteArray();
			String source = new String(byteArray,"UTF-8");
			final Matcher isWatch = Pattern.compile("watchBtns.*class=\"noWatching\"").matcher(source);
			if (isWatch.find()) {//まだお気に入りにされていない
				Log.d("NLiveRoid","Did't watching " );
				final Matcher token_mc = Pattern.compile("Globals.hash.*?=.*?'.*.*?'")
						.matcher(source);
				if(!token_mc.find())return -12;
				final Matcher reg_mc = Pattern.compile("watchRegisterBtn.*register:[0-9]+").matcher(source);
				if(reg_mc.find()){
					final String[] reg = reg_mc.group().split(":");
					if(reg.length < 2||reg[1] == null || reg[1].equals("")){
						return -10;
					}
				ACT.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(p != null && p.isShowing()){
							p.cancel();
						}
						new AlertDialog.Builder(ACT).setMessage("このユーザーをお気に入りに追加しますか?")
						.setPositiveButton("YES", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(p==null)p = new ProgressDialog(ACT);
					    		p.setMessage("追加中...");
					    		p.show();
								favorite(true,token_mc.group().replaceAll("Globals.hash| |=|'", ""),reg[1]);
							}
						}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create().show();
					}
				});
				}else{
					return -11;
				}

			}else{//すでにお気に入りにされている
				Log.d("NLiveRoid","Already watching " );
				final Matcher token_mc = Pattern.compile("Globals.hash.*?=.*?'.*.*?'")
						.matcher(source);
				if(!token_mc.find())return -12;
				final Matcher reg_mc = Pattern.compile("watchRegisterBtn.*register:[0-9]+").matcher(source);
				if(reg_mc.find()){
					final String[] reg = reg_mc.group().split(":");
					if(reg.length < 2||reg[1] == null || reg[1].equals("")){
						return -10;
					}
				ACT.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(p != null && p.isShowing()){
							p.cancel();
						}
						new AlertDialog.Builder(ACT).setMessage("このユーザーのお気に入りを解除しますか?")
						.setPositiveButton("YES", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(p==null)p = new ProgressDialog(ACT);
					    		p.setMessage("解除中...");
					    		p.show();
								favorite(false,token_mc.group().replaceAll("Globals.hash| |=|'", ""),reg[1]);
							}
						}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create().show();
					}
				});
				}else{
					return -11;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			return 0;
		}

		private void favorite(final boolean isAdd,final String token,final String reg) {
			Log.d("NLiveRoid","PARAMS ---- " +  token + " " + reg);
			new AsyncTask<Void,Void,Integer>(){

				@Override
				public void onCancelled(){
					super.onCancelled();
					if(p != null && p.isShowing())p.cancel();
				}
				@Override
				protected Integer doInBackground(Void... params) {
					HttpURLConnection con2;
					try {
					con2 = (HttpURLConnection) new URL(
								URLEnum.FAVARITE_API + (isAdd? "add":"delete")).openConnection();

					con2.setRequestProperty("Cookie",
							Request.getSessionID(error));//セッションセットはいらないかもしれない
					if(error == null || error.getErrorCode() != 0){
						return 0;
					}
					con2.setDoOutput(true);
					PrintStream out = new PrintStream(con2.getOutputStream());
					out.print((isAdd? "item_type=1&item_id="+reg +"&token="+token:"id_list[1][]="+reg+"&token="+token));
					InputStream is = con2.getInputStream();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					int size = 0;
					byte[] byteArray = new byte[1024];
					while ((size = is.read(byteArray)) != -1) {
						bos.write(byteArray, 0, size);
					}
					String response = new String(bos.toByteArray(),"UTF-8");
					is.close();
					bos.close();
					Log.d("NLiveRoid","Favorite Response " + response);
					if(response.matches(".*status.*ok.*")){
						return 1;
					}else if(response.contains("error")){
						return -1;
					}
					con2.disconnect();
					} catch (MalformedURLException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
					return 0;
				}
				@Override
				protected void onPostExecute(Integer arg){
					if(p != null && p.isShowing()){
						p.cancel();
					}
					if(arg == 1){
						if(isAdd){
							MyToast.customToastShow(ACT, "お気に入りユーザーに追加しました");
						}else{
							MyToast.customToastShow(ACT, "お気に入りユーザーを解除しました");
						}
					}else if(arg == -1){
						if(isAdd){
							MyToast.customToastShow(ACT, "お気に入り追加処理に失敗");
						}else{
							MyToast.customToastShow(ACT, "お気に入り解除処理に失敗");
						}
					}
				}

			}.execute();

		}
	@Override
	protected void onPostExecute(Integer arg){
		if(p != null && p.isShowing()){
			p.cancel();
		}
		if(arg == 0){
			if(error != null){
				error.showErrorToast();
			}
		}else if(arg == -11){
			MyToast.customToastShow(ACT, "処理に失敗(ボタン未検出→自分を登録?)");
		}else{
			MyToast.customToastShow(ACT, "謎の問題が発生:code"+arg);
		}
		}
	}

	@Override
	public void setETColor(int color, boolean isBg) {
		if(isBg){
			editText.setBackgroundColor(color);
		}else{
			editText.setTextColor(color);
		}
	}

}
