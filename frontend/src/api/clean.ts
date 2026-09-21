import request from '@/utils/request'

export interface CleanSummaryData {
  rawCount: number
  duplicateCount: number
  errorCount: number
  cleanCount: number
}

export interface CleanChartsData {
  compare: {
    categories: string[]
    raw: number[]
    clean: number[]
  }
  quality: Array<{
    value: number
    name: string
  }>
  daily: {
    dates: string[]
    throughput: number[]
  }
}

export const getCleanSummary = () => {
  return request<any, CleanSummaryData>({
    url: '/clean/summary',
    method: 'get'
  })
}

export const getCleanCharts = () => {
  return request<any, CleanChartsData>({
    url: '/clean/charts',
    method: 'get'
  })
}
