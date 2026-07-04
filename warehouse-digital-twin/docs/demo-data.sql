USE warehouse_twin;

INSERT INTO warehouse_event(event_type,camera_id,slot_id,object_type,actor_type,confidence,image_url,event_time,status,remark)
SELECT 'PUT_IN',1,1,'carton','GRIPPER',0.93000,'',NOW(),'CONFIRMED','规范放置'
WHERE NOT EXISTS (SELECT 1 FROM warehouse_event WHERE remark='规范放置');

INSERT INTO warehouse_event(event_type,camera_id,slot_id,object_type,actor_type,confidence,image_url,event_time,status,remark)
SELECT 'TAKE_OUT',1,2,'box','PERSON_AND_GRIPPER',0.88000,'',DATE_SUB(NOW(),INTERVAL 15 MINUTE),'PENDING','等待人工复核'
WHERE NOT EXISTS (SELECT 1 FROM warehouse_event WHERE remark='等待人工复核');

INSERT INTO alarm(alarm_type,location,level,trigger_time,image_url,status,remark)
SELECT 'PERSON_FORKLIFT_PROXIMITY','A区主通道','HIGH',NOW(),'','PENDING','人员与叉车距离过近'
WHERE NOT EXISTS (SELECT 1 FROM alarm WHERE alarm_type='PERSON_FORKLIFT_PROXIMITY' AND status='PENDING');

INSERT INTO person_state(person_id,camera_id,state,risk_score,visual_weight,audio_weight,text_weight,behavior_weight,environment_weight,`timestamp`)
SELECT 'P001','CAM_02','FATIGUE',0.8200,0.4500,0.2500,0.1000,0.2000,0.0000,NOW()
WHERE NOT EXISTS (SELECT 1 FROM person_state WHERE person_id='P001');
