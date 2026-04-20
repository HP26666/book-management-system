export const ROLE_OPTIONS = [
  { id: 1, code: 'admin', label: '系统管理员' },
  { id: 2, code: 'librarian', label: '图书管理员' },
  { id: 3, code: 'reader', label: '普通读者' }
]

export const BOOK_STATUS_OPTIONS = [
  { label: '上架', value: 1, tag: 'success' },
  { label: '下架', value: 0, tag: 'info' }
]

export const BORROW_STATUS_OPTIONS = [
  { label: '申请中', value: 0, tag: 'warning' },
  { label: '待取书', value: 1, tag: 'primary' },
  { label: '借阅中', value: 2, tag: 'success' },
  { label: '已归还', value: 3, tag: 'info' },
  { label: '已拒绝', value: 4, tag: 'danger' },
  { label: '逾期', value: 5, tag: 'danger' }
]

export const RESERVATION_STATUS_OPTIONS = [
  { label: '预约中', value: 0, tag: 'warning' },
  { label: '已通知', value: 1, tag: 'success' },
  { label: '已取消', value: 2, tag: 'info' },
  { label: '已过期', value: 3, tag: 'danger' }
]

export const NOTICE_TYPE_OPTIONS = [
  { label: '信息', value: 1, tag: 'info' },
  { label: '成功', value: 2, tag: 'success' },
  { label: '警告', value: 3, tag: 'warning' }
]

export const READER_TYPE_OPTIONS = [
  { label: '普通读者', value: 'general' },
  { label: '学生读者', value: 'student' },
  { label: '教师读者', value: 'teacher' }
]

export function findOptionLabel(options, value, fallback = '-') {
  return options.find(item => item.value === value || item.code === value)?.label || fallback
}

export function findOptionTag(options, value, fallback = 'info') {
  return options.find(item => item.value === value)?.tag || fallback
}