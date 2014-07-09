package nliveroid.nlr.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import nliveroid.nlr.main.parser.XMLparser;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CommunityList  extends Activity{
	private Activity ACT;
	private ArrayAdapter<String[]> adapter;
	private LayoutInflater inflater;
	private HashMap<String,String> isAlert;
	private ArrayList<String> ids;
	private ArrayList<String> titles;
	private ArrayList<String> ownerNames;
	private ListView listview;
	private String communityFile = "communityList";
	private int width;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ACT = this;
		inflater = LayoutInflater.from(this);
		View parent = inflater.inflate(R.layout.commu_list, null);
		width = ((NLiveRoid)getApplicationContext()).getViewWidth();
		TextView head0 = (TextView) parent.findViewById(R.id.commu_head_idtitle);
		head0.setWidth( width / 2);
		TextView head1 = (TextView) parent.findViewById(R.id.commu_head_owner);
		head1.setWidth(width / 2);
		TextView head2 = (TextView) parent.findViewById(R.id.commu_head_owner);
		head2.setWidth(width);
		listview = (ListView) parent.findViewById(R.id.commu_list);
		adapter = new HandleListAdapter(this, R.layout.commu_lsit_row);
		listview.setAdapter(adapter);
		registerForContextMenu(listview);
		ids = new ArrayList<String>();
		titles = new ArrayList<String>();
		ownerNames = new ArrayList<String>();
		// ファイル読み込み
		readList();
		for (int i = 0; i < ids.size(); i++) {
			adapter.add(new String[] { titles.get(i), ids.get(i),ownerNames.get(i) });
		}

		Button update = (Button)parent.findViewById(R.id.commu_update);
		update.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				new UpdateTask().execute();
			}
		});
		Button allselect = (Button)parent.findViewById(R.id.commu_allselect);
		allselect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

			}
		});
		Button alldeselect = (Button)parent.findViewById(R.id.commu_alldeselect);
		update.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
			}
		});

	}

	class UpdateTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... arg0) {

			return null;
		}

		@Override
		protected void onPostExecute(Void arg){
//			if
		}

	}
	class HandleListAdapter extends ArrayAdapter<String[]> {

		public HandleListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View paramView,
				ViewGroup paramViewGroup) {
			// String[] array = getItem(position);
			View view = inflater.inflate(R.layout.commu_lsit_row, null);
			TextView id = (TextView) view.findViewById(R.id.commu_coid);
			id.setWidth(width / 2);
			id.setText(ids.get(position));
			TextView ownname = (TextView) view.findViewById(R.id.commu_ownername);
			ownname.setText(ownerNames.get(position));
			ownname.setWidth(width / 2);
			TextView title = (TextView) view.findViewById(R.id.commu_cotitle);
			title.setText(titles.get(position));
			title.setWidth(width);

			return view;
		}

	}
	/**
	 * コミュニティファイルの読み込み
	 *
	 * @author Owner
	 *
	 */

	protected void readList() {// 設定ファイルを読み込み

		FileInputStream fis = null;

		try {
			fis = openFileInput(communityFile);
			BufferedReader  br = new BufferedReader(new InputStreamReader(fis));
            String ch = "";
            String source = "";
            while ((ch = br.readLine()) != null) {
            	source += ch;
            }
            br.close();
            fis.close();
			XMLparser.setCommunityMaps(ids, ownerNames, titles, isAlert, source.getBytes());
		} catch (FileNotFoundException e) {
			writeHandleName();//xmlヘッダフッタだけを書き込み
			readList();
//			e.printStackTrace();
		} catch(Exception e){
			MyToast.customToastShow(this, "コミュニティリストの読み込みに失敗しました");
		}




	}



	/**
	 * コミュニティリストの書き込み
	 *
	 * @author Owner
	 *
	 */

	private void writeHandleName() {
		try {

			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<CommunityList xmlns=\"http://nliveroid-tutorial.appspot.com/CommunityList/\">\n";
			for (int i = 0; i < ids.size(); i++) {
				xml += "<community id=\""+ ids.get(i)+ "\" ownername=\""+ ownerNames.get(i) + "\" title=\"" + titles.get(i)+ "\">" + isAlert.get(ids.get(i)) + "</user>\n";
			}
			xml += "</CommunityList>";

			Context mContext = createPackageContext("nliveroid.nlr.main", CONTEXT_RESTRICTED);
			FileOutputStream fos = mContext.openFileOutput(communityFile, MODE_PRIVATE);
			fos.write(xml.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
			e.printStackTrace();
		} catch (IOException e) {
			ErrorCode error = ((NLiveRoid)getApplicationContext()).getError();
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}


}
