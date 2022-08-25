package com.hua.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

/**
 * @create 2022-04-07 0:20
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) //加载配置类
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * String类型测试
     */
    @Test
    public void testStrings() {
        String redisKey = "test:count";

        //设置String类型数据
        redisTemplate.opsForValue().set(redisKey, 1);
        //获取数据
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        //增加value的值
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        //减少value的值
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    /**
     * Hash类型测试
     */
    @Test
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    /**
     * List类型测试
     */
    @Test
    public void testLists() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    /**
     * Set类型测试
     */
    @Test
    public void testSets(){
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "嬴政", "韩非", "卫庄", "李斯", "盖聂");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    /**
     * SortSet类型测试
     */
    @Test
    public void testSortSets(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 90);
        redisTemplate.opsForZSet().add(redisKey, "孙悟空", 60);
        redisTemplate.opsForZSet().add(redisKey, "猪八戒", 80);
        redisTemplate.opsForZSet().add(redisKey, "沙和尚", 75);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 40);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "猪八戒"));
        //（默认是升序）降序排序，获取唐僧的排名 排名从0开始
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "唐僧"));
        //（默认是升序）降序排序，获取前三名
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));

    }

    /**
     * key测试
     */
    @Test
    public void testKeys(){

        //删除key
        redisTemplate.delete("test:user");

        //判断key是否存在
        System.out.println(redisTemplate.hasKey("test:user"));

        //设置key的过期时间为10 单位为second
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    /**
     * 当操作一个key时，可以将key绑定到对象上，简化操作
     */
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";

        //绑定key
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);

        //BoundHashOperations operations = redisTemplate.boundHashOps(redisKey);
        //BoundListOperations operations = redisTemplate.boundListOps(redisKey);
        //BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        //BoundZSetOperations operations = redisTemplate.boundZSetOps(redisKey);

        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());

    }

    /**
     * Redis编程式事务
     */
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String redisKey = "test:tx";

                //开启事务
                operations.multi();

                //向redis数据库插入数据，由于开启了事务，命令并不会立即执行，
                // 而是放在一个队列了，等事务提交的时候一并执行
                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                //试图读取为提交事务的数据
                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });

        System.out.println(obj);
    }

    /**
     * 统计20万个重复数据的独立总数
     */
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";

        //添加二十万条重复数据
        for (int i = 0; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 0; i < 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);

    }

    /**
     * 将三组数据合并，在统计合并后的重复数据的独立总数
     */
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);   //查询不重复的个数 19891（正确是20000， redis这个数据结构得到的只是大概的值，有误差）
        System.out.println(size);
    }


    /**
     * HyperLogLog: 里面存储不重复的数据
     */
    @Test
    public void testHyperLogLog2(){
        String redisKey2 = "test:hll:08";
        int n = 1;
        for (int i = 1; i <= 10000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, n);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey2);   //查询不重复的个数 19891（正确是20000， redis这个数据结构得到的只是大概的值，有误差）
        System.out.println(size);   //结果：1
    }

    /**
     * 统计一组数据的布尔值
     */
    @Test
    public void testMitMap(){
        String redisKey = "test:bm:01";

        //记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 6, true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));    //false
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));    //false

        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);    //3：有三个为true
    }

    //统计3组数据的布尔值，并对这3组数据做OR运算
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 2, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);    //7: 从0-6 为true

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));    //true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));    //true
    }
}
