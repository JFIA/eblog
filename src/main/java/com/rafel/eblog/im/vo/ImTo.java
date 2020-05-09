package com.rafel.eblog.im.vo;

import lombok.Data;

@Data
public class ImTo {

    private Long id;
    private String username;
    private String type;
    private String avatar;
    private Integer members;

}
