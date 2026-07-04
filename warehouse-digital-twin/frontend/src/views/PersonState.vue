<script setup>
import { onMounted, ref } from "vue";
import { personApi } from "../api";
import { ElMessage } from "element-plus";
const rows = ref([]),
  form = ref({
    personId: "P001",
    cameraId: "CAM_02",
    state: "FATIGUE",
    riskScore: 0.82,
    visualWeight: 0.45,
    audioWeight: 0.25,
    textWeight: 0.1,
    behaviorWeight: 0.2,
    environmentWeight: 0,
  });
async function load() {
  rows.value = await personApi.list();
}
async function simulate() {
  form.value.timestamp = new Date().toISOString().slice(0, 19);
  await personApi.create(form.value);
  ElMessage.success("模拟融合结果已提交");
  load();
}
onMounted(load);
</script>
<template>
  <div class="toolbar"><el-button type="primary" @click="simulate">提交模拟多模态结果</el-button></div>
  <article class="panel">
    <el-table :data="rows"
      ><el-table-column prop="timestamp" label="时间" /><el-table-column prop="personId" label="人员" /><el-table-column
        prop="cameraId"
        label="摄像头" /><el-table-column prop="state" label="融合状态"
        ><template #default="s"
          ><el-tag :type="s.row.state === 'NORMAL' ? 'success' : 'warning'">{{ s.row.state }}</el-tag></template
        ></el-table-column
      ><el-table-column prop="riskScore" label="风险分" /><el-table-column
        prop="visualWeight"
        label="视觉权重" /><el-table-column prop="audioWeight" label="语音权重" /><el-table-column
        prop="behaviorWeight"
        label="行为权重"
    /></el-table>
  </article>
</template>
