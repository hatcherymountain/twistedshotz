package com.asahi.shots;
import com.asahi.Twisted;
import com.hatchery.Hatchery;

import java.sql.*;
import java.util.ArrayList;

public class Shots {

	private Twisted  t = null;
	private Hatchery h = null;

	public Shots(Hatchery h, Twisted t) {
		this.h = h;
		this.t=t;
	}

	public void load()
	{
		
	}
	
	private String clean(String v) { 
		return com.hatchery.utils.Strings.cleanString(v);
	}
	/**
	 * Update shot meta.
	 * @param HttpServletRequest
	 */
	public void update(jakarta.servlet.http.HttpServletRequest r)
	{
		if(h.isAdmin()||h.isSuper())
		{
			Connection c = h.c();
			Statement  s = null;
			try { 
				
				s = c.createStatement();
				String shotid = r.getParameter("shotid"); int id = h.d(shotid);
				if(id>0) { 
					
					
					String title  = r.getParameter("title"); title = clean(title);
					if(title.length()>2) { 
						s.addBatch("update ts_shotz set title='" + title + "' where shotid="+id+"");
					}
					
					String d = r.getParameter("description"); d = clean(d);
					s.addBatch("update ts_shotz set description='" + d + "' where shotid="+id+"");
					
					String t = r.getParameter("tags"); t = clean(t);
					s.addBatch("update ts_shotz set tags='" + t + "' where shotid="+id+"");
					
					String tid = r.getParameter("thirstieid"); tid = clean(tid);
					s.addBatch("update ts_shotz set thirstieid='" + tid + "' where shotid="+id+"");
					
					String f = r.getParameter("flavors"); f = clean(f);
					s.addBatch("update ts_shotz set flavors='" + f + "' where shotid="+id+"");
					
					String color = r.getParameter("color"); color = clean(color);
					s.addBatch("update ts_shotz set color='" + color + "' where shotid="+id+"");
					
					
					String typeof = r.getParameter("typeof"); int iType = com.hatchery.utils.Strings.getIntFromString(typeof);
					s.addBatch("update ts_shotz set typeof="+iType+" where shotid="+id+"");
					
					String status = r.getParameter("status"); int iS = com.hatchery.utils.Strings.getIntFromString(status);
					s.addBatch("update ts_shotz set status="+iS+" where shotid="+id+"");
					
					String fav = r.getParameter("favorite"); int iF = com.hatchery.utils.Strings.getIntFromString(fav);
					s.addBatch("update ts_shotz set favorite="+iF+" where shotid="+id+"");
					
					s.executeBatch();
					
				} else { 
					throw new Exception("No valid shot identifier provided");
				}
				
			} catch(Exception e)
			{
				h.log().log("Errors updating shot. Err:" + e.toString(),"Shots","update");
			} finally { 
				h.cleanup(c,s);
			}
		}
	}
	
	public void updateImage(String shotid, String url, String typeof)
	{
	 if(h.isAdmin() || h.isSuper())
	 {
		 
			Connection c = h.c();
			Statement  s = null;
			
			
	try { 
				
				s = c.createStatement();
				
				int id = h.d(shotid);
				
				if(id>0) { 
				
					url = clean(url);
					int iType = com.hatchery.utils.Strings.getIntFromString(typeof);
					
					String field = "";
					
					switch(iType) { 
						case 0:field="shotImage";break;
						case 1:field="fourImage";break;
						case 2:field="packImage";break;
					}
					
					if(url.length() > 0)
					{
						
						
						String sql = "update ts_shotz set "+field+"='"+url+"' where shotid="+id+"";
						s.execute(sql);
						
					}
					
					
				} else { 
					throw new Exception("No valid shot identifier provided");
				}
				
			} catch(Exception e)
			{
				h.log().log("Errors updating shot. Err:" + e.toString(),"Shots","updateImage");
			} finally { 
				h.cleanup(c,s);
			}
	 }
	}
	
	
	/**
	 * Adds new shot
	 * @param name
	 * @return String shot identifier encoded
	 */
	public String add(String name)
	{
		String shotid = null;
		if(h.isAdmin()||h.isSuper()) { 
		Connection c = h.c();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		name = com.hatchery.Hatchery.clean(name);
		if(name.length()>0) { 
			
			try {
	
				String k = java.util.UUID.randomUUID().toString();
				String sql = "insert into ts_shotz values(null,'"+k+"','','"+name+"','','','','',0,'','',0,0,'" + com.asahi.Constants.DEFAULT_BG_COLOR + "')";
				
				ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.executeUpdate();
	
				rs = ps.getGeneratedKeys();
				
				while(rs.next())
				{
					int sid = rs.getInt(1);
					shotid = h.e(sid);
				}
	
			} catch (Exception e) {
				h.log().log("Errors adding shot. Err:" + e.toString(), "Importer", "addOrders");
			} finally {
				h.cleanup(c, ps, rs);
			}
			}
		}
		return shotid;
	}
	
	/** Get the shots... **/

	public ArrayList<Shot> shots(boolean onlyActive, boolean onlyFavorites, boolean onlyPackItems, String tags) {
		ArrayList<Shot> lst = new ArrayList<Shot>();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();

			String sql = "select shotid,skey,thirstieid,title,description, tags,shotimage,fourImage,typeof, packImage,flavors,status,favorite,color from ts_shotz	 where shotid>0";

			if (onlyFavorites) {
				sql = sql + " and status=1";
			}
			
			if (onlyFavorites) {
				sql = sql + " and favorite=1";
			}

			tags = com.hatchery.Hatchery.clean(tags);
			if (tags.length() > 0) {
				sql = sql + " and (tags like '%" + tags + "%')";
			}

			sql = sql + " order by title asc";
			
			rs = s.executeQuery(sql);

			while (rs.next()) {

				int sid = rs.getInt(1);
				String skey = rs.getString(2);
				String tid = rs.getString(3); tid = clean(tid);
				String title = rs.getString(4);
				String desc = rs.getString(5);
				String tagitems = rs.getString(6);
				String si = rs.getString(7);
				String fi = rs.getString(8);
				int typeof = rs.getInt(9);
				String pack = rs.getString(10);
				String flavors = rs.getString(11);
				boolean active = rs.getInt(12) == 1 ? true : false;
				boolean fav = rs.getInt(13) == 1 ? true : false;
				String color = rs.getString(14); if(color==null||color.length()==0) { color = com.asahi.Constants.DEFAULT_BG_COLOR; } 

				Shot shot = new Shot(sid, skey, tid, title, desc, tagitems, si, fi,typeof, pack, flavors, active, fav,color);

				if (onlyPackItems && pack.length()>0) {
						lst.add(shot);
				} else {
					lst.add(shot);
				}
			}

		} catch (Exception e) {
			h.log().log("Errors getting shots. Err:" + e.toString(), "Shots", "shots");
		} finally {
			h.cleanup(c, s, rs);
		}

		return lst;
	}

	/**
	 * Get a shot
	 * @param shotid
	 * @return
	 */
	public Shot shot(String shotid) {
		Shot shot = null;
		
		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();

			int id = h.d(shotid);
			String sql = "select shotid,skey,thirstieid,title,description, tags,shotimage,fourImage,typeof, packImage,flavors,status,favorite,color from ts_shotz where shotid=" + id + "";

			
			rs = s.executeQuery(sql);

			while (rs.next()) {

				int sid = rs.getInt(1);
				String skey = rs.getString(2);
				String tid = rs.getString(3); tid=clean(tid);
				String title = rs.getString(4);
				String desc = rs.getString(5);
				String tagitems = rs.getString(6);
				String si = rs.getString(7);
				String fi = rs.getString(8);
				int typeof = rs.getInt(9);
				String pack = rs.getString(10);
				String flavors = rs.getString(11);
				boolean active = rs.getInt(12) == 1 ? true : false;
				boolean fav = rs.getInt(13) == 1 ? true : false;
				String color = rs.getString(14); if(color==null||color.length()==0) { color = com.asahi.Constants.DEFAULT_BG_COLOR; } 

    			shot = new Shot(sid, skey, tid, title, desc, tagitems, si, fi, typeof, pack, flavors, active, fav,color);
			}

		} catch (Exception e) {
			h.log().log("Errors getting unique shot. Err:" + e.toString(), "Shots", "shot");
		} finally {
			h.cleanup(c, s, rs);
		}

		return shot;
	}

}
