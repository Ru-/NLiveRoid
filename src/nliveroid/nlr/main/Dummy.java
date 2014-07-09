package nliveroid.nlr.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class Dummy extends Activity{
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent top = new Intent();
		top.putExtra("scheme", getIntent().getDataString());
		 //NLRを起動する
		top.setClassName("nliveroid.nlr.main", "nliveroid.nlr.main.TopTabs");
  		try{
			startActivity(top);
		}catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
		this.finish();
	}

}
