package com.game.sdk.floatwindow;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.game.sdk.log.L;

/**
 * Created by liu hong liang on 2017/5/15.
 * 新版方向传感器实现
 */
public final class OrientationSensorManager implements SensorEventListener {
    public static OrientationSensorManager instance;
    private SensorManager mSensorManager;
    private Sensor accSensor;
    private Sensor manSensor;
    //用于计算的值
    float[] values = new float[3];
    float[] R = new float[9];
    //监听获得值
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    private OrientationSensorEventListener mOrientationListener = null;
    private PhoneReversalListener phoneReversalListener = null;
    private float beforeZvalue;
    private long recordTime;

    public OrientationSensorEventListener getListener() {
        return mOrientationListener;
    }

    public void setListener(OrientationSensorEventListener mListener) {
        this.mOrientationListener = mListener;
    }

    public PhoneReversalListener getPhoneReversalListener() {
        return phoneReversalListener;
    }

    public void setPhoneReversalListener(PhoneReversalListener phoneReversalListener) {
        this.phoneReversalListener = phoneReversalListener;
    }

    private OrientationSensorManager(Context context) {
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        //加速度感应器
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //地磁感应器
        manSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public synchronized static OrientationSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrientationSensorManager(context);
        }
        return instance;
    }

    //再次强调：注意activity暂停的时候释放
    public void onPause() {
        if(mSensorManager!=null){
            mSensorManager.unregisterListener(this);
        }
    }

    public void onResume() {
        //注册监听和监测的速率
        if((mOrientationListener !=null||phoneReversalListener!=null)&&mSensorManager!=null){
            mSensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, manSensor, SensorManager.SENSOR_DELAY_UI);
            Log.e("hongliang","注册了监听");
        }
    }

    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        mSensorManager = null;
        instance = null;
        mOrientationListener = null;
        phoneReversalListener=null;
        L.e("hongliang","重力感应注销");
    }

    private void calculateOrientation() {
        if(SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)){
            values= SensorManager.getOrientation(R, values);
            if(values!=null){
                values[0] = (float) Math.toDegrees(values[0]);
                values[1] = (float) Math.toDegrees(values[1]);
                values[2] = (float) Math.toDegrees(values[2]);
                if (mOrientationListener != null) {
                    mOrientationListener.onSensorChanged(values);
                }
                if(phoneReversalListener!=null){
                    if(System.currentTimeMillis()-recordTime<=200){ //记录时差200ms
                        if(Math.abs(180- Math.abs(values[2]))<=25) { //接近180
                            if(Math.abs((Math.abs(values[2])-beforeZvalue))>=20) {//与上次差距超过20
                                phoneReversalListener.onReversalChanged();
                            }
                            recordTime= System.currentTimeMillis();
                            beforeZvalue= Math.abs(values[2]);
                        }else {
                            recordTime= System.currentTimeMillis();
                            beforeZvalue= Math.abs(values[2]);
                        }
                    }else {
                        recordTime= System.currentTimeMillis();
                        beforeZvalue= Math.abs(values[2]);
                    }
                }
            }
        }
    }

    /**
     * 数值变化
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }
        calculateOrientation();
    }

    /**
     * 精度变化
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    /**
     * 方向传感器监听器
     */
    public interface OrientationSensorEventListener {
        /**
         * 方向传感器改变回调通知
         *
         * @param values 长度为3的数组，方别记录x,y,z的改变
         */
        void onSensorChanged(float[] values);
    }

    public interface PhoneReversalListener{
        /**
         * 屏幕翻转
         */
        void onReversalChanged();
    }
}
