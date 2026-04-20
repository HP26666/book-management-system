import { http } from '../utils/request'

export function getNoticeList(params) {
  return http.get('/notices', params)
}

export function getNoticeDetail(id) {
  return http.get(`/notices/${id}`)
}