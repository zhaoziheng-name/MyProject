package niko.mapper;

import niko.base.BaseMapper;
import niko.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}