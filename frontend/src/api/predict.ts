import request from '@/utils/request'

export interface ModelMetricsData {
  modelName: string
  trainWindow: string
  predictPeriod: string
  mae: number
  rmse: number
  r2: string
}

export interface PredictionTrendsData {
  univ: {
    years: string[]
    history: Array<number | null>
    predict: Array<number | null>
  }
  major: {
    years: string[]
    ai: number[]
    bigdata: number[]
    se: number[]
  }
  enroll: {
    years: string[]
    enrollTotal: number[]
    populationLimit: number[]
  }
}

export const getModelMetrics = () => {
  return request<any, ModelMetricsData>({
    url: '/predict/model-metrics',
    method: 'get'
  })
}

export const getPredictionTrends = () => {
  return request<any, PredictionTrendsData>({
    url: '/predict/trends',
    method: 'get'
  })
}
