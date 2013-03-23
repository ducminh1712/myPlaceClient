package org.k2htm.tnc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import edu.k2htm.clientHelper.DuplicateUserException;
import edu.k2htm.clientHelper.HoaHelper;
import edu.k2htm.datahelper.User;

public class LogInActivity extends Activity {
	private EditText edtUsr, edtPass;
	private TextView btnReg;
	private Button btnConfirm, btnFbLogin;
	private TrafficNetworkClient mApplication;
	public static final String TAG = "Login Activity";
	private boolean check_usr_ok = false;
	// facebook
	Facebook facebook = new Facebook("294323060688115");
	AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.login_layout);
		// app obj
		mApplication = (TrafficNetworkClient) getApplication();
		// get view
		edtUsr = (EditText) findViewById(R.id.edtUsername);
		edtPass = (EditText) findViewById(R.id.edtPassword);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		btnReg = (TextView) findViewById(R.id.btnReg);
		btnFbLogin = (Button) findViewById(R.id.btnFacebook);

		// on click listener
		// btnFbLogin
		btnFbLogin.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				facebook.authorize(LogInActivity.this, new DialogListener() {
					public void onComplete(Bundle values) {
						// The user has logged in, so now you can query and
						// use their Facebook info
						mAsyncRunner.request("me", new RequestListener() {

							@Override
							public void onComplete(String response, Object state) {
								// TODO Auto-generated method stub
								try {
									Log.i(TAG, response);
									// parse JSON
									JSONObject jsonObj = new JSONObject(
											response);
									Log.i(TAG, jsonObj.getString("username"));
									// save info to App obj
									mApplication.setFbJson(response);
									mApplication.setUser(jsonObj.getString("username")+"@facebook.com");
									mApplication.setLoginAsFb(true);
									// login / reg with server
									new LoginAsFBTask().execute(jsonObj.getString("username")+"@facebook.com");
								} catch (FacebookError e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onIOException(IOException e,
									Object state) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onFileNotFoundException(
									FileNotFoundException e, Object state) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onMalformedURLException(
									MalformedURLException e, Object state) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onFacebookError(FacebookError e,
									Object state) {
								// TODO Auto-generated method stub

							}
						});

					}

					public void onFacebookError(FacebookError error) {
						Toast.makeText(LogInActivity.this,
								"Something went wrong. Please try again.",
								Toast.LENGTH_LONG).show();
					}

					public void onError(DialogError error) {
						Toast.makeText(LogInActivity.this,
								"Something went wrong. Please try again.",
								Toast.LENGTH_LONG).show();
					}

					public void onCancel() {
						Toast.makeText(LogInActivity.this,
								"Something went wrong. Please try again.",
								Toast.LENGTH_LONG).show();
					}
				});

			}
		});
		// btnReg
		btnReg.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				// TODO Auto-generated method stub

				switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					btnReg.setTextColor(0xFFFF6600); // orange
					break;
				case MotionEvent.ACTION_UP:
					btnReg.setTextColor(0xFF000000); // black
					break;
				}
				return false;
			}
		});
		// tvReg
		btnReg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				register(v);
			}
		});
		// btnConfirm
		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (edtUsr.getText().toString().equals("")
						|| edtPass.getText().toString().equals("")) {
					Log.i(TAG, "usr_str == null || pass_str == null");
					Toast.makeText(LogInActivity.this,
							R.string.toast_usr_pass_null, Toast.LENGTH_SHORT)
							.show();
				} else {
					// CHECK USERNAME AND PASSWORD
					String[] para = new String[2];
					para[0] = edtUsr.getText().toString();
					para[1] = edtPass.getText().toString();
					LoginTask task = new LoginTask();

					Log.i(TAG, "task execute");
					task.execute(para[0], para[1]);
				}
			}

		});
	}

	public void register(View view) {

		// call intent
		Intent oIntent = new Intent(LogInActivity.this, RegisterActivity.class);
		startActivity(oIntent);
		Log.i(TAG, "To Register.class ");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		btnReg.setTextColor(0xFF000000); // black
		setProgressBarIndeterminateVisibility(false);
	}

	private class LoginTask extends AsyncTask<String, String, Boolean> {
		Boolean result;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			User mUser = new User(params[0], params[1], new HoaHelper(
					TrafficNetworkClient.ADDRESS));
			try {

				Log.i(TAG, "doinbackground result=checkuser()");
				result = mUser.checkUser();
				Log.i(TAG, "result=checkuser() ook\n result = " + result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i(TAG, e.getMessage());
				publishProgress(getText(R.string.network_error) + "");
				return false;
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			Toast.makeText(LogInActivity.this, "Network Error",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			check_usr_ok = result;
			Log.i(TAG, check_usr_ok + "!");
			if (check_usr_ok) {
				Log.i(TAG, "check_usr_ok");
				// SET CURRENT USER IN APPLICATION OBJECT
				mApplication.setUser(edtUsr.getText().toString());
				mApplication.setLoginAsFb(false);
				mApplication.setFbJson("");
				Log.i(TAG, "Logged in as : " + mApplication.getUser());
				// call intent
				Intent oIntent = new Intent(LogInActivity.this,
						TrafficMap.class);
				startActivity(oIntent);
				Log.i(TAG,
						"Login successfully. To TrafficMap.class . Finalize LogInActivity.this");
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Log.i(TAG, "check_usr_ok==false");
				Toast.makeText(LogInActivity.this,
						R.string.toast_usr_pass_check_failed,
						Toast.LENGTH_SHORT).show();
			}

			setProgressBarIndeterminateVisibility(false);
		}
	}

	private class LoginAsFBTask extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		@Override
		protected Boolean doInBackground(String... params) {
			// new User with password == md5Encode of Username
			User mUser = new User(params[0], md5Encode(params[0]),
					new HoaHelper(TrafficNetworkClient.ADDRESS));
			Log.i(TAG, "new User : " + params[0] + ":" + md5Encode(params[0]));
			try {
				mUser.register();		
				
			} catch (DuplicateUserException ex) {
				publishProgress("Login with Facebook account");
				return true;
			} catch (Exception e) {
				publishProgress(getText(R.string.network_error) + "");
				return false;
			}
			publishProgress("Login with Facebook account");
			return true;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			Toast.makeText(LogInActivity.this,values[0],
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			check_usr_ok = result;
			Log.i(TAG, check_usr_ok + "!");
			if (check_usr_ok) {
				Log.i(TAG, "check_usr_ok");
				// SET CURRENT USER IN APPLICATION OBJECT
				Log.i(TAG, "Logged in as : " + mApplication.getUser());
				// call intent
				Intent oIntent = new Intent(LogInActivity.this,
						TrafficMap.class);
				startActivity(oIntent);
				Log.i(TAG,
						"Login successfully. To TrafficMap.class . Finalize LogInActivity.this");
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Log.i(TAG, "check_usr_ok==false");
				Toast.makeText(LogInActivity.this,
						R.string.toast_usr_pass_check_failed,
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	private String genPass() {
		return "shgs29083t723ius";

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public String md5Encode(String s) {
		try {
			Log.i(TAG,"gen MD5");
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

			Log.i(TAG,"gen MD5 complete");
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
