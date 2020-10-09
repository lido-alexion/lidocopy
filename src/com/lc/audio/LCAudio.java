package com.lc.audio;

import com.lc.common.Settings;

public class LCAudio extends Thread {

	private String filePath = null;

	public LCAudio() {
		this.start();
	}

	public LCAudio(String path) {
		filePath = path;
		this.start();
	}

	public void run() {
		if (!Settings.SOUND_MUTED) {
			if (filePath != null)
				new playLoadedFile().play(filePath);
			else {
				Synthesize.getSyntheticData(3, 900.0, 16000.0F);
				Synthesize.prepareSoundStream();
				new playSound().start();
			}
		}
	}
}
