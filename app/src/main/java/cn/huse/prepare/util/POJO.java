package cn.huse.prepare.util;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "pojo")
public class POJO {
    @Column(name = "id",isId = true)
    private int id;
    @Column(name = "value")
    private String value;
    @Column(name = "time")
    private String time;
}
