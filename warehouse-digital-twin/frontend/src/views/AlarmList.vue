<script setup>
import { onMounted, ref } from "vue";
import { alarmApi } from "../api";
import { ElMessage, ElMessageBox } from "element-plus";
const rows = ref([]);
async function load() {
  rows.value = await alarmApi.list();
}
async function handle(row) {
  const { value } = await ElMessageBox.prompt("填写处理说明", "处理告警");
  await alarmApi.handle(row.id, { handler: "当前管理员", remark: value });
  ElMessage.success("告警已处理");
  load();
}
onMounted(load);
</script>
<template>
  <article class="panel">
    <div class="panel-title">
      <h2>安全风险告警</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" stripe
      ><el-table-column prop="alarmType" label="告警类型" /><el-table-column
        prop="location"
        label="位置"
      /><el-table-column prop="level" label="等级"
        ><template #default="s"
          ><el-tag :type="s.row.level === 'CRITICAL' ? 'danger' : 'warning'">{{ s.row.level }}</el-tag></template
        ></el-table-column
      ><el-table-column prop="triggerTime" label="触发时间" /><el-table-column
        prop="status"
        label="状态"
      /><el-table-column prop="handler" label="处理人" /><el-table-column label="操作"
        ><template #default="s"
          ><el-button v-if="s.row.status !== 'HANDLED'" link type="primary" @click="handle(s.row)"
            >处理</el-button
          ></template
        ></el-table-column
      ></el-table
    >
  </article>
</template>
