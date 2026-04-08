<template>
  <div class="paper-list">
    <div class="header">
      <h2>试卷管理</h2>
      <el-button type="primary" @click="handleAdd">新增试卷</el-button>
    </div>

    <el-table :data="papers" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="试卷名称" />
      <el-table-column prop="subject" label="科目" />
      <el-table-column prop="totalScore" label="总分" />
      <el-table-column prop="duration" label="考试时长(分钟)" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchPapers"
      @current-change="fetchPapers"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑试卷' : '新增试卷'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="试卷名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-input v-model="form.subject" placeholder="请输入科目" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="0" :max="1000" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="10" :max="300" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const submitting = ref(false)
const papers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  subject: '',
  totalScore: 100,
  duration: 90,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subject: [{ required: true, message: '请输入科目', trigger: 'blur' }]
}

const fetchPapers = async () => {
  loading.value = true
  try {
    const res = await api.getPaperList(currentPage.value, pageSize.value)
    papers.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', subject: '', totalScore: 100, duration: 90, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await api.updatePaper(form)
        } else {
          await api.addPaper(form)
        }
        ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
        dialogVisible.value = false
        fetchPapers()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该试卷吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await api.deletePaper(row.id)
        ElMessage.success('删除成功')
        fetchPapers()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchPapers()
})
</script>

<style scoped>
.paper-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
