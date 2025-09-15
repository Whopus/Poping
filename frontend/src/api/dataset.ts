import { http } from '@/utils/http'
import type {
  DatasetCreatePayload,
  DatasetDetail,
  DatasetListResult,
  DatasetQuery,
  DatasetUploadResponse,
} from '@/types/dataset'

export const datasetApi = {
  async uploadFile(file: File, onProgress?: (percent: number) => void) {
    const formData = new FormData()
    formData.append('file', file)

    const response = await http.upload<DatasetUploadResponse>('/v1/datasets/upload', formData, {
      onUploadProgress: event => {
        if (onProgress && event.total) {
          const percent = Math.round((event.loaded / event.total) * 100)
          onProgress(percent)
        }
      },
    })

    return response.data
  },

  async createDataset(payload: DatasetCreatePayload) {
    const response = await http.post<DatasetDetail>('/v1/datasets', payload)
    return response.data
  },

  async fetchDatasets(params: DatasetQuery = {}) {
    const response = await http.get<DatasetListResult>('/v1/datasets', {
      params,
    })
    return response.data
  },

  async fetchDatasetDetail(datasetId: string) {
    const response = await http.get<DatasetDetail>(`/v1/datasets/${datasetId}`)
    return response.data
  },
}
