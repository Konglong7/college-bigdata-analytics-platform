import request from '@/utils/request'

export interface RecommendQuery {
  province: string
  score: number
  subjectType: string
  targetLevel?: string
  targetType?: string
  targetProvince?: string
}

export interface RecommendSchool {
  id: number
  schoolName: string
  province: string
  city: string
  schoolLevel: string
  schoolType: string
  ruankeRank?: number
  predictScore: number
  scoreDiff: number
  probPercent: number
  tier: '冲' | '稳' | '保'
  recommendReason: string
  topMajors: string[]
  tags: string[]
  gaokaoSite: string
}

export interface RecommendResult {
  userProvince: string
  userScore: number
  subjectType: string
  rushList: RecommendSchool[]
  steadyList: RecommendSchool[]
  safeList: RecommendSchool[]
}

/**
 * 智能志愿梯度推荐匹配接口
 */
export const matchVolunteers = (data: RecommendQuery): Promise<RecommendResult> => {
  return request({
    url: '/recommend/match',
    method: 'post',
    data
  })
}
