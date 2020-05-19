package com.rafel.eblog.im.message;

import com.rafel.eblog.im.vo.ImTo;
import com.rafel.eblog.im.vo.ImUser;
import lombok.Data;

@Data
public class ChatImMess {

    private ImUser mine;
    private ImTo to;

}
