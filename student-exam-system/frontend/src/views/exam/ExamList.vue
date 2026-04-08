<template>
  <div class="exam-list">
    <div class="header">
      <h2>考试列表</h2>
    </div>

    <el-table :data="exams" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="paperName" label="试卷名称" />
      <el-table-column prop="startTime" label="开始时间" />
      <el-table-column prop="endTime" label="结束时间" />
      <el-table-column prop="duration" label="时长(分钟)" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            size="small" 
            :disabled="row.status !== 1"
            @click="handleStartExam(row)"
          >
            开始考试
          </el-button>
          <el-button size="small" @click="handleViewResult(row)">查看成绩</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchExams"
      @current-change="fetchExams"
      style="margin-top: 20px; justify-content: flex-end"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const loading = ref(false)
const exams = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return texts[status] || '未知'
}

const fetchExams = async () => {
  loading.value = true
  try {
    const res = await api.getExamList(currentPage.value, pageSize.value)
    exams.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

const handleStartExam = (row) => {
  router.push(`/exam/take/${row.id}`)
}

const handleViewResult = (row) => {
  router.push(`/result/list?examId=${row.id}`)
}

onMounted(() => {
  fetchExams()
})
</script>

<style scoped>
.exam-list {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}
</style>
