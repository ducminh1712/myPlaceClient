package org.k2htm.tnc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import edu.k2htm.clientHelper.HoaHelper;
import edu.k2htm.datahelper.Comment;
import edu.k2htm.datahelper.CommentGetter;
import edu.k2htm.datahelper.VoteSetGetter;

public class IncidentDetailActivity extends Activity {
	private ListView lvComment;
	private Button btnSendComment;
	private ImageButton btnShare;
	private EditText edtContentComment;
	private TextView tvUsername, tvDes;
	private TextView tvType, tvTime;
	private TextView tvUpVote, tvDownVote;
	private ImageView imvSmall, imvBig;
	private String tmpImageUri;
	private Bitmap tmpBitmapImage;
	private int cautionID;
	private TrafficNetworkClient mApplication;
	public static final String USERNAME = "username";
	public static final String TYPE = "type";
	public static final String TIME = "time";
	public static final String UPVOTE = "upvote";
	public static final String DOWNVOTE = "downvote";
	public static final String DESCRIPTION = "description";
	public static final String IMAGE = "image";
	public static final String ID = "cautionID";
	private static final String TAG = "IncidentDetailActivity";
	// facebook
	Facebook mFacebook = new Facebook("294323060688115");
	AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(mFacebook);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_incident_details);
		// get app object
		mApplication = (TrafficNetworkClient) getApplication();
		// get view
		lvComment = (ListView) findViewById(R.id.lvComment);
		btnSendComment = (Button) findViewById(R.id.btnSendComment);
		edtContentComment = (EditText) findViewById(R.id.edtContentComment);
		tvUsername = (TextView) findViewById(R.id.tvUsername);
		tvDes = (TextView) findViewById(R.id.tvDescription);
		tvType = (TextView) findViewById(R.id.tvIncType);
		tvUpVote = (TextView) findViewById(R.id.tvUpVote);
		tvDownVote = (TextView) findViewById(R.id.tvDownVote);
		tvTime = (TextView) findViewById(R.id.tvTime);
		imvSmall = (ImageView) findViewById(R.id.imvSmall);
		imvBig = (ImageView) findViewById(R.id.imvBig);
		btnSendComment = (Button) findViewById(R.id.btnSendComment);
		btnShare = (ImageButton) findViewById(R.id.btnShare);
		// set loading image
		imvSmall.setImageResource(R.drawable.loading_image);
		imvBig.setImageResource(R.drawable.loading_image);
		// set on Click

		// btnb share
		btnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tmpBitmapImage != null) {

					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.putExtra(Intent.EXTRA_TEXT, tvDes.getText());
					// put image
					shareIntent.putExtra(android.content.Intent.EXTRA_STREAM,
							getUrifromBitmap(tmpBitmapImage));
					shareIntent.setType("image/jpeg");
					startActivity(Intent
							.createChooser(shareIntent, "Share via"));

				} else {
					Toast.makeText(IncidentDetailActivity.this,
							"Image is still loading. Please wait a moment.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// btn send comment
		btnSendComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "click");
				if (!edtContentComment.getText().toString().equals("")) {
					new SendCommentTask().execute(edtContentComment.getText()
							.toString());
				} else {
					new GetCommentTask().execute(cautionID);
				}

				edtContentComment.setText("");
			}
		});
		// hide imvBig
		imvBig.setVisibility(View.GONE);
		loadInfo();
		// load comment
		new GetCommentTask().execute(cautionID);
		// loadComment();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.activity_incident_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.comment_refresh:
			new GetCommentTask().execute(cautionID);
			new VoteTask().execute("getvote");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadInfo() {
		// TODO Auto-generated method stub
		// get bundle
		Intent iIntent = getIntent();
		Bundle iBundle = iIntent.getExtras();
		tvUsername.setText(iBundle.getString(USERNAME));
		if (iBundle.containsKey(DESCRIPTION)) {
			tvDes.setText(iBundle.getString(DESCRIPTION));
		} else {
			tvDes.setText("No Description.");
		}
		tvType.setText(iBundle.getString(TYPE));
		tvTime.setText(iBundle.getString(TIME));
		tvUpVote.setText(iBundle.getString(UPVOTE));
		tvDownVote.setText(iBundle.getString(DOWNVOTE));
		// set ID
		cautionID = iBundle.getInt(ID);
		// get image
		GetImageTask getImageTask = new GetImageTask();
		getImageTask.execute(iBundle.getString(IMAGE));
		Log.i(TAG, "image : " + iBundle.getString(IMAGE));
	}

	void loadComment(ArrayList<Comment> comments) {

		// ArrayList<Comment> comments = new ArrayList<Comment>();
		// comments.add(new Comment("abc1", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc2", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc3", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc4", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// comments.add(new Comment("abc5", 2, "abc1", new Date().getTime()));
		// Tiep di.
		CommentItemAdapter adapter = new CommentItemAdapter(this, comments);
		lvComment.setAdapter(adapter);

	}

	private class GetImageTask extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... url) {
			// TODO Auto-generated method stub
			URL myFileUrl = null;
			try {
				// TEST
				// myFileUrl = new URL(
				// "http://www.belovedcars.com/wp-content/uploads/2012/03/2012-traffic-jam.jpg");
				// END TEST
				myFileUrl = new URL(url[0]);
				Log.i(TAG, myFileUrl + "");

				HttpURLConnection conn = (HttpURLConnection) myFileUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				Options options = new Options();
				options.inSampleSize = 2;
				tmpBitmapImage = BitmapFactory.decodeStream(is, null, options);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (tmpBitmapImage != null) {
				imvSmall.setImageBitmap(tmpBitmapImage);
				imvBig.setImageBitmap(tmpBitmapImage);
			}
		}
	}

	private class GetCommentTask extends
			AsyncTask<Integer, ArrayList<Comment>, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			CommentGetter mCommentGetter = new CommentGetter(
					params[0].intValue(), new HoaHelper(
							TrafficNetworkClient.ADDRESS));
			ArrayList<Comment> commentList;

			try {
				commentList = mCommentGetter.getComments(params[0].intValue());
				publishProgress(commentList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i(TAG, e.getMessage());
				publishProgress(null);
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(ArrayList<Comment>... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (values[0] == null) {
				Toast.makeText(IncidentDetailActivity.this,
						getText(R.string.network_error), Toast.LENGTH_SHORT)
						.show();
			} else {
				loadComment(values[0]);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
		}

	}

	private class SendCommentTask extends
			AsyncTask<String, String, ArrayList<Comment>> {
		CommentItemAdapter adapter;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Log.i(TAG, "pre");
		}

		@Override
		protected ArrayList<Comment> doInBackground(String... params) {
			// TODO Auto-generated method stub

			Log.i(TAG, "do");
			Comment mComment = new Comment(mApplication.getUser(), cautionID,
					params[0], new HoaHelper(TrafficNetworkClient.ADDRESS));
			try {
				Log.i(TAG, "send comment");
				mComment.sendComment();
				publishProgress("Comment Sent!");
				return new CommentGetter(cautionID, new HoaHelper(
						TrafficNetworkClient.ADDRESS)).getComments(cautionID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i(TAG, e.getMessage());
				publishProgress(getText(R.string.network_error) + "");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			Toast.makeText(IncidentDetailActivity.this, values[0],
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(ArrayList<Comment> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
			if (result != null) {
				loadComment(result);
			}

		}
	}

	public void showImage(View v) {
		imvBig.setVisibility(View.VISIBLE);

	}

	public void hideImage(View v) {
		imvBig.setVisibility(View.GONE);
	}

	public class VoteTask extends AsyncTask<String, String, int[]> {
		String vote = "";

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected int[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			vote = params[0];
			VoteSetGetter mVoteSetGetter = new VoteSetGetter(new HoaHelper(
					TrafficNetworkClient.ADDRESS));
			try {
				if (params[0].equals(IncidentDetailActivity.UPVOTE)) {

					mVoteSetGetter
							.vote(mApplication.getUser(), cautionID, true);

				} else if (params[0].equals(IncidentDetailActivity.DOWNVOTE)) {
					mVoteSetGetter.vote(mApplication.getUser(), cautionID,
							false);

				}

				return mVoteSetGetter.getVote(cautionID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i(TAG, e.getMessage());
				publishProgress(e.getMessage() + "");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (values[0].equals(getText(R.string.network_error))) {

				Toast.makeText(IncidentDetailActivity.this,
						getText(R.string.network_error), Toast.LENGTH_SHORT)
						.show();

			}
		}

		@Override
		protected void onPostExecute(int[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
			if (result == null) {
				return;
			}
			Log.i(TAG, result[0] + ":" + result[1]);
			tvUpVote.setText(Integer.toString(result[0]));
			tvDownVote.setText(result[1] + "");

		}

	}

	public void voteUp(View v) {

		new VoteTask().execute(IncidentDetailActivity.UPVOTE);
	}

	public void voteDown(View v) {
		new VoteTask().execute(IncidentDetailActivity.DOWNVOTE);
	}

	// save tmpBitmap to disk to get Uri
	public Uri getUrifromBitmap(Bitmap bitmapImage) {
		if (bitmapImage != null) {
			FileOutputStream fileOutputStream = null;
			File file = new File(Environment.getExternalStorageDirectory(),
					"tmp.jpg");
			try {
				fileOutputStream = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);
			tmpBitmapImage.compress(CompressFormat.JPEG, 100, bos);
			try {
				bos.flush(); 
				bos.close();
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return Uri.fromFile(file);
		} else {
			return null;
		}
	}

}
