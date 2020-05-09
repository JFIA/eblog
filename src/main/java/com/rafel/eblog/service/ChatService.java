package com.rafel.eblog.service;

import com.rafel.eblog.im.vo.ImMess;
import com.rafel.eblog.im.vo.ImUser;

import java.util.List;

public interface ChatService {
    ImUser getCurrentUser();

    void setGroupHistoryMsg(ImMess responseMess);

    List<Object> getGroupHistoryMsg(int count);
}
