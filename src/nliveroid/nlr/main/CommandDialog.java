package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import nliveroid.nlr.main.parser.BSPTokenParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CommandDialog extends AlertDialog.Builder{
	private CommentPostable postable;
	private View parent;
	private CheckBox bsp;
	private Spinner bspColorSpinner;
	private TextView bspNameTX;
	private EditText bspNameET;
	private Button bspNameCommitBT;
	private TextView bspColorTX;
	private TableRow parentRow;
	public CommandDialog(final CommentPostable postable, boolean isOwner,final String sessionid,final String lv) {
		super((Context)postable);
		this.postable = postable;
		this.setTitle("コマンド");
		LayoutInflater inflater = LayoutInflater.from((Context)postable);
		parent = inflater.inflate(R.layout.commanddialog, null);
		this.setView(parent);

		//主コメチェックボックス
		CheckBox ownerCheck = (CheckBox)parent.findViewById(R.id.command_owner);
		if(isOwner){
			ownerCheck.setVisibility(View.VISIBLE);
			if(postable.getCmd().isOwner()){
			ownerCheck.setChecked(true);
			}else{
				ownerCheck.setChecked(false);
			}
		}else{
			ownerCheck.setEnabled(false);
		}
		ownerCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean flag) {//タップした後の値が返ってってくる
				postable.getCmd().setOwner(flag);
			}
		});
		//BSP
		//BSP確認ボタン
				Button bspCheck = (Button)parent.findViewById(R.id.command_bsp_check);

				bspCheck.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						parentRow = (TableRow)parent.findViewById(R.id.bsp_check_parent);
						parentRow.removeAllViews();
						TableLayout tl = new TableLayout((Context)postable);
						tl.setStretchAllColumns(true);
						//ビューをプログレスバーに変更する
						ProgressBar p = new ProgressBar((Context)postable);
						TextView dummy0 = new TextView((Context)postable);
						dummy0.setText("権限情報確認中");
						TextView dummy1 = new TextView((Context)postable);
						TableRow childrow = new TableRow((Context)postable);
						childrow.addView(p,-1,-2);
						childrow.addView(dummy0,-1,-2);
						childrow.addView(dummy1,-1,-2);
						tl.addView(childrow);
						parentRow.addView(tl,-1,-2);
						new GetBSPToken().execute(sessionid,lv);
					}
				});
	if(postable.getCmd().isBSPEnable()&&postable.getCmd().getBSPToken() != null &&!postable.getCmd().getBSPToken().equals("")){
		bspCheck.setVisibility(View.GONE);
			enableBSP();
	}



		 ArrayAdapter<String> posAdp = new ArrayAdapter<String>((Context)postable, android.R.layout.simple_spinner_item);
	        posAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        // アイテムを追加します
	        posAdp.add("naka");
	        posAdp.add("ue");
	        posAdp.add("shita");
	        posAdp.add("migi");
	        posAdp.add("hidari");
	         String cmdValue = postable.getCmd().getValue(CommandKey.Align);
	        int aPos = 0;
	        if(cmdValue.equals("")){
	        	aPos = 0;
	        }else if(cmdValue.equals("ue")){
	        	aPos = 1;
	        }else if(cmdValue.equals("shita")){
	        	aPos = 2;
	        }else if(cmdValue.equals("migi")){
	        	aPos = 3;
	        }else if(cmdValue.equals("hidari")){
	        	aPos = 4;
	        }
	        Spinner alignsp = (Spinner) parent.findViewById(R.id.position);
	        // アダプターを設定します
	        alignsp.setAdapter(posAdp);
	        alignsp.setSelection(aPos);
	        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
	        alignsp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view,
	                    int position, long id) {
	                Spinner pos = (Spinner) parent;
	                if(((String)pos.getSelectedItem()).equals("naka")){
	                	postable.setCmd(CommandKey.Align,"");
	                }else{
	                postable.setCmd(CommandKey.Align,((String) pos.getSelectedItem()));
	                }
	            }
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
	        });


	        ArrayAdapter<String> sizeAdp = new ArrayAdapter<String>((Context)postable, android.R.layout.simple_spinner_item);
	        sizeAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        // アイテムを追加します
	        sizeAdp.add("midium");
	        sizeAdp.add("small");
	        sizeAdp.add("big");
	        cmdValue = postable.getCmd().getValue(CommandKey.Size);
	        int sPos = 0;
	        if(cmdValue.equals("")){
	        	sPos = 0;
	        }else if(cmdValue.equals("small")){
	        	sPos = 1;
	        }else if(cmdValue.equals("big")){
	        	sPos = 2;
	        }
	        Spinner size = (Spinner) parent.findViewById(R.id.size);
	        size.setAdapter(sizeAdp);
	        size.setSelection(sPos);
	        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
	        size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view,
	                    int position, long id) {
	                Spinner size = (Spinner) parent;
	                // 選択されたアイテムを取得します
	                if(((String)size.getSelectedItem()).equals("midium")){//midiumってのが入らないように
	                	postable.setCmd(CommandKey.Size,"");
	                }else{
	                postable.setCmd(CommandKey.Size,((String) size.getSelectedItem()));
	                }

	            }

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
	        });


	        ArrayAdapter<String> colAdp = new ArrayAdapter<String>((Context)postable, android.R.layout.simple_spinner_item);
	        colAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        // アイテムを追加します
	        colAdp.add("white");
	        colAdp.add("red");
	        colAdp.add("pink");
	        colAdp.add("orange");
	        colAdp.add("yellow");
	        colAdp.add("green");
	        colAdp.add("cyan");
	        colAdp.add("blue");
	        colAdp.add("purple");
	        colAdp.add("black");
	        colAdp.add("white2");
	        colAdp.add("red2");
	        colAdp.add("orange2");
	        colAdp.add("yellow2");
	        colAdp.add("green2");
	        colAdp.add("blue2");
	        colAdp.add("purple2");
	        cmdValue = postable.getCmd().getValue(CommandKey.Color);
	        int cPos = 0;
	        if(cmdValue.equals("")){
	        	cPos = 0;
	        }else if(cmdValue.equals("red")){
	        	cPos = 1;
	        }else if(cmdValue.equals("pink")){
	        	cPos = 2;
	        }else if(cmdValue.equals("orange")){
	        	cPos = 3;
	        }else if(cmdValue.equals("yellow")){
	        	cPos = 4;
	        }else if(cmdValue.equals("green")){
	        	cPos = 5;
	        }else if(cmdValue.equals("cyan")){
	        	cPos = 6;
	        }else if(cmdValue.equals("blue")){
	        	cPos = 7;
	        }else if(cmdValue.equals("purple")){
	        	cPos = 8;
	        }else if(cmdValue.equals("black")){
	        	cPos = 9;
	        }else if(cmdValue.equals("white2")){
	        	cPos = 10;
	        }else if(cmdValue.equals("red2")){
	        	cPos = 11;
	        }else if(cmdValue.equals("orange2")){
	        	cPos = 12;
	        }else if(cmdValue.equals("yellow2")){
	        	cPos = 13;
	        }else if(cmdValue.equals("green2")){
	        	cPos = 14;
	        }else if(cmdValue.equals("blue2")){
	        	cPos = 15;
	        }else if(cmdValue.equals("purple2")){
	        	cPos = 16;
	        }
	        Spinner color = (Spinner) parent.findViewById(R.id.color);
	        // アダプターを設定します
	        color.setAdapter(colAdp);
	        color.setSelection(cPos);
	        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
	        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view,
	                    int position, long id) {
	                Spinner color = (Spinner) parent;
	                if(((String)color.getSelectedItem()).equals("white")){
	                	postable.setCmd(CommandKey.Color,"");
	                }else{
	                postable.setCmd(CommandKey.Color,((String) color.getSelectedItem()));
	                }
	            }

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
	        });

	        this.setPositiveButton("CLOSE", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
	        });
	}


	private void enableBSP(){

		postable.getCmd().setBSPEnable(true);
		bsp = (CheckBox)parent.findViewById(R.id.command_bsp);
	bspNameET = (EditText)parent.findViewById(R.id.bsp_name);
	bspNameTX = (TextView)parent.findViewById(R.id.bsp_name_label);
	bspColorTX = (TextView)parent.findViewById(R.id.bsp_color_label);
	bspColorSpinner = (Spinner)parent.findViewById(R.id.bsp_color);
	bspNameCommitBT = (Button)parent.findViewById(R.id.bsp_name_commit_bt);
	ArrayAdapter<String> bspColorAdapter = new ArrayAdapter<String>((Context)postable, android.R.layout.simple_spinner_item);
	bspColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	bspColorAdapter.add("white");//色は投稿時文字列で欲しいから
	bspColorAdapter.add("red");
        bspColorAdapter.add("green");
        bspColorAdapter.add("blue");
        bspColorAdapter.add("cyan");
        bspColorAdapter.add("yellow");
        bspColorAdapter.add("purple");
        bspColorAdapter.add("pink");
        bspColorAdapter.add("orange");
        bspColorAdapter.add("niconicowhite");
        String bspColor = postable.getCmd().getBSPColor();
        int setBSPColorValue = 0;
        if(bspColor.equals("white")){
        	//0のまま
        }else if(bspColor.equals("red")){
        	setBSPColorValue = 1;
        }else if(bspColor.equals("green")){
        	setBSPColorValue = 2;
        }else if(bspColor.equals("blue")){
        	setBSPColorValue = 3;
        }else if(bspColor.equals("cyan")){
        	setBSPColorValue = 4;
        }else if(bspColor.equals("yellow")){
        	setBSPColorValue = 5;
        }else if(bspColor.equals("purple")){
        	setBSPColorValue = 6;
        }else if(bspColor.equals("pink")){
        	setBSPColorValue = 7;
        }else if(bspColor.equals("orange")){
        	setBSPColorValue = 8;
        }else if(bspColor.equals("niconicowhite")){
        	setBSPColorValue = 9;
        }
        bspColorSpinner.setAdapter(bspColorAdapter);
        bspColorSpinner.setSelection(setBSPColorValue);
		//BSPが有効だった
		bsp.setVisibility(View.VISIBLE);
		bsp.setChecked(true);
		bspNameET.setVisibility(View.VISIBLE);
		bspNameTX.setVisibility(View.VISIBLE);
		bspColorTX.setVisibility(View.VISIBLE);
		bspColorSpinner.setVisibility(View.VISIBLE);
			bspColorSpinner.setSelection(setBSPColorValue);
		bspColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
            	String selectedItem = (String) bspColorSpinner.getItemAtPosition(position);
            	Log.d("Log","SELECTED COLOR "+selectedItem);
               postable.getCmd().setBSPColor(selectedItem);
            }
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });
		bspNameCommitBT.setVisibility(View.VISIBLE);
		if(postable.getCmd().getBSPName() != null&& !postable.getCmd().getBSPName().equals("")){
			bspNameET.setHint(postable.getCmd().getBSPName());
		}
		bspNameCommitBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!bspNameET.getText().toString().equals("")){
					postable.getCmd().setBSPName(bspNameET.getText().toString());
				}
			}
		});
	bsp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton compoundbutton,
				boolean flag) {//タップした後の値が返ってってくる
				postable.getCmd().setBSPEnable(flag);
		}
	});
	}

	public class GetBSPToken extends AsyncTask<String,Void,Integer>{
		private boolean ENDFLAG = true;
		private String token;
		private boolean isFailed;
		@Override
		protected Integer doInBackground(String... params) {
			HttpURLConnection con=null;
			try {
				con = (HttpURLConnection)new URL(URLEnum.PC_WATCHBASEURL+params[1]).openConnection();
				con.setRequestProperty("Cookie", params[0]);
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24");
				con.setInstanceFollowRedirects(false);
				InputStream source1 = con.getInputStream();
				BSPTokenParser handler = new BSPTokenParser(this);
				  org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
			        parser.setContentHandler(handler);
			        parser.parse(new InputSource(source1));
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return -3;
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			} catch (SAXException e) {
				e.printStackTrace();
			}
			con.disconnect();
				long startT1 = System.currentTimeMillis();
				while(ENDFLAG){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						ENDFLAG = false;
						e.printStackTrace();
						return 1;//キャンセル
					}catch(IllegalArgumentException e){
						Log.d("NLiveRoid","IllegalArgumentException at CommandDialog GetBSPToken");
						e.printStackTrace();
						ENDFLAG = false;
						return 1;//キャンセル
					}
					if(System.currentTimeMillis()-startT1>60000){
						//タイムアウト
						ENDFLAG = false;
						return -2;
					}
				}

			return 0;
		}
		public void finishCallBack(String token){
			this.token = token;
			postable.getCmd().setBSPToken(token);
			ENDFLAG = false;
		}
		public void finishCallBack() {
			//トークン無くて、footerがあった
			isFailed = true;
			ENDFLAG = false;
		}
		@Override
		protected void onPostExecute(Integer arg){
			if(parentRow != null){
				parentRow.removeAllViews();
			}
			if(isFailed){
				MyToast.customToastShow((Context)postable, "権限がないようです");
			}else{
		switch(arg){
			case  -2:
				MyToast.customToastShow((Context)postable, "接続タイムアウト");
				break;
			case -3:
				MyToast.customToastShow((Context)postable, "アクセスURLが間違い\n仕様変更されたようです");
				break;
			case 0:
				if(token == null){
					MyToast.customToastShow((Context)postable, "BSP権限が確認できませんでした");
				}else{
					enableBSP();
				}
				break;
			}
			}
		}

	}





}
