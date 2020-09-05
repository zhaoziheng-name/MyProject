package niko.mapper;

import niko.base.BaseMapper;
import niko.model.Setting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettingMapper extends BaseMapper<Setting> {
}