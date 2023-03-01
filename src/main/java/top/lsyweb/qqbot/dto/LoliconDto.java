package top.lsyweb.qqbot.dto;

import lombok.Data;

import java.util.List;

/**
 * lolicon涩图API返回对象
 */
@Data
public class LoliconDto {
    private int pid;

    private int p;

    private int uid;

    private String title;

    private String author;

    private boolean r18;

    private Integer width;

    private Integer height;

    private List<String> tags;

    private String ext;

    private Integer uploadDate;

    private String urls;
}
