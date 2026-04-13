<template>
  <div class="page-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="姓名/学号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input v-model="queryParams.classId" placeholder="班级ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar" v-if="userStore.isAdmin">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增学生
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enrollmentDate" label="入学日期" width="120" />
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
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="150" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input v-model="form.classId" />
        </el-form-item>
        <el-form-item label="入学日期">
          <el-date-picker v-model="form.enrollmentDate" type="date" value-format="YYYY-MM-DD" />
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
import { getStudents, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryParams = reactive({ page: 1, size: 10, keyword: '', classId: '' })
const form = ref({})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStudents(queryParams)
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
  queryParams.classId = ''
  queryParams.page = 1
  fetchData()
}

function handleAdd() {
  form.value = { gender: 1 }
  dialogTitle.value = '新增学生'
  dialogVisible.value = true
}

function handleEdit(row) {
  form.value = { ...row }
  dialogTitle.value = '编辑学生'
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除学生【${row.name}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.value.id) {
      await updateStudent(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createStudent(form.value)
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
.search-form { margin-bottom: 0; }
.toolbar { margin: 16px 0; }
.pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
