package com.alsfox.mall.appliaction;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alsfox.mall.R;
import com.alsfox.mall.constances.AppConstant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yehuijifeng
 * on 2015/11/26.
 * 全局捕获异常类
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    //程序的Context对象
    private Context context;

    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return CrashHandlers.mvpAppliaction;
    }

    private static class CrashHandlers {
        private static final CrashHandler mvpAppliaction = new CrashHandler();
    }


    /**
     * 初始化
     */
    public void init(Context context) {
        this.context = context;
        formatter = new SimpleDateFormat(context.getResources().getString(R.string.default_time_type));
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppConstant.TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(AppConstant.TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(AppConstant.TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                /**
                 * 这里换成自定义路径
                 */
                String path = AppConstant.LOG_PATH;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(AppConstant.TAG, "insert file error!", e);
        }
        return null;
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, context.getResources().getString(R.string.error_exit_app), Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        //收集设备参数信息
        collectDeviceInfo(context);
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(AppConstant.TAG, "error : ", e);
            }

//            Intent intent = new Intent(context, HomeActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent restartIntent = PendingIntent.getActivity(
//                    context, 0, intent, 0);
//            //退出程序
//            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//            ActivityCollector.finishAll();

//            //退出程序
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(1);
        }
    }
}
