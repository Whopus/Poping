<template>
  <div class="dataset-page dark-theme">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">数据集管理</h1>
        <p class="page-subtitle">数据集创建、管理和版本控制</p>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" @click="importDataset">
          <IconComponents name="upload" class="btn-icon" />
          导入数据集
        </button>
        <button class="btn-primary" @click="createDataset">
          <IconComponents name="plus" class="btn-icon" />
          创建数据集
        </button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="search-box">
        <IconComponents name="search" class="search-icon" />
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索数据集名称或描述"
          class="search-input"
        />
      </div>
      
      <div class="filter-controls">
        <select v-model="selectedType" class="filter-select">
          <option value="">所有类型</option>
          <option value="text">文本</option>
          <option value="image">图像</option>
          <option value="audio">音频</option>
          <option value="video">视频</option>
          <option value="table">表格</option>
        </select>
        
        <select v-model="selectedStatus" class="filter-select">
          <option value="">所有状态</option>
          <option value="processing">处理中</option>
          <option value="ready">就绪</option>
          <option value="error">错误</option>
        </select>
        
        <select v-model="sortBy" class="filter-select">
          <option value="created">创建时间</option>
          <option value="updated">更新时间</option>
          <option value="name">名称</option>
          <option value="size">大小</option>
        </select>
      </div>
    </div>

    <!-- 处理进度提醒 -->
    <transition name="fade">
      <div v-if="progressInfo" class="processing-banner">
        <div class="banner-content">
          <IconComponents name="activity" class="banner-icon" />
          <div>
            <p class="banner-title">正在解析数据集</p>
            <p class="banner-message">{{ progressInfo.message }}</p>
          </div>
        </div>
        <div class="banner-progress">
          <div class="banner-progress-track">
            <div class="banner-progress-fill" :style="{ width: `${progressInfo.progress}%` }"></div>
          </div>
          <span class="banner-progress-text">{{ progressInfo.progress }}%</span>
        </div>
      </div>
    </transition>

    <!-- 数据集网格 -->
    <div class="dataset-grid" v-if="sortedDatasets.length > 0">
      <div
        v-for="dataset in sortedDatasets"
        :key="dataset.id"
        class="dataset-card"
        @click="viewDataset(dataset)"
      >
        <div class="card-header">
          <div class="dataset-type">
            <IconComponents :name="getTypeIcon(dataset.type)" class="type-icon" />
            <span class="type-label">{{ getTypeLabel(dataset.type) }}</span>
          </div>
          <div class="dataset-status" :class="`status-${dataset.status}`">
            {{ getStatusLabel(dataset.status) }}
          </div>
        </div>
        
        <div class="card-content">
          <h3 class="dataset-name">{{ dataset.name }}</h3>
          <p class="dataset-description">{{ dataset.description }}</p>
          
          <div class="dataset-stats">
            <div class="stat-item">
              <span class="stat-label">记录数</span>
              <span class="stat-value">{{ formatNumber(dataset.recordCount) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">大小</span>
              <span class="stat-value">{{ formatSize(dataset.totalSize) }}</span>
            </div>
          </div>

          <div class="dataset-tags" v-if="dataset.tags.length > 0">
            <span v-for="tag in dataset.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </div>

        <div class="card-progress" v-if="dataset.status === 'processing'">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: `${dataset.processingProgress}%` }"></div>
          </div>
          <span class="progress-text">{{ dataset.processingProgress }}%</span>
        </div>

        <div class="card-footer">
          <div class="dataset-meta">
            <span class="meta-item">{{ formatDate(dataset.updatedAt) }}</span>
            <span class="meta-item">{{ dataset.owner?.name || '未知' }}</span>
          </div>

          <div class="card-actions" @click.stop>
            <button class="action-btn" @click="editDataset(dataset)" title="编辑">
              <IconComponents name="edit" />
            </button>
            <button class="action-btn" @click="shareDataset(dataset)" title="分享">
              <IconComponents name="share" />
            </button>
            <button class="action-btn danger" @click="deleteDataset(dataset)" title="删除">
              <IconComponents name="delete" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="datasetList.length === 0 && !loading">
      <div class="empty-icon">
        <IconComponents name="dataset" size="lg" />
      </div>
      <h3>暂无数据集</h3>
      <p>您还没有创建任何数据集，点击下方按钮开始创建。</p>
      <button class="btn-primary" @click="createDataset">
        <IconComponents name="plus" class="btn-icon" />
        创建数据集
      </button>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="goToPage(currentPage - 1)"
      >
        上一页
      </button>
      
      <div class="page-numbers">
        <button 
          v-for="page in visiblePages" 
          :key="page" 
          class="page-number" 
          :class="{ active: page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        class="page-btn" 
        :disabled="currentPage === totalPages" 
        @click="goToPage(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <!-- 创建数据集弹窗 -->
    <div class="modal-mask" v-if="showCreateModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>创建数据集</h3>
          <button class="modal-close" @click="closeCreateModal">
            <IconComponents name="close" />
          </button>
        </div>
        <form class="modal-body" @submit.prevent="saveDataset">
          <div class="form-group">
            <label for="dataset-name">数据集标题</label>
            <input
              id="dataset-name"
              v-model="createForm.name"
              type="text"
              placeholder="示例：客户反馈数据集"
              required
            />
          </div>

          <div class="form-group">
            <label for="dataset-description">描述</label>
            <textarea
              id="dataset-description"
              v-model="createForm.description"
              rows="4"
              placeholder="示例：2024年客户反馈文本数据，包含情感分析标注"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="dataset-type">数据类型</label>
              <select id="dataset-type" v-model="createForm.type">
                <option value="text">文本</option>
                <option value="image">图像</option>
                <option value="audio">音频</option>
                <option value="video">视频</option>
                <option value="table">表格</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>标签（最多3个）</label>
            <div class="tag-input">
              <input
                v-model="createForm.tagInput"
                type="text"
                placeholder="输入标签并回车"
                @keyup.enter.prevent="addTag"
              />
              <button type="button" class="add-tag-btn" @click="addTag">添加</button>
            </div>
            <div class="tag-chips" v-if="createForm.tags.length">
              <span v-for="tag in createForm.tags" :key="tag" class="chip">
                {{ tag }}
                <button type="button" class="chip-close" @click="removeTag(tag)">
                  <IconComponents name="close" />
                </button>
              </span>
            </div>
          </div>

          <div class="form-group">
            <label>上传文件</label>
            <div class="file-uploader" @click="openFileDialog">
              <input ref="fileInputRef" type="file" multiple @change="handleFileChange" />
              <div class="uploader-icon">
                <IconComponents name="upload" />
              </div>
              <p>点击或拖拽上传多个文件，支持文本、CSV、图像等格式</p>
            </div>
            <ul class="file-list" v-if="uploadItems.length">
              <li v-for="file in uploadItems" :key="file.uid" class="file-item">
                <div class="file-info">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatSize(file.size) }}</span>
                </div>
                <div class="file-progress">
                  <div class="progress-track">
                    <div class="progress-fill" :style="{ width: `${file.progress}%` }"></div>
                  </div>
                  <span :class="['file-status', file.status]">
                    {{ file.status === 'uploaded' ? '已上传' : file.status === 'uploading' ? '上传中' : file.status === 'error' ? '失败' : '待上传' }}
                  </span>
                </div>
                <button type="button" class="remove-file" @click.stop="removeFile(file.uid)">
                  <IconComponents name="delete" />
                </button>
              </li>
            </ul>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-secondary" @click="closeCreateModal">取消</button>
            <button type="submit" class="btn-primary" :disabled="createLoading">
              <span v-if="createLoading" class="loading-dot"></span>
              保存并解析
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import IconComponents from '@/components/icons/IconComponents.vue'
import { confirmDialog } from '@/utils/confirm'
import { datasetApi } from '@/api/dataset'
import type {
  DatasetCreateFilePayload,
  DatasetListItem,
  DatasetProgressMessage,
} from '@/types/dataset'
import toast from '@/utils/toast'

const router = useRouter()

interface UploadFileItem extends DatasetCreateFilePayload {
  uid: string
  name: string
  status: 'pending' | 'uploading' | 'uploaded' | 'error'
  progress: number
  error?: string
}

// 列表相关状态
const datasetList = ref<DatasetListItem[]>([])
const loading = ref(false)
const searchQuery = ref('')
const selectedType = ref('')
const selectedStatus = ref('')
const sortBy = ref('updated')
const currentPage = ref(1)
const pageSize = ref(12)
const totalCount = ref(0)

// 创建数据集
const showCreateModal = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  name: '',
  description: '',
  type: 'text',
  tags: [] as string[],
  tagInput: '',
})
const uploadItems = ref<UploadFileItem[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)

// 进度跟踪
const progressInfo = ref<DatasetProgressMessage | null>(null)
const progressSocket = ref<WebSocket | null>(null)

// 计算属性
const totalPages = computed(() => (totalCount.value > 0 ? Math.ceil(totalCount.value / pageSize.value) : 1))

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const sortedDatasets = computed(() => {
  const list = [...datasetList.value]
  switch (sortBy.value) {
    case 'name':
      return list.sort((a, b) => a.name.localeCompare(b.name))
    case 'size':
      return list.sort((a, b) => b.totalSize - a.totalSize)
    case 'created':
      return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    case 'updated':
    default:
      return list.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  }
})

// 数据加载
const loadDatasets = async (page = currentPage.value) => {
  loading.value = true
  try {
    const response = await datasetApi.fetchDatasets({
      page,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      type: selectedType.value || undefined,
      status: selectedStatus.value || undefined,
    })
    datasetList.value = response.items || []
    totalCount.value = response.total || 0
    currentPage.value = response.page || page
  } catch (error) {
    toast.error({ title: '加载失败', message: '数据集列表加载失败，请稍后重试。' })
  } finally {
    loading.value = false
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
  loadDatasets(page)
}

// 创建数据集流程
const openCreateModal = () => {
  showCreateModal.value = true
}

const resetCreateForm = () => {
  createForm.name = ''
  createForm.description = ''
  createForm.type = 'text'
  createForm.tags = []
  createForm.tagInput = ''
  uploadItems.value = []
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const closeCreateModal = () => {
  showCreateModal.value = false
  resetCreateForm()
}

const addTag = () => {
  const value = createForm.tagInput.trim()
  if (!value) return
  if (createForm.tags.includes(value)) {
    toast.info({ title: '提示', message: '标签已存在' })
    createForm.tagInput = ''
    return
  }
  if (createForm.tags.length >= 3) {
    toast.warning({ title: '提示', message: '最多选择3个标签' })
    return
  }
  createForm.tags.push(value)
  createForm.tagInput = ''
}

const removeTag = (tag: string) => {
  createForm.tags = createForm.tags.filter(item => item !== tag)
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files) return
  const files = Array.from(target.files)

  for (const file of files) {
    const uid = `${Date.now()}-${file.name}-${Math.random()}`
    const item: UploadFileItem = {
      uid,
      name: file.name,
      status: 'uploading',
      progress: 0,
      objectKey: '',
      fileUrl: '',
      originalName: file.name,
      size: file.size,
      contentType: file.type,
    }
    uploadItems.value.push(item)

    try {
      const result = await datasetApi.uploadFile(file, percent => {
        item.progress = percent
      })
      item.objectKey = result.objectKey
      item.fileUrl = result.fileUrl
      item.originalName = result.originalName
      item.size = result.size
      item.contentType = result.contentType
      item.status = 'uploaded'
      item.progress = 100
    } catch (error) {
      item.status = 'error'
      item.error = '上传失败'
      toast.error({ title: '上传失败', message: `文件 ${file.name} 上传失败，请重试。` })
    }
  }

  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const openFileDialog = () => {
  fileInputRef.value?.click()
}

const removeFile = (uid: string) => {
  uploadItems.value = uploadItems.value.filter(item => item.uid !== uid)
}

const saveDataset = async () => {
  if (!createForm.name.trim()) {
    toast.warning({ title: '校验失败', message: '请填写数据集标题' })
    return
  }
  const validFiles = uploadItems.value.filter(item => item.status === 'uploaded')
  if (validFiles.length === 0) {
    toast.warning({ title: '校验失败', message: '请至少上传一个文件' })
    return
  }

  createLoading.value = true
  try {
    const payload = {
      name: createForm.name.trim(),
      description: createForm.description.trim(),
      type: createForm.type,
      tags: createForm.tags,
      files: validFiles.map(item => ({
        objectKey: item.objectKey,
        fileUrl: item.fileUrl,
        originalName: item.originalName,
        size: item.size,
        contentType: item.contentType,
      })) as DatasetCreateFilePayload[],
    }

    const response = await datasetApi.createDataset(payload)
    toast.success({ title: '创建成功', message: '数据集已保存并开始解析' })
    closeCreateModal()
    connectProgress(response.id)
    loadDatasets(1)
  } catch (error) {
    toast.error({ title: '创建失败', message: '创建数据集失败，请稍后再试。' })
  } finally {
    createLoading.value = false
  }
}

// WebSocket 进度
const connectProgress = (datasetId: string) => {
  disconnectProgress()
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const host = window.location.host
  const ws = new WebSocket(`${protocol}://${host}/api/v1/datasets/progress?datasetId=${datasetId}`)
  ws.onmessage = event => {
    try {
      const message = JSON.parse(event.data) as DatasetProgressMessage
      progressInfo.value = message
      if (message.progress >= 100 && message.status !== 'processing') {
        setTimeout(() => {
          progressInfo.value = null
          disconnectProgress()
          loadDatasets()
        }, 1500)
      }
    } catch (error) {
      console.error('解析进度消息失败', error)
    }
  }
  ws.onerror = () => {
    console.warn('进度 WebSocket 连接失败')
  }
  ws.onclose = () => {
    progressSocket.value = null
  }
  progressSocket.value = ws
}

const disconnectProgress = () => {
  if (progressSocket.value) {
    progressSocket.value.close()
    progressSocket.value = null
  }
}

// 页面行为
const createDataset = () => {
  openCreateModal()
}

const importDataset = () => {
  toast.info({ title: '提示', message: '导入功能即将上线，敬请期待。' })
}

const viewDataset = (dataset: DatasetListItem) => {
  router.push(`/dashboard/dataset/${dataset.id}`)
}

const editDataset = (dataset: DatasetListItem) => {
  toast.info({ title: '提示', message: `编辑功能即将上线：${dataset.name}` })
}

const shareDataset = (dataset: DatasetListItem) => {
  toast.info({ title: '分享', message: `分享链接将很快提供：${dataset.name}` })
}

const deleteDataset = async (dataset: DatasetListItem) => {
  if (await confirmDialog(`确定要删除数据集 "${dataset.name}" 吗？`)) {
    toast.info({ title: '提示', message: '删除功能暂未开放。' })
  }
}

// 工具函数
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    text: 'docs',
    image: 'image',
    audio: 'audio',
    video: 'video',
    table: 'table',
  }
  return icons[type] || 'dataset'
}

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    text: '文本',
    image: '图像',
    audio: '音频',
    video: '视频',
    table: '表格',
  }
  return labels[type] || type
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    processing: '处理中',
    ready: '就绪',
    error: '错误',
  }
  return labels[status] || status
}

const formatNumber = (num: number) => {
  return num ? num.toLocaleString() : '0'
}

const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  const size = bytes / Math.pow(1024, i)
  return `${size.toFixed(2)} ${sizes[i]}`
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 监听筛选变化
let searchTimer: number | undefined
watch(searchQuery, () => {
  if (searchTimer) {
    window.clearTimeout(searchTimer)
  }
  searchTimer = window.setTimeout(() => {
    currentPage.value = 1
    loadDatasets(1)
  }, 300)
})

watch([selectedType, selectedStatus], () => {
  currentPage.value = 1
  loadDatasets(1)
})

// 初始化
onMounted(() => {
  loadDatasets()
})

onBeforeUnmount(() => {
  disconnectProgress()
  if (searchTimer) {
    window.clearTimeout(searchTimer)
  }
})
</script>

<style scoped>
.dataset-page {
  padding: 24px;
  background-color: #f9fafb;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-primary {
  background: #000000;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 6px rgba(59, 130, 246, 0.25);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(59, 130, 246, 0.35);
}

.btn-secondary {
  background-color: white;
  color: #4b5563;
  border: 1px solid #e5e7eb;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.btn-secondary:hover {
  background-color: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.filter-bar {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.processing-banner {
  background: #111827;
  color: #f8fafc;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.35);
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-icon {
  width: 36px;
  height: 36px;
  color: #38bdf8;
}

.banner-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.banner-message {
  margin: 4px 0 0;
  color: rgba(226, 232, 240, 0.8);
  font-size: 14px;
}

.banner-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 220px;
}

.banner-progress-track {
  flex: 1;
  height: 8px;
  background: rgba(148, 163, 184, 0.3);
  border-radius: 999px;
  overflow: hidden;
}

.banner-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #38bdf8, #60a5fa);
  border-radius: 999px;
}

.banner-progress-text {
  font-weight: 600;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.search-box {
  flex: 1;
  min-width: 300px;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  background-color: white;
  transition: all 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #000000;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #9ca3af;
}

.filter-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  background-color: white;
  min-width: 120px;
  transition: all 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #000000;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.1);
}

.dataset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.dataset-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.dataset-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #000000 0%, #374151 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.dataset-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.dataset-card:hover::before {
  transform: scaleX(1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.dataset-type {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #4b5563;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-icon {
  width: 14px;
  height: 14px;
}

.dataset-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-ready {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
}

.status-processing {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.status-error {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
}

.card-content {
  margin-bottom: 20px;
}

.dataset-name {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.dataset-description {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 15px;
}

.dataset-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #6b7280;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-weight: 500;
  color: #9ca3af;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.dataset-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 8px;
  background-color: #f3f4f6;
  color: #4b5563;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.card-progress {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin: 0 24px 16px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.dataset-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item {
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  color: #6b7280;
  position: relative;
}

.action-btn:hover {
  background-color: #f3f4f6;
  color: #374151;
  transform: scale(1.1);
}

.action-btn.danger:hover {
  background-color: #fee2e2;
  color: #dc2626;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  backdrop-filter: blur(3px);
}

.modal-container {
  background: #ffffff;
  border-radius: 18px;
  width: 720px;
  max-width: 90vw;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.modal-close {
  border: none;
  background: none;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #111827;
}

.modal-body {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  font-size: 15px;
  transition: border 0.2s ease, box-shadow 0.2s ease;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.form-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.tag-input {
  display: flex;
  gap: 12px;
}

.tag-input input {
  flex: 1;
}

.add-tag-btn {
  padding: 0 16px;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.add-tag-btn:hover {
  background: #1d4ed8;
}

.tag-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.chip {
  background: #e0f2fe;
  color: #0f172a;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.chip-close {
  border: none;
  background: none;
  color: #0369a1;
  cursor: pointer;
  display: flex;
}

.file-uploader {
  border: 2px dashed #cbd5f5;
  background: #f8fafc;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  position: relative;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.file-uploader:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.file-uploader input {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  opacity: 0;
  cursor: pointer;
}

.uploader-icon {
  display: flex;
  justify-content: center;
  color: #2563eb;
  margin-bottom: 12px;
}

.file-list {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
}

.file-info {
  flex: 1;
}

.file-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-status {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
}

.file-status.uploaded {
  color: #16a34a;
}

.file-status.uploading {
  color: #2563eb;
}

.file-status.error {
  color: #dc2626;
}

.remove-file {
  border: none;
  background: none;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-footer .btn-secondary {
  padding: 12px 20px;
  border-radius: 10px;
  background: #f3f4f6;
  border: none;
  cursor: pointer;
  font-weight: 600;
  color: #1f2937;
}

.modal-footer .btn-primary {
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.modal-footer .btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  animation: spin 1s linear infinite;
}

.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  max-width: 500px;
  margin: 0 auto;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  color: #d1d5db;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.empty-state p {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 80px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #000000;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.page-btn {
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.page-btn:hover:not(:disabled) {
  background-color: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
  min-width: 40px;
  text-align: center;
}

.page-number:hover {
  background-color: #f9fafb;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-number.active {
  background: linear-gradient(135deg, #000000 0%, #374151 100%);
  color: white;
  border-color: #000000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.25);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .dataset-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .dataset-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .btn-primary,
  .btn-secondary {
    justify-content: center;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 20px;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filter-controls {
    justify-content: stretch;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .dataset-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .dataset-card {
    padding: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .card-actions {
    justify-content: flex-end;
  }
  
  .dataset-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .dataset-page {
    padding: 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .btn-primary,
  .btn-secondary {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .filter-bar {
    padding: 16px;
  }
  
  .dataset-card {
    padding: 16px;
  }
  
  .dataset-name {
    font-size: 18px;
  }
  
  .page-numbers {
    flex-wrap: wrap;
  }
}
</style>