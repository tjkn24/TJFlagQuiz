package com.tjknsoft.tjflagquiz;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

        mSounds.put(R.raw.correct, this.mShortPlayer.load(pContext, R.raw.correct, 1));
        mSounds.put(R.raw.wrong, this.mShortPlayer.load(pContext, R.raw.wrong, 1));
        mSounds.put(R.raw.perfect, this.mShortPlayer.load(pContext, R.raw.perfect, 1));
        mSounds.put(R.raw.wrong2, this.mShortPlayer.load(pContext, R.raw.wrong2, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = (Integer) mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}
