<template>
  <div class="dashboard">
    <h2 class="welcome">欢迎回来，{{ userStore.userInfo?.realName }}</h2>
    <el-row :gutter="20" class="stats">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon :size="48" :color="item.color"><component :is="item.icon" /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Avatar, School, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const stats = ref([
  { title: '学生总数', icon: 'User', color: '#409eff', value: 120 },
  { title: '教师总数', icon: 'Avatar', color: '#67c23a', value: 30 },
  { title: '班级总数', icon: 'School', color: '#e6a23c', value: 8 },
  { title: '课程总数', icon: 'Reading', color: '#f56c6c', value: 24 }
])
</script>

<style scoped>
.welcome {
  margin-bottom: 20px;
}

.stats {
  margin-top: 20px;
}

.stat-item {
  display: flex;
  gap: 20px;
  align-items: center;
  padding: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-title {
  color: #999;
  font-size: 14px;
}
</style>
