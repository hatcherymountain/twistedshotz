package com.asahi.shots;

import com.asahi.Twisted;
import com.hatchery.Hatchery;

import java.sql.*;
import java.util.ArrayList;

public class Shots {

	private Twisted t = null;
	private Hatchery h = null;

	public Shots(Hatchery h, Twisted t) {
		this.h = h;
		this.t = t;
	}

	public void load() {

	}

	private String clean(String v) {
		return com.hatchery.utils.Strings.cleanString(v);
	}

	/**
	 * Update shot meta.
	 * 
	 * @param HttpServletRequest
	 */
	public void update(jakarta.servlet.http.HttpServletRequest r) {
		if (h.isAdmin() || h.isSuper()) {
			Connection c = h.c();
			Statement s = null;
			try {

				s = c.createStatement();
				String shotid = r.getParameter("shotid");
				int id = h.d(shotid);
				if (id > 0) {

					String title = r.getParameter("title");
					title = clean(title);
					if (title.length() > 2) {
						s.addBatch("update ts_shotz set title='" + title + "' where shotid=" + id + "");
					}

					String d = r.getParameter("description");
					d = clean(d);
					s.addBatch("update ts_shotz set description='" + d + "' where shotid=" + id + "");

					String skey = r.getParameter("skey");
					skey = clean(skey);
					if (skey.length() > 0) {
						skey = skey.replace(" ", "-").toLowerCase();
						s.addBatch("update ts_shotz set skey='" + skey + "' where shotid=" + id + "");
					}

					String t = r.getParameter("tags");
					t = clean(t);
					s.addBatch("update ts_shotz set tags='" + t + "' where shotid=" + id + "");

					String tid = r.getParameter("thirstieid");
					tid = clean(tid);
					s.addBatch("update ts_shotz set thirstieid='" + tid + "' where shotid=" + id + "");

					String f = r.getParameter("flavors");
					f = clean(f);
					s.addBatch("update ts_shotz set flavors='" + f + "' where shotid=" + id + "");

					String color = r.getParameter("color");
					color = clean(color);
					s.addBatch("update ts_shotz set color='" + color + "' where shotid=" + id + "");

					String typeof = r.getParameter("typeof");
					int iType = com.hatchery.utils.Strings.getIntFromString(typeof);
					s.addBatch("update ts_shotz set typeof=" + iType + " where shotid=" + id + "");

					String status = r.getParameter("status");
					int iS = com.hatchery.utils.Strings.getIntFromString(status);
					s.addBatch("update ts_shotz set status=" + iS + " where shotid=" + id + "");

					String fav = r.getParameter("favorite");
					int iF = com.hatchery.utils.Strings.getIntFromString(fav);
					s.addBatch("update ts_shotz set favorite=" + iF + " where shotid=" + id + "");

					s.executeBatch();

				} else {
					throw new Exception("No valid shot identifier provided");
				}

			} catch (Exception e) {
				h.log().log("Errors updating shot. Err:" + e.toString(), "Shots", "update");
			} finally {
				h.cleanup(c, s);
			}
		}
	}

	public void updateImage(String shotid, String url, String typeof) {
		if (h.isAdmin() || h.isSuper()) {

			Connection c = h.c();
			Statement s = null;

			try {

				s = c.createStatement();

				int id = h.d(shotid);

				if (id > 0) {

					url = clean(url);
					int iType = com.hatchery.utils.Strings.getIntFromString(typeof);

					String field = "";

					switch (iType) {
					case 0:
						field = "shotImage";
						break;
					case 1:
						field = "fourImage";
						break;
					case 2:
						field = "packImage";
						break;
					}

					if (url.length() > 0) {

						String sql = "update ts_shotz set " + field + "='" + url + "' where shotid=" + id + "";
						s.execute(sql);

					}

				} else {
					throw new Exception("No valid shot identifier provided");
				}

			} catch (Exception e) {
				h.log().log("Errors updating shot. Err:" + e.toString(), "Shots", "updateImage");
			} finally {
				h.cleanup(c, s);
			}
		}
	}

	/**
	 * Adds new shot
	 * 
	 * @param name
	 * @return String shot identifier encoded
	 */
	public String add(String name) {
		String shotid = null;
		if (h.isAdmin() || h.isSuper()) {
			Connection c = h.c();
			PreparedStatement ps = null;
			ResultSet rs = null;

			name = com.hatchery.Hatchery.clean(name);
			if (name.length() > 0) {

				try {

					String k = java.util.UUID.randomUUID().toString();
					String sql = "insert into ts_shotz values(null,'" + k + "','','" + name
							+ "','','','','',0,'','',0,0,'" + com.asahi.Constants.DEFAULT_BG_COLOR + "')";

					ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					ps.executeUpdate();

					rs = ps.getGeneratedKeys();

					while (rs.next()) {
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

	public void addTag(String name) {

		if (h.isAdmin() || h.isSuper()) {
			Connection c = h.c();
			PreparedStatement ps = null;
			ResultSet rs = null;

			name = com.hatchery.Hatchery.clean(name);
			if (name.length() > 0) {

				try {

					String sql = "insert into ts_tags values(null,?)";

					ps = c.prepareStatement(sql);
					ps.setString(1, name);
					ps.execute();

				} catch (Exception e) {
					h.log().log("Errors adding tag. Err:" + e.toString(), "Importer", "addTag");
				} finally {
					h.cleanup(c, ps, rs);
				}
			}
		}
	}

	/** Get the shots... **/

	public ArrayList<Shot> shots(boolean onlyActive, boolean onlyFavorites, boolean onlyPackItems, boolean allowPacks,
			String tags) {
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

			if (!allowPacks) {
				sql = sql + " and typeof=0";
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
				String tid = rs.getString(3);
				tid = clean(tid);
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
				String color = rs.getString(14);
				if (color == null || color.length() == 0) {
					color = com.asahi.Constants.DEFAULT_BG_COLOR;
				}

				Shot shot = new Shot(sid, skey, tid, title, desc, tagitems, si, fi, typeof, pack, flavors, active, fav,
						color);

				if (onlyPackItems && pack.length() > 0) {
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
	 * Get a shot using a unique key
	 * 
	 * @param k
	 * @return
	 */
	public Shot shotFromKey(String k) {
		Shot shot = null;

		Connection c = h.c();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			k = com.hatchery.Hatchery.clean(k);
			k = k.trim().toLowerCase();

			String sql = "select shotid,skey,thirstieid,title,description, tags,shotimage,fourImage,typeof, packImage,"
					+ "flavors,status,favorite,color from ts_shotz where skey=?";

			ps = c.prepareStatement(sql);
			ps.setString(1, k);
			rs = ps.executeQuery();

			while (rs.next()) {

				int sid = rs.getInt(1);
				String skey = rs.getString(2);
				String tid = rs.getString(3);
				tid = clean(tid);
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
				String color = rs.getString(14);
				if (color == null || color.length() == 0) {
					color = com.asahi.Constants.DEFAULT_BG_COLOR;
				}

				shot = new Shot(sid, skey, tid, title, desc, tagitems, si, fi, typeof, pack, flavors, active, fav,
						color);
			}

		} catch (Exception e) {
			h.log().log("Errors getting unique shot using key. Err:" + e.toString(), "Shots", "shotFromKey");
		} finally {
			h.cleanup(c, ps, rs);
		}

		return shot;
	}

	/**
	 * Get a shot
	 * 
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
			String sql = "select shotid,skey,thirstieid,title,description, tags,shotimage,fourImage,typeof, packImage,flavors,status,favorite,color from ts_shotz where shotid="
					+ id + "";

			rs = s.executeQuery(sql);

			while (rs.next()) {

				int sid = rs.getInt(1);
				String skey = rs.getString(2);
				String tid = rs.getString(3);
				tid = clean(tid);
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
				String color = rs.getString(14);
				if (color == null || color.length() == 0) {
					color = com.asahi.Constants.DEFAULT_BG_COLOR;
				}

				shot = new Shot(sid, skey, tid, title, desc, tagitems, si, fi, typeof, pack, flavors, active, fav,
						color);
			}

		} catch (Exception e) {
			h.log().log("Errors getting unique shot. Err:" + e.toString(), "Shots", "shot");
		} finally {
			h.cleanup(c, s, rs);
		}

		return shot;
	}

	public ArrayList<Shot> related(String parentshotid, boolean secured) {
		ArrayList<Shot> lst = new ArrayList<Shot>();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();
			int pid = h.d(parentshotid);

			String sql = "select kidid from  ts_related where pshotid=" + pid + "";

			rs = s.executeQuery(sql);
			while (rs.next()) {
				int kidid = rs.getInt(1);
				String k = h.e(kidid);
				Shot shot = shot(k);
				if (shot != null) {

					if (!secured) {
						if (shot.active()) {
							lst.add(shot);
						}
					} else {
						lst.add(shot);
					}

				}
			}

		} catch (Exception e) {
			h.log().log("Are two shots related?. Err:" + e.toString(), "Shots", "related");
		} finally {
			h.cleanup(c, s);
		}

		return lst;
	}

	public void toggleMapping(String tagid, String shotid) {
		if (mapped(tagid, shotid)) {

			removeMap(tagid, shotid);
		} else {
			map(tagid, shotid);
		}
	}

	public void removeMap(String tagid, String shotid) {

		Connection c = h.c();
		Statement s = null;

		try {

			s = c.createStatement();
			int tid = h.d(tagid);
			int sid = h.d(shotid);

			String sql = "delete from ts_tag_mapping where tagid=" + tid + " and shotid=" + sid + "";

			s.execute(sql);

		} catch (Exception e) {
			h.log().log("Remove Map. Err:" + e.toString(), "Shots", "removeMap");
		} finally {
			h.cleanup(c, s);
		}

	}

	public void removeTag(String tagid) {

		Connection c = h.c();
		Statement s = null;

		try {

			s = c.createStatement();
			int tid = h.d(tagid);

			String sql = "delete from ts_tag_mapping where tagid=" + tid + "";

			s.execute(sql);

			String sql2 = "delete from ts_tags where tagid=" + tid + "";

			s.execute(sql2);

		} catch (Exception e) {
			h.log().log("Remove Tag Error. Err:" + e.toString(), "Shots", "Remove a TAG");
		} finally {
			h.cleanup(c, s);
		}

	}

	public void map(String tagid, String shotid) {

		Connection c = h.c();
		Statement s = null;

		try {

			s = c.createStatement();
			int tid = h.d(tagid);
			int sid = h.d(shotid);

			String sql = "insert into ts_tag_mapping values(" + tid + "," + sid + ")";
			s.execute(sql);

		} catch (Exception e) {
			h.log().log("Map a shot to a tag. Err:" + e.toString(), "Shots", "Map");
		} finally {
			h.cleanup(c, s);
		}

	}

	public String shotTagList(int shotid) {

		StringBuffer sb = new StringBuffer();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();

			String sql = "select distinct tagid from ts_tag_mapping where shotid=" + shotid + "";
			rs = s.executeQuery(sql);
			while (rs.next()) {
				int tid = rs.getInt(1);
				sb.append("t" + tid + " ");
			}

		} catch (Exception e) {
			h.log().log("Shots mapped to a given tag Isotope. Err:" + e.toString(), "Shots", "shotTagList");
		} finally {
			h.cleanup(c, s);
		}

		return sb.toString();
	}

	public ArrayList<Shot> mappedShots(String tagid, boolean activeOnly) {

		ArrayList<Shot> shots = new ArrayList<Shot>();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();
			int tid = h.d(tagid);

			String sql = "select distinct shotid from ts_tag_mapping where tagid=" + tid + "";
			rs = s.executeQuery(sql);
			while (rs.next()) {
				String shotid = h.e(rs.getInt(1));
				Shot shot = shot(shotid);
				if (shot != null) {
					if (activeOnly) {
						if (shot.active()) {
							shots.add(shot);
						}
					} else {
						shots.add(shot);
					}
				}
			}

		} catch (Exception e) {
			h.log().log("Shots mapped to a given tag. Err:" + e.toString(), "Shots", "mappedShots");
		} finally {
			h.cleanup(c, s);
		}

		return shots;
	}

	public boolean mapped(String tagid, String shotid) {

		boolean mapped = false;

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();
			int tid = h.d(tagid);
			int sid = h.d(shotid);

			String sql = "select count(*) from ts_tag_mapping where tagid=" + tid + " and shotid=" + sid + "";
			rs = s.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(1) > 0) {
					mapped = true;
				}
			}

		} catch (Exception e) {
			h.log().log("Is a shot mapped to a tag??. Err:" + e.toString(), "Shots", "mapped");
		} finally {
			h.cleanup(c, s);
		}

		return mapped;
	}

	public void unrelate(String parentid, String kidid) {
		if (h.isSuper()) {

			Connection c = h.c();
			Statement s = null;

			try {

				s = c.createStatement();
				int pid = h.d(parentid);
				int kid = h.d(kidid);

				String sql = "delete from ts_related where pshotid=" + pid + " and kidid=" + kid + "";
				s.execute(sql);

			} catch (Exception e) {
				h.log().log("Errors unrelating shots. Err:" + e.toString(), "Shots", "unrelate");
			} finally {
				h.cleanup(c, s);
			}

		}
	}

	public boolean related(String parentid, String kidid) {

		boolean is = false;

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();
			int pid = h.d(parentid);
			int kid = h.d(kidid);

			String sql = "select count(*) from  ts_related where pshotid=" + pid + " and kidid=" + kid + "";
			rs = s.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(1) > 0) {
					is = true;
				}
			}

		} catch (Exception e) {
			h.log().log("Are two shots related?. Err:" + e.toString(), "Shots", "related");
		} finally {
			h.cleanup(c, s);
		}

		return is;

	}

	public void relate(String parentid, String kidid) {

		if (h.isSuper()) {

			if (!related(parentid, kidid)) {

				Connection c = h.c();
				Statement s = null;

				try {

					s = c.createStatement();
					int pid = h.d(parentid);
					int kid = h.d(kidid);

					String sql = "insert into ts_related values(" + pid + "," + kid + ")";

					s.execute(sql);

				} catch (Exception e) {
					h.log().log("Errors relating shots. Err:" + e.toString(), "Shots", "relate");
				} finally {
					h.cleanup(c, s);
				}

			}
		}
	}

	/**
	 * 
	 * Get all the tags
	 * 
	 * @return
	 */
	public ArrayList<Tag> tags() {

		ArrayList<Tag> lst = new ArrayList<Tag>();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();

			String sql = "select tagid,tag from ts_tags order by tag asc";

			rs = s.executeQuery(sql);

			while (rs.next()) {
				int id = rs.getInt(1);
				String t = rs.getString(2);
				Tag tag = new Tag(id, t);
				lst.add(tag);
			}

		} catch (Exception e) {
			h.log().log("Errors getting tags. Err:" + e.toString(), "Shots", "tags");
		} finally {
			h.cleanup(c, s, rs);
		}

		return lst;
	}

	public ArrayList<Shot> randomList(int max, String excludeid, boolean excludePacks) {

		ArrayList<Shot> lst = new ArrayList<Shot>();

		Connection c = h.c();
		Statement s = null;
		ResultSet rs = null;

		try {

			s = c.createStatement();

			int eid = h.d(excludeid);

			String sql = "select shotid from ts_shotz where (shotid!=" + eid + ")";

			if (excludePacks) {
				sql = sql + " and typeof=0";
			}

			sql = sql + " LIMIT " + max + "";

			rs = s.executeQuery(sql);
			while (rs.next()) {
				String shotid = h.e(rs.getInt(1));
				Shot shot = shot(shotid);
				if (shot != null) {
					if (shot.active()) {
						lst.add(shot);
					}
				}
			}

		} catch (Exception e) {
			h.log().log("Errors getting random products. Err:" + e.toString(), "Shots", "randomList");
		} finally {
			h.cleanup(c, s, rs);
		}

		java.util.Collections.shuffle(lst);
		return lst;
	}

}
