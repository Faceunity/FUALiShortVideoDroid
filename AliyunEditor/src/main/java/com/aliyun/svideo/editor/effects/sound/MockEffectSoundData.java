package com.aliyun.svideo.editor.effects.sound;

import com.aliyun.editor.AudioEffectType;
import com.aliyun.svideo.editor.R;

import java.util.ArrayList;
import java.util.List;

public class MockEffectSoundData {

    private static List<SoundEffectInfo> mSoundData = new ArrayList<>();

    private static SoundModel[] sModels = {
        new SoundModel(R.string.alivc_editor_dialog_sound_default, 0, R.drawable.alivc_svideo_effect_sound_default, AudioEffectType.EFFECT_TYPE_DEFAULT),
        new SoundModel(R.string.alivc_editor_dialog_sound_loli, 50, R.drawable.alivc_svideo_effect_sound_loli, AudioEffectType.EFFECT_TYPE_LOLITA),
        new SoundModel(R.string.alivc_editor_dialog_sound_uncle, 50, R.drawable.alivc_svideo_effect_sound_uncle, AudioEffectType.EFFECT_TYPE_UNCLE),
        new SoundModel(R.string.alivc_editor_dialog_sound_echo, 50, R.drawable.alivc_svideo_effect_sound_echo, AudioEffectType.EFFECT_TYPE_ECHO),
        new SoundModel(R.string.alivc_editor_dialog_sound_reverb, 50, R.drawable.alivc_svideo_effect_sound_reverb, AudioEffectType.EFFECT_TYPE_REVERB),
        new SoundModel(R.string.alivc_editor_dialog_sound_robot, 50, R.drawable.alivc_svideo_effect_sound_robot, AudioEffectType.EFFECT_TYPE_ROBOT),
        new SoundModel(R.string.alivc_editor_dialog_sound_devil, 50, R.drawable.alivc_svideo_effect_sound_devil, AudioEffectType.EFFECT_TYPE_BIG_DEVIL),
        new SoundModel(R.string.alivc_editor_dialog_sound_minions, 50, R.drawable.alivc_svideo_effect_sound_minions, AudioEffectType.EFFECT_TYPE_MINIONS),

    };

    static {

        for (SoundModel model : sModels) {
            SoundEffectInfo soundEffectInfo = new SoundEffectInfo();
            soundEffectInfo.soundNameId = model.mNameId;
            soundEffectInfo.audioEffectType = model.mType;
            soundEffectInfo.soundWeight = model.mWeight;
            soundEffectInfo.imgIcon = model.mResourceId;
            mSoundData.add(soundEffectInfo);
        }

    }

    public static List<SoundEffectInfo> getEffectSound() {
        return mSoundData;
    }

    private static class SoundModel {
        private int mNameId;
        private int mWeight;
        private int mResourceId;
        private AudioEffectType mType;

        private SoundModel(int nameId, int weight, int resourceId, AudioEffectType type) {
            mNameId = nameId;
            mWeight = weight;
            mResourceId = resourceId;
            mType = type;
        }
    }

}
