import request from '@/utils/request'
import { UnivDetail } from './university'

export interface RadarComparison {
  indicators: Array<{ name: string; max: number }>
  series: Array<{ name: string; value: number[] }>
}

export interface ScoreComparison {
  years: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface UnivCompareResult {
  schools: UnivDetail[]
  radarComparison: RadarComparison
  scoreComparison: ScoreComparison
  metricMatrix: Array<Record<string, any>>
}

/**
 * 获取多校综合实力横向对比画像与量化指标
 */
export const getUniversityCompare = (ids: number[]): Promise<UnivCompareResult> => {
  const idsParam = ids && ids.length ? ids.join(',') : '1,2'
  return request({
    url: `/university/compare?ids=${idsParam}`,
    method: 'get'
  })
}
