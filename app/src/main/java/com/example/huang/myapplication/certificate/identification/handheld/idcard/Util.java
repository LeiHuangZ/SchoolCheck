package com.example.huang.myapplication.certificate.identification.handheld.idcard;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


import com.example.huang.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Util {

	
	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;

	public static void initSoundPool(Context context){
		Util.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(1, sp.load(context, R.raw.msg, 1));
	}

	public static  void play(int sound, int number){
		AudioManager am = (AudioManager)Util.context.getSystemService(Util.context.AUDIO_SERVICE);
	    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

	        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        float volumnRatio = audioCurrentVolume/audioMaxVolume;
	        sp.play(
	        		suondMap.get(sound),
	        		audioCurrentVolume,
	        		audioCurrentVolume,
	                1,
	                number,
	                1);
	    }
	public static String getTime(){
		String model = "yyyy-MM-dd HH:mm:ss";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(model);
		String dateTime = format.format(date);
		return  dateTime;
	}	
	
}
