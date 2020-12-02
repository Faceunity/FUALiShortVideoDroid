package com.aliyun.svideo.editor.effects.sound;

import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;

public class SoundEffectInfo extends EffectInfo {
    public int soundNameId;
    public int imgIcon;

    public SoundEffectInfo() {
        type = UIEditorPage.SOUND;
    }
}
