<script setup>
import { onMounted, onUnmounted, ref, watch, nextTick } from "vue";
import * as echarts from "echarts";
import { twinApi } from "../api";
import { useRealtimeStore } from "../store";
const data = ref({
    totalSlots: 0,
    occupiedSlots: 0,
    emptySlots: 0,
    todayPutIn: 0,
    todayTakeOut: 0,
    activeAlarms: 0,
    onlineCameras: 0,
  }),
  chartEl = ref(),
  store = useRealtimeStore();
let chart;
async function load() {
  data.value = await twinApi.state();
  await nextTick();
  chart = echarts.init(chartEl.value);
  chart.setOption({
    tooltip: {},
    color: ["#27a774", "#d9ded9"],
    series: [
      {
        type: "pie",
        radius: ["55%", "78%"],
        label: { formatter: "{b}\n{c}" },
        data: [
          { name: "已占用", value: data.value.occupiedSlots },
          { name: "空闲", value: data.value.emptySlots },
        ],
      },
    ],
  });
}
watch(() => store.lastMessage, load);
onMounted(load);
onUnmounted(() => chart?.dispose());
</script>
<template>
  <section class="cards">
    <article class="card">
      <span>总库位</span><b>{{ data.totalSlots }}</b>
    </article>
    <article class="card">
      <span>已占用 / 空闲</span><b>{{ data.occupiedSlots }} / {{ data.emptySlots }}</b>
    </article>
    <article class="card">
      <span>今日放置 / 取走</span><b>{{ data.todayPutIn }} / {{ data.todayTakeOut }}</b>
    </article>
    <article class="card">
      <span>在线摄像头 / 告警</span><b>{{ data.onlineCameras }} / {{ data.activeAlarms }}</b>
    </article>
  </section>
  <section class="grid-2">
    <article class="panel">
      <div class="panel-title">
        <h2>库位占用态势</h2>
        <el-tag type="success">实时</el-tag>
      </div>
      <div ref="chartEl" style="height: 330px"></div>
    </article>
    <article class="panel">
      <div class="panel-title"><h2>系统状态</h2></div>
      <el-descriptions :column="1" border
        ><el-descriptions-item label="感知服务">运行中</el-descriptions-item
        ><el-descriptions-item label="状态窗口">8 帧 / 6 帧确认</el-descriptions-item
        ><el-descriptions-item label="推送通道">WebSocket</el-descriptions-item
        ><el-descriptions-item label="摄像头形态">仓库固定安装</el-descriptions-item></el-descriptions
      >
    </article>
  </section>
</template>
