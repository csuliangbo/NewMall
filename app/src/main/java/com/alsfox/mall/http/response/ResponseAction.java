package com.alsfox.mall.http.response;


import com.alsfox.mall.http.HttpBean;
import com.alsfox.mall.http.request.RequestAction;

/**
 * Created by Luhao on 2016/6/24.
 * 网络请求返回参照类
 */
public class ResponseAction<T> {
    private RequestAction requestAction;//请求枚举
    private Throwable throwable;//Retrofit报错
    private String errorMessage;//错误信息
    private HttpBean<T> httpBean;//返回的结果，直接被Retrofit解析过了
    private int requestCode;//非服务器返回的错误码

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public HttpBean<T> getHttpBean() {
        return httpBean;
    }

    public void setHttpBean(HttpBean<T> httpBean) {
        this.httpBean = httpBean;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public RequestAction getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(RequestAction requestAction) {
        this.requestAction = requestAction;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
