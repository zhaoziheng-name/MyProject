package niko.mapper;

import niko.base.BaseMapper;
import niko.model.Record;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
}