<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="课程名称/编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="教师ID">
          <el-input v-model="queryParams.teacherId" clearable />
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input v-model="queryParams.classId" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar" v-if="userStore.isAdmin">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增课程
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="teacherId" label="教师ID" width="100" />
        <el-table-column prop="classId" label="班级ID" width="100" />
        <el-table-column prop="schedule" label="上课时间" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" v-if="userStore.isAdmin">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="form.courseCode" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" :precision="1" />
        </el-form-item>
        <el-form-item label="任课教师ID">
          <el-input v-model="form.teacherId" />
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input v-model="form.classId" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.schedule" placeholder="如：周一 1-2节" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCourses, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({ page: 1, size: 10, keyword: '', teacherId: '', classId: '' })
const form = ref({})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseCode: [{ required: true, message: '请输入课程编号', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCourses(queryParams)
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
  queryParams.keyword = ''
  queryParams.teacherId = ''
  queryParams.classId = ''
  queryParams.page = 1
  fetchData()
}

function handleAdd() {
  form.value = { credit: 1.0 }
  dialogTitle.value = '新增课程'
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogTitle.value = '编辑课程'
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除课程【${row.courseName}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.value.id) {
      await updateCourse(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCourse(form.value)
      ElMessage.success('创建成功')
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
