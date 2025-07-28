package com.asahi;
import com.hatchery.Hatchery;
import com.asahi.shots.Shots;

public class Twisted {

	private Shots shots = null;
	private Hatchery h = null;
	
	public Twisted(Hatchery h)
	{
		this.h=h;
	}
	
	public Shots shots() {
		if(shots==null) { 
			shots = new Shots(h,this);
		}
		return shots;
	}
	
	
	
}
