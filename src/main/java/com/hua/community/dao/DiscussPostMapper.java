package com.hua.community.dao;

import com.hua.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @create 2022-03-22 14:05
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * 按照用户id查询帖子，userId为0时 查询所有帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    //@Param注解用于给参数别名
    //如果只有一个参数，并且在<IF>里使用，则必须加别名
    /**
     * 按userid统计用户帖子数量
     * userId为0时 统计所有用户帖子
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 插入帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 根据id查询指定帖子
     * @param postId
     * @return
     */
    DiscussPost selectDiscussPostById(int postId);

    /**
     * 更新帖子评论数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id, int commentCount);

    /**
     * 根据帖子id修改帖子类型
     * 0：普通； 1：置顶
     * @param id
     * @param type
     * @return
     */
    int updateType(int id, int type);

    /**
     * 根据帖子id修改帖子状态
     * 0：正常； 1：精华； 2：拉黑
     * @param id
     * @param status
     * @return
     */
    int updateStatus(int id, int status);

    /**
     * 更新帖子分数
     * @param id
     * @param score
     * @return
     */
    int updateScore(int id, double score);
}
