package com.github.anrimian.musicplayer.data.controllers.music.players.exoplayer;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@UnstableApi public class StereoVolumeProcessor implements AudioProcessor {

    private int channelCount;
    private int sampleRateHz;
    private int[] pendingOutputChannels;

    private boolean active;
    private int[] outputChannels;
    private ByteBuffer buffer;
    private short[] tempBuffer;
    private ByteBuffer outputBuffer;
    private boolean inputEnded;

    private float[] volume;

    private static final int LEFT_SPEAKER = 0;
    private static final int RIGHT_SPEAKER = 1;

    public StereoVolumeProcessor() {
        buffer = EMPTY_BUFFER;
        outputBuffer = EMPTY_BUFFER;
        channelCount = Format.NO_VALUE;
        sampleRateHz = Format.NO_VALUE;
    }

    public void setChannelMap(int[] outputChannels) {
        pendingOutputChannels = outputChannels;
    }

    @NonNull
    @Override
    public AudioFormat configure(AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        int sampleRateHz = inputAudioFormat.sampleRate;
        int channelCount = inputAudioFormat.channelCount;
        @C.Encoding int encoding = inputAudioFormat.encoding;

        if (volume == null) {
            throw new IllegalStateException("volume has not been set! Call setVolume(float left,float right)");
        }

        boolean outputChannelsChanged = !Arrays.equals(pendingOutputChannels, outputChannels);
        outputChannels = pendingOutputChannels;
        if (outputChannels == null) {
            active = false;
            return inputAudioFormat;
        }
        if (encoding != C.ENCODING_PCM_16BIT) {
            throw new UnhandledAudioFormatException(inputAudioFormat);
        }
        if (!outputChannelsChanged && this.sampleRateHz == sampleRateHz
                && this.channelCount == channelCount) {
            return inputAudioFormat;
        }
        this.sampleRateHz = sampleRateHz;
        this.channelCount = channelCount;

        active = true;

        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int position = inputBuffer.position();
        int limit = inputBuffer.limit();
        int size = limit - position;
        int samplesCount = size / 2;

        if (buffer.capacity() < size) {
            buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            buffer.clear();
        }

        if (isActive()) {
            if (tempBuffer == null || tempBuffer.length < samplesCount) {
                tempBuffer = new short[samplesCount];
            }

            inputBuffer.order(ByteOrder.nativeOrder());
            inputBuffer.asShortBuffer().get(tempBuffer, 0, samplesCount);

            float leftVol = volume[0];
            float rightVol = volume[1];

            for (int i = 0; i < samplesCount; i += 2) {
                tempBuffer[i] = (short) (tempBuffer[i] * leftVol);
                if (i + 1 < samplesCount) {
                    tempBuffer[i+1] = (short) (tempBuffer[i+1] * rightVol);
                }
            }

            buffer.asShortBuffer().put(tempBuffer, 0, samplesCount);
            buffer.position(size);
        } else {
            buffer.put(inputBuffer);
        }

        inputBuffer.position(limit);
        buffer.flip();
        outputBuffer = buffer;
    }

    @Override
    public void queueEndOfStream() {
        inputEnded = true;
    }

    public void setVolume(float left, float right) {
        volume = new float[]{left, right};
    }

    public float getLeftVolume() {
        return volume[LEFT_SPEAKER];
    }

    public float getRightVolume() {
        return volume[RIGHT_SPEAKER];
    }

    @NonNull
    @Override
    public ByteBuffer getOutput() {
        ByteBuffer outputBuffer = this.outputBuffer;
        this.outputBuffer = EMPTY_BUFFER;
        return outputBuffer;
    }

    @SuppressWarnings("ReferenceEquality")
    @Override
    public boolean isEnded() {
        return inputEnded && outputBuffer == EMPTY_BUFFER;
    }

    @Override
    public void flush() {
        outputBuffer = EMPTY_BUFFER;
        inputEnded = false;
    }

    @Override
    public void reset() {
        flush();
        buffer = EMPTY_BUFFER;
        channelCount = Format.NO_VALUE;
        sampleRateHz = Format.NO_VALUE;
        outputChannels = null;
        active = false;
    }
}