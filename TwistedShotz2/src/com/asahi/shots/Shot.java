package com.asahi.shots;
public record Shot(int shotid,
		String key, String thirstieid, 
		String title, 
		String description, 
		String tags,
		String shotImage, 
		String fourImage,
		int typeof, 
		String packImage,
		String flavors, 
		boolean active, 
		boolean favorite,String color) {}