package cn.redcdn.hnyd.contacts;

import cn.redcdn.hnyd.util.SideBase;

/**
 * Created by Administrator on 2017/4/12 0012.
 */

public abstract class LetterInfo implements SideBase {

    public String letter;

    public void setLetter(String Letter){
        this.letter = Letter;
    }

    @Override
    public String getLetterName() {
        return this.letter;
    }
}
