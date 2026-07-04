<script setup>
import { onMounted } from "vue";
import { useRealtimeStore } from "./store";
const realtime = useRealtimeStore();
onMounted(realtime.connect);
const menus = [
  ["/", "◫", "驾驶舱"],
  ["/camera", "◉", "实时识别"],
  ["/slots", "▦", "库位管理"],
  ["/events", "⇄", "取放事件"],
  ["/alarms", "△", "告警管理"],
  ["/twin", "◇", "数字孪生"],
  ["/persons", "♙", "人员状态"],
];
</script>
<template>
  <div class="layout">
    <aside>
      <div class="brand">
        <b>仓</b><span>智仓孪生<small>WAREHOUSE TWIN</small></span>
      </div>
      <nav>
        <router-link v-for="m in menus" :key="m[0]" :to="m[0]"
          ><i>{{ m[1] }}</i
          >{{ m[2] }}</router-link
        >
      </nav>
      <div class="online">
        <i :class="{ off: !realtime.connected }"></i>{{ realtime.connected ? "实时通道在线" : "实时通道重连中" }}
      </div>
    </aside>
    <main>
      <header>
        <div>
          <small>DIGITAL TWIN CONTROL</small>
          <h1>{{ $route.meta.title }}</h1>
        </div>
        <el-tag :type="realtime.connected ? 'success' : 'warning'">{{
          realtime.connected ? "WebSocket 在线" : "连接中"
        }}</el-tag>
      </header>
      <router-view />
    </main>
  </div>
</template>
