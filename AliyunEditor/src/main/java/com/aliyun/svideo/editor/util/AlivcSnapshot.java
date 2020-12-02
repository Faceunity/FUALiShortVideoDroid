package com.aliyun.svideo.editor.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.util.Log;
import android.view.SurfaceView;

import com.aliyun.svideo.common.utils.ThreadUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * use glReadPixels get frame
 * stackoverflow Accepted answer：https://stackoverflow.com/questions/27817577/android-take-screenshot-of-surface-view-shows-black-screen
 */
public class AlivcSnapshot {
    public static final String TAG = "AlivcSnapshot";
    /**
     * 是否正在生成封面中
     */
    private boolean isSnapshotting;
    /**
     * useTextureIDGetFrame
     * @param srcTextureID
     * @param mSurfaceView
     * @param outPutFile
     */
    public void useTextureIDGetFrame(int srcTextureID, SurfaceView mSurfaceView, int width, int height, File outPutFile) {
        isSnapshotting = true;
        IntBuffer prevTex = IntBuffer.allocate(1);
        GLES20.glGetFramebufferAttachmentParameteriv(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, prevTex);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, srcTextureID, 0);
        saveFrame(outPutFile, mSurfaceView, width, height);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, prevTex.get(0), 0);
    }

    /**
     * takeFrame
     * @param outPutFile
     * @param mSurfaceView
     */
    private void saveFrame(final File outPutFile, SurfaceView mSurfaceView, final int width, final int height) {


        //final int width = mSurfaceView.getWidth();
        //final int height = mSurfaceView.getHeight();
        final ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height,
                            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        checkGlError("glReadPixels");
        buf.rewind();
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = null;

                bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bmp.setHasAlpha(true);
                bmp.copyPixelsFromBuffer(buf);
                //对生成的图像作翻转处理
                Matrix mx = new Matrix();
                mx.setScale(1, -1);

                Bitmap bmpReversal = Bitmap.createBitmap(bmp, 0, 0, width, height, mx, true);
                try {
                    saveBitmapToFile(bmpReversal, outPutFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmp.recycle();
                bmpReversal.recycle();
                isSnapshotting = false;
            }
        });

    }

    /**
     * 保存图片数据
     * @param bitmap
     * @param outPutFile
     */
    private void saveBitmapToFile(final Bitmap bitmap, final File outPutFile) throws IOException {
        String filename = outPutFile.toString();
        if (!outPutFile.exists()) {
            outPutFile.createNewFile();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }
    /**
     * Checks to see if a GLES error has been raised.
     */
    private static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    public boolean isSnapshotting() {
        return isSnapshotting;
    }
}
