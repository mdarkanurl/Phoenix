package com.phoenix.alarm;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * Generates the four alarm chimes in memory so the app has no binary audio
 * assets and can loop a tone indefinitely while an alarm is ringing.
 */
public final class ChimePlayer {

    private static final int SAMPLE_RATE = 44_100;

    private volatile AudioTrack track;
    private volatile boolean stopRequested;
    private Thread playbackThread;

    public void start(String soundName, boolean loop) {
        start(soundName, loop, loop ? 1.0f : 0.65f);
    }

    public void start(String soundName, boolean loop, float volume) {
        stop();
        stopRequested = false;

        playbackThread = new Thread(() -> playOnBackgroundThread(soundName, loop, volume), "alarm-chime");
        playbackThread.start();
    }

    public void stop() {
        stopRequested = true;
        releaseTrack();
    }

    private void playOnBackgroundThread(String soundName, boolean loop, float volume) {
        AudioTrack localTrack = null;
        try {
            short[] samples = buildSamples(soundName);
            if (stopRequested) return;

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            AudioFormat format = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build();

            localTrack = new AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(samples.length * 2)
                    .build();

            localTrack.write(samples, 0, samples.length);
            localTrack.setVolume(volume);

            if (loop) {
                localTrack.setLoopPoints(0, samples.length, -1);
            }

            track = localTrack;
            if (stopRequested) {
                releaseLocalTrack(localTrack);
                if (track == localTrack) track = null;
                return;
            }

            localTrack.play();

            if (!loop) {
                long durationMs = Math.max(500L, (samples.length * 1000L) / SAMPLE_RATE);
                Thread.sleep(durationMs + 250L);
                releaseLocalTrack(localTrack);
                if (track == localTrack) track = null;
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // Audio is best-effort; the alarm screen and notification still work.
        } finally {
            if (localTrack != null && !loop) {
                releaseLocalTrack(localTrack);
                if (track == localTrack) track = null;
            }
        }
    }

    private void releaseLocalTrack(AudioTrack localTrack) {
        try {
            if (localTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                localTrack.stop();
            }
        } catch (Exception ignored) {
        }
        try {
            localTrack.flush();
        } catch (Exception ignored) {
        }
        try {
            localTrack.release();
        } catch (Exception ignored) {
        }
    }

    private void releaseTrack() {
        AudioTrack localTrack = track;
        track = null;
        if (localTrack == null) return;
        releaseLocalTrack(localTrack);
    }

    private static short[] buildSamples(String soundName) {
        float[] frequencies;
        int[] durations;
        int[] gaps;

        switch (soundName) {
            case "Night Owl":
                frequencies = new float[]{392.00f, 523.25f, 440.00f, 349.23f};
                durations = new int[]{1100, 1000, 1200, 1600};
                gaps = new int[]{200, 200, 200, 900};
                break;
            case "Morning Dew":
                frequencies = new float[]{659.25f, 880.00f, 987.77f, 1318.51f};
                durations = new int[]{600, 700, 900, 1200};
                gaps = new int[]{150, 150, 150, 700};
                break;
            case "Soft Chime":
                frequencies = new float[]{440.00f, 554.37f, 659.25f};
                durations = new int[]{900, 1000, 1400};
                gaps = new int[]{200, 200, 900};
                break;
            case "Slow Rise":
            default:
                frequencies = new float[]{523.25f, 659.25f, 783.99f, 1046.50f};
                durations = new int[]{800, 900, 1100, 1500};
                gaps = new int[]{150, 150, 150, 700};
                break;
        }

        int totalSamples = 0;
        for (int i = 0; i < frequencies.length; i++) {
            totalSamples += msToSamples(durations[i] + gaps[i]);
        }

        float[] mix = new float[totalSamples];
        int cursor = 0;
        for (int i = 0; i < frequencies.length; i++) {
            renderNote(mix, cursor, frequencies[i], durations[i], 0.24f);
            cursor += msToSamples(durations[i] + gaps[i]);
        }

        short[] pcm = new short[totalSamples];
        for (int i = 0; i < mix.length; i++) {
            float value = Math.max(-1f, Math.min(1f, mix[i]));
            pcm[i] = (short) (value * Short.MAX_VALUE);
        }
        return pcm;
    }

    private static void renderNote(
            float[] output,
            int startSample,
            float frequency,
            int durationMs,
            float amplitude
    ) {
        int durationSamples = msToSamples(durationMs);
        double durationSeconds = durationMs / 1000.0;

        for (int i = 0; i < durationSamples && startSample + i < output.length; i++) {
            double time = i / (double) SAMPLE_RATE;
            double attack = Math.min(1.0, time / 0.02);
            double release = Math.min(1.0, (durationSeconds - time) / 0.15);
            double envelope = attack * release * Math.exp(-1.7 * time / durationSeconds);

            double wave = Math.sin(2.0 * Math.PI * frequency * time)
                    + 0.34 * Math.sin(4.0 * Math.PI * frequency * time)
                    + 0.14 * Math.sin(6.0 * Math.PI * frequency * time);

            output[startSample + i] += (float) (wave * envelope * amplitude);
        }
    }

    private static int msToSamples(int milliseconds) {
        return Math.max(1, (SAMPLE_RATE * milliseconds) / 1000);
    }
}
