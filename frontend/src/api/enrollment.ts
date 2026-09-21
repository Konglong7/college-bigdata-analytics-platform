import request from '@/utils/request'

export interface CompareStatsData {
  years: string[]
  applicants: number[]
  admissions: number[]
  rates: number[]
}

export interface FunnelItem {
  value: number
  name: string
}

export interface MatrixPlanData {
  universities: string[]
  provinces: string[]
  points: Array<[number, number, number]>
}

export const getCompareStats = () => {
  return request<any, CompareStatsData>({
    url: '/enrollment/compare-stats',
    method: 'get'
  })
}

export const getBatchFunnel = () => {
  return request<any, FunnelItem[]>({
    url: '/enrollment/batch-funnel',
    method: 'get'
  })
}

export const getMatrixPlan = () => {
  return request<any, MatrixPlanData>({
    url: '/enrollment/matrix-plan',
    method: 'get'
  })
}
