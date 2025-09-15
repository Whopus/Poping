<template>
  <div class="dataset-detail-page">
    <div class="detail-header">
      <button class="back-button" @click="goBack">
        <IconComponents name="arrow-left" class="icon" />
        返回
      </button>
      <div class="title-block">
        <h1>{{ dataset?.name || '数据集详情' }}</h1>
        <p v-if="dataset?.description" class="subtitle">{{ dataset?.description }}</p>
      </div>
      <div class="status-block" v-if="dataset">
        <span class="status-label" :class="`status-${dataset.status}`">{{ getStatusLabel(dataset.status) }}</span>
        <span class="updated-at">更新于 {{ formatDate(dataset.updatedAt) }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading-panel">
      <div class="spinner"></div>
      <p>正在加载数据集信息...</p>
    </div>

    <div v-else-if="dataset" class="detail-content">
      <section class="info-section">
        <div class="info-card">
          <div class="info-row">
            <div>
              <span class="info-label">数据类型</span>
              <span class="info-value">{{ getTypeLabel(dataset.type) }}</span>
            </div>
            <div>
              <span class="info-label">记录数量</span>
              <span class="info-value">{{ formatNumber(dataset.recordCount) }}</span>
            </div>
            <div>
              <span class="info-label">数据大小</span>
              <span class="info-value">{{ formatSize(dataset.totalSize) }}</span>
            </div>
            <div>
              <span class="info-label">创建者</span>
              <span class="info-value">{{ dataset.owner?.name || '未知' }}</span>
            </div>
          </div>

          <div class="progress-section" v-if="dataset.status !== 'ready' || progressInfo">
            <div class="progress-header">
              <span>处理进度</span>
              <span>{{ (progressInfo?.progress ?? dataset.processingProgress) }}%</span>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: `${progressInfo?.progress ?? dataset.processingProgress}%` }"
              ></div>
            </div>
            <p class="progress-message">{{ progressInfo?.message || dataset.lastMessage || '等待处理' }}</p>
          </div>

          <div class="tag-list" v-if="dataset.tags.length">
            <span v-for="tag in dataset.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </div>
      </section>

      <section class="file-section">
        <div class="section-header">
          <h2>关联文件</h2>
          <span class="file-count">共 {{ dataset.files.length }} 个文件</span>
        </div>

        <div class="file-table" v-if="dataset.files.length">
          <div class="file-table-header">
            <span>文件名</span>
            <span class="file-size">大小</span>
            <span class="file-status">状态</span>
            <span class="file-time">上传时间</span>
            <span class="file-actions">操作</span>
          </div>
          <div v-for="file in dataset.files" :key="file.fileId" class="file-row">
            <span class="file-name">{{ file.originalName }}</span>
            <span class="file-size">{{ formatSize(file.size) }}</span>
            <span class="file-status">
              <span :class="['status-badge', `status-${file.parseStatus}`]">{{ getParseStatusLabel(file.parseStatus) }}</span>
              <span v-if="file.parseMessage" class="status-message">{{ file.parseMessage }}</span>
            </span>
            <span class="file-time">{{ formatDate(file.createdAt) }}</span>
            <span class="file-actions">
              <button class="preview-btn" @click="previewFile(file)" :disabled="file.parseStatus !== 'completed'">
                预览
              </button>
            </span>
          </div>
        </div>

        <div v-else class="empty-files">
          <p>暂无关联文件。</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import IconComponents from '@/components/icons/IconComponents.vue'
import { datasetApi } from '@/api/dataset'
import type { DatasetDetail, DatasetFile, DatasetProgressMessage } from '@/types/dataset'
import toast from '@/utils/toast'

const route = useRoute()
const router = useRouter()

const dataset = ref<DatasetDetail | null>(null)
const loading = ref(true)
const progressInfo = ref<DatasetProgressMessage | null>(null)
const socket = ref<WebSocket | null>(null)

const goBack = () => {
  router.back()
}

const loadDataset = async (datasetId: string) => {
  loading.value = true
  try {
    const response = await datasetApi.fetchDatasetDetail(datasetId)
    dataset.value = response
    if (response.status === 'processing') {
      connectProgress(datasetId)
    } else {
      disconnect()
      progressInfo.value = null
    }
  } catch (error) {
    toast.error({ title: '加载失败', message: '无法加载数据集详情' })
  } finally {
    loading.value = false
  }
}

const connectProgress = (datasetId: string) => {
  disconnect()
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const host = window.location.host
  const ws = new WebSocket(`${protocol}://${host}/api/v1/datasets/progress?datasetId=${datasetId}`)
  ws.onmessage = event => {
    try {
      const message = JSON.parse(event.data) as DatasetProgressMessage
      progressInfo.value = message
      if (dataset.value) {
        dataset.value.processingProgress = message.progress
        dataset.value.status = message.status
        dataset.value.lastMessage = message.message
      }
      if (message.progress >= 100 && message.status !== 'processing') {
        setTimeout(() => {
          progressInfo.value = null
          disconnect()
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
    socket.value = null
  }
  socket.value = ws
}

const disconnect = () => {
  if (socket.value) {
    socket.value.close()
    socket.value = null
  }
}

const formatSize = (bytes: number) => {
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  if (!bytes) return '0 B'
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  const size = bytes / Math.pow(1024, i)
  return `${size.toFixed(2)} ${sizes[i]}`
}

const formatNumber = (num: number) => {
  return num ? num.toLocaleString() : '0'
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
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
    ready: '已完成',
    error: '解析失败',
  }
  return labels[status] || status
}

const getParseStatusLabel = (status: DatasetFile['parseStatus']) => {
  const labels: Record<DatasetFile['parseStatus'], string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    error: '失败',
  }
  return labels[status]
}

const previewFile = (file: DatasetFile) => {
  if (file.parseStatus !== 'completed') {
    toast.info({ title: '提示', message: '文件解析完成后才能预览' })
    return
  }
  toast.info({ title: '预览功能', message: '预览功能即将上线，敬请期待。' })
}

onMounted(() => {
  const datasetId = route.params.id as string
  if (datasetId) {
    loadDataset(datasetId)
  }
})

onBeforeUnmount(() => {
  disconnect()
})
</script>

<style scoped>
.dataset-detail-page {
  padding: 24px;
  background: #f9fafb;
  min-height: 100vh;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: none;
  color: #2563eb;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.2s ease;
}

.back-button:hover {
  background: rgba(37, 99, 235, 0.12);
}

.back-button .icon {
  width: 16px;
  height: 16px;
}

.title-block {
  flex: 1;
  margin: 0 24px;
}

.title-block h1 {
  margin: 0;
  font-size: 28px;
  color: #111827;
}

.subtitle {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 16px;
}

.status-block {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 13px;
}

.status-processing {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.status-ready {
  background: rgba(34, 197, 94, 0.12);
  color: #16a34a;
}

.status-error {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.updated-at {
  font-size: 13px;
  color: #6b7280;
}

.loading-panel {
  background: #fff;
  border-radius: 12px;
  padding: 80px 40px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.1);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section .info-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.info-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 24px;
}

.info-label {
  display: block;
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.6px;
}

.info-value {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.progress-section {
  margin-top: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.progress-bar {
  height: 10px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  transition: width 0.3s ease;
}

.progress-message {
  margin-top: 10px;
  font-size: 14px;
  color: #4b5563;
}

.tag-list {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #f1f5f9;
  color: #0f172a;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
}

.file-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: #111827;
}

.file-count {
  color: #6b7280;
  font-size: 14px;
}

.file-table-header,
.file-row {
  display: grid;
  grid-template-columns: 3fr 1fr 1.5fr 1.5fr 1fr;
  gap: 16px;
  padding: 12px 0;
  align-items: center;
}

.file-table-header {
  font-size: 13px;
  text-transform: uppercase;
  color: #9ca3af;
  letter-spacing: 0.6px;
  border-bottom: 1px solid #e5e7eb;
}

.file-row {
  border-bottom: 1px solid #f3f4f6;
}

.file-row:last-child {
  border-bottom: none;
}

.file-name {
  font-weight: 600;
  color: #111827;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 8px;
}

.status-pending {
  background: rgba(107, 114, 128, 0.12);
  color: #4b5563;
}

.status-processing {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.status-completed {
  background: rgba(34, 197, 94, 0.12);
  color: #16a34a;
}

.status-error {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.status-message {
  display: block;
  font-size: 12px;
  color: #6b7280;
}

.preview-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #2563eb;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.preview-btn:disabled {
  border-color: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}

.preview-btn:not(:disabled):hover {
  background: #2563eb;
  color: #fff;
}

.empty-files {
  text-align: center;
  color: #6b7280;
  padding: 48px 16px;
  background: #f8fafc;
  border-radius: 12px;
}
</style>
