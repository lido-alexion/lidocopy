package com.lc.audio;

import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class playLoadedFile {

	void play(String path) {

		File soundFile = new File(path);

		try {
			Synthesize.audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			Synthesize.audioFormat = Synthesize.audioInputStream.getFormat();

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, Synthesize.audioFormat);
			Synthesize.sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			new PlayThread().start();
		} catch (Exception e) {
		}
	}

	class PlayThread extends Thread {

		public void run() {
			try {
				Synthesize.sourceDataLine.open(Synthesize.audioFormat);
				Synthesize.sourceDataLine.start();
				int cnt;

				while ((cnt = Synthesize.audioInputStream.read(SoundConstants.audioData, 0, SoundConstants.audioData.length)) != -1) {
					if (cnt > 0) {
						Synthesize.sourceDataLine.write(SoundConstants.audioData, 0, cnt);
					}
				}
				Synthesize.sourceDataLine.drain();
				Synthesize.sourceDataLine.close();
			} catch (Exception e) {
			}
		}
	}

}
