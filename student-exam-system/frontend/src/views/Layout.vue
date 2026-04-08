<template>
  <div class="layout">
    <el-container>
      <el-header class="header">
        <div class="logo">学生考试系统</div>
        <div class="menu">
          <el-menu mode="horizontal" :router="true" :default-active="$route.path" background-color="#545c64" text-color="#fff" active-text-color="#ffd04b">
            <el-menu-item index="/exam/list">考试列表</el-menu-item>
            <el-menu-item index="/paper/list">试卷管理</el-menu-item>
            <el-menu-item index="/result/list">成绩查询</el-menu-item>
          </el-menu>
        </div>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              {{ userStore.userInfo?.username || '用户' }}
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  background-color: #545c64;
  padding: 0 20px;
}

.logo {
  color: white;
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
}

.menu {
  flex: 1;
}

.el-menu {
  border-bottom: none;
}

.user-info {
  color: white;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main {
  background-color: #f5f7fa;
}
</style>
