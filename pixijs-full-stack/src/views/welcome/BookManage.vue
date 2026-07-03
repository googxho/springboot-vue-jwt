<script setup>
import { ref, onMounted } from 'vue'
import { get, post, put, del } from '@/net'

const books = ref([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 弹窗状态
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, title: '', desc: '', price: '' })

function fetchBooks() {
  loading.value = true
  get('/api/book/list', (data) => {
    books.value = data || []
    loading.value = false
  }, (msg) => {
    errorMsg.value = msg
    loading.value = false
  })
}

function openAdd() {
  isEdit.value = false
  form.value = { id: null, title: '', desc: '', price: '' }
  showDialog.value = true
}

function openEdit(book) {
  isEdit.value = true
  form.value = { ...book }
  showDialog.value = true
}

function handleSave() {
  if (!form.value.title.trim()) { errorMsg.value = '请输入书名'; return }
  if (!form.value.price) { errorMsg.value = '请输入价格'; return }
  loading.value = true
  errorMsg.value = ''
  const payload = {
    ...(isEdit.value ? { id: form.value.id } : {}),
    title: form.value.title.trim(),
    desc: form.value.desc.trim(),
    price: parseFloat(form.value.price)
  }
  const action = isEdit.value
    ? (s, f) => put('/api/book/update', payload, s, f)
    : (s, f) => post('/api/book/add', payload, s, f)
  action(() => {
    loading.value = false
    showDialog.value = false
    successMsg.value = isEdit.value ? '图书更新成功' : '图书添加成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchBooks()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

function handleDelete(book) {
  if (!confirm(`确定删除《${book.title}》吗？`)) return
  loading.value = true
  del(`/api/book/${book.id}`, () => {
    loading.value = false
    successMsg.value = '删除成功'
    setTimeout(() => successMsg.value = '', 2000)
    fetchBooks()
  }, (msg) => {
    loading.value = false
    errorMsg.value = msg
  })
}

onMounted(fetchBooks)
</script>

<template>
  <div class="manage-page">
    <div class="page-header">
      <h2>📚 图书管理</h2>
      <button class="btn-primary" @click="openAdd">+ 新增图书</button>
    </div>

    <div v-if="successMsg" class="msg-success">{{ successMsg }}</div>
    <div v-if="errorMsg" class="msg-error">{{ errorMsg }}</div>

    <div v-if="loading" class="loading">加载中...</div>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>书名</th>
          <th>简介</th>
          <th>价格</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>{{ book.id }}</td>
          <td>{{ book.title }}</td>
          <td class="desc-cell">{{ book.desc }}</td>
          <td>¥{{ book.price }}</td>
          <td class="action-cell">
            <button class="btn-edit" @click="openEdit(book)">编辑</button>
            <button class="btn-delete" @click="handleDelete(book)">删除</button>
          </td>
        </tr>
        <tr v-if="books.length === 0">
          <td colspan="5" class="empty-row">暂无图书数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="showDialog = false">
      <div class="dialog-card">
        <h3>{{ isEdit ? '编辑图书' : '新增图书' }}</h3>
        <div class="form-group">
          <label>书名</label>
          <input v-model="form.title" placeholder="请输入书名" />
        </div>
        <div class="form-group">
          <label>简介</label>
          <textarea v-model="form.desc" placeholder="请输入简介" rows="3"></textarea>
        </div>
        <div class="form-group">
          <label>价格</label>
          <input v-model="form.price" type="number" step="0.01" placeholder="请输入价格" />
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
.desc-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action-cell { display: flex; gap: 8px; }
.btn-edit { padding: 4px 12px; background: #e3f2fd; color: #1565c0; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
.btn-delete { padding: 4px 12px; background: #ffebee; color: #c62828; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; }
.empty-row { text-align: center; color: #999; padding: 40px !important; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.dialog-card { background: white; border-radius: 16px; padding: 24px 28px; width: 420px; max-width: 90%; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.dialog-card h3 { margin-bottom: 20px; font-size: 18px; color: #333; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 4px; font-weight: 500; }
.form-group input, .form-group textarea {
  width: 100%; padding: 10px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; outline: none; box-sizing: border-box; transition: border-color 0.2s;
}
.form-group input:focus, .form-group textarea:focus { border-color: #667eea; }
.dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
</style>
