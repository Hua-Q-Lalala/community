package com.hua.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hua.community.dao.DiscussPostMapper;
import com.hua.community.entity.DiscussPost;
import com.hua.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @create 2022-03-22 14:49
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;    //过滤敏感词

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口：Cache, LoadingCache, AsyncLoadingCache

    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)   //最大缓存
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)  //在写入之后 设置数据的过期时间
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception { //当缓存为空时，加载缓存
                        if(key == null && key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = key.split(":");
                        if(params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 可以在这里添加二级缓存，当一级缓存为空时，查询二级缓存。 当两级缓存都为空时，再查询数据库
                        //二级缓存： Redis -> mysql

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {//当缓存为空时，加载缓存
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /**
     * 查询所有帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode){
        //if(userId == 0 && orderMode == 1){  //对热门帖子进行缓存
        //    return postListCache.get(offset + ":" + limit);
        //}

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 根据userid查询用户发布了多少条帖子
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId){
        //if(userId == 0){    //当在首页查询帖子总数时缓存
        //    return postRowsCache.get(userId);
        //}

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 插入帖子
     * @param post
     * @return
     */
    public int addDiscussPost(DiscussPost post){
        if (post == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));


        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 根据id查询指定帖子
     * @param postId
     * @return
     */
    public DiscussPost findDiscussPostById(int postId){
        return discussPostMapper.selectDiscussPostById(postId);
    }

    /**
     * 更新帖子评论数量
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     *  根据帖子id修改帖子类型
     *  0：普通； 1：置顶
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 根据帖子id修改帖子状态
     * 0：正常； 1：精华； 2：拉黑
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 更新帖子分数
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }
}

