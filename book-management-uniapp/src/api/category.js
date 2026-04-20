import { http } from '../utils/request'

export function getCategoryTree() {
  return http.get('/categories/tree')
}