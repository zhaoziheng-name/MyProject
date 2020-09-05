package niko.mapper;

import niko.base.BaseMapper;
import niko.model.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}