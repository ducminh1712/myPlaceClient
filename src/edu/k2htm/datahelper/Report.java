package edu.k2htm.datahelper;



public class Report {
	public static final String TAG = "Report";
	public static final String PERIOD = "Period";
	
	private String name;
	private String username;
	private long time;
	private int lat;
	private int lng;
	private String image;
	private String description;
	private PlaceHelper placeHelper;
	private short type;
	private int placeID;
	
	//
	private int voteUp;
	private int voteDown;
	
	//
	public Report(int placeID,String name, String username, long time, int lat, int lng,
			String des, short type, String image,int up,int down) {
		setUsername(username);
		setTime(time);
		setLat(lat);
		setLng(lng);
		setDescription(des);
		setType(type);
		setImage(image);
		setPlaceID(placeID);
		this.setVoteUp(up);
		this.setVoteDown(down);
	}

	public Report() {
		// TODO Auto-generated constructor stub
	}

	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getLat() {
		return lat;
	}

	public void setLat(int lat) {
		this.lat = lat;
	}

	public int getLng() {
		return lng;
	}

	public void setLng(int lng) {
		this.lng = lng;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public PlaceHelper getPlaceHelper() {
		return placeHelper;
	}

	public void setPlaceHelper(PlaceHelper placeHelper) {
		this.placeHelper = placeHelper;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.username + "\n");
		buffer.append(this.image + "\n");
		buffer.append(this.getDescription() + "\n");
		buffer.append(this.lat + " " + this.lng + "\n");
		buffer.append(this.time + "\n");
		buffer.append(this.type + "\n");
		return buffer.toString();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public int getPlaceID() {
		return placeID;
	}
	public void setPlaceID(int placeID) {
		this.placeID = placeID;
	}

	public int getVoteUp() {
		return voteUp;
	}

	public void setVoteUp(int voteUp) {
		this.voteUp = voteUp;
	}

	public int getVoteDown() {
		return voteDown;
	}

	public void setVoteDown(int voteDown) {
		this.voteDown = voteDown;
	}
}
