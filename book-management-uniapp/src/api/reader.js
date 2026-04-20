import { http } from '../utils/request'

export function getMyReaderInfo() {
  return http.get('/readers/me')
}