import { get } from '../utils/request'

export const getMyReader = () => get('/api/readers/me')
