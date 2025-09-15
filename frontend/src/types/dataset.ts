export interface DatasetFile {
  fileId: string
  originalName: string
  objectKey: string
  fileUrl: string
  size: number
  contentType?: string
  parseStatus: 'pending' | 'processing' | 'completed' | 'error'
  parseMessage?: string
  createdAt: string
}

export interface DatasetOwnerInfo {
  id: string
  name: string
}

export interface DatasetListItem {
  id: string
  name: string
  description: string
  type: string
  status: 'processing' | 'ready' | 'error'
  tags: string[]
  totalSize: number
  recordCount: number
  processingProgress: number
  createdAt: string
  updatedAt: string
  owner: DatasetOwnerInfo
}

export interface DatasetListResult {
  items: DatasetListItem[]
  total: number
  page: number
  size: number
}

export interface DatasetDetail extends DatasetListItem {
  lastMessage?: string
  files: DatasetFile[]
}

export interface DatasetUploadResponse {
  objectKey: string
  fileUrl: string
  originalName: string
  size: number
  contentType?: string
}

export interface DatasetCreateFilePayload {
  objectKey: string
  fileUrl: string
  originalName: string
  size: number
  contentType?: string
}

export interface DatasetCreatePayload {
  name: string
  description: string
  type: string
  tags: string[]
  files: DatasetCreateFilePayload[]
}

export interface DatasetProgressMessage {
  datasetId: string
  progress: number
  status: 'processing' | 'ready' | 'error'
  message: string
  timestamp: number
}

export interface DatasetQuery {
  page?: number
  size?: number
  type?: string
  status?: string
  keyword?: string
}
