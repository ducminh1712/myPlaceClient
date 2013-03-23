package org.k2htm.tnc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import edu.k2htm.clientHelper.HoaHelper;
import edu.k2htm.datahelper.Place;

public class ReportMap extends MapActivity {
	private MapController mapController;
	private MapView map = null;
	private GeoPoint currentPoint;
	private MyLocationOverlay me = null;
	private ImageView imvImage;
	private TextView tvLat, tvLong;
	private EditText edtDes;
	private TrafficNetworkClient mApplicaion;
	private Spinner spnType;
	private Uri imageUri;
	private String imageUriStr = "";
	public static final String TAG = "ReportMapActivity";
	public static final int CODE_IMAGE_PICKER = 201;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_report_map);
		// app object
		mApplicaion = (TrafficNetworkClient) getApplication();
		// get view
		tvLat = ((TextView) findViewById(R.id.latitudeText));
		tvLong = ((TextView) findViewById(R.id.longitudeText));
		spnType = (Spinner) findViewById(R.id.spn_type);
		imvImage = (ImageView) findViewById(R.id.imbImage);
		edtDes = (EditText) findViewById(R.id.edtDescription);

		// set location get from bundle from trafficMap
		setCurLocation();
		// set text
		tvLat.setText(String.valueOf((int) (currentPoint.getLatitudeE6())));
		tvLong.setText(String.valueOf((int) (currentPoint.getLongitudeE6())));

		// mapview setting
		map = (MapView) findViewById(R.id.map);

		map.getController().setCenter(currentPoint);
		map.getController().setZoom(15);

		// turn on zoom controller if device not support multitouch
		if (getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
			// do multitouch
			map.setBuiltInZoomControls(false);
		} else {
			// do magnifying glass
			map.setBuiltInZoomControls(true);
		}

		Drawable marker = getResources()
				.getDrawable(R.drawable.indicator_blank);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		map.getOverlays().add(new SitesOverlay(marker));

		me = new MyLocationOverlay(this, map);
		map.getOverlays().add(me);
	}

	public void callImagePicker(View view) {
		Intent pickIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File photo = new File(Environment.getExternalStorageDirectory(),
				"tmp.jpg");
		// "tmp.jpg");
		// File photo=null;
		// try {
		// photo =File.createTempFile("", ".jpg");
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		imageUriStr = photo.getAbsolutePath();
		imageUri = Uri.parse(imageUriStr);
		String pickTitle = "Select or take a new Picture";
		Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				new Intent[] { takePhotoIntent });

		startActivityForResult(chooserIntent, CODE_IMAGE_PICKER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_report_map, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_add_confirm:
			try {
				if (edtDes.getText().toString().equals("")) {
					edtDes.setText("No Description.");
				}
				imageUriStr = getRealPathFromURI(imageUri);
				Log.i(TAG,
						"spinner select : " + spnType.getSelectedItemPosition()
								+ "\nClick confirm "
								+ tvLat.getText().toString() + " "
								+ tvLong.getText().toString() + "\nImage uri: "
								+ imageUriStr);
				// input
				String name = "TestPlaceName";
				String username = mApplicaion.getUser();
				short type = (short) spnType.getSelectedItemPosition();
				int lat = Integer.parseInt(tvLat.getText().toString());
				int lng = Integer.parseInt(tvLong.getText().toString());
				File image;
				if (imageUriStr.equals("")) {
					imageUriStr = getText(R.drawable.default_image).toString();
				}

				image = new File(imageUriStr);

				String comment = edtDes.getText().toString();

				// new Caution object
				Place mPlace = new Place(name, username, type, lat, lng, image,
						comment, new HoaHelper(TrafficNetworkClient.ADDRESS));
				Log.i(TAG, "Create report : " + "\nusername : " + username
						+ "\ntype" + type + "\nCLat:Lng "
						+ tvLat.getText().toString() + ":"
						+ tvLong.getText().toString() + "\nDescription :"
						+ comment + tvLong.getText().toString()
						+ "\nImage uri: " + imageUriStr);

				// SendToServer
				new SendReportTask().execute(mPlace);
				// writeToFile(Integer.parseInt(tvLat.getText().toString()),
				// Integer.parseInt((tvLong.getText().toString())),
				// spnType.getSelectedItemPosition(), edtDes.getText()
				// .toString(), imageUriStr);
			} catch (Exception e) {
				e.printStackTrace();
			}

			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CODE_IMAGE_PICKER:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Uri selectedImage = data.getData();
					Log.i(TAG, "Image Uri :  " + selectedImage);
					if (selectedImage != null) {
						// DO STH WITH THE URI
						imvImage.setImageURI(selectedImage);
						imageUri = selectedImage;
						break;
					}
				} else {
					if (imageUri != null) {
						imvImage.setImageURI(imageUri);
					}
				}
			}
		}
		if (imvImage.getDrawable() == null) {
			imvImage.setBackgroundResource(R.drawable.btn_camera);
		}
	}

	public void centerToCurrentLocation(View view) {
		animateToCurrentLocation();

	}

	public void animateToCurrentLocation() {
		if (currentPoint != null) {
			map.getController().animateTo(currentPoint);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		setProgressBarIndeterminateVisibility(false);
		me.enableCompass();
	}

	@Override
	public void onPause() {
		super.onPause();
		me.disableCompass();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return (false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_S) {
			map.setSatellite(!map.isSatellite());
			return (true);
		} else if (keyCode == KeyEvent.KEYCODE_Z) {
			map.displayZoomControls(true);
			return (true);
		}

		return (super.onKeyDown(keyCode, event));
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private OverlayItem inDrag = null;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;

			dragImage = (ImageView) findViewById(R.id.drag);
			xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
			yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

			items.add(new OverlayItem(currentPoint, "Choose Position",
					"Drag to incident's position"));

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		public int size() {
			return (items.size());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean result = false;

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {
					Point p = new Point(0, 0);

					map.getProjection().toPixels(item.getPoint(), p);

					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;

						break;
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = map.getProjection().fromPixels(
						x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
						inDrag.getSnippet());

				items.add(toDrop);
				populate();

				inDrag = null;
				result = true;
				// settext
				Log.i(TAG,
						"touch up " + pt.getLatitudeE6() + " "
								+ pt.getLongitudeE6());
				tvLat.setText("" + pt.getLatitudeE6());
				tvLong.setText("" + pt.getLongitudeE6());

			}

			return (result || super.onTouchEvent(event, mapView));
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage
					.getLayoutParams();

			lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
					- yDragImageOffset - yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}

	private void setCurLocation() {
		Intent iIntent = getIntent();
		Bundle iBundle = iIntent.getExtras();
		currentPoint = new GeoPoint(iBundle.getInt(TrafficMap.LAT) + 10,
				iBundle.getInt(TrafficMap.LONG) + 10);
		Log.i("report", "lat: " + iBundle.getInt(TrafficMap.LAT) + "\nlong:"
				+ iBundle.getInt(TrafficMap.LONG));
	}

	public class SendReportTask extends AsyncTask<Place, String, Void> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// Bat xoay xoay
			Log.i(TAG, "xoay xoay on");
			Toast.makeText(ReportMap.this, "Please wait...", Toast.LENGTH_SHORT)
					.show();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(Place... mCaution) {
			// TODO Auto-generated method stub
			try {
				mCaution[0].report();
				Log.i(TAG, "Sending caution ok");
				publishProgress("Send place ok");
				if (mCaution[0].getImage() != null)
					mCaution[0].getImage().delete();
			} catch (Exception e) {
				publishProgress(getText(R.string.network_error) + "");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (values[0].equals("Send caution ok")) {
				Log.i(TAG, "report success");
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// tat xoay xoay
			Toast.makeText(ReportMap.this, "Report successfully",
					Toast.LENGTH_SHORT).show();
			setProgressBarIndeterminateVisibility(false);
			Intent oIntet = new Intent(ReportMap.this, TrafficMap.class);
			startActivity(oIntet);
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			CursorLoader loader = new CursorLoader(ReportMap.this, contentUri,
					proj, null, null, null);
			Cursor cursor = loader.loadInBackground();
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (NullPointerException e) {
			return imageUriStr;
		}
	}

}
