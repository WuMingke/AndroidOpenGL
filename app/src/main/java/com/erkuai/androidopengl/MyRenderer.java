package com.erkuai.androidopengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2019/9/17.
 * <p>
 * SurfaceView实质是将底层显存Surface显示在界面上，而GLSurfaceView做的就是在这个基础上增加OpenGL绘制环境
 * <p>
 * 顶点着色器：VerticesShader
 * <p>
 * 片元着色器：FragmentShader
 */

public class MyRenderer implements GLSurfaceView.Renderer {

    private int program;
    private int vPosition;
    private int uColor;

    // 顶点着色器的脚本
    private static final String verticesShader
            = "attribute vec2 vPosition;            \n" // 顶点位置属性vPosition
            + "void main(){                         \n"
            + "   gl_Position = vec4(vPosition,0,1);\n" // 确定顶点位置
            + "}";

    // 片元着色器的脚本
    private static final String fragmentShader
            = "precision mediump float;         \n" // 声明float类型的精度为中等(精度越高越耗资源)
            + "uniform vec4 uColor;             \n" // uniform的属性uColor
            + "void main(){                     \n"
            + "   gl_FragColor = uColor;        \n" // 给此片元的填充色
            + "}";

    /**
     * 当GLSurfaceView中的Surface被创建的时候(界面显示)回调此方法，一般在这里做一些初始化
     *
     * @param gl     gl10 1.0版本的OpenGL对象，这里用于兼容老版本，用处不大
     * @param config egl的配置信息(GLSurfaceView会自动创建egl，这里可以先忽略)
     */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 初始化着色器
        // 基于顶点着色器与片元着色器创建程序
        program = createProgram(verticesShader, fragmentShader);
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        uColor = GLES20.glGetUniformLocation(program, "uColor");

        // 设置clear color颜色RGBA(这里仅仅是设置清屏时GLES20.glClear()用的颜色值而不是执行清屏)
        GLES20.glClearColor(1.0f, 0, 0, 1.0f);
    }

    /**
     * 当GLSurfaceView中的Surface被改变的时候回调此方法(一般是大小变化)
     *
     * @param gl
     * @param width  Surface的宽度
     * @param height Surface的高度
     */

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置绘图的窗口(可以理解成在画布上划出一块区域来画图)
        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * 当Surface需要绘制的时候回调此方法
     * <p>
     * 根据GLSurfaceView.setRenderMode()设置的渲染模式不同回调的策略也不同：
     * GLSurfaceView.RENDERMODE_CONTINUOUSLY : 固定一秒回调60次(60fps)
     * GLSurfaceView.RENDERMODE_WHEN_DIRTY   : 当调用GLSurfaceView.requestRender()之后回调一次
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // 获取图形的顶点坐标
        FloatBuffer vertices = getVertices();
        // 清屏
        GLES20.glClear(GLES20.GL_DEPTH_BITS | GLES20.GL_COLOR_BUFFER_BIT);
        // 使用某套shader程序
        GLES20.glUseProgram(program);
        // 为画笔指定顶点位置数据(vPosition)
        /**
         * index 指定要修改的顶点属性的索引值
         * size 指定每个顶点属性的组件数量。必须为1、2、3或者4。初始值为4。
         * type 指定数组中每个组件的数据类型。
         * normalized 指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）。
         * stride 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
         * pointer 指定第一个组件在数组的第一个顶点属性中的偏移量。该数组与GL_ARRAY_BUFFER绑定，储存于缓冲区中。初始值为0；
         */
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertices);
        // 允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(vPosition);
        // 设置属性uColor(颜色 索引,R,G,B,A)
        GLES20.glUniform4f(uColor, 0.0f, 1.0f, 0.0f, 1.0f);
        // 绘制
        /**
         * mode 绘制方式
         * first 从数组缓存中的哪一位开始绘制
         * count 数组中顶点的数量
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
    }

    /**
     * 获取图形的顶点
     * 由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
     * 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
     *
     * @return 顶点Buffer
     */

    private FloatBuffer getVertices() {
        float vertices[] = {
                0.0f, 0.5f,
                -0.5f, -0.5f,
                0.5f, -0.5f
        };

        // 创建顶点坐标数据缓冲
        // vertices.length*4是因为一个float占四个字节
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        //设置字节顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        //转换为Float型缓冲
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        //向缓冲区中放入顶点坐标数据
        floatBuffer.put(vertices);
        //设置缓冲区起始位置
        floatBuffer.position(0);

        return floatBuffer;
    }

    /**
     * 创建shader程序的方法
     *
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    private int createProgram(String vertexSource, String fragmentSource) {
        //加载顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        // 加载片元着色器
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        // 创建程序
        int program = GLES20.glCreateProgram();

        // 若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {
            // 向程序中加入顶点着色器
            GLES20.glAttachShader(program, vertexShader);
            // 向程序中加入片元着色器
            GLES20.glAttachShader(program, pixelShader);
            // 链接程序
            GLES20.glLinkProgram(program);

            // 存放链接成功program数量的数组
            int[] linkStatus = new int[1];
            // 获取program的链接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            // 若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("wmk", "Could not link program: ");
                Log.e("wmk", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }

    /**
     * 加载制定shader的方法
     *
     * @param shaderType shader的类型 GLES20.GL_VERTEX_SHADER   GLES20.GL_FRAGMENT_SHADER
     * @param sourceCode shader的脚本
     * @return shader索引
     */

    private int loadShader(int shaderType, String sourceCode) {
        // 创建一个新shader
        int shader = GLES20.glCreateShader(shaderType);
        // 若创建成功则加载shader
        if (shader != 0) {
            // 加载shader的源代码
            GLES20.glShaderSource(shader, sourceCode);
            // 编译shader
            GLES20.glCompileShader(shader);
            // 存放编译成功shader数量的数组
            int[] complied = new int[1];
            // 获取Shader的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, complied, 0);
            //若编译失败则显示错误日志并删除此shader
            if (complied[0] == 0) {
                Log.i("wmk", "Could not compile shader " + shaderType + ":");
                Log.i("wmk", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}
