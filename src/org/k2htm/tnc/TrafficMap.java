package org.k2htm.tnc;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.k2htm.clientHelper.HoaHelper;
import edu.k2htm.datahelper.Report;
import edu.k2htm.datahelper.ReportGetter;
import edu.k2htm.datahelper.VoteSetGetter;

public class TrafficMap extends MapActivity implements LocationListener {
	private static MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private GeoPoint currentPoint;
	private Location currentLocation = null;
	private TrafficOverlay currPosOverlay;
	private TextView tvProvider;
	private Bitmap tmpBitmapImage;
	private boolean refreshing = false;
	private TextView tvUsername;
	private TextView tvType;
	private TextView tvUpVote, tvDownVote;
	private TrafficNetworkClient mApplication;
	private TextView tvDes, tvTime;
	private MenuItem refreshMenuItem;
	private ImageView imvBig, imvSmall;
	private LinearLayout llDetail, llPopupImage;
	private ListView lvComment;
	private Report curReport;
	public static final int REQUEST_CODE = 100;
	public static final String TAG = "Traffic Map";
	public static final String LONG = "longitude";
	public static final String LAT = "latitude";
	public static final String INCIDENT_TYPE = "title";
	public static final String INCIDENT_DESCRIPTION = "des";
	public static final int TRAFFIC_JAM_CODE = 0;
	public static final String TRAFFIC_JAM_STRING = "Traffic Jam";
	public static final int ACCIDENT_CODE = 1;
	public static final String ACCIDENT_STRING = "Accident";
	public static final int BLOCKED_CODE = 2;
	public static final String BLOCKED_STRING = "Blocked";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_traffic_map);
		// app obj
		mApplication = (TrafficNetworkClient) getApplication();
		Toast.makeText(TrafficMap.this,
				getText(R.string.login_user_toast) + mApplication.getUser(),
				Toast.LENGTH_SHORT).show();
		// find view
		imvBig = (ImageView) findViewById(R.id.imvBig);
		imvSmall = (ImageView) findViewById(R.id.imvSmall);
		tvProvider = ((TextView) findViewById(R.id.providerText));
		mapView = (MapView) findViewById(R.id.mapView);
		llDetail = (LinearLayout) findViewById(R.id.llDetail);
		// lvComment = (ListView) findViewById(R.id.lvComment);
		tvDes = (TextView) findViewById(R.id.tvDescription);
		tvType = (TextView) findViewById(R.id.tvIncType);
		tvUsername = (TextView) findViewById(R.id.tvUsername);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvUpVote = (TextView) findViewById(R.id.tvUpVote);
		tvDownVote = (TextView) findViewById(R.id.tvDownVote);
		// hide view
		llDetail.setVisibility(View.GONE);
		imvBig.setVisibility(View.GONE);
		// turn on zoom controller if device not support multitouch
		if (getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
			// do multitouch
			mapView.setBuiltInZoomControls(false);
		} else {
			// do magnifying glass
			mapView.setBuiltInZoomControls(true);
		}
		// mapview setting
		mapView.setSatellite(false);
		mapController = mapView.getController();
		mapController.setZoom(15);
		getLastLocation();
		animateToCurrentLocation();
		drawCurrPositionOverlay();

	}

	public void centerToCurrentLocation(View view) {
		animateToCurrentLocation();
	}

	public void showDetail(IncidentOverlayItem overlayItem) throws Exception {
		// clear
		tvType.setText("");
		// show Des
		tvDes.setText("");
		// show vote
		tvUpVote.setText("");
		tvUpVote.setText("");
		// do
		llDetail.setVisibility(View.VISIBLE);
		imvSmall.setImageResource(R.drawable.loading_image);
		imvBig.setImageResource(R.drawable.loading_image);

		curReport = overlayItem.getReport();

		Log.i(TAG, "show detail of report :" + curReport.toString());
		// tvType.setText(curReport.getType()+"");
		// show Type
		String typeStr = "";
		switch (curReport.getType()) {
		case TrafficMap.TRAFFIC_JAM_CODE:
			typeStr = getText(R.string.incident_type_0) + "";
			break;
		case TrafficMap.ACCIDENT_CODE:
			typeStr = getText(R.string.incident_type_1) + "";
			break;
		case TrafficMap.BLOCKED_CODE:
			typeStr = getText(R.string.incident_type_2) + "";
			break;
		default:
			break;
		}

		tvType.setText(typeStr);
		// show Des
		tvDes.setText(curReport.getDescription());
		// show vote
		tvUpVote.setText(curReport.getVoteUp() + "");
		tvUpVote.setText(curReport.getVoteDown() + "");
		// Show time
		String dateFormat = "hh:mm dd/MM/yyyy ";
		DateFormat formatter = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(curReport.getTime());
		tvTime.setText(formatter.format(calendar.getTime()) + "");
		// set usrname
		tvUsername.setText(curReport.getUsername());
		// center map to incident position
		GeoPoint curPoint = new GeoPoint(curReport.getLat(), curReport.getLng());
		if (curPoint != null) {
			mapController.animateTo(curPoint);
		}

		// download image
		GetImageTask getImageTask = new GetImageTask();
		getImageTask.execute(overlayItem.getReport().getImage());

	}

	public void hideDetail(View view) {
		llDetail.setVisibility(View.GONE);
	}

	public void getLastLocation() {
		String provider = getBestProvider();
		currentLocation = locationManager.getLastKnownLocation(provider);
		if (currentLocation == null) {

			currentLocation = TrafficOverlay.convertGpToLoc(new GeoPoint(
					21027555, 105849538));
			Toast.makeText(
					this,
					"Location not yet acquired.Set to default location : (21.027555;105.849538)",
					Toast.LENGTH_LONG).show();
		}
		setCurrentLocation(currentLocation);
		tvProvider.setText("Provider :" + getBestProvider());
	}

	public void animateToCurrentLocation() {
		if (currentPoint != null) {
			mapController.animateTo(currentPoint);
		}

	}

	public String getBestProvider() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String bestProvider = locationManager.getBestProvider(criteria, true);
		Log.d("provider used", bestProvider);
		return bestProvider;
	}

	// public void setCurrentLocation() {
	// currentPoint = new GeoPoint(21027555, 105849538);
	//
	// ((TextView) findViewById(R.id.latitudeText)).setText("Latitude : "
	// + String.valueOf((int) (currentPoint.getLatitudeE6())));
	// ((TextView) findViewById(R.id.longitudeText)).setText("Longitude : "
	// + String.valueOf((int) (currentPoint.getLongitudeE6())));
	// ((TextView) findViewById(R.id.accuracyText)).setText("Accuracy : "
	// + String.valueOf("N/A"));
	//
	// drawCurrPositionOverlay();
	// }

	public void setCurrentLocation(Location location) {
		int currLatitude = (int) (location.getLatitude() * 1E6);
		int currLongitude = (int) (location.getLongitude() * 1E6);
		currentPoint = new GeoPoint(currLatitude, currLongitude);

		((TextView) findViewById(R.id.latitudeText)).setText("Latitude : "
				+ String.valueOf((int) (currentLocation.getLatitude() * 1E6)));
		((TextView) findViewById(R.id.longitudeText)).setText("Longitude : "
				+ String.valueOf((int) (currentLocation.getLongitude() * 1E6)));
		((TextView) findViewById(R.id.accuracyText)).setText("Accuracy : "
				+ String.valueOf(location.getAccuracy()) + " m");

		drawCurrPositionOverlay();
	}

	public void drawCurrPositionOverlay() {
		List<Overlay> overlays = mapView.getOverlays();
		overlays.remove(currPosOverlay);
		Drawable marker = getResources().getDrawable(R.drawable.icon_you);
		currPosOverlay = new TrafficOverlay(marker, mapView, this);
		if (currentPoint != null) {
			IncidentOverlayItem youItem = new IncidentOverlayItem(currentPoint,
					"You are here!", "");
			currPosOverlay.addOverlay(youItem);
			overlays.add(currPosOverlay);
			currPosOverlay.setCurrentLocation(currentLocation);
		}
	}

	public void drawIncidentOverlay(ArrayList<Report> result) {
		mapView.getOverlays().clear();
		drawCurrPositionOverlay();
		mApplication.setReportList(result);
		List<Overlay> overlays = mapView.getOverlays();
		// create
		ArrayList<TrafficOverlay> incidentOverlays = new ArrayList<TrafficOverlay>();
		Drawable marker = getResources().getDrawable(R.drawable.indicator_jam);
		incidentOverlays.add(new TrafficOverlay(marker, mapView, this));
		marker = getResources().getDrawable(R.drawable.indicator_accident);
		incidentOverlays.add(new TrafficOverlay(marker, mapView, this));
		marker = getResources().getDrawable(R.drawable.indicator_blocked);
		incidentOverlays.add(new TrafficOverlay(marker, mapView, this));

		/*
		 * read from ArrayList
		 */
		Log.i(TAG, "read List:" + result.size());
		//
		// String tmp_lat;
		// String tmp_long;
		// String tmp_type;
		// String des_string;
		// String type_string = TRAFFIC_JAM_STRING;
		// String imageUri;
		// String username;
		// String time;
		for (int i = 0; i < result.size(); i++) {
			Report curReport = result.get(i);
			// tmp_lat = curReport.getLat();
			// Log.i(TAG, "tmp_lat:" + tmp_lat);
			// tmp_long = curReport.getLng();
			// Log.i(TAG, "tmp_long:" + tmp_long);
			// tmp_type = curReport.getType();
			// Log.i(TAG, "tmp_type:" + tmp_type);
			// des_string = curReport.getDescription();
			// Log.i(TAG, "tmp_des:" + des_string);
			// imageUri = curReport.getImage();
			// Log.i(TAG, "image uri:" + imageUri);
			// username = curReport.getUsername();
			// Log.i(TAG, "username:" + username);
			// time = curReport.getTime();
			// Log.i(TAG, "time:" + time);
			// get type string
			// switch (tmp_type) {
			// case TrafficMap.TRAFFIC_JAM_CODE:
			// type_string = TRAFFIC_JAM_STRING;
			// break;
			// case TrafficMap.ACCIDENT_CODE:
			// type_string = ACCIDENT_STRING;
			// break;
			// case TrafficMap.BLOCKED_CODE:
			// type_string = BLOCKED_STRING;
			// break;
			// default:
			// break;
			// }
			IncidentOverlayItem overlayItem = new IncidentOverlayItem(curReport);

			// draw correct icon at that position
			switch (curReport.getType()) {
			case TrafficMap.TRAFFIC_JAM_CODE:
				incidentOverlays.get(TRAFFIC_JAM_CODE).addOverlay(overlayItem);
				break;
			case TrafficMap.ACCIDENT_CODE:
				incidentOverlays.get(ACCIDENT_CODE).addOverlay(overlayItem);
				break;
			case TrafficMap.BLOCKED_CODE:
				incidentOverlays.get(BLOCKED_CODE).addOverlay(overlayItem);
				break;
			default:
				incidentOverlays.get(TRAFFIC_JAM_CODE).addOverlay(overlayItem);
				break;
			}
		}

		// add overlay (check setting to see which overlay to show)
		boolean[] mShowType = mApplication.getShowType();
		for (int index = 0; index < mShowType.length; index++) {
			if (mShowType[index]) {
				overlays.add(incidentOverlays.get(index));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_traffic_map, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub

		boolean[] showType = mApplication.getShowType();
		int index = item.getOrder() - 1;
		switch (item.getItemId()) {
		case R.id.menu_add:
			Bundle oBundle = new Bundle();
			oBundle.putInt(LAT, currentPoint.getLatitudeE6());
			oBundle.putInt(LONG, currentPoint.getLongitudeE6());
			Intent oIntent = new Intent(TrafficMap.this, ReportMap.class);
			oIntent.putExtras(oBundle);
			startActivityForResult(oIntent, REQUEST_CODE);
			break;
		case R.id.map_refresh:
			new GetReportTask().execute();

			break;
		case R.id.min10:
			mApplication.setTimeFilter(10);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast) 
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.min30:
			mApplication.setTimeFilter(30);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast) 
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.hour1:
			mApplication.setTimeFilter(60);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast) 
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.hour3:
			mApplication.setTimeFilter(180);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast)
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.hour6:
			mApplication.setTimeFilter(360);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast) 
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.hour12:
			mApplication.setTimeFilter(720);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast)
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.all:
			mApplication.setTimeFilter(-1);

			item.setChecked(true);
			Toast.makeText(
					TrafficMap.this,
					getString(R.string.menu_filter_success_toast)
							+ item.getTitle(), Toast.LENGTH_SHORT).show();
			new GetReportTask().execute();
			break;
		case R.id.type0:
			showType[index] = !(mApplication.getShowType())[index];
			item.setChecked(!item.isChecked());
			new GetReportTask().execute();
			break;
		case R.id.type1:

			showType[index] = !(mApplication.getShowType())[index];
			item.setChecked(!item.isChecked());
			new GetReportTask().execute();
			break;
		case R.id.type2:
			showType[index] = !(mApplication.getShowType())[index];
			item.setChecked(!item.isChecked());
			new GetReportTask().execute();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		setCurrentLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		locationManager
				.requestLocationUpdates(getBestProvider(), 1000, 1, this);
		setProgressBarIndeterminateVisibility(false);
		GetReportTask mGetReportTask = new GetReportTask();
		mGetReportTask.execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		locationManager.removeUpdates(this);
	}

	public GeoPoint getCurrentPoint() {
		return currentPoint;
	}

	private class GetReportTask extends
			AsyncTask<Void, String, ArrayList<Report>> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected ArrayList<Report> doInBackground(Void... values) {
			// TODO Auto-generated method stub
			ReportGetter mReportGetter = new ReportGetter(new HoaHelper(
					TrafficNetworkClient.ADDRESS));
			try {
				Log.i(TAG, "getReport(" + mApplication.getTimeFilter()
						+ ") start");
				// TEST
				return mReportGetter.getReports(mApplication.getTimeFilter());
				// END TEST
				// return
				// mReportGetter.getReports(mApplication.getTimeFilter());

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
			Toast.makeText(TrafficMap.this, values[0], Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(ArrayList<Report> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null) {
				return;
			}
			setProgressBarIndeterminateVisibility(false);
			drawIncidentOverlay(result);

		}

	}

	private class GetImageTask extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Toast.makeText(TrafficMap.this, "Loading Image", Toast.LENGTH_SHORT)
					.show();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(String... url) {
			// TODO Auto-generated method stub
			URL myFileUrl = null;
			try {
				// FIX VOTE
				VoteSetGetter mVoteSetGetter = new VoteSetGetter(new HoaHelper(
						TrafficNetworkClient.ADDRESS));

				try {
					curReport.setVoteUp(mVoteSetGetter.getVote(curReport
							.getPlaceID())[0]);
					curReport.setVoteDown(mVoteSetGetter.getVote(curReport
							.getPlaceID())[1]);
					Log.i(TAG, "curRep vote :" + curReport.getVoteUp() + ""
							+ curReport.getVoteDown());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TEST
				// myFileUrl = new URL(
				// "http://www.belovedcars.com/wp-content/uploads/2012/03/2012-traffic-jam.jpg");
				// END TEST
				myFileUrl = new URL(url[0]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				HttpURLConnection conn = (HttpURLConnection) myFileUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 20;
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
			tvUpVote.setText(curReport.getVoteUp() + "");
			tvDownVote.setText(curReport.getVoteDown() + "");
			setProgressBarIndeterminateVisibility(false);
			if (result) {
				imvSmall.setImageBitmap(tmpBitmapImage);
				imvBig.setImageBitmap(tmpBitmapImage);
				setProgressBarIndeterminateVisibility(false);
			} else {
				Toast.makeText(TrafficMap.this, "Cannot download Image",
						Toast.LENGTH_SHORT).show();
				setProgressBarIndeterminateVisibility(false);
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			tmpBitmapImage.recycle();
		}
	
	}

	// public void showComment(View v) {
	// // Get comment THieu ID
	// ShowDetailsWithComment comment = new ShowDetailsWithComment();
	// comment.execute();
	// }

	public void hideDetail() {
		llDetail.setVisibility(View.GONE);
	}

	public void hideImage(View v) {
		imvBig.setVisibility(View.GONE);
	}

	public void showImage(View v) {
		imvBig.setVisibility(View.VISIBLE);
	}

	public void toDetailAct(View v) {
		Intent oIntent = new Intent(TrafficMap.this,
				IncidentDetailActivity.class);
		Bundle mBundle = new Bundle();
		// User
		mBundle.putString(IncidentDetailActivity.USERNAME, tvUsername.getText()
				.toString());
		// Type
		mBundle.putString(IncidentDetailActivity.TYPE, tvType.getText()
				.toString());
		// Time
		mBundle.putString(IncidentDetailActivity.TIME, tvTime.getText()
				.toString());
		// Upvote
		mBundle.putString(IncidentDetailActivity.UPVOTE, tvUpVote.getText()
				.toString());
		// down vote
		mBundle.putString(IncidentDetailActivity.DOWNVOTE, tvDownVote.getText()
				.toString());
		// ImageUrl
		mBundle.putString(IncidentDetailActivity.IMAGE, curReport.getImage());
		// Description
		if (!tvDes.getText().equals("")) {
			mBundle.putString(IncidentDetailActivity.DESCRIPTION, tvDes
					.getText().toString());
		}
		// ID
		mBundle.putInt(IncidentDetailActivity.ID, curReport.getPlaceID());

		oIntent.putExtras(mBundle);
		startActivity(oIntent);

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

					mVoteSetGetter.vote(mApplication.getUser(),
							curReport.getPlaceID(), true);
					Log.i(TAG, "vote Up");

				} else if (params[0].equals(IncidentDetailActivity.DOWNVOTE)) {
					mVoteSetGetter.vote(mApplication.getUser(),
							curReport.getPlaceID(), false);
					Log.i(TAG, "vote Down");
				}

				return mVoteSetGetter.getVote(curReport.getPlaceID());
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

				Toast.makeText(TrafficMap.this,
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
}
