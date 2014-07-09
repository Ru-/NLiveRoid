package nliveroid.nlr.main;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class SettingTabs extends TabActivity{

	@Override
	public void onCreate(Bundle bundle){
			super.onCreate(bundle);
		 requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		    LayoutInflater factory = LayoutInflater.from(this);
		    View parent = factory.inflate(R.layout.common_tablayout , null);
		    setContentView(parent);

		    TabHost tabHost = this.getTabHost();
		    Intent primitiveIntent = new Intent(this, PrimitiveSetting.class);
		    Intent tableDispIntent = new Intent(this, Details.class);
		    primitiveIntent.putExtra("session", getIntent().getStringExtra("session"));
		    TabSpec tabSpec0 = tabHost.newTabSpec("pref_tab1").setContent(primitiveIntent).setIndicator("基本設定");
		    tabHost.addTab(tabSpec0);//ここでonCreateされる
		    TabSpec tabSpec1 = tabHost.newTabSpec("pref_tab2").setContent(tableDispIntent).setIndicator("オプション設定");
		    tabHost.addTab(tabSpec1);
		    //コメントオンリーと初期テーブル表示が共存できないので先にPrimitiveを表示してインスタンス化しておく
		    tabHost.setCurrentTab(0);
	}


}
