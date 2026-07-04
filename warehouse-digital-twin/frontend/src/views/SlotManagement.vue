<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { slotApi } from '../api'

const rows = ref([])
const dialog = ref(false)
const current = ref(null)
const form = ref({ status: 'EMPTY', cargoType: '', operator: '', remark: '' })

async function load() {
  rows.value = await slotApi.list()
}

function open(row) {
  current.value = row
  form.value = {
    status: row.status,
    cargoType: row.cargoType || '',
    operator: '',
    remark: '',
  }
  dialog.value = true
}

async function correct() {
  await slotApi.correct(current.value.id, form.value)
  ElMessage.success('修正已记录并推送')
  dialog.value = false
  await load()
}

onMounted(load)
</script>

<template>
  <article class="panel">
    <div class="panel-title">
      <h2>库位状态与区域标定</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="code" label="库位编号" />
      <el-table-column prop="cameraId" label="摄像头 ID" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'OCCUPIED' ? 'success' : scope.row.status === 'ABNORMAL' ? 'danger' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cargoType" label="货物类型" />
      <el-table-column prop="polygon" label="标定 Polygon" show-overflow-tooltip />
      <el-table-column prop="lastUpdateTime" label="最后更新" />
      <el-table-column label="操作" width="110">
        <template #default="scope">
          <el-button link type="primary" @click="open(scope.row)">人工修正</el-button>
        </template>
      </el-table-column>
    </el-table>
  </article>

  <el-dialog v-model="dialog" title="人工修正库位" width="480">
    <el-form label-position="top">
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option v-for="item in ['EMPTY', 'OCCUPIED', 'ABNORMAL']" :key="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="货物类型"><el-input v-model="form.cargoType" /></el-form-item>
      <el-form-item label="操作人"><el-input v-model="form.operator" /></el-form-item>
      <el-form-item label="修正原因"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="correct">确认修正</el-button>
    </template>
  </el-dialog>
</template>
