package com.warehouse.twin.algorithm;
import org.junit.jupiter.api.Test; import org.springframework.data.redis.core.StringRedisTemplate; import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.mock;
class StateWindowServiceTest {@Test void requiresSixOfEightFrames(){var s=new StateWindowService(mock(StringRedisTemplate.class),false);for(int i=0;i<7;i++)assertThat(s.append(1L,i<6)).isEmpty();assertThat(s.append(1L,false)).contains(true);}}
