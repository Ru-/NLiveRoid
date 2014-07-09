package nliveroid.nlr.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CommentListAdapter extends ArrayAdapter<String[]> {
	public CommentListAdapter(Context context) {
		super(context, R.layout.comment_row);
	}
	@Override
	public View getView(int position, View paramView,
			ViewGroup paramViewGroup) {
		return paramView;
	}
	public void addRow(String[] str) {
		super.add(str);
	}
}// End of CommentListAdapter

