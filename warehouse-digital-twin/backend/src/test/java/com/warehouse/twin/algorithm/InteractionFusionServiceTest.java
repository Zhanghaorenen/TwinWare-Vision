package com.warehouse.twin.algorithm;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InteractionFusionServiceTest {
    @Test void recognizesPersonAndGripperAcrossFrames(){
        var service=new InteractionFusionService();
        InteractionFusionService.Decision decision=null;
        for(int i=0;i<8;i++)decision=service.observe(1L,.91,.88,null);
        assertThat(decision.actorType()).isEqualTo("PERSON_AND_GRIPPER");
        assertThat(decision.personScore()).isGreaterThan(.8);
        assertThat(decision.gripperScore()).isGreaterThan(.8);
    }
}
