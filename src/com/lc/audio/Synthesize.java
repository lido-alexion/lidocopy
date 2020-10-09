package com.lc.audio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Synthesize {

	/**
	 * The code in classes below includes the declaration of three instance
	 * variables used to create a SourceDataLine object that feeds data to the
	 * speakers on playback.
	 */
	public static AudioFormat audioFormat;
	public static AudioInputStream audioInputStream;
	public static SourceDataLine sourceDataLine;

	public static ByteBuffer byteBuffer;
	public static ShortBuffer shortBuffer;
	public static int byteLength;
	public static double frq = 990.0;
	public static float samplingRate = 16000.0F;

	public static void getSyntheticData(int soundCode, double freq, float sampleRate) {

		byte[] synDataBuffer = SoundConstants.audioData;
		frq = freq;
		samplingRate = sampleRate;

		/**
		 * The code below begins by wrapping the incoming audioData array in a
		 * ByteBuffer object. Then a ShortBuffer object is created as a short
		 * view of the ByteBuffer object.
		 * 
		 * This makes it possible to store short data directly into the
		 * audioData array by invoking the put method on the ShortData view of
		 * the array.
		 * 
		 * It also gets and saves the required length of the synthetic sound
		 * data in bytes.
		 */
		byteBuffer = ByteBuffer.wrap(synDataBuffer);
		shortBuffer = byteBuffer.asShortBuffer();

		byteLength = synDataBuffer.length;

		if (soundCode == 0)
			tones();
		else if (soundCode == 1)
			stereoPanning();
		else if (soundCode == 2)
			stereoPingpong();
		else if (soundCode == 3)
			fmSweep();
		else if (soundCode == 4)
			decayPulse();
		else if (soundCode == 5)
			echoPulse();
		else if (soundCode == 6)
			waWaPulse();

	}// end getSyntheticData method

	static void tones() {

		/**
		 * Set different parameters
		 */
		setValues(1, 16000.0F, 950.0);

		double freq = SoundConstants.freq;

		/**
		 * Each iteration of the loop: - Generates a data sample as type double.
		 * - Casts that data to type short. - Invokes the put method on the
		 * ShortBuffer object to store the sample in the byte array named
		 * audioData.
		 * 
		 * The loop iterates once for each required data sample, producing the
		 * required number of synthetic data samples before terminating.
		 */
		for (int cnt = 0; cnt < SoundConstants.sampLength; cnt++) {

			/**
			 * This calculates the time in seconds, by dividing the sample
			 * number by the number of samples per second.
			 */
			double time = cnt / SoundConstants.sampleRate;

			/**
			 * This time value is multiplied by three different frequency
			 * values, a factor of 2, and the constant PI to produce three
			 * different values in radians to be used as arguments to the
			 * Math.sin method.
			 * 
			 * The three values in radians are passed to three separate
			 * invocations of the Math.sin method to produce the sum of three
			 * separate sine values as type double. This sum is divided by 3 to
			 * produce the numeric average of the three sine values.
			 */
			double sinValue = (Math.sin(2 * Math.PI * freq * time) + Math.sin(2 * Math.PI * (freq / 1.8) * time) + Math.sin(2 * Math.PI * (freq / 1.5) * time)) / 3.0;

			/**
			 * The numeric average of the three sine values is multiplied by the
			 * constant 16000, cast to type short, and put into the output
			 * array.
			 * 
			 * The type short is inherently a signed 16-bit type with big-endian
			 * byte order in Java, which is exactly what we need for the audio
			 * data format that we elected to use.
			 * 
			 * Why was the constant value of 16000 used?
			 * 
			 * I wanted the sound to be loud enough to hear easily. I also
			 * wanted to make certain that I didn't overflow the maximum value
			 * that can be contained in a value of type short.
			 * 
			 * The maximum value produced by the Math.sin method is 1.0. Thus,
			 * the maximum possible value in the average of the three sine
			 * values is also 1.0. The constant value of 16000 was chosen
			 * because it is approximately half the maximum value that can be
			 * contained in a value of type short. Thus, the maximum value that
			 * this algorithm can produce is approximately half the maximum
			 * value that can be contained in type short.
			 */
			shortBuffer.put((short) (16000 * sinValue));

		}

	}// end method tones

	static void stereoPanning() {

		/**
		 * Set different parameters
		 */
		setValues(2, 16000.0F, 600.0);

		double freq = SoundConstants.freq;

		for (int cnt = 0; cnt < SoundConstants.sampLength; cnt++) {

			/**
			 * This method generates two channels of data. One channel will
			 * ultimately be supplied to each speaker at playback. The apparent
			 * sweep from the left speaker to the right speaker is accomplished
			 * by:
			 * 
			 * - Causing the strength of the signal applied to the left speaker
			 * to decrease from a maximum value to zero over the (one second)
			 * time span of the signal.
			 * 
			 * - Causing the strength of the signal applied to the right speaker
			 * to increase from zero to the maximum value over the time span of
			 * the signal.
			 * 
			 * The code below computes the time-varying gain to be applied to
			 * the data for each channel during each iteration of the for loop.
			 * The gain for the left channel varies from 16000 to zero while the
			 * gain for the right channel varies from zero to 16000.
			 */
			double rightGain = 16000.0 * cnt / SoundConstants.sampLength;
			double leftGain = 16000.0 - rightGain;

			double time = cnt / SoundConstants.sampleRate;

			/**
			 * Generate data for the left speaker:
			 * 
			 * - Generates a double sine value for the left speaker at the
			 * correct frequency for the left speaker.
			 * 
			 * - Multiplies that sine value by the time-varying leftGain value
			 * for the left speaker.
			 * 
			 * - Casts the double value to type short.
			 * 
			 * - Puts the two bytes that constitute the sample for the left
			 * speaker into the output array.
			 */
			double sinValue = Math.sin(2 * Math.PI * (freq) * time);
			shortBuffer.put((short) (leftGain * sinValue));

			/**
			 * Generate data for the right speaker:
			 * 
			 * - Generates a double sine value for the right speaker at the
			 * correct frequency for the right speaker (0.8 times the frequency
			 * of the left speaker).
			 * 
			 * - Multiplies that sine value by the time-varying rightGain value
			 * for the right speaker.
			 * 
			 * - Casts the double value to type short.
			 * 
			 * - Puts the two bytes that constitute the sample for the right
			 * speaker in the output array, immediately following the two bytes
			 * that were put there by the code above.
			 */
			sinValue = Math.sin(2 * Math.PI * (freq * 0.8) * time);
			shortBuffer.put((short) (rightGain * sinValue));

		}
	}// end of method

	static void stereoPingpong() {

		/**
		 * Set different parameters
		 */
		setValues(2, 16000.0F, 600.0);

		double freq = SoundConstants.freq;
		double leftGain = 0.0;
		double rightGain = 16000.0;

		for (int cnt = 0; cnt < SoundConstants.sampLength; cnt++) {

			/**
			 * swap gain values:
			 * 
			 * This code uses the modulus operator to swap the gain values
			 * between the left and right channels each time the iteration
			 * counter value is an even multiple of one-eighth of the sample
			 * length. For the audioData array of 64000 bytes, this amounts to
			 * one swap of the gain values every 2000 samples, or eight times
			 * during the one-second elapsed time of the sound.
			 */
			if (cnt % (SoundConstants.sampLength / 8) == 0) {
				double temp = leftGain;
				leftGain = rightGain;
				rightGain = temp;
			}

			double time = cnt / SoundConstants.sampleRate;

			/**
			 * Generate data for left speaker
			 */
			double sinValue = Math.sin(2 * Math.PI * (freq) * time);
			shortBuffer.put((short) (leftGain * sinValue));

			/**
			 * Generate data for right speaker
			 */
			sinValue = Math.sin(2 * Math.PI * (freq * 0.8) * time);
			shortBuffer.put((short) (rightGain * sinValue));

		}

	}// end of method

	static void fmSweep() {

		/**
		 * Set different parameters
		 */
		setValues(1, 16000.0F, 600.0);

		double lowFreq = 100.0;
		double highFreq = 1000.0;

		for (int cnt = 0; cnt < SoundConstants.sampLength; cnt++) {
			double time = cnt / SoundConstants.sampleRate;

			double freq = lowFreq + cnt * (highFreq - lowFreq) / SoundConstants.sampLength;
			double sinValue = Math.sin(2 * Math.PI * freq * time);
			shortBuffer.put((short) (16000 * sinValue));
		}
	}// end of method

	static void decayPulse() {

		/**
		 * Set different parameters
		 */
		setValues(1, 16000.0F, 499.0);

		double freq = SoundConstants.freq;

		for (int cnt = 0; cnt < SoundConstants.sampLength; cnt++) {
			double scale = 2 * cnt;
			if (scale > SoundConstants.sampLength)
				scale = SoundConstants.sampLength;
			double gain = 16000 * (SoundConstants.sampLength - scale) / SoundConstants.sampLength;
			double time = cnt / SoundConstants.sampleRate;

			double sinValue = (Math.sin(2 * Math.PI * freq * time) + Math.sin(2 * Math.PI * (freq / 1.8) * time) + Math.sin(2 * Math.PI * (freq / 1.5) * time)) / 3.0;
			shortBuffer.put((short) (gain * sinValue));
		}
	}// end of method

	static void echoPulse() {

		/**
		 * Set different parameters
		 */
		setValues(1, 16000.0F, 499.0);

		double freq = SoundConstants.freq;

		int cnt2 = -8000;
		int cnt3 = -16000;
		int cnt4 = -24000;

		for (int cnt1 = 0; cnt1 < SoundConstants.sampLength; cnt1++, cnt2++, cnt3++, cnt4++) {
			double val = echoPulseHelper(cnt1, freq);

			if (cnt2 > 0) {
				val += 0.7 * echoPulseHelper(cnt2, freq);
			}

			if (cnt3 > 0) {
				val += 0.49 * echoPulseHelper(cnt3, freq);
			}

			if (cnt4 > 0) {
				val += 0.34 * echoPulseHelper(cnt4, freq);
			}

			shortBuffer.put((short) val);
		}

	}// end of method

	static double echoPulseHelper(int cnt, double freq) {
		double scale = 2 * cnt;
		if (scale > SoundConstants.sampLength)
			scale = SoundConstants.sampLength;

		double gain = 16000 * (SoundConstants.sampLength - scale) / SoundConstants.sampLength;
		double time = cnt / SoundConstants.sampleRate;

		double sinValue = (Math.sin(2 * Math.PI * freq * time) + Math.sin(2 * Math.PI * (freq / 1.8) * time) + Math.sin(2 * Math.PI * (freq / 1.5) * time)) / 3.0;

		return (short) (gain * sinValue);
	}// end of method

	static void waWaPulse() {

		/**
		 * Set different parameters
		 */
		setValues(1, 16000.0F, 499.0);

		double freq = SoundConstants.freq;

		int cnt2 = -8000;
		int cnt3 = -16000;
		int cnt4 = -24000;

		for (int cnt1 = 0; cnt1 < SoundConstants.sampLength; cnt1++, cnt2++, cnt3++, cnt4++) {
			double val = waWaPulseHelper(cnt1, freq);
			if (cnt2 > 0) {
				val += -0.7 * waWaPulseHelper(cnt2, freq);
			}

			if (cnt3 > 0) {
				val += 0.49 * waWaPulseHelper(cnt3, freq);
			}

			if (cnt4 > 0) {
				val += -0.34 * waWaPulseHelper(cnt4, freq);
			}

			shortBuffer.put((short) val);
		}
	}// end of method

	static double waWaPulseHelper(int cnt, double freq) {
		double scale = 2 * cnt;
		if (scale > SoundConstants.sampLength)
			scale = SoundConstants.sampLength;

		double gain = 16000 * (SoundConstants.sampLength - scale) / SoundConstants.sampLength;
		double time = cnt / SoundConstants.sampleRate;

		double sinValue = (Math.sin(2 * Math.PI * freq * time) + Math.sin(2 * Math.PI * (freq / 1.8) * time) + Math.sin(2 * Math.PI * (freq / 1.5) * time)) / 3.0;

		return (short) (gain * sinValue);
	}// end of method

	static void setValues(int channels, float sampleRate, double frequency) {
		SoundConstants.channels = channels;
		SoundConstants.sampleRate = sampleRate;
		SoundConstants.bytesPerSamp = 2 * channels;
		SoundConstants.sampLength = byteLength / SoundConstants.bytesPerSamp;
		SoundConstants.freq = frequency;

		SoundConstants.freq = frq;
		SoundConstants.sampleRate = samplingRate;
	}

	public static void prepareSoundStream() {
		try {
			/**
			 * Get an input stream on the byte array containing the data
			 */
			InputStream byteArrayInputStream = new ByteArrayInputStream(SoundConstants.audioData);

			/**
			 * Get the required audio format
			 */
			audioFormat = new AudioFormat(SoundConstants.sampleRate, SoundConstants.sampleSizeInBits, SoundConstants.channels, SoundConstants.signed,
					SoundConstants.bigEndian);

			/**
			 * Get an audio input stream from the ByteArrayInputStream
			 */
			audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, SoundConstants.audioData.length / audioFormat.getFrameSize());

			/**
			 * Get info on the required data line
			 */
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

			/**
			 * Get a SourceDataLine object
			 */
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}