<script setup>
import { ref, onMounted } from 'vue'
import { get, post, put, del } from '@/net'

const students = ref([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 弹窗状态
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, name: '', sex: '男', grade: '' })

function fetchStudents() {
  loading.value = true
  get('/api/student/list', (data) => {
    students.value = data || []
    loading.value = false
  }, (msg) => {
    errorMsg.value = msg
    loading.value = false
  })
}

function openAdd() {
  isEdit.value = false
  form.value = { id: null, name: '', sex: '男', grade: '' }
  showDialog.value = true
}

function openEdit(student) {
  isEdit.value = true
  form.value = { ...student }
  showDialog.value = true
}

function handleSave() {
  if (!form.value.name.trim()) { errorMsg.value = '请输入姓名'; return }
  if (!form.value.grade.trim()) { errorMsg.value = '请输入年级'; return }
  loading.value = true
  errorMsg.value = ''
  const payload = {
    ...(isEdit.value ? { id: form.value.id } : {}),
    name: form.value.name.trim(),
    sex: form.value.sex,
    grade: form.value.grade.trim()
  }
  const action = isEdit.value
    ? (s, f) => put('/api/student/update', payload, s, f)
    : (s, f) => post('/api/student/add', payload, s, f)
  action(() => {
    loading.value = false
    showDialog.value = false
    successMsg.value = isEdit.value ? '学生更新成功' : '学生添加成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchStudents()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

function handleDelete(student) {
  if (!confirm(`确定删除学生「${student.name}」吗？`)) return
  loading.value = true
  del(`/api/student/${student.id}`, () => {
    loading.value = false
    successMsg.value = '删除成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchStudents()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

onMounted(fetchStudents)
</script>

<template>
  <div class="manage-page">
    <div class="page-header">
      <h2>👨‍🎓 学生管理</h2>
      <button class="btn-primary" @click="openAdd">+ 新增学生</button>
    </div>

    <div v-if="successMsg" class="msg-success">{{ successMsg }}</div>
    <div v-if="errorMsg" class="msg-error">{{ errorMsg }}</div>

    <div v-if="loading" class="loading">加载中...</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>姓名</th>
          <th>性别</th>
          <th>年级</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in students" :key="s.id">
          <td>{{ s.id }}</td>
          <td>{{ s.name }}</td>
          <td>{{ s.sex }}</td>
          <td>{{ s.grade }}年级</td>
          <td class="action-cell">
            <button class="btn-edit" @click="openEdit(s)">编辑</button>
            <button class="btn-delete" @click="handleDelete(s)">删除</button>
          </td>
        </tr>
        <tr v-if="students.length === 0">
          <td colspan="5" class="empty-row">暂无学生数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="showDialog = false">
      <div class="dialog-card">
        <h3>{{ isEdit ? '编辑学生' : '新增学生' }}</h3>
        <div class="form-group">
          <label>姓名</label>
          <input v-model="form.name" placeholder="请输入姓名" />
        </div>
        <div class="form-group">
          <label>性别</label>
          <select v-model="form.sex">
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div class="form-group">
          <label>年级</label>
          <input v-model="form.grade" placeholder="请输入年级" />
        </div>
        <div v-if="errorMsg" class="msg-error">{{ errorMsg }}</div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="showDialog = false">取消</button>
          <button class="btn-primary" @click="handleSave" :disabled="loading">
            {{ loading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manage-page { padding: 24px; height: 100%; overflow-y: auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; color: #333; }
.btn-primary {
  padding: 8px 20px; background: #667eea; color: white; border: none;
  border-radius: 8px; font-size: 14px; cursor: pointer; transition: all 0.2s;
}
.btn-primary:hover { background: #5a6fd6; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel {
  padding: 8px 20px; background: #eee; color: #666; border: none;
  border-radius: 8px; font-size: 14px; cursor: pointer;
}
.msg-success { background: #e8f5e9; color: #2e7d32; padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.msg-error { background: #fff0f0; color: #c62828; padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.loading { text-align: center; padding: 40px; color: #999; }
.data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.data-table th { background: #f8f9fa; padding: 12px 16px; text-align: left; font-size: 13px; color: #666; font-weight: 600; }
.data-table td { padding: 12px 16px; border-top: 1px solid #f0f0f0; font-size: 14px; color: #333; }
.action-cell { display: flex; gap: 8px; }
.btn-edit { padding: 4px 12px; background: #e3f2fd; color: #1565c0; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
.btn-delete { padding: 4px 12px; background: #ffebee; color: #c62828; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
.empty-row { text-align: center; color: #999; padding: 40px !important; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.dialog-card { background: white; border-radius: 16px; padding: 24px 28px; width: 420px; max-width: 90%; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.dialog-card h3 { margin-bottom: 20px; font-size: 18px; color: #333; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 4px; font-weight: 500; }
.form-group input, .form-group select {
  width: 100%; padding: 10px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; outline: none; box-sizing: border-box; transition: border-color 0.2s;
}
.form-group input:focus, .form-group select:focus { border-color: #667eea; }
.dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
</style>
