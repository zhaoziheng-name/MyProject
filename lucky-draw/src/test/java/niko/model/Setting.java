package niko.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import niko.base.BaseEntity;

/**
 * 抽奖设置
 */
@Getter
@Setter
@ToString
public class Setting extends BaseEntity {
    
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 每次抽奖人数
     */
    private Integer batchNumber;

    /**
     * 创建时间
     */
    private Date createTime;
}