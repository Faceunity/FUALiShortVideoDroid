package com.aliyun.svideo.editor.draft;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.aliyun.common.utils.StringUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.editor.draft.AliyunDraftManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

/**
 * 草稿箱
 */
public class DraftListActivity extends FragmentActivity {
    /**
     * 服务端接口地址
     */
    public static String SERVER_URL_DEFAULT = "http://30.211.64.221:5000/";
    public static String SERVER_USER_NAME_DEFAULT = "Coder.Pi";
    public static String SERVER_GET_PROJECTS_URL;
    public static String SERVER_ADD_PROJECT_URL;
    public static String SERVER_DELETE_URL;
    public static String SERVER_UPLOAD_URL;

    public static final int REQUEST_IMAGE = 2021;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private AliyunDraft curCoverDraft;
    private LocalDraftAdapter mLocalDraftAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_activity_draft_list);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        findViewById(R.id.alivc_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        findViewById(R.id.alivc_draft_config_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showConfigDialog();
            }
        });
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("cloud_draft_config", Context.MODE_PRIVATE);
        String serverUrl = preferences.getString("server_url", SERVER_URL_DEFAULT);
        String userName = preferences.getString("user_name", SERVER_USER_NAME_DEFAULT);
        initCloudConfig(serverUrl, userName);
        initData();
    }

    private void initCloudConfig(String serverUrl, String userName) {
        SERVER_GET_PROJECTS_URL = serverUrl + "get_projects/" + userName;
        SERVER_ADD_PROJECT_URL = serverUrl + "add_project/" + userName;
        SERVER_DELETE_URL = serverUrl + "delete_project/" + userName;
        SERVER_UPLOAD_URL = serverUrl + "upload_resource/" + userName;
    }

    private void initData() {
        //复制出错图片
        EditorCommon.copySelf(mTabLayout.getContext(), "svideo_res");
        DraftPagerAdapter adapter = new DraftPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    public void showMenu(boolean hideBackup, final View.OnClickListener onClickListener, final Object tag) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                v.setTag(tag);
                onClickListener.onClick(v);
                bottomSheetDialog.dismiss();
            }
        };
        View bottomSheet = View.inflate(this, R.layout.alivc_editor_draft_bottom_sheet, null);
        bottomSheet.findViewById(R.id.alivc_rename_btn).setOnClickListener(clickListener);
        bottomSheet.findViewById(R.id.alivc_update_cover_btn).setOnClickListener(clickListener);
        bottomSheet.findViewById(R.id.alivc_copy_btn).setOnClickListener(clickListener);
        bottomSheet.findViewById(R.id.alivc_delete_btn).setOnClickListener(clickListener);
        bottomSheet.findViewById(R.id.alivc_backup_btn).setOnClickListener(clickListener);
        if (hideBackup) {
            bottomSheet.findViewById(R.id.alivc_backup_btn).setVisibility(View.GONE);
        } else {
            bottomSheet.findViewById(R.id.alivc_backup_btn).setVisibility(View.VISIBLE);
        }
        bottomSheetDialog.setContentView(bottomSheet);
        bottomSheetDialog.show();
    }

    public void showCloudMenu(final View.OnClickListener onClickListener, final Object tag) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                v.setTag(tag);
                onClickListener.onClick(v);
                bottomSheetDialog.dismiss();
            }
        };
        View bottomSheet = View.inflate(this, R.layout.alivc_editor_cloud_draft_bottom_sheet, null);
        bottomSheet.findViewById(R.id.alivc_delete_btn).setOnClickListener(clickListener);
        bottomSheetDialog.setContentView(bottomSheet);
        bottomSheetDialog.show();
    }

    public void showRenameDialog(String name, DraftRenameDialogFragment.OnRenameListener onRenameListener) {
        DraftRenameDialogFragment draftRenameDialog = new DraftRenameDialogFragment();
        draftRenameDialog.setOnRenameListener(onRenameListener);
        draftRenameDialog.setDraftName(name);
        draftRenameDialog.show(getSupportFragmentManager(), DraftRenameDialogFragment.class.getSimpleName());
    }

    public void showConfigDialog() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("cloud_draft_config", Context.MODE_PRIVATE);
        String serverUrl = preferences.getString("server_url", SERVER_URL_DEFAULT);
        String userName = preferences.getString("user_name", SERVER_USER_NAME_DEFAULT);
        CloudDraftConfigDialogFragment cloudDraftConfigDialog = new CloudDraftConfigDialogFragment();
        cloudDraftConfigDialog.setServerUrl(serverUrl);
        cloudDraftConfigDialog.setUserName(userName);
        cloudDraftConfigDialog.setOnCloudDraftConfigListener(new CloudDraftConfigDialogFragment.OnCloudDraftConfigListener() {
            @Override
            public void onConfig(final String serverUrl, final String name) {
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("cloud_draft_config", Context.MODE_PRIVATE);
                preferences.edit()
                           .putString("server_url", serverUrl)
                           .putString("user_name", name).commit();
                initCloudConfig(serverUrl, name);
                initData();
            }
        });
        cloudDraftConfigDialog.show(getSupportFragmentManager(), CloudDraftConfigDialogFragment.class.getSimpleName());
    }

    public void updateCover(LocalDraftAdapter localDraftAdapter,AliyunDraft item){
        mLocalDraftAdapter = localDraftAdapter;
        curCoverDraft = item;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            String errorMsg = "";
            if (resultCode == RESULT_OK && curCoverDraft != null) {
                String path = getRealPathFromURI(data.getData());
                if (!StringUtils.isEmpty(path)) {
                    AliyunDraftManager.getInstance(getApplication()).updateCover(curCoverDraft, new Source(path));
                    mLocalDraftAdapter.notifyDataSetChanged();
                } else {
                    errorMsg = getText(R.string.alivc_svideo_draft_cover_failed).toString();
                }
            } else {
                errorMsg = getText(R.string.alivc_svideo_draft_cover_cancel).toString();
            }
            if (!StringUtils.isEmpty(errorMsg)) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
            curCoverDraft = null;
            mLocalDraftAdapter = null;
        }
    }

    private String getRealPathFromURI(final Uri uri) {
        if (null == uri) {
            return null;
        }
        String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
