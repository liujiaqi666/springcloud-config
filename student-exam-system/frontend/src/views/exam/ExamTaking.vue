<template>
  <div class="exam-taking">
    <div class="exam-header">
      <div class="info">
        <h2>{{ examInfo.paperName }}</h2>
        <p>剩余时间：<span class="timer">{{ formatTime(remainingTime) }}</span></p>
      </div>
      <el-button type="primary" @click="submitExam" :loading="submitting">提交试卷</el-button>
    </div>

    <el-divider />

    <div class="questions">
      <el-card v-for="(question, index) in questions" :key="question.id" class="question-card">
        <template #header>
          <div class="question-header">
            <span class="question-title">第 {{ index + 1 }} 题 ({{ question.score }}分)</span>
            <el-tag :type="getTypeByType(question.type)">{{ getTypeText(question.type) }}</el-tag>
          </div>
        </template>

        <div class="question-content">
          <p class="question-text">{{ question.content }}</p>

          <!-- 单选题/多选题 -->
          <div v-if="question.type === 1 || question.type === 2" class="options">
            <el-checkbox-group 
              v-if="question.type === 2" 
              v-model="answers[question.id]"
            >
              <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                <el-checkbox :label="String.fromCharCode(65 + optIndex)">
                  {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <el-radio-group v-else v-model="answers[question.id]">
              <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                <el-radio :label="String.fromCharCode(65 + optIndex)">
                  {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                </el-radio>
              </div>
            </el-radio-group>
          </div>

          <!-- 判断题 -->
          <div v-else-if="question.type === 3" class="judgment">
            <el-radio-group v-model="answers[question.id]">
              <el-radio :label="1">正确</el-radio>
              <el-radio :label="0">错误</el-radio>
            </el-radio-group>
          </div>

          <!-- 填空题 -->
          <div v-else-if="question.type === 4" class="fill-blank">
            <el-input 
              v-model="answers[question.id]" 
              placeholder="请输入答案"
              style="width: 300px"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="question.type === 5" class="short-answer">
            <el-input 
              v-model="answers[question.id]" 
              type="textarea" 
              :rows="4"
              placeholder="请输入答案"
            />
          </div>
        </div>
      </el-card>
    </div>

    <div class="submit-bar">
      <el-button type="primary" size="large" @click="submitExam" :loading="submitting">
        提交试卷
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

const examInfo = ref({})
const questions = ref([])
const answers = reactive({})
const remainingTime = ref(0)
const submitting = ref(false)
let timer = null

const getTypeText = (type) => {
  const texts = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
  return texts[type] || '未知'
}

const getTypeByType = (type) => {
  const types = { 1: '', 2: 'warning', 3: 'success', 4: 'info', 5: 'danger' }
  return types[type] || ''
}

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const loadExam = async () => {
  try {
    const res = await api.startExam(examId)
    examInfo.value = res.data.examInfo
    questions.value = res.data.questions
    remainingTime.value = res.data.duration * 60
    
    // 初始化答案
    questions.value.forEach(q => {
      answers[q.id] = q.type === 2 ? [] : ''
    })
    
    // 启动倒计时
    startTimer()
  } catch (error) {
    ElMessage.error('加载考试失败')
    router.push('/exam/list')
  }
}

const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('考试时间已到，自动提交试卷')
      submitExam()
    }
  }, 1000)
}

const submitExam = async () => {
  if (submitting.value) return
  
  // 检查是否有未答题
  const unanswered = questions.value.filter(q => {
    const answer = answers[q.id]
    if (q.type === 2) return answer.length === 0
    return !answer || answer.trim() === ''
  })
  
  if (unanswered.length > 0) {
    try {
      await ElMessageBox.confirm(`还有 ${unanswered.length} 道题未作答，确定要提交吗？`, '提示', { type: 'warning' })
    } catch {
      return
    }
  }
  
  submitting.value = true
  try {
    const answerList = Object.keys(answers).map(id => ({
      questionId: parseInt(id),
      answer: Array.isArray(answers[id]) ? answers[id].join(',') : answers[id]
    }))
    
    await api.submitExam(examId, answerList)
    ElMessage.success('交卷成功')
    router.push('/result/list')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadExam()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.exam-taking {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info h2 {
  margin: 0 0 10px 0;
}

.timer {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-title {
  font-weight: bold;
}

.question-text {
  font-size: 16px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.option-item {
  margin: 10px 0;
}

.submit-bar {
  text-align: center;
  margin-top: 30px;
  padding: 20px;
  border-top: 1px solid #eee;
}
</style>
