package nliveroid.nlr.main;

import java.io.IOException;
import java.io.InputStream;

import nliveroid.nlr.main.ErrorCode;
import nliveroid.nlr.main.MyToast;
import nliveroid.nlr.main.Request;
import nliveroid.nlr.main.URLEnum;
import nliveroid.nlr.main.parser.CommunityPageParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

public class CommuBrowser extends AsyncTask<Void,Void,Void>{
	private Context context;
	private ErrorCode error;
	private ProgressDialog p;
	private ProgressBar progressBar;
	private boolean ENDFLAG = true;
	private int index;
	private String co;
	private String result;
	protected CommuBrowser(Context context,ErrorCode error,ProgressBar progressbar,int index , String co){
		this.index = index;
		this.co = co;
		this.context = context;
		this.error = error;
		this.progressBar = progressbar;
	}
	@Override
	public void onPreExecute(){
		super.onPreExecute();
		p = new ProgressDialog(context);
		p.setMessage("情報取得中...");
		p.cancel();
		p.show();
	}
	@Override
	public void onCancelled(){
		if(p != null && p.isShowing())p.cancel();
		super.onCancelled();
	}
	@Override
	protected Void doInBackground(Void... params) {
		 try {
			 if(error == null || error.getErrorCode() != 0){
					return null;
				}
			InputStream is = Request.doGetToInputStreamFromFixedSession(Request.getSessionID(error), URLEnum.COMMUNITYURL + co, error);
			if(error == null || error.getErrorCode() != 0){
				return null;
			}
			org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
			  CommunityPageParser handler = new CommunityPageParser(index,this);
		        parser.setContentHandler(handler);
		        parser.parse(new InputSource(is));
		        progressBar.setProgress(35);
		  } catch (org.xml.sax.SAXNotRecognizedException e) {
		      // Should not happen.
			  e.printStackTrace();
		      throw new RuntimeException(e);
		  } catch (org.xml.sax.SAXNotSupportedException e) {
		      // Should not happen.
			  e.printStackTrace();
		      throw new RuntimeException(e);
		  } catch (IOException e) {
			  e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		long startT = System.currentTimeMillis();
		while(ENDFLAG){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				ENDFLAG = false;
				break;
			}catch(IllegalArgumentException e1){
				e1.printStackTrace();
				Log.d("NLiveRoid","IllegalArgumentException at Request getImageForList");
				ENDFLAG = false;
				break;
			}
			if((System.currentTimeMillis() - startT) > 30000){
				error.setErrorCode(-10);
				return null;
			}
		}
		return null;
	}
	public void onPageFinished(String result){
		ENDFLAG = false;
		this.result = result;
	}
@Override
protected void onPostExecute(Void arg){
	if(p != null && p.isShowing())p.cancel();
	if(error != null){
		if(error.getErrorCode() != 0){
			error.showErrorToast();
		}else if(result != null){
			if(result.equals("Nothing")){
				if(index == 0){
					MyToast.customToastShow(context, "ブロマガをみつけられませんでした");
				}else{
					MyToast.customToastShow(context, "オーナーページをみつけられませんでした");
				}
				return;
			}
			Uri uri = Uri.parse(result);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setDataAndType(uri, "text/html");
			context.startActivity(intent);
		}
	}
}
}
