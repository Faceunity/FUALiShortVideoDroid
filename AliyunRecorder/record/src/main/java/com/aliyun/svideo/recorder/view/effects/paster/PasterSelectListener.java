package com.aliyun.svideo.recorder.view.effects.paster;

import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;

public interface PasterSelectListener {
    public void onPasterSelected(PreviewPasterForm imvForm);

    /**
     * 选择的贴图下载完成
     * @param path paster path
     */
    void onSelectPasterDownloadFinish(String path);
}
