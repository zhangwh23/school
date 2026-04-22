<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="学生">
          <el-select v-model="queryParams.studentId" clearable placeholder="请选择学生" style="width: 180px">
            <el-option v-for="s in studentOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="queryParams.courseId" clearable placeholder="请选择课程" style="width: 180px">
            <el-option v-for="c in courseOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
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
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="courseName" label="课程" width="180" />
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
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option v-for="s in studentOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
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
import { getStudents } from '@/api/student'
import { getCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({ page: 1, size: 10, studentId: null, courseId: null, semester: '' })
const form = ref({})

// 下拉框选项
const studentOptions = ref([])
const courseOptions = ref([])

// 加载下拉框数据
async function loadDropdownData() {
  const [students, courses] = await Promise.all([
    getStudents({ page: 1, size: 1000 }),
    getCourses({ page: 1, size: 1000 })
  ])
  studentOptions.value = (students.data?.records || []).map(s => ({ label: `${s.name} (${s.studentNo})`, value: s.id }))
  courseOptions.value = (courses.data?.records || []).map(c => ({ label: c.courseName, value: c.id }))
}

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
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
  queryParams.studentId = null
  queryParams.courseId = null
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

onMounted(() => {
  loadDropdownData()
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.toolbar { margin: 16px 0; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
