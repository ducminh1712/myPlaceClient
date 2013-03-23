package org.k2htm.tnc;

import edu.k2htm.clientHelper.DuplicateUserException;
import edu.k2htm.clientHelper.HoaHelper;
import edu.k2htm.datahelper.User;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private EditText edtUsr, edtPass, edtRePass;
	private Button btnConfirm;
	public static final String TAG = "Register Activity";
	private boolean reg_usr_ok = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reg_layout);
		// get view
		edtUsr = (EditText) findViewById(R.id.edtUsername);
		edtPass = (EditText) findViewById(R.id.edtPassword);
		edtRePass = (EditText) findViewById(R.id.edtRePassword);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		// on click listener
		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String edtPassStr = edtPass.getText().toString();
				String edtRePassStr = edtRePass.getText().toString();
				Log.i(TAG, edtPassStr + ":" + edtRePassStr);
				if (edtUsr.getText().toString().equals("")
						|| edtPass.getText().toString().equals("")
						|| edtRePass.getText().toString().equals("")) {
					Log.i(TAG, "usr_str == null || pass_str == null");
					Toast.makeText(RegisterActivity.this,
							R.string.toast_usr_pass_null, Toast.LENGTH_SHORT)
							.show();
				} else if (!edtPassStr.equals(edtRePassStr)) {
					Log.i(TAG, "pass " + edtPass.getText().toString()
							+ " != repass " + edtRePass.getText().toString());
					Toast.makeText(RegisterActivity.this,
							R.string.toast_pass_repass_error,
							Toast.LENGTH_SHORT).show();
				} else {

					String usr_str = edtUsr.getText().toString();
					String pass_str = edtPass.getText().toString();

					Log.i(TAG, "RegTask execute");
					RegTask mRegTask = new RegTask();
					mRegTask.execute(usr_str, pass_str);

				}
			}
		});
	}

	private class RegTask extends AsyncTask<String, String, Void> {
		boolean result = false;

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub

			User mUser = new User(params[0], params[1], new HoaHelper(
					TrafficNetworkClient.ADDRESS));
			try {

				Log.i(TAG, "doinbackground result=regUser()");
				mUser.register();
				result = true;
				Log.i(TAG, "result=checkuser() ook\n result = " + result);
			} catch (DuplicateUserException ex) {
				Log.i(TAG, ex.getMessage());
				publishProgress(getText(R.string.duplicate_error) + "");

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
//IF FACEBOOK  -> REG ACC FACEBOOK
			Toast.makeText(RegisterActivity.this, values[0], Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void para) {
			// TODO Auto-generated method stub
			super.onPostExecute(para);
			// REG USERNAME AND PASSWORD
			if (result == true) {
				Log.i(TAG, "reg_usr_ok");
				// CREATEM USER HERE
				// call intent
				Intent oIntent = new Intent(RegisterActivity.this,
						LogInActivity.class);
				// BUNDLE ?
				startActivity(oIntent);
				Log.i(TAG,
						"Register successfully. To LogInActivity.class . Finalize RegisterActivity.this");
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} 
//			else {
//				Log.i(TAG, "check_usr_ok==false");
//				Toast.makeText(RegisterActivity.this,
//						R.string.toast_usr_pass_check_failed,
//						Toast.LENGTH_SHORT).show();
//			}
		}
	}
}
