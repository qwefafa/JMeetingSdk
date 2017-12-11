package cn.redcdn.hnyd.im.work.collection;

import cn.redcdn.hnyd.im.util.xutils.http.client.RequestParams;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/8.
 */

public class GetInterfaceParams {

    //////////////////////////////////收藏分享功能   开始/////////////////////////////
    public static RequestParams getAddCollectionItemParams(String nubeNumber, String token, String id, String data){
        CustomLog.d("GetInterfaceParams"," 获取 添加收藏记录 的请求参数：nubeNumber=" + nubeNumber + "|id=" + id+ "|token=" + token+ "|data=" + data);
        RequestParams params = new RequestParams();
        params.addBodyParameter("service", "addItem");
        params.addBodyParameter("nubeNumber", nubeNumber);
        params.addBodyParameter("token", token);
        params.addBodyParameter("id", id);
        params.addBodyParameter("data", data);
        return params;
    }

    public static RequestParams getDeleteCollectionItemParams(String nubeNumber,String token,String id){
        CustomLog.d("GetInterfaceParams"," 获取 删除收藏记录 的请求参数：nubeNumber=" + nubeNumber + "|id=" + id+ "|token=" + token);
        RequestParams params = new RequestParams();
        params.addBodyParameter("service", "deleteItem");
        params.addBodyParameter("nubeNumber", nubeNumber);
        params.addBodyParameter("token", token);
        params.addBodyParameter("id", id);
        return params;
    }

    public static RequestParams getQueryCollectionItemParams(String nubeNumber,String token,String startTime){
        CustomLog.d("GetInterfaceParams"," 获取 查询收藏记录 的请求参数：nubeNumber=" + nubeNumber + "|startTime=" + startTime+ "|token=" + token);
        RequestParams params = new RequestParams();
        params.addBodyParameter("service", "getItems");
        params.addBodyParameter("nubeNumber", nubeNumber);
        params.addBodyParameter("token", token);
        params.addBodyParameter("startTime", startTime);
        return params;
    }

}
