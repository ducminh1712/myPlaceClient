package edu.k2htm.datahelper;

import java.util.ArrayList;

public interface CommentHelper extends DataHelper{
	public void send(String username,int placeID,String comment) throws Exception;
	public ArrayList<Comment> getComments(int placeID) throws Exception;
}
