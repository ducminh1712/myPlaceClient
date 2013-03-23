package edu.k2htm.clientHelper;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import android.util.Log;
import edu.k2htm.datahelper.Place;
import edu.k2htm.datahelper.PlaceHelper;
import edu.k2htm.datahelper.CheckUserHelper;
import edu.k2htm.datahelper.Comment;
import edu.k2htm.datahelper.CommentGetter;
import edu.k2htm.datahelper.CommentHelper;
import edu.k2htm.datahelper.DataHelper;
import edu.k2htm.datahelper.Report;
import edu.k2htm.datahelper.ReportGetHelper;
import edu.k2htm.datahelper.ReportGetter;
import edu.k2htm.datahelper.User;
import edu.k2htm.datahelper.VoteHelper;
import edu.k2htm.datahelper.VoteSetGetter;

public class HoaHelper implements PlaceHelper, CheckUserHelper,
		CommentHelper, VoteHelper, ReportGetHelper {
	// public String Destination = "http://10.10.131.43:8080";

	// public static final String
	private String destination = "192.168.100.56:8080";
	public static final String ROOT="/TrafficNetWork";
	public static final String CHECK_USER = "/TrafficNetWork/Login";
	public static final String REGISTRATION = "/TrafficNetWork/Register";
	public static final String SEND_COMMET = "/TrafficNetWork/SendComment";
	public static final String GET_COMMENT = "/TrafficNetWork/GetComment";
	public static final String SEND_VOTE = "/TrafficNetWork/SendVote";
	public static final String GET_VOTE = "/TrafficNetWork/GetVote";
	public static final String GET_REPORT = "/TrafficNetWork/GetInfo";
	public static final String SEND_INFO = "/TrafficNetWork/SendInfo";
	public static final String TAG = "HoaHelper";
	public ExecuteRequest executeRequest;
	private Scanner scanner;
	private Scanner scanner4;

	public HoaHelper(String des) {
		// TODO Auto-generated constructor stub
		this.destination=des;
	}

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		executeRequest = new ExecuteRequest();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void report(String name, String username, short type, long time, int lat,
			int lng, File image, String comment) throws Exception {
		// TODO Auto-generated method stub
		Log.i(TAG,"report() start"+type);
		executeRequest.setTag(Place.DB_PLACE_NAME_COL, Place.DB_PLACE_LAT_COL,
				Place.DB_PLACE_LNG_COL, Place.DB_PLACE_DESCRIPTION_COL,
				Place.DB_PLACE_TIME_COL, Place.DB_PLACE_USERNAME_COL,
				Place.DB_PLACE_TYPE_COL, Place.DB_PLACE_IMAGE_COL);

		Log.i(TAG,"execRequest setTag ok");
		Scanner scanner = new Scanner(executeRequest.executePost(name, lat, lng,
				comment, time, username, image, type, destination + SEND_INFO));
		
		if (scanner.next().equals(DataHelper.STATUS_FAIL))
			throw new Exception();
	}

	@Override
	public boolean checkUser(String username, String password) throws Exception {
		// TODO Auto-generated method stub
		Log.i(TAG, "checkUser:" + username + ": " + password);
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(User.DB_USER_USERNAME_COL, username));
		qparams.add(new BasicNameValuePair(User.DB_USER_PASSWORD_COL, password));
		URI uri = URIUtils.createURI("http", destination, -1, CHECK_USER,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		Log.d("uri", uri.toString());
		HttpGet httpget = new HttpGet(uri);
		Scanner scanner = new Scanner(executeRequest.execute(httpget));
		String result = scanner.next();
		System.out.println(result);
		Log.d(TAG,result+":result");
		if (result.equalsIgnoreCase(DataHelper.STATUS_OK))
			return true;
		return false;
	}

	@Override
	public boolean register(String username, String password) throws Exception {
		// TODO Auto-generated method stub
		this.init();

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(User.DB_USER_USERNAME_COL, username));
		qparams.add(new BasicNameValuePair(User.DB_USER_PASSWORD_COL, password));
		URI uri = URIUtils.createURI("http", destination, -1, REGISTRATION,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		scanner = new Scanner(executeRequest.execute(httpget));
		String result = scanner.next();
		Log.d(TAG,"Register result:"+result);
		if (result.equalsIgnoreCase(DataHelper.STATUS_OK))
			return true;
		{
			throw new DuplicateUserException();
			
		}
	}

	@Override
	public void send(String username, int placeID, String comment)
			throws Exception {
		// TODO Auto-generated method stub
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Comment.DB_COMMENT_COMMENTER_COL,
				username));
		qparams.add(new BasicNameValuePair(Comment.DB_COMMENT_PLACE_COL,
				placeID + ""));
		qparams.add(new BasicNameValuePair(Comment.DB_COMMENT_COMMENT_COL,
				comment));
		URI uri = URIUtils.createURI("http", destination, -1, SEND_COMMET,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		scanner = new Scanner(executeRequest.execute(httpget));
		if (scanner.next().equals(DataHelper.STATUS_FAIL))
			throw new Exception("Fail");
	}

	@Override
	public ArrayList<Comment> getComments(int placeID) throws Exception {
		// TODO Auto-generated method stub

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Comment.DB_COMMENT_PLACE_COL,
				placeID + ""));
		URI uri = URIUtils.createURI("http", destination, -1, GET_COMMENT,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		String result = executeRequest.execute(httpget);

		Log.i(TAG, result);
		return CommentGetter.parseXmlDocument(result);
	}

	@Override
	public int[] getVote(int placeID) throws Exception {
		// TODO Auto-generated method stub
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(VoteSetGetter.DB_VOTE_PLACE,
				placeID + ""));
		URI uri = URIUtils.createURI("http", destination, -1, GET_VOTE,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		scanner = new Scanner(executeRequest.execute(httpget));
		int temp[] = new int[2];
		temp[0] = scanner.nextInt();
		temp[1] = scanner.nextInt();
		return temp;
	}

	@Override
	public void vote(int placeID, String username, boolean bonus)
			throws Exception {
		// TODO Auto-generated method stub
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(VoteSetGetter.DB_VOTE_PLACE,
				placeID + ""));
		qparams.add(new BasicNameValuePair(VoteSetGetter.DB_VOTE_VOTER_COL,
				username + ""));
		qparams.add(new BasicNameValuePair(VoteSetGetter.DB_VOTE_TYPE_COL,
				bonus + ""));
		URI uri = URIUtils.createURI("http", destination, -1, SEND_VOTE,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		scanner4 = new Scanner(executeRequest.execute(httpget));
		if (scanner4.next().equals(DataHelper.STATUS_FAIL))
			throw new Exception("Already Vote!!");
	}

	@Override
	public ArrayList<Report> getReport(int periodMin) throws Exception {
		// dAS
		Log.d(TAG,"getReport:"+periodMin);
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair(Report.PERIOD, periodMin + ""));
		URI uri = URIUtils.createURI("http", destination, -1, GET_REPORT,
				URLEncodedUtils.format(qparams, "UTF-8"), null);
		HttpGet httpget = new HttpGet(uri);
		ArrayList<Report> res = ReportGetter.parseReportXml(executeRequest.execute(httpget));
		if(res!=null){
			for(int i=0;i<res.size();i++){
				res.get(i).setImage("http://"+destination+ROOT+"/"+res.get(i).getImage());
				Log.d(TAG,"Img:"+i+":"+res.get(i).getImage());
			}
		}
		return res;
	}

	public static void main(String args[]) throws Exception {
		// new HoaHelper().register("khiemns", "abc123");
		// System.out.println(new User("khiemns", "abc123", new
		// HoaHelper()).checkUser());
		// Caution caution = new Caution("khiemns",(short) 1, 825, 543, new File
		// ("C:/Users/anhhoa/Desktop/wondering.gif"), "flkashfhlas", new
		// HoaHelper());
		// caution.report();
		// long temp = System.nanoTime();
		// for(int i = 0; i <= 2000 ; i++){
		// Comment comment = new Comment("khiemns", 1,
		// "thang khiesdhsdgh mgsdlfighsdgsd cute" + i, new
		// HoaHelper());
		// try {
		// comment.sendComment();
		// } catch (Exception e) {
		// System.out.println(e.getMessage());
		// }
		// }
		// System.out.println(System.nanoTime() - temp);
		// VoteSetGetter getter = new VoteSetGetter(new HoaHelper());
		// int a[] = getter.getVote(1);
		// System.out.println(a[0] + " " + a[1]);
		CommentGetter commentGetter = new CommentGetter(1, new HoaHelper(""));
		commentGetter.getComments(1);
	}
}
