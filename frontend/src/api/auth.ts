import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  role?: string
}

export interface LoginResult {
  token: string
  username: string
  role: string
}

export interface UserInfoResult {
  id: number
  username: string
  role: string
  createTime: string
}

export const login = (data: LoginParams) => {
  return request<any, LoginResult>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const register = (data: RegisterParams) => {
  return request<any, void>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  return request<any, UserInfoResult>({
    url: '/auth/info',
    method: 'get'
  })
}
