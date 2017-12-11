package cn.redcdn.menuview.view;

import android.content.Context;

import cn.redcdn.util.MResource;

public abstract class ExitView extends BaseView {
  public ExitView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_exit_view"));
  }
}