package com.aliyun.race.sample.render;

import com.aliyun.race.AliyunPoint;

import java.nio.FloatBuffer;
import static android.opengl.GLES20.*;
import static com.aliyun.race.sample.render.GLUtils.glCheck;

public class DrawPoints {
    private GLProgram mProgram;
    private FloatBuffer mPoints;

    public DrawPoints()
    {
        mProgram = new GLProgram();
        mProgram.init(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
    }

    public void addPoints(AliyunPoint[] points, int width, int height) {
        if (points == null) {
            return;
        }
        float[] buffer = new float[106 * 2];

        for (int i = 0, j = 0; i < 106; i++) {
            buffer[j] = points[i].x / width * 2.F - 1.F;
            buffer[j + 1] = 1.F - points[i].y  / height * 2.F;
            j = j + 2;
        }
        if (mPoints == null) {
            mPoints = GLUtils.allocateBuffer(buffer);
        }
        mPoints.put(buffer, 0, buffer.length);
        mPoints.position(0);
    }

    public void draw()
    {
        if (mPoints == null) {
            return;
        }
        int program = mProgram.getProgram();
        glUseProgram(program);
        int position = glGetAttribLocation(program, "a_position");
        glCheck();
        glEnableVertexAttribArray(position);
        glCheck();
        glVertexAttribPointer(position, 2, GL_FLOAT, false, 0, mPoints);
        glCheck();
        glDrawArrays(GL_POINTS, 0, 106);
        glCheck();
    }

    public void destroy() {
        mProgram.destroy();
    }

    private final static String VERTEX_SHADER_SOURCE = "attribute vec2 a_position;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_PointSize = 8.0;\n" +
            "    gl_Position = vec4(a_position, 0, 1.0);\n" +
            "}";

    private final static String FRAGMENT_SHADER_SOURCE = "#ifdef GL_ES\n" +
            "precision highp float;\n" +
            "#endif\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = vec4(0, 0, 1.0, 1.0);\n" +
            "}";


}