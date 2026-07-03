<script setup>
import { ref, onMounted } from 'vue'
import { get, post, del } from '@/net'

const borrows = ref([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 借书弹窗
const showDialog = ref(false)
const form = ref({ sid: '', bid: '' })
const students = ref([])
const books = ref([])

function fetchBorrows() {
  loading.value = true
  get('/api/borrow/list', (data) => {
    borrows.value = data || []
    loading.value = false
  }, (msg) => {
    errorMsg.value = msg
    loading.value = false
  })
}

function openBorrow() {
  form.value = { sid: '', bid: '' }
  // 加载学生和图书列表供选择
  get('/api/student/list', (data) => { students.value = data || [] })
  get('/api/book/list', (data) => { books.value = data || [] })
  showDialog.value = true
}

function handleBorrow() {
  if (!form.value.sid || !form.value.bid) {
    errorMsg.value = '请选择学生和图书'
    return
  }
  loading.value = true
  errorMsg.value = ''
  post('/api/borrow/borrow', {
    sid: parseInt(form.value.sid),
    bid: parseInt(form.value.bid)
  }, () => {
    loading.value = false
    showDialog.value = false
    successMsg.value = '借阅成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchBorrows()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

function handleReturn(borrow) {
  if (!confirm(`确定归还《${borrow.bookName}》吗？`)) return
  loading.value = true
  del(`/api/borrow/return/${borrow.id}`, () => {
    loading.value = false
    successMsg.value = '归还成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchBorrows()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

function formatTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}

onMounted(fetchBorrows)
</script>

<template>
  <div class="manage-page">
    <div class="page-header">
      <h2>📋 借阅管理</h2>
      <button class="btn-primary" @click="openBorrow">+ 借书</button>
    </div>

    <div v-if="successMsg" class="msg-success">{{ successMsg }}</div>
    <div v-if="errorMsg" class="msg-error">{{ errorMsg }}</div>

    <div v-if="loading" class="loading">加载中...</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>学生</th>
          <th>图书</th>
          <th>借阅时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="b in borrows" :key="b.id">
          <td>{{ b.id }}</td>
          <td>{{ b.studentName || '-' }}</td>
          <td>{{ b.bookName || '-' }}</td>
          <td>{{ formatTime(b.time) }}</td>
          <td>
            <button class="btn-return" @click="handleReturn(b)">还书</button>
          </td>
        </tr>
        <tr v-if="borrows.length === 0">
          <td colspan="5" class="empty-row">暂无借阅记录</td>
        </tr>
      </tbody>
    </table>

    <!-- 借书弹窗 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="showDialog = false">
      <div class="dialog-card">
        <h3>借书</h3>
        <div class="form-group">
          <label>选择学生</label>
          <select v-model="form.sid">
            <option value="">-- 请选择学生 --</option>
            <option v-for="s in students" :key="s.id" :value="s.id">
              {{ s.name }} ({{ s.grade }}年级)
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>选择图书</label>
          <select v-model="form.bid">
            <option value="">-- 请选择图书 --</option>
            <option v-for="b in books" :key="b.id" :value="b.id">
              {{ b.title }} (¥{{ b.price }})
            </option>
          </select>
        </div>
        <div v-if="errorMsg" class="msg-error">{{ errorMsg }}</div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="showDialog = false">取消</button>
          <button class="btn-primary" @click="handleBorrow" :disabled="loading">
            {{ loading ? '提交中...' : '确认借阅' }}
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
.btn-return { padding: 4px 12px; background: #fff3e0; color: #e65100; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
.empty-row { text-align: center; color: #999; padding: 40px !important; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.dialog-card { background: white; border-radius: 16px; padding: 24px 28px; width: 420px; max-width: 90%; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.dialog-card h3 { margin-bottom: 20px; font-size: 18px; color: #333; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 4px; font-weight: 500; }
.form-group select {
  width: 100%; padding: 10px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; outline: none; box-sizing: border-box; background: white;
}
.form-group select:focus { border-color: #667eea; }
.dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
</style>
