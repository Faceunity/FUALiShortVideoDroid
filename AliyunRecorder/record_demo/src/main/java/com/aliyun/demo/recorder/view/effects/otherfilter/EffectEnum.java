package com.aliyun.demo.recorder.view.effects.otherfilter;


import com.aliyun.demo.R;

import java.util.ArrayList;

/**
 * Created by hyj on 2018/11/6.
 */

public enum EffectEnum {
    EffectNone("none", R.mipmap.alivc_svideo_effect_none, "none", 1, Effect.EFFECT_TYPE_NONE, 0),

    Effect_bling("bling", R.mipmap.bling, "normal/bling.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_fengya_ztt_fu("fengya_ztt_fu", R.mipmap.fengya_ztt_fu, "normal/fengya_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_hudie_lm_fu("hudie_lm_fu", R.mipmap.hudie_lm_fu, "normal/hudie_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_touhua_ztt_fu("touhua_ztt_fu", R.mipmap.touhua_ztt_fu, "normal/touhua_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_juanhuzi_lm_fu("juanhuzi_lm_fu", R.mipmap.juanhuzi_lm_fu, "normal/juanhuzi_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_mask_hat("mask_hat", R.mipmap.mask_hat, "normal/mask_hat.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_yazui("yazui", R.mipmap.yazui, "normal/yazui.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_yuguan("yuguan", R.mipmap.yuguan, "normal/yuguan.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),

    Effect_hez_ztt_fu("hez_ztt_fu", R.mipmap.hez_ztt_fu, "background/hez_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, R.string.hez_ztt_fu + 0),
    Effect_gufeng_zh_fu("gufeng_zh_fu", R.mipmap.gufeng_zh_fu, "background/gufeng_zh_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_xiandai_ztt_fu("xiandai_ztt_fu", R.mipmap.xiandai_ztt_fu, "background/xiandai_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_sea_lm_fu("sea_lm_fu", R.mipmap.sea_lm_fu, "background/sea_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_ice_lm_fu("ice_lm_fu", R.mipmap.ice_lm_fu, "background/ice_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),

    Effect_ctrl_rain("ctrl_rain", R.mipmap.ctrl_rain, "gesture/ctrl_rain.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ctrl_rain + 0),
    Effect_ctrl_snow("ctrl_snow", R.mipmap.ctrl_snow, "gesture/ctrl_snow.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ctrl_snow + 0),
    Effect_ctrl_flower("ctrl_flower", R.mipmap.ctrl_flower, "gesture/ctrl_flower.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ctrl_flower + 0),
    Effect_fu_lm_koreaheart("fu_lm_koreaheart", R.mipmap.fu_lm_koreaheart, "gesture/ssd_thread_korheart.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.fu_lm_koreaheart + 0),
    Effect_ssd_thread_cute("ssd_thread_cute", R.mipmap.ssd_thread_cute, "gesture/ssd_thread_cute.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ssd_thread_cute + 0),
    Effect_ssd_thread_six("ssd_thread_six", R.mipmap.ssd_thread_six, "gesture/ssd_thread_six.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ssd_thread_six + 0),
    Effect_ssd_thread_thumb("ssd_thread_thumb", R.mipmap.ssd_thread_thumb, "gesture/ssd_thread_thumb.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ssd_thread_thumb + 0),

    Effect_Dongm_Lvj("dongm_lvj", R.mipmap.dongmlvjing, "fuzzytoonfilter.bundle", 1, Effect.EFFECT_TYPE_DONGM_LVJ, 0),

    Effect_frog_Animoji("frog_Animoji", R.mipmap.frog_animoji, "animoji/frog_st_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_huangya_Animoji("huangya_Animoji", R.mipmap.huangya_animoji, "animoji/huangya_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_hetun_Animoji("hetun_Animoji", R.mipmap.hetun_animoji, "animoji/hetun_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_douniuquan_Animoji("douniuquan_Animoji", R.mipmap.douniuquan_animoji, "animoji/douniuquan_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_hashiqi_Animoji("hashiqi_Animoji", R.mipmap.hashiqi_animoji, "animoji/hashiqi_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_baimao_Animoji("baimao_Animoji", R.mipmap.baimao_animoji, "animoji/baimao_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    //    Effect_chaiquan_Animoji("chaiquan_Animoji", R.mipmap.chaiquan_animoji, "animoji/chaiquan_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_kuloutou_Animoji("kuloutou_Animoji", R.mipmap.kuloutou_animoji, "animoji/kuloutou_Animoji.bundle", 1, Effect.EFFECT_TYPE_ANIMOJI, 0),

    Effect_facewarp2("facewarp2", R.mipmap.facewarp2, "facewarp/facewarp2.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp3("facewarp3", R.mipmap.facewarp3, "facewarp/facewarp3.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp4("facewarp4", R.mipmap.facewarp4, "facewarp/facewarp4.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp5("facewarp5", R.mipmap.facewarp5, "facewarp/facewarp5.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp6("facewarp6", R.mipmap.facewarp6, "facewarp/facewarp6.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),

    Effect_bluebird("bluebird", R.mipmap.bluebird, "ar/bluebird.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_lanhudie("lanhudie", R.mipmap.lanhudie, "ar/lanhudie.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_fenhudie("fenhudie", R.mipmap.fenhudie, "ar/fenhudie.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger_huang("tiger_huang", R.mipmap.tiger_huang, "ar/tiger_huang.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger_bai("tiger_bai", R.mipmap.tiger_bai, "ar/tiger_bai.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_afd("afd", R.mipmap.afd, "ar/afd.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_baozi("baozi", R.mipmap.baozi, "ar/baozi.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger("tiger", R.mipmap.tiger, "ar/tiger.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_xiongmao("xiongmao", R.mipmap.xiongmao, "ar/xiongmao.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_armesh("armesh", R.mipmap.armesh, "ar/armesh.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_armesh_ex("armesh_ex", R.mipmap.armesh_ex, "ar/armesh_ex.bundle", 4, Effect.EFFECT_TYPE_AR, 0),

    Effect_future_warrior("future_warrior", R.mipmap.future_warrior, "expression/future_warrior.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.future_warrior),
    Effect_jet_mask("jet_mask", R.mipmap.jet_mask, "expression/jet_mask.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.jet_mask),
    Effect_sdx2("sdx2", R.mipmap.sdx2, "expression/sdx2.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.sdx2),
    Effect_luhantongkuan_ztt_fu("luhantongkuan_ztt_fu", R.mipmap.luhantongkuan_ztt_fu, "expression/luhantongkuan_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.luhantongkuan_ztt_fu),
    Effect_qingqing_ztt_fu("qingqing_ztt_fu", R.mipmap.qingqing_ztt_fu, "expression/qingqing_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.qingqing_ztt_fu),
    Effect_xiaobianzi_zh_fu("xiaobianzi_zh_fu", R.mipmap.xiaobianzi_zh_fu, "expression/xiaobianzi_zh_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.xiaobianzi_zh_fu),
    Effect_xiaoxueshen_ztt_fu("xiaoxueshen_ztt_fu", R.mipmap.xiaoxueshen_ztt_fu, "expression/xiaoxueshen_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.xiaoxueshen_ztt_fu),

    Effect_douyin_old("douyin_01", R.mipmap.douyin_old, "musicfilter/douyin_01.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, 0),
    Effect_douyin("douyin_02", R.mipmap.douyin, "musicfilter/douyin_02.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, 0);

    private String bundleName;
    private int resId;
    private String path;
    private int maxFace;
    private int effectType;
    private int description;

    EffectEnum(String name, int resId, String path, int maxFace, int effectType, int description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.maxFace = maxFace;
        this.effectType = effectType;
        this.description = description;
    }

    public String bundleName() {
        return bundleName;
    }

    public int resId() {
        return resId;
    }

    public String path() {
        return path;
    }

    public int maxFace() {
        return maxFace;
    }

    public int effectType() {
        return effectType;
    }

    public int description() {
        return description;
    }

    public Effect effect() {
        return new Effect(bundleName, resId, path, maxFace, effectType, description);
    }

    public static ArrayList<Effect> getEffectsByEffectType(int effectType) {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(EffectNone.effect());
        for (EffectEnum e : EffectEnum.values()) {
            if (e.effectType == effectType) {
                effects.add(e.effect());
            }
        }
        return effects;
    }
}
