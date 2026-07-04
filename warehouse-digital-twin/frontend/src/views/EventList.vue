<script setup>
import { onMounted, ref } from "vue";
import { eventApi } from "../api";
import { ElMessage } from "element-plus";
const rows = ref([]),
  dialog = ref(false),
  current = ref(),
  form = ref({ decision: "CONFIRMED", personId: "", points: 0, warningLevel: "提醒", reviewer: "", remark: "" });
const actorLabel = (x) =>
  ({ PERSON: "人员", GRIPPER: "机械爪", PERSON_AND_GRIPPER: "人员+机械爪", UNKNOWN: "未知" })[x] || "-";
async function load() {
  rows.value = await eventApi.list();
}
function open(row) {
  current.value = row;
  dialog.value = true;
  form.value = { decision: "CONFIRMED", personId: "", points: 0, warningLevel: "提醒", reviewer: "", remark: "" };
}
async function review() {
  await eventApi.review(current.value.id, form.value);
  ElMessage.success("复核记录已提交");
  dialog.value = false;
  load();
}
onMounted(load);
</script>
<template>
  <article class="panel">
    <div class="panel-title">
      <h2>取放事件记录</h2>
      <el-tag>AI 不直接执行奖惩</el-tag>
    </div>
    <el-table :data="rows" stripe
      ><el-table-column prop="eventTime" label="时间" width="180" /><el-table-column prop="eventType" label="事件"
        ><template #default="s"
          ><el-tag
            :type="s.row.eventType === 'PUT_IN' ? 'success' : s.row.eventType === 'TAKE_OUT' ? 'warning' : 'info'"
            >{{ s.row.eventType }}</el-tag
          ></template
        ></el-table-column
      ><el-table-column prop="cameraId" label="摄像头" /><el-table-column prop="slotId" label="库位" /><el-table-column
        prop="objectType"
        label="目标"
      /><el-table-column label="执行者"
        ><template #default="s"
          ><el-tag>{{ actorLabel(s.row.actorType) }}</el-tag></template
        ></el-table-column
      ><el-table-column prop="confidence" label="置信度" /><el-table-column
        prop="status"
        label="状态"
      /><el-table-column label="操作"
        ><template #default="s"
          ><el-button v-if="s.row.status === 'PENDING'" link type="primary" @click="open(s.row)"
            >人工复核</el-button
          ></template
        ></el-table-column
      ></el-table
    >
  </article>
  <el-dialog v-model="dialog" title="事件与奖惩复核" width="520"
    ><el-alert
      title="必须结合录像、领料单和人员身份复核；模型结果不能直接作为处罚决定。"
      type="warning"
      :closable="false"
    /><el-form label-position="top" style="margin-top: 15px"
      ><div class="form-row">
        <el-form-item label="结论"
          ><el-select v-model="form.decision"
            ><el-option label="确认事件" value="CONFIRMED" /><el-option
              label="识别误报"
              value="DISMISSED" /></el-select></el-form-item
        ><el-form-item label="人员编号"><el-input v-model="form.personId" /></el-form-item
        ><el-form-item label="积分（负数处罚）"
          ><el-input-number v-model="form.points" :min="-100" :max="100" /></el-form-item
        ><el-form-item label="警告等级"
          ><el-select v-model="form.warningLevel"
            ><el-option v-for="x in ['提醒', '警告', '严重警告']" :key="x" :value="x" /></el-select
        ></el-form-item>
      </div>
      <el-form-item label="复核人"><el-input v-model="form.reviewer" /></el-form-item
      ><el-form-item label="依据与说明"><el-input v-model="form.remark" type="textarea" /></el-form-item></el-form
    ><template #footer
      ><el-button @click="dialog = false">取消</el-button
      ><el-button type="primary" @click="review">提交复核</el-button></template
    ></el-dialog
  >
</template>
