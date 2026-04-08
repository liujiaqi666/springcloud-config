<template>
  <div class="result-list">
    <div class="header">
      <h2>考试成绩</h2>
    </div>

    <el-table :data="results" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="examName" label="考试名称" />
      <el-table-column prop="paperName" label="试卷名称" />
      <el-table-column prop="score" label="得分" />
      <el-table-column prop="totalScore" label="总分" />
      <el-table-column prop="submitTime" label="提交时间" />
      <el-table-column label="详情">
        <template #default="{ row }">
          <el-button size="small" @click="handleViewDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchResults"
      @current-change="fetchResults"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 成绩详情对话框 -->
    <el-dialog v-model="detailVisible" title="成绩详情" width="800px">
      <div v-if="selectedResult" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试名称">{{ selectedResult.examName }}</el-descriptions-item>
          <el-descriptions-item label="试卷名称">{{ selectedResult.paperName }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ selectedResult.score }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ selectedResult.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="正确率">
            {{ ((selectedResult.score / selectedResult.totalScore) * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ selectedResult.submitTime }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px">答题情况</h3>
        <el-table :data="selectedResult.questionDetails" style="margin-top: 10px">
          <el-table-column prop="questionContent" label="题目" show-overflow-tooltip />
          <el-table-column prop="userAnswer" label="你的答案" width="120" />
          <el-table-column prop="correctAnswer" label="正确答案" width="120" />
          <el-table-column prop="score" label="得分" width="80" />
          <el-table-column prop="questionScore" label="题目总分" width="80" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()
const loading = ref(false)
const results = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const selectedResult = ref(null)

const fetchResults = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (route.query.examId) {
      params.examId = route.query.examId
    }
    const res = await api.getResultList(params)
    results.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (row) => {
  try {
    const res = await api.getResultDetail(row.id)
    selectedResult.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  fetchResults()
})
</script>

<style scoped>
.result-list {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}
</style>
