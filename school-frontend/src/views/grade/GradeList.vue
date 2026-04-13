<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="学生ID">
          <el-input v-model="queryParams.studentId" clearable />
        </el-form-item>
        <el-form-item label="课程ID">
          <el-input v-model="queryParams.courseId" clearable />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="queryParams.semester" placeholder="如: 2025-2026-1" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar" v-if="!userStore.isStudent">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>录入成绩
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="courseId" label="课程ID" width="100" />
        <el-table-column prop="score" label="成绩" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.score >= 60 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="examType" label="考试类型" width="120" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="createTime" label="录入时间" />
        <el-table-column label="操作" width="120" v-if="!userStore.isStudent">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input v-model="form.studentId" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseId">
          <el-input v-model="form.courseId" />
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" :precision="2" />
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="form.examType" placeholder="请选择" style="width: 100%">
            <el-option label="期中" value="期中" />
            <el-option label="期末" value="期末" />
            <el-option label="平时" value="平时" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2025-2026-1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getGrades, createGrade, updateGrade } from '@/api/grade'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({ page: 1, size: 10, studentId: '', courseId: '', semester: '' })
const form = ref({})

const rules = {
  studentId: [{ required: true, message: '请输入学生ID', trigger: 'blur' }],
  courseId: [{ required: true, message: '请输入课程ID', trigger: 'blur' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getGrades(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.studentId = ''
  queryParams.courseId = ''
  queryParams.semester = ''
  queryParams.page = 1
  fetchData()
}

function handleAdd() {
  form.value = {}
  dialogTitle.value = '录入成绩'
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogTitle.value = '编辑成绩'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.value.id) {
      await updateGrade(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createGrade(form.value)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    fetchData()
  })
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.toolbar { margin: 16px 0; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
