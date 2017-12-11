package cn.redcdn.hnyd.im.fileTask;

import cn.redcdn.hnyd.im.bean.FileTaskBean;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ChangeUIInterface {

    public void onStart(FileTaskBean bean) {
    }


    public void onProcessing(FileTaskBean bean,long current, long total) {
    }


    public void onSuccess(FileTaskBean bean,String result) {
    }


    public void onFailure(FileTaskBean bean,Throwable error, String msg) {
    }
}
