package com.warehouse.twin.algorithm;
import org.springframework.beans.factory.annotation.Value; import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.stereotype.Service; import java.util.*; import java.util.concurrent.*;
@Service
public class StateWindowService {
    private static final int WINDOW=8,THRESHOLD=6; private final Map<Long,Deque<Boolean>> local=new ConcurrentHashMap<>(); private final StringRedisTemplate redis; private final boolean redisEnabled;
    public StateWindowService(StringRedisTemplate redis,@Value("${warehouse.state.redis-enabled:false}") boolean enabled){this.redis=redis;this.redisEnabled=enabled;}
    public synchronized Optional<Boolean> append(Long slotId,boolean occupied){
        Deque<Boolean> q=local.computeIfAbsent(slotId,k->new ArrayDeque<>());q.addLast(occupied);while(q.size()>WINDOW)q.removeFirst();
        if(redisEnabled)try{String key="warehouse:slot:window:"+slotId;redis.opsForList().rightPush(key,occupied?"1":"0");redis.opsForList().trim(key,-WINDOW,-1);}catch(Exception ignored){}
        if(q.size()<WINDOW)return Optional.empty();long yes=q.stream().filter(Boolean::booleanValue).count();if(yes>=THRESHOLD)return Optional.of(true);if(WINDOW-yes>=THRESHOLD)return Optional.of(false);return Optional.empty();
    }
    public int evidenceCount(Long slotId,boolean value){return (int)local.getOrDefault(slotId,new ArrayDeque<>()).stream().filter(x->x==value).count();}
}
