package com.warehouse.twin.algorithm;

import com.warehouse.twin.dto.DetectionRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Uses an eight-frame evidence window and gated-style weighting inspired by the multimodal model. */
@Service
public class InteractionFusionService {
    private static final int WINDOW=8;
    private final Map<Long,Deque<Evidence>> windows=new ConcurrentHashMap<>();

    public synchronized Decision observe(Long slotId,double personConfidence,double gripperConfidence,DetectionRequest.InteractionContext context){
        Deque<Evidence> q=windows.computeIfAbsent(slotId,k->new ArrayDeque<>());q.addLast(new Evidence(personConfidence,gripperConfidence));while(q.size()>WINDOW)q.removeFirst();
        double personVisual=visualScore(q,true),gripperVisual=visualScore(q,false);
        double externalPerson=context==null||context.personActionScore()==null?personVisual:context.personActionScore();
        double externalGripper=context==null||context.gripperActionScore()==null?gripperVisual:context.gripperActionScore();
        double gate=context==null||context.fusionConfidence()==null?.3:Math.min(.5,context.fusionConfidence()*.5);
        double person=(1-gate)*personVisual+gate*externalPerson,gripper=(1-gate)*gripperVisual+gate*externalGripper;
        boolean p=person>=.45,g=gripper>=.45;String actor=p&&g?"PERSON_AND_GRIPPER":p?"PERSON":g?"GRIPPER":"UNKNOWN";
        return new Decision(actor,round(person),round(gripper),round(Math.max(person,gripper)));
    }
    private double visualScore(Deque<Evidence> q,boolean person){double present=q.stream().filter(x->(person?x.person:x.gripper)>.2).count()/(double)WINDOW;double peak=q.stream().mapToDouble(x->person?x.person:x.gripper).max().orElse(0);return .6*present+.4*peak;}
    private double round(double value){return Math.round(value*10000d)/10000d;}
    private record Evidence(double person,double gripper){}
    public record Decision(String actorType,double personScore,double gripperScore,double confidence){
        public String description(boolean takeOut){String action=takeOut?"取走":"放置";return switch(actorType){case "PERSON"->"融合判定：人员"+action;case "GRIPPER"->"融合判定：机械爪"+action;case "PERSON_AND_GRIPPER"->"融合判定：人员与机械爪共同"+action;default->"未识别"+action+"执行者";};}
    }
}
